package com.atguigu.tingshu.album.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.atguigu.tingshu.album.mapper.AlbumAttributeValueMapper;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumAttributeValueVo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {

	@Autowired
	private AlbumInfoMapper albumInfoMapper;


	@Autowired
	private AlbumAttributeValueMapper albumAttributeValueMapper;

	@Autowired
	private AlbumStatMapper albumStatMapper;

	@Autowired
	private TrackInfoMapper trackInfoMapper;

	/**
	 * 新增专辑
	 * 1.向专辑信息表新增一条记录
	 * 2.向专辑属性关系表新增若干条记录
	 * 3.向专辑统计表中新增四条记录
	 *
	 * @param albumInfoVo 专辑相关信息
	 * @param userId      用户ID
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveAlbumInfo(AlbumInfoVo albumInfoVo, Long userId) {
		AlbumInfo albumInfo2 = BeanUtil.copyProperties(albumInfoVo, AlbumInfo.class);
		if(albumInfo2.getId() !=null) {
			Long id = albumInfo2.getId();
			updateAlbumInfo( id,  albumInfoVo);
		}
		//1.向专辑信息表新增一条记录
		//1.1 将前端提交专辑VO对象转为专辑PO对象
		AlbumInfo albumInfo = BeanUtil.copyProperties(albumInfoVo, AlbumInfo.class);
		//1.2 部分属性赋值
		albumInfo.setUserId(userId);
		//付费类型为非免费的专辑 免费试听集数为5，试听秒数(不限制)
		if (!SystemConstant.ALBUM_PAY_TYPE_FREE.equals(albumInfo.getPayType())) {
			albumInfo.setTracksForFree(5);
		}
		//目前没有平台审核端暂时写为通过
		albumInfo.setStatus(SystemConstant.ALBUM_STATUS_PASS);
		albumInfoMapper.insert(albumInfo);
		Long albumInfoId = albumInfo.getId();
		//2.向专辑属性关系表新增若干条记录
		List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
		if(CollectionUtil.isNotEmpty(albumAttributeValueVoList))  {
			albumAttributeValueVoList.forEach(albumAttributeValueVo -> {
				AlbumAttributeValue albumAttributeValue = BeanUtil.copyProperties(albumAttributeValueVo
						, AlbumAttributeValue.class);
				albumAttributeValue.setAlbumId(albumInfoId);
				albumAttributeValueMapper.insert(albumAttributeValue);
			});
		}
		//3.向专辑统计表中新增四条记录(播放数，订阅数，购买数，评论数)
		this.saveAlbumStat(albumInfoId, SystemConstant.ALBUM_STAT_PLAY);
		this.saveAlbumStat(albumInfoId, SystemConstant.ALBUM_STAT_SUBSCRIBE);
		this.saveAlbumStat(albumInfoId, SystemConstant.ALBUM_STAT_BROWSE);
		this.saveAlbumStat(albumInfoId, SystemConstant.ALBUM_STAT_COMMENT);
	}

	@Override
	public void saveAlbumStat(Long albumId, String statType) {
		AlbumStat albumStat = new AlbumStat();
		albumStat.setAlbumId(albumId);
		albumStat.setStatType(statType);
		albumStat.setStatNum(0);
		albumStatMapper.insert(albumStat);
	}

	@Override
	public Page<AlbumListVo> getUserAlbumByPage(Page<AlbumListVo> pageInfo, AlbumInfoQuery albumInfoQuery) {

		return albumInfoMapper.getUserAlbumByPage(pageInfo, albumInfoQuery);
	}

	@Override
	public void removeAlbumInfo(Long id) {
		albumInfoMapper.deleteById(id);

		LambdaQueryWrapper<AlbumStat> albumStatLambdaQueryWrapper = new LambdaQueryWrapper<>();
		albumStatLambdaQueryWrapper.eq(AlbumStat::getAlbumId,id);
		albumStatMapper.delete(albumStatLambdaQueryWrapper);


		LambdaQueryWrapper<AlbumAttributeValue> lambdaQueryWrapper = new LambdaQueryWrapper<>();
		lambdaQueryWrapper.eq(AlbumAttributeValue::getAlbumId,id);
		albumAttributeValueMapper.delete(lambdaQueryWrapper);

		LambdaQueryWrapper<TrackInfo> trackInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
		trackInfoLambdaQueryWrapper.eq(TrackInfo::getAlbumId, id);
		trackInfoMapper.delete(trackInfoLambdaQueryWrapper);

	}

	@Override
	public AlbumInfoVo getAlbumInfo(Long id) {
		AlbumInfo albumInfo = albumInfoMapper.selectById(id);
		LambdaQueryWrapper<AlbumAttributeValue> albumAttributeValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
		albumAttributeValueLambdaQueryWrapper.eq(AlbumAttributeValue::getAlbumId,id);
		List<AlbumAttributeValue> albumAttributeValues = albumAttributeValueMapper.selectList(albumAttributeValueLambdaQueryWrapper);
		albumInfo.setAlbumAttributeValueVoList(albumAttributeValues);
		AlbumInfoVo albumInfoVo = BeanUtil.copyProperties(albumInfo, AlbumInfoVo.class);
		return albumInfoVo;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateAlbumInfo(Long id, AlbumInfoVo albumInfoVo) {
		//1.修改专辑信息
		//1.1 将专辑VO转为PO对象
		AlbumInfo albumInfo = BeanUtil.copyProperties(albumInfoVo, AlbumInfo.class);
		albumInfo.setId(id);
		//1.2 执行修改专辑信息
		albumInfoMapper.updateById(albumInfo);

		//2.修改专辑属性信息
		//2.1 先根据专辑ID条件删除专辑属性关系(逻辑删除)
		LambdaQueryWrapper<AlbumAttributeValue> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AlbumAttributeValue::getAlbumId, id);
		albumAttributeValueMapper.delete(queryWrapper);
		//2.2 再根据用户提交专辑属性新增（关联专辑ID）
		List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
		if (CollectionUtil.isNotEmpty(albumAttributeValueVoList)) {
			albumAttributeValueVoList.forEach(albumAttributeValueVo -> {
				//转为PO对象 关联专辑ID
				AlbumAttributeValue albumAttributeValue = BeanUtil.copyProperties(albumAttributeValueVo, AlbumAttributeValue.class);
				albumAttributeValue.setAlbumId(id);
				albumAttributeValueMapper.insert(albumAttributeValue);
			});
		}
	}

	@Override
	public List<AlbumInfo> getUserAllAlbumList(Long userId) {
		LambdaQueryWrapper<AlbumInfo> albumInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
		albumInfoLambdaQueryWrapper.eq(AlbumInfo::getUserId,userId)
				.select(AlbumInfo::getUserId,AlbumInfo::getAlbumTitle)
				.orderByDesc(AlbumInfo::getCreateTime)
				.last("limit 200");
		return albumInfoMapper.selectList(albumInfoLambdaQueryWrapper);
	}

}

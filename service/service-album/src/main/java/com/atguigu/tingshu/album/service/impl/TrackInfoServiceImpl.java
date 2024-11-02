package com.atguigu.tingshu.album.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.util.UploadFileUtil;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {

	@Autowired
	private TrackInfoMapper trackInfoMapper;

	@Autowired
	private VodConstantProperties properties;


	@Autowired
	private AlbumInfoMapper albumInfoMapper;


	@Autowired
	private VodService vodService;


	@Autowired
	private TrackStatMapper trackStatMapper;

	@Override
	public Map<String, String> uploadTrack(MultipartFile file) {
        try {
//			String path = UploadFileUtil.uploadTempPath(properties.getTempPath(), file);
//			VodUploadClient client = new VodUploadClient(properties.getSecretId(), properties.getSecretKey());
//			VodUploadRequest request = new VodUploadRequest();
//			request.setMediaFilePath(path);
//            VodUploadResponse response = client.upload(properties.getRegion(), request);
//			if (response !=null) {
//				String mediaUrl = response.getMediaUrl();
//				String fileId = response.getFileId();
				HashMap<String, String> map = new HashMap<>();
				map.put("mediaFileId","1397757896634052508");
				map.put("mediaUrl","https://1314122510.vod-qcloud.com/a643de76vodcq1314122510/2ee63e941397757896634052508/KbKOQ0RkYa8A.mp3");
				return map;
//			}
			//return null;
        } catch (Exception e) {
			log.error("云颠跛失败,{}",e);
            throw new RuntimeException(e);
        }

	}

	@Override
	public void saveTrackInfo(Long userId, TrackInfoVo trackInfoVo) {
		TrackInfo trackInfo = BeanUtil.copyProperties(trackInfoVo, TrackInfo.class);

		trackInfo.setUserId(userId);

		trackInfo.setStatus(SystemConstant.TRACK_STATUS_PASS);

		trackInfo.setSource(SystemConstant.TRACK_SOURCE_USER);

		AlbumInfo albumInfo = albumInfoMapper.selectById(trackInfo.getAlbumId());
		Integer includeTrackCount = albumInfo.getIncludeTrackCount();
		trackInfo.setOrderNum(includeTrackCount+1);

		TrackMediaInfoVo mediaInfoVo = vodService.getTrackMediaInfo(trackInfo.getMediaFileId());
		if(mediaInfoVo != null) {
			trackInfo.setMediaDuration(BigDecimal.valueOf(mediaInfoVo.getDuration()));
			trackInfo.setMediaSize(mediaInfoVo.getSize());
			trackInfo.setMediaType(mediaInfoVo.getType());
		}

		trackInfoMapper.insert(trackInfo);


		albumInfo.setIncludeTrackCount(albumInfo.getEstimatedTrackCount()+1);
		albumInfoMapper.updateById(albumInfo);


		this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_PLAY);
		this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_COLLECT);
		this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_PRAISE);
		this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_COMMENT);
	}


	public void saveTrackStat(Long id, String statType) {
		TrackStat trackStat = new TrackStat();
		trackStat.setTrackId(id);
		trackStat.setStatType(statType);
		trackStat.setStatNum(0);
		trackStatMapper.insert(trackStat);
	}


	@Override
	public void updateTrack(Long id, TrackInfoVo trackInfoVo) {
		TrackInfo trackInfo = trackInfoMapper.selectById(id);
		String mediaFileId = trackInfo.getMediaFileId();
		BeanUtil.copyProperties(trackInfoVo,trackInfo);
		if(!mediaFileId.equals(trackInfo.getMediaFileId())) {
			TrackMediaInfoVo trackMediaInfo = vodService.getTrackMediaInfo(trackInfo.getMediaFileId());
			trackInfo.setMediaType(trackMediaInfo.getType());
			trackInfo.setMediaSize(trackMediaInfo.getSize());
			trackInfo.setMediaDuration(BigDecimal.valueOf(trackMediaInfo.getDuration()));
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteInfoById(Long id) {
		TrackInfo trackInfo = trackInfoMapper.selectById(id);
		Integer orderNum = trackInfo.getOrderNum();
		trackInfoMapper.updateTrackNum(trackInfo.getAlbumId(), orderNum);

		trackInfoMapper.deleteById(id);

		LambdaQueryWrapper<TrackStat> trackStatLambdaQueryWrapper = new LambdaQueryWrapper<>();
		trackStatLambdaQueryWrapper.eq(TrackStat::getTrackId,id);
		trackStatMapper.delete(trackStatLambdaQueryWrapper);

		AlbumInfo albumInfo = albumInfoMapper.selectById(trackInfo.getAlbumId());
		albumInfo.setIncludeTrackCount(albumInfo.getEstimatedTrackCount() -1);
		albumInfoMapper.updateById(albumInfo);

		vodService.deleteTrackMedia(trackInfo.getMediaFileId());
	}

	@Override
	public Page<TrackListVo> getUserTrackByPage(Page<TrackListVo> trackListVoPage, TrackInfoQuery trackInfoQuery) {
		return trackInfoMapper.getUserTrackByPage(trackListVoPage, trackInfoQuery);
	}


}

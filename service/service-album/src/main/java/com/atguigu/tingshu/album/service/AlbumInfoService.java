package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AlbumInfoService extends IService<AlbumInfo> {


    void saveAlbumInfo(AlbumInfoVo albumInfoVo, Long userId);


    /**
     * 新增专辑统计信息
     * @param albumId 专辑ID
     * @param statType 统计类型
     */
    void saveAlbumStat(Long albumId, String statType);

    Page<AlbumListVo> getUserAlbumByPage(Page<AlbumListVo> pageInfo, AlbumInfoQuery albumInfoQuery);

    void removeAlbumInfo(Long id);

    AlbumInfoVo getAlbumInfo(Long id);

    void updateAlbumInfo(Long id, AlbumInfoVo albumInfoVo);

    List<AlbumInfo> getUserAllAlbumList(Long userId);

}

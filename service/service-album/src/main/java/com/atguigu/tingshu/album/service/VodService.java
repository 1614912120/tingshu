package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;

public interface VodService {

    TrackMediaInfoVo getTrackMediaInfo(String mediaFileId);

    void deleteTrackMedia(String mediaFileId);

}

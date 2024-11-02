package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencentcloudapi.cam.v20190116.models.CreateSAMLProviderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.simpleframework.xml.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "声音管理")
@RestController
@RequestMapping("api/album")
@SuppressWarnings({"all"})
public class TrackInfoApiController {

	@Autowired
	private TrackInfoService trackInfoService;

	/**
	 * 云颠跛
	 */
	@Operation(summary = "上传音视频文件到腾讯云点播服务")
	@PostMapping("/trackInfo/uploadTrack")

	public Result<Map<String,String>>  uploadTracK(MultipartFile file) {
		Map<String,String> mapResult = trackInfoService.uploadTrack(file);
		return Result.ok(mapResult);
	}

	@Operation(summary = "新增声音")
	@PostMapping("/trackInfo/saveTrackInfo")
	public Result saveTrackInfo(@RequestBody @Validated TrackInfoVo trackInfoVo){
		Long userId = AuthContextHolder.getUserId();
		trackInfoService.saveTrackInfo(userId,trackInfoVo);
		return Result.ok();
	}


	@Operation(summary =  "获取当前分类列表")
	@PostMapping("/trackInfo/findUserTrackPage/{page}/{limit}")
	public Result<Page<TrackListVo>> getUserTrackByPage(@PathVariable int page, @PathVariable int limit, @RequestBody TrackInfoQuery trackInfoQuery) {
		Long userId = AuthContextHolder.getUserId();
		trackInfoQuery.setUserId(userId);
		Page<TrackListVo> trackListVoPage = new Page<>(page, limit);
		trackListVoPage = trackInfoService.getUserTrackByPage(trackListVoPage,trackInfoQuery);
		return Result.ok(trackListVoPage);
	}



	@Operation(summary = "根据声音查询声音信息")
	@GetMapping("/trackInfo/getTrackInfo/{id}")
	public Result<TrackInfo> getTrackInfo(@PathVariable Long id) {
		TrackInfo info = trackInfoService.getById(id);
		return Result.ok(info);
	}


	@Operation(summary = "修改声音信息")
	@PostMapping("/trackInfo/updateTrackInfo/{id}")
	public Result updateTrackInfo(@PathVariable Long id,@RequestBody @Validated TrackInfoVo trackInfoVo) {
		trackInfoService.updateTrack(id,trackInfoVo);
		return Result.ok();
	}


	@Operation(summary = "删除声音信息")
	@DeleteMapping("/trackInfo/removeTrackInfo/{id}")
	public Result deleteInfo(@PathVariable Long id) {
		trackInfoService.deleteInfoById(id);
		return Result.ok();
	}





}


package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.login.GuiGuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.http.auth.AUTH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "专辑管理")
@RestController
@RequestMapping("api/album")
@SuppressWarnings({"all"})
public class AlbumInfoApiController {

	private static final Logger log = LoggerFactory.getLogger(AlbumInfoApiController.class);
	@Autowired
	private AlbumInfoService albumInfoService;


	/**
	 * TODO 该接口必须登录才能访问
	 * 创作者新增专辑
	 *
	 * @return
	 */
	@Operation(summary = "新增专辑")
	@PostMapping("/albumInfo/saveAlbumInfo")
	public Result saveAlbumInfo(@RequestBody @Validated AlbumInfoVo albumInfoVo) {
		Long userId = AuthContextHolder.getUserId();
		albumInfoService.saveAlbumInfo(albumInfoVo,userId);
		return Result.ok();
	}
	/**
	 * TODO 该接口必须登录才能访问
	 * 分页查询当前用户专辑列
	 *
	 * @param page           页码
	 * @param limit          页大小
	 * @param albumInfoQuery 分页查询条件
	 * @return
	 */
	@GuiGuLogin
	@Operation(summary = "分页查询当前用户专辑列表")
	@PostMapping("/albumInfo/findUserAlbumPage/{page}/{limit}")
	public Result<Page<AlbumListVo>> getUserAlbumByPage(@PathVariable int page, @PathVariable int limit,
														@RequestBody AlbumInfoQuery albumInfoQuery){
		log.info("getUserAlbumByPage执行");
		Long userId = AuthContextHolder.getUserId();
		System.out.println("id============"+userId);
		albumInfoQuery.setUserId(userId);

		Page<AlbumListVo> pageInfo = new Page<>(page, limit);
		pageInfo = albumInfoService.getUserAlbumByPage(pageInfo, albumInfoQuery);
		return Result.ok(pageInfo);

	}

	/**
	 * 根据专辑ID删除专辑
	 * @param id
	 * @return
	 */
	@Operation(summary = "根据专辑ID删除专辑")
	@DeleteMapping("albumInfo/removeAlbumInfo/{id}")
	public Result removeAlbumInfo(@PathVariable Long id) {
		albumInfoService.removeAlbumInfo(id);
		return Result.ok();
	}


	/**
	 * TODO 该接口必须登录才能访问
	 * 根据专辑ID查询专辑信息包含专辑属性列表
	 * @param id
	 * @return
	 */
	@Operation(summary = "根据专辑ID查询专辑信息包含专辑属性列表")
	@GetMapping("/albumInfo/getAlbumInfo/{id}")
	public Result<AlbumInfoVo> getAlbumInfo(@PathVariable("id") Long id) {
		AlbumInfoVo albumInfo = albumInfoService.getAlbumInfo(id);
		return Result.ok(albumInfo);
	}



	/**
	 * TODO 该接口必须登录才能访问
	 * 专辑修改
	 * @param id 专辑ID
	 * @param albumInfoVo 修改后专辑信息
	 * @return
	 */
	@Operation(summary = "修改专辑")
	@PutMapping("/albumInfo/updateAlbumInfo/{id}")
	public Result updateAlbumInfo(@PathVariable("id") Long id, @RequestBody AlbumInfoVo albumInfoVo){
		albumInfoService.updateAlbumInfo(id, albumInfoVo);
		return Result.ok();
	}

	/**
	 * TODO 该接口必须登录才能访问
	 * 查询当前登录用户所有专辑列表
	 *
	 * @return
	 */
	@Operation(summary = "查询当前用户所有专辑列表")
	@GetMapping("/albumInfo/findUserAllAlbumList")
	public Result<List<AlbumInfo>> getUserAllAlbumList() {
		Long userId = AuthContextHolder.getUserId();
		List<AlbumInfo> list =  albumInfoService.getUserAllAlbumList(userId);
		return Result.ok(list);
	}


}


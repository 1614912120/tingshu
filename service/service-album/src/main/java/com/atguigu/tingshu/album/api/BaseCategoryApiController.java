package com.atguigu.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.mapper.BaseAttributeMapper;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseAttributeValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "分类管理")
@RestController
@RequestMapping(value="/api/album")
@SuppressWarnings({"all"})
public class BaseCategoryApiController {

	@Autowired
	private BaseCategoryService baseCategoryService;

	@Autowired
	private BaseAttributeMapper baseAttributeMapper;

	@Operation(summary = "查询所有分类")
	@GetMapping("/category/getBaseCategoryList")
	public Result<List<JSONObject>> getBaseCategoryList() {
	    List<JSONObject> list = baseCategoryService.getBaseCategoryList();
		return Result.ok(list);
	}
	@Operation(summary = "根据一级分类Id获取分类属性以及属性值（标签名，标签值）列表")
	@GetMapping("/category/findAttribute/{category1Id}")
	public Result<List<BaseAttribute>>getAttributeByCategory1Id (@PathVariable Long category1Id){
		List<BaseAttribute> attributeByCategory1Id = baseAttributeMapper.getAttributeByCategory1Id(category1Id);
		return Result.ok(attributeByCategory1Id);
	}


}


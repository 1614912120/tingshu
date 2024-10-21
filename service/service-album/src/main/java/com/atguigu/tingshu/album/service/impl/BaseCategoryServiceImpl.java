package com.atguigu.tingshu.album.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.mapper.BaseCategory1Mapper;
import com.atguigu.tingshu.album.mapper.BaseCategory2Mapper;
import com.atguigu.tingshu.album.mapper.BaseCategory3Mapper;
import com.atguigu.tingshu.album.mapper.BaseCategoryViewMapper;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"all"})
public class BaseCategoryServiceImpl extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategoryService {

	@Autowired
	private BaseCategory1Mapper baseCategory1Mapper;

	@Autowired
	private BaseCategory2Mapper baseCategory2Mapper;

	@Autowired
	private BaseCategory3Mapper baseCategory3Mapper;


	@Autowired
	private BaseCategoryViewMapper baseCategoryViewMapper;
	//1.从视图获取
	@Override
	public List<JSONObject> getBaseCategoryList() {

		List<BaseCategoryView> baseCategoryViewsList = baseCategoryViewMapper.selectList(null);
		ArrayList<JSONObject> list = new ArrayList<>();
		Map<Long, List<BaseCategoryView>> category1MapList = baseCategoryViewsList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
		for (Map.Entry<Long, List<BaseCategoryView>> longListEntry : category1MapList.entrySet()) {
			JSONObject jsonObject = new JSONObject();
			Long category1Id = longListEntry.getKey();
			String category1Name = longListEntry.getValue().get(0).getCategory1Name();
			jsonObject.put("categoryName",category1Name);
			jsonObject.put("categoryId",category1Id);

			Map<Long, List<BaseCategoryView>> category2MapList = longListEntry.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
			ArrayList<JSONObject> jsonObjects2 = new ArrayList<>();
			for (Map.Entry<Long, List<BaseCategoryView>> listEntry2 : category2MapList.entrySet()) {
				JSONObject jsonObject2 = new JSONObject();
				Long category2Id = listEntry2.getKey();
				String category2Name = listEntry2.getValue().get(0).getCategory2Name();
				jsonObject2.put("categoryName",category2Name);
				jsonObject2.put("categoryId",category2Id);

				Map<Long, List<BaseCategoryView>> category3MapList = listEntry2.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));

				List<JSONObject> category3List = new ArrayList<>();
				for (Map.Entry<Long, List<BaseCategoryView>> listEntry3 : category3MapList.entrySet()) {
					JSONObject jsonObject3 = new JSONObject();
					String category3Name = listEntry3.getValue().get(0).getCategory3Name();
					Long category3Id = listEntry3.getKey();
					jsonObject3.put("categoryName",category3Name);
					jsonObject3.put("categoryId",category3Id);
					category3List.add(jsonObject3);
				}
				jsonObject2.put("categoryChild",category3List);

				jsonObjects2.add(jsonObject2);
			}
			jsonObject.put("categoryChild",jsonObjects2);

			list.add(jsonObject);

		}
		return list;
	}
}

package com.atguigu.tingshu.user.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.service.KafkaService;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.mapper.UserInfoMapper;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

	@Autowired
	private UserInfoMapper userInfoMapper;

	@Autowired
	private WxMaService wxMaService;

    @Autowired
    private RedisTemplate redisTemplate;


	@Autowired
	private KafkaService kafkaService;

	@Override
	public Map<String, String> wxLogin(String code) {
		try {
			WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
			if(sessionInfo != null) {
				//String openid = sessionInfo.getOpenid();
				String openid = "odo3j4q2KskkbbW-krfE-cAxUnzW";
				//2.根据openId查询用户记录  TODO 固定写死OpenID odo3j4q2KskkbbW-krfE-cAxUnzU
				LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
				userInfoLambdaQueryWrapper.eq(UserInfo::getWxOpenId,openid);
				UserInfo userInfo = userInfoMapper.selectOne(userInfoLambdaQueryWrapper);
				if(userInfo == null) {
					userInfo = new UserInfo();
					userInfo.setWxOpenId(openid);
					userInfo.setNickname("听友"+ IdUtil.getSnowflake().nextId());
					userInfoMapper.insert(userInfo);
					//TODO 发送异步MQ消息，通知账户微服务初始化当前用户账户余额信息
					kafkaService.sendMessage(KafkaConstant.QUEUE_USER_REGISTER,userInfo.getId().toString());
				}
				//2.2 根据OpenID获取到用户记录，

				//3.为登录微信用户生成令牌，将令牌存入Redis中
				String token = IdUtil.fastSimpleUUID();
				String loginKey = RedisConstant.USER_LOGIN_KEY_PREFIX+token;
				UserInfoVo userInfoVo = BeanUtil.copyProperties(userInfo, UserInfoVo.class);
				redisTemplate.opsForValue().set(loginKey,userInfoVo,RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);

				HashMap<String, String> map = new HashMap<>();
				map.put("token",token);
				return map;
			}
			return null;
		} catch (WxErrorException e) {
			log.error("微信登录异常：{}", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public UserInfoVo getUserInfoVoByUserId(Long userId) {
		UserInfo userInfo = userInfoMapper.selectById(userId);
		if (userInfo != null) {
			//属性拷贝,创建UserInfoVo 对象
			UserInfoVo userInfoVo = BeanUtil.copyProperties(userInfo, UserInfoVo.class);
			return userInfoVo;
		}
		return null;
	}

	@Override
	public void updateUser(UserInfoVo userInfoVo, Long userId) {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(userId);
		userInfo.setNickname(userInfoVo.getNickname());
		userInfo.setAvatarUrl(userInfoVo.getAvatarUrl());
		userInfoMapper.updateById(userInfo);
	}
}

package com.atguigu.tingshu.common.login;

import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class GuiGuLoginAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Around("execution(* com.atguigu.tingshu.*.api.*.*(..)) && @annotation(guiGuLogin)")
    @SneakyThrows
    public Object guiguLoginAspect(ProceedingJoinPoint pjp,GuiGuLogin guiGuLogin) throws Throwable {
        log.info("前置通知逻辑执行");
        //执行目标方法

        //1.尝试从请求对象中获取用户Token（请求头）
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = sra.getRequest();
        HttpServletResponse response = sra.getResponse();
        //2.根据Token获取用户信息（用户ID，用户昵称）
        String token = request.getHeader("token");
        String loginKey = RedisConstant.USER_LOGIN_KEY_PREFIX+token;

        UserInfoVo userInfoVo = (UserInfoVo)redisTemplate.opsForValue().get(loginKey);
        if (guiGuLogin.required()) {
            //要求用户必须登录才可以，如果此时用户信息为空抛出异常，小程序员引导用户进行登录
            if (userInfoVo == null) {
                throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
            }
        }
        //3.将用户信息隐式传入，在当前线程生命周期内获取到用户信息
        if(userInfoVo != null) {
            AuthContextHolder.setUserId(userInfoVo.getId());
            AuthContextHolder.setUsername(userInfoVo.getNickname());
        }
        Object proceed = pjp.proceed();
        log.info("后置通知逻辑执行");
        AuthContextHolder.removeUserId();
        AuthContextHolder.removeUsername();
        return  proceed;
    }
}

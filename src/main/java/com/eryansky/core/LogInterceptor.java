/**
 *  Copyright (c) 2012-2014 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.IpUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.browser.BrowserType;
import com.eryansky.common.utils.browser.BrowserUtils;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys.entity.Log;
import com.eryansky.modules.sys.service.LogManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 系统拦截器
 *
 * @author 尔演&Eryan eryanwcp@gmail.com
 * @version 2014-6-28
 */
public class LogInterceptor implements HandlerInterceptor {

    private static LogManager logManager = SpringContextHolder.getBean(LogManager.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {

        String requestRri = request.getRequestURI();
        String uriPrefix = request.getContextPath();

        if (StringUtils.startsWith(requestRri, uriPrefix) && ex != null) {

            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            if (sessionInfo != null) {

                StringBuilder params = new StringBuilder();
                int index = 0;
                for (Object param : request.getParameterMap().keySet()) {
                    params.append((index++ == 0 ? "" : "&") + param + "=");
                    params.append(StringUtils.abbr(StringUtils.endsWithIgnoreCase((String) param, "password")
                            ? "" : request.getParameter((String) param), 100));
                }

                Log log = new Log();
                log.setLoginName(sessionInfo.getLoginName());
                log.setType(LogType.exception.getValue());
                BrowserType browserType = BrowserUtils.getBrowserType(request);
                log.setBrowserType(browserType == null ? null : browserType.toString());
                log.setIp(IpUtils.getIpAddr(request));
                log.setModule(request.getRequestURI());
                log.setAction(request.getMethod());
                String exceptionInfo = ex != null ? ex.toString() : "";
                StringBuffer exceptionRemark = new StringBuffer();
                exceptionRemark.append("请求参数：").append(params.toString()).append("异常信息：").append(exceptionInfo);
                log.setRemark(exceptionRemark.toString());
                logManager.save(log);
            }
        }

    }

}

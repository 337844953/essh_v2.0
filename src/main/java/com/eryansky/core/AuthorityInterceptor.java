/**
 *  Copyright (c) 2012-2014 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.security.SecurityConstants;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security._enum.Logical;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.core.security.annotation.RequiresRoles;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.modules.sys.service.ResourceManager;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 权限拦截器
 */
public class AuthorityInterceptor extends HandlerInterceptorAdapter {


    protected Logger logger = LoggerFactory.getLogger(getClass());

    private List<String> excludeUrls = Lists.newArrayList();// 不需要拦截的资源

    public List<String> getExcludeUrls() {
        return excludeUrls;
    }

    public void setExcludeUrls(List<String> excludeUrls) {
        this.excludeUrls = excludeUrls;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o) throws Exception {
        ResourceManager resourceManager = SpringContextHolder.getBean(ResourceManager.class);
        //登录用户
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String requestUrl = request.getRequestURI();
        // 不拦截的URL
        if (Collections3.isNotEmpty(excludeUrls)) {
            for(String excludeUrl:excludeUrls){
                boolean flag = StringUtils.simpleWildcardMatch(excludeUrl,requestUrl);
                if(flag){
                    return true;
                }
            }
        }


        if(sessionInfo != null){
            //清空session中清空未被授权的访问地址
            Object unAuthorityUrl = request.getSession().getAttribute(SecurityConstants.SESSION_UNAUTHORITY_URL);
            if(unAuthorityUrl != null){
                request.getSession().setAttribute(SecurityConstants.SESSION_UNAUTHORITY_URL,null);
            }

            HandlerMethod handler= null;
            try {
                handler = (HandlerMethod) o;
            } catch (ClassCastException e) {
                logger.error(e.getMessage(),e);
            }

//            if(o instanceof HandlerMethod){
            if(handler != null){
                //需要登录
                RequiresUser requiresUser = handler.getMethodAnnotation(RequiresUser.class);
                if(requiresUser != null){
                    return true;
                }

                //角色要求注解
                RequiresRoles requiresRoles = handler.getMethodAnnotation(RequiresRoles.class);
                if(requiresRoles != null){
                    String[] roles = requiresRoles.value();
                    for(String role:roles){
                        boolean permittedRole = SecurityUtils.isPermittedRole(role);
                        if(Logical.AND.equals(requiresRoles.logical())){
                            if(permittedRole == false){
                                logger.warn("用户{}未被授权URL:{}！", sessionInfo.getLoginName(), requestUrl);
                                request.getRequestDispatcher(SecurityConstants.SESSION_UNAUTHORITY_PAGE).forward(request, httpServletResponse);
                                return false;
                            }
                        }else{
                            if(permittedRole == true){
                                return true;
                            }
                        }
                    }
                }

                //资源/权限 要求注解
                RequiresPermissions requiresPermissions = handler.getMethodAnnotation(RequiresPermissions.class);
                if(requiresPermissions != null){
                    String[] permissions = requiresPermissions.value();
                    for(String permission:permissions){
                        boolean permittedResource = SecurityUtils.isPermitted(permission);
                        if(Logical.AND.equals(requiresPermissions.logical())){
                            if(permittedResource == false){
                                logger.warn("用户{}未被授权URL:{}！", sessionInfo.getLoginName(), requestUrl);
                                request.getRequestDispatcher(SecurityConstants.SESSION_UNAUTHORITY_PAGE).forward(request, httpServletResponse);
                                return false;
                            }
                        }else{
                            if(permittedResource == true){
                                return true;
                            }
                        }
                    }
                }
            }



            String url = StringUtils.replaceOnce(requestUrl, request.getContextPath(), "");
            //检查用户是否授权该URL
            boolean isAuthority = resourceManager.isAuthority(url,sessionInfo.getUserId());
            if(!isAuthority){
                logger.warn("用户{}未被授权URL:{}！", sessionInfo.getLoginName(), requestUrl);
                request.getRequestDispatcher(SecurityConstants.SESSION_UNAUTHORITY_PAGE).forward(request, httpServletResponse);
                return false; //返回到登录页面
            }

            return true;
        }else{
            request.getRequestDispatcher(SecurityConstants.SESSION_UNAUTHORITY_LOGIN_PAGE).forward(request, httpServletResponse);
            return false; //返回到登录页面
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        if(e != null){

        }
    }
}

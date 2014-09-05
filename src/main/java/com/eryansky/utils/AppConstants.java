/**
 *  Copyright (c) 2012-2014 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.utils;

import com.eryansky.common.utils.SysConstants;
import com.eryansky.common.utils.io.PropertiesLoader;

/**
 * 系统使用的静态变量.
 * 
 * @author 尔演&Eryan eryanwcp@gmail.com
 * @date 2013-03-17 上午8:25:36
 */
public class AppConstants {
	/**
	 * 修改用户密码 个人(需要输入原始密码)
	 */
	public static final String USER_UPDATE_PASSWORD_YES = "1";
	/**
	 * 修改用户密码 个人(不需要输入原始密码)
	 */
	public static final String USER_UPDATE_PASSWORD_NO = "0";

    private static PropertiesLoader config = null;

    /**
     * 配置文件路径
     */
    public static final String CONFIG_FILE_PATH = "config.properties";
    /**
     * 配置文件日志保留时间 key
     */
    public static final String CONFIG_LOGKEEPTIME = "logKeepTime";

    /**
     * 配置文件(config.properties)
     */
    public static PropertiesLoader getConfig() {
        if(config == null){
            config = new PropertiesLoader(CONFIG_FILE_PATH);
        }
        return config;
    }

    /**
     * 日志保留时间 天(默认值:30).
     */
    public static int getLogKeepTime(){
        return AppConstants.getConfig().getInteger(CONFIG_LOGKEEPTIME,30);
    }


    /**
     * 获取管理端根路径
     */
    public static String getAdminPath() {
        return SysConstants.getAppConfig().getProperty("adminPath");
    }

    /**
     * 获取前端根路径
     */
    public static String getFrontPath() {
        return SysConstants.getAppConfig().getProperty("frontPath");
    }

    /**
     * 获取URL后缀
     */
    public static String getUrlSuffix() {
        return SysConstants.getAppConfig().getProperty("urlSuffix");
    }


    /**
     * 获取配置
     */
    public static String getConfig(String key) {
        return SysConstants.getAppConfig().getProperty(key);
    }
}

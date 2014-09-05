/**
 *  Copyright (c) 2012-2014 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.listener;

import com.eryansky.common.web.listener.DefaultSystemInitListener;
import com.eryansky.core.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSessionEvent;

/**
 * 系统初始化监听 继承默认系统启动监听器.
 * 
 * @author 尔演&Eryan eryanwcp@gmail.com
 * @date 2012-12-11 下午4:56:54
 */
public class SystemInitListener extends DefaultSystemInitListener{

	private static final Logger logger = LoggerFactory
			.getLogger(SystemInitListener.class);

	public SystemInitListener() {
	}

	/**
	 * session销毁
	 */
	public void sessionDestroyed(HttpSessionEvent evt) {
		logger.debug("sessionDestroyed");
		String sessionId = evt.getSession().getId();
		SecurityUtils.removeUserFromSession(sessionId,true);
	}

}

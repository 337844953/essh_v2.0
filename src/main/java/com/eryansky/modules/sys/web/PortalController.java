/**
 *  Copyright (c) 2012-2014 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Datagrid;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.PropertyFilter;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.modules.sys.entity.Bug;
import com.eryansky.modules.sys.service.BugManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;

/**
 * Portal主页门户管理
 *
 * @author 尔演&Eryan eryanwcp@gmail.com
 * @date   2014-07-31 12:30
 */
@Controller
@RequestMapping(value = "/portal")
public class PortalController extends SimpleController {

    @Autowired
    private BugManager bugManager;

    @RequestMapping("")
    public ModelAndView portal() {
        ModelAndView modelAnView = new ModelAndView("layout/portal");
        return modelAnView;
    }

    /**
     * 我的通知
     *
     * @return
     */
    @RequestMapping("bug")
    public ModelAndView notice() {
        ModelAndView modelAnView = new ModelAndView("layout/portal-bug");
        Page<Bug> page = new Page<Bug>(Page.DEFAULT_PAGESIZE);
        page = bugManager.find(page,new ArrayList<PropertyFilter>());
        Datagrid datagrid = new Datagrid(page.getTotalCount(),page.getResult());
        modelAnView.addObject("bugDatagrid",datagrid);
        return modelAnView;
    }

}

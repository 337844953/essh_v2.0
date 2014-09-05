<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<script type="text/javascript" src="${ctxStatic}/app/layout/center.js" charset="utf-8"></script>
<div id="layout_center_tabs" fit="true" style="overflow: hidden;">
	<%--<div id="layout_center_tabs_index" title="首页" data-options="href:'${ctx}/common/layout/portal',iconCls:'icon-application'"></div>--%>
</div>
<div id="layout_center_tabsMenu" style="width: 120px;display:none;">
	<div type="refresh" data-options="iconCls:'easyui-icon-reload'">刷新</div>
	<div class="menu-sep"></div>
	<div type="close" data-options="iconCls:'easyui-icon-cancel'">关闭</div>
	<div type="closeOther">关闭其他</div>
	<div type="closeAll">关闭所有</div>
</div>
<%--全屏工具栏--%>
<div id="layout_center_tabs_full-tools" style="display: none">
    <a href="#" class="easyui-linkbutton easyui-tooltip" title="全屏" data-options="iconCls:'eu-icon-full_screen',plain:true"
       onclick="javascript:screenToggle(true);"></a>
    <a href="#" class="easyui-linkbutton easyui-tooltip" title="刷新" data-options="iconCls:'easyui-icon-reload',plain:true"
       onclick="javascript:refresh();"></a>
    <a href="#" class="easyui-linkbutton easyui-tooltip" title="关闭" data-options="iconCls:'easyui-icon-cancel',plain:true"
       onclick="javascript:cancel();"></a>
</div>
<%--退出全屏工具栏--%>
<div id="layout_center_tabs_unfull-tools" style="display: none;">
    <a href="#" class="easyui-linkbutton easyui-tooltip" title="退出全屏" data-options="iconCls:'eu-icon-exit_full_screen',plain:true"
       onclick="javascript:screenToggle(false);"></a>
    <a href="#" class="easyui-linkbutton easyui-tooltip" title="刷新" data-options="iconCls:'easyui-icon-reload',plain:true"
       onclick="javascript:refresh();"></a>
    <a href="#" class="easyui-linkbutton easyui-tooltip" title="关闭" data-options="iconCls:'easyui-icon-cancel',plain:true"
       onclick="javascript:cancel();"></a>
</div>
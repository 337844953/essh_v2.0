<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<%@ include file="/common/meta.jsp"%>
<style type="text/css">
.title{
	font-size:16px;
	font-weight:bold;
	padding:20px 10px;
	background:#eee;
	overflow:hidden;
	border-bottom:1px solid #ccc;
}
</style>
<script src="${ctxStatic}/js/easyui-${ev}/datagridview/datagrid-bufferview.js"></script>
<link href="${ctxStatic}/js/bootstrap/2.3.2/custom/bootstrap.css" rel="stylesheet">
<script type="text/javascript" src="${ctxStatic}/app/layout/portal.js" charset="utf-8"></script>
<div class="easyui-layout" fit="true" style="width:100%;height:100%;overflow: hidden;">
	<div region="center" style="height: 100%;overflow: hidden;">
		<div id="layout_portal_portal"
			style="position: relative;width:100%;height:100%; overflow-x: hidden;">
			<div></div>
			<div></div>
		</div>	
	</div>
	<div  style="height: 100%;width:200px;overflow: hidden;"
          data-options="region:'east',title:'日历',href:'${ctx}/common/layout/portal-east'">
	</div>
</div>


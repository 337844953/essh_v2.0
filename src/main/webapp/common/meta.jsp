﻿<%@ page import="com.eryansky.common.utils.browser.BrowserType" %>
<%@ page import="com.eryansky.common.utils.browser.BrowserUtils" %>
<%
    String ctx = request.getContextPath();
    BrowserType browserType = BrowserUtils.getBrowserType(request);
%>
<c:set var="ev" value="1.4" />
<meta http-equiv="X-UA-Compatible" content="IE=EDGE;chrome=1" />
<meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
<meta http-equiv="Cache-Control" content="no-store"/>
<meta http-equiv="Pragma" content="no-cache"/>
<meta http-equiv="Expires" content="0"/>
<meta name="author" content="尔演&Eryan" />
<link rel="shortcut icon" href="${ctxStatic}/img/favicon.ico" />
<script type="text/javascript" charset="utf-8">
    var ctx = "${ctx}";
    var ctxStatic = "${ctxStatic}";
</script>
<link rel="stylesheet" type="text/css" href="${ctxStatic}/css/default.css" />
<link rel="stylesheet" type="text/css" href="${ctxStatic}/css/form_style.css" />
<%-- 引入jQuery --%>
<%
    if (browserType != null
            && (browserType.equals(BrowserType.IE11) || browserType.equals(BrowserType.IE10) || browserType.equals(BrowserType.IE9)
            || browserType.equals(BrowserType.Chrome) || browserType.equals(BrowserType.Firefox)
            || browserType.equals(BrowserType.Safari))) {
        out.println("<script type='text/javascript' src='" + ctx + "/static/js/jquery/jquery-2.1.0.min.js' charset='utf-8'></script>");
    } else {
        out.println("<script type='text/javascript' src='" + ctx + "/static/js/jquery/jquery-1.10.2.min.js' charset='utf-8'></script>");
        out.println("<script type='text/javascript' src='" + ctx + "/static/js/jquery/jquery-migrate-1.2.1.min.js' charset='utf-8'></script>");
    }
%>
<%-- jQuery Cookie插件 --%>
<script type="text/javascript" src="${ctxStatic}/js/jquery/jquery.cookie-min.js" charset="utf-8"></script>

<%--<link rel="stylesheet" type="text/css" href="${ctxStatic}/js/bootstrap/3.2.0/css/bootstrap.css" />--%>
<%--<link rel="stylesheet" type="text/css" href="${ctxStatic}/js/bootstrap/2.3.2/css/bootstrap.css" />--%>

<link id="easyuiTheme" rel="stylesheet" type="text/css" href="${ctxStatic}/js/easyui-${ev}/themes/<c:out value="${cookie.easyuiThemeName.value}" default="bootstrap"/>/easyui.css" />

<%--<link rel="stylesheet" type="text/css" href="${ctxStatic}/js/easyui-${ev}/themes/icon.css" />--%>
<link rel="stylesheet" type="text/css" href="${ctxStatic}/js/easyui-${ev}/extend/icon/easyui-icon.css" />
<script type="text/javascript" src="${ctxStatic}/js/My97DatePicker/WdatePicker.js" charset="utf-8"></script>
<script type="text/javascript" src="${ctxStatic}/js/easyui-${ev}/jquery.easyui.mine.js" charset="utf-8"></script>
<script type="text/javascript" src="${ctxStatic}/js/easyui-${ev}/locale/easyui-lang-zh_CN.js" charset="utf-8"></script>

<link rel="stylesheet" type="text/css" href="${ctxStatic}/js/easyui-${ev}/extend/my97/my97.css" />
<script type="text/javascript" src="${ctxStatic}/js/easyui-${ev}/extend/my97/jquery.easyui.my97.js" charset="utf-8"></script>

<link rel="stylesheet" type="text/css" href="${ctxStatic}/js/easyui-${ev}/portal/portal.css">
<script type="text/javascript" src="${ctxStatic}/js/easyui-${ev}/portal/jquery.portal-min.js" charset="utf-8"></script>

<link rel="stylesheet" type="text/css" href="${ctxStatic}/js/easyui-${ev}/extend/icon/eu-icon.css" />
<%-- jQuery方法扩展 --%>
<script type="text/javascript" src="${ctxStatic}/js/jquery/jquery-extend.js" charset="utf-8"></script>
<%-- easyui扩展 --%>
<script type="text/javascript" src="${ctxStatic}/js/easyui-${ev}/extend/js/easyui-extend.js" charset="utf-8"></script>
<!-- 屏蔽键盘等事件 -->
<%-- 
<script type="text/javascript" src="${ctxStatic}/js/prohibit.js" charset="utf-8"></script>
--%>
<%-- easyui自定义表单校验扩展 --%>
<script type="text/javascript" src="${ctxStatic}/js/easyui-${ev}/extend/js/validatebox-extend.js" charset="utf-8"></script>
<%-- easyui后台异步校验 --%>
<script type="text/javascript" src="${ctxStatic}/js/easyui-${ev}/extend/js/validatebox-ajax.js" charset="utf-8"></script>

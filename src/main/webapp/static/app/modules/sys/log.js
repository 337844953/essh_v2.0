var log_datagrid;
var log_search_form;
var log_keepTime_form;
var log_keepTime_dialog;
$(function () {
    log_search_form = $('#log_search_form').form();
    $('#log_keepTime_form').form({
        url: ctx+'/sys/log/updateKeepTime',
        onSubmit: function(param){
            $.messager.progress({
                title : '提示信息！',
                text : '数据处理中，请稍后....'
            });
            var isValid = $(this).form('validate');
            if (!isValid) {
                $.messager.progress('close');
            }
            return isValid;
        },
        success: function(data){
            $.messager.progress('close');
            data = $.parseJSON(data);
            if (data.code == 1) {
                eu.showMsg(data.msg);//操作结果提示
            } else {
                eu.showAlertMsg(data.msg, 'error');
            }
        }
    });
    //数据列表
    log_datagrid = $('#log_datagrid').datagrid({
        url: ctx+'/sys/log/datagrid',
        fit:true,
        pagination: true,//底部分页
        rownumbers: true,//显示行数
        fitColumns: false,//自适应列宽
        striped: true,//显示条纹
        pageSize: 20,//每页记录数
        singleSelect: false,//单选模式
        checkbox: true,
        nowrap: false,
        border: false,
        remoteSort: true,//是否通过远程服务器对数据排序
        sortName: 'id',//默认排序字段
        sortOrder: 'desc',//默认排序方式 'desc' 'asc'
        idField: 'id',
        frozenColumns:[[
            {field: 'ck',checkbox: true,width: 60},
            {field: 'typeView',title: '日志类型',width: 120},
            {field: 'loginName',title: '登录名',width: 120}
        ]],
        columns: [ [
            {field: 'id',title: '主键',hidden: true,sortable: true,align: 'right', width: 80 } ,
            {field: 'ip', title: 'IP地址', width: 100} ,
            {field: 'browserType', title: '客户端', width: 100} ,
            {field: 'module',title: '模块', width: 160},
            {field: 'action',title: '操作',width: 200},
            {field: 'operTime',title: '操作时间',width: 136,sortable: true} ,
            {field: 'actionTime',title: '操作耗时(ms)',width: 100},
            {field: 'remark',title: '备注',width: 200 }
        ]],
        toolbar:[{
            text:'删除',
            iconCls:'easyui-icon-remove',
            handler:function(){del()}
        },'-',{
            text:'清空所有',
            iconCls:'easyui-icon-no',
            handler:function(){delAll()}
        },'-',{
            text:'设置日志保留时间',
            iconCls:'easyui-icon-edit',
            handler:function(){
                showKeepTimeDialog();
            }
        }]
    }).datagrid("showTooltip");

    //日志类型 搜索选项
    $('#filter_EQI_type').combobox({
        url:ctx+'/sys/log/logTypeCombobox?selectType=all',
        editable:false,//是否可编辑
        width:120
    });
});

function formKeepTimeInit(){
    log_keepTime_form = $('#log_keepTime_form').form({
        url: ctx+'/sys/log/updateKeepTime',
        onSubmit: function(param){
            $.messager.progress({
                title : '提示信息！',
                text : '数据处理中，请稍后....'
            });
            var isValid = $(this).form('validate');
            if (!isValid) {
                $.messager.progress('close');
            }
            return isValid;
        },
        success: function(data){
            $.messager.progress('close');
            var json = $.parseJSON(data);
            if (json.code ==1){
                log_keepTime_dialog.dialog('destroy');//销毁对话框
                eu.showMsg(json.msg);//操作结果提示
            }else {
                eu.showAlertMsg(json.msg,'error');
            }
        }
    });
}
//显示弹出窗口
function showKeepTimeDialog(){
    var dialogUrl = ctx+"/sys/log/time";
    //弹出对话窗口
    log_keepTime_dialog = $('<div/>').dialog({
        title:'设置日志保留时间',
        top:20,
        width : 360,
        height:136,
        modal : true,
        href : dialogUrl,
        buttons : [ {
            text : '保存',
            iconCls : 'easyui-icon-save',
            handler : function() {
                log_keepTime_form.submit();
            }
        },{
            text : '关闭',
            iconCls : 'easyui-icon-cancel',
            handler : function() {
                log_keepTime_dialog.dialog('destroy');
            }
        }],
        onClose : function() {
            log_keepTime_dialog.dialog('destroy');
        },
        onLoad:function(){
            formKeepTimeInit();
        }
    });

}

//删除
function del() {
    var rows = log_datagrid.datagrid('getSelections');
    if (rows.length > 0) {
        $.messager.confirm('确认提示！', '您确定要删除当前选中的所有行？', function (r) {
            if (r) {
                var ids = new Array();
                $.each(rows,function(i,row){
                    ids[i] = row.id;
                });
                $.ajax({
                    url:ctx+'/sys/log/remove',
                    type:'post',
                    data: {ids:ids},
                    traditional:true,
                    dataType:'json',
                    success:function(data) {
                        if (data.code==1){
                            log_datagrid.datagrid('clearSelections');//取消所有的已选择项
                            log_datagrid.datagrid('load');//重新加载列表数据
                            eu.showMsg(data.msg);//操作结果提示
                        } else {
                            eu.showAlertMsg(data.msg,'error');
                        }
                    }
                });
            }
        });
    } else {
        eu.showMsg("请选择要操作的对象！");
    }
}

/**
 * 清空所有日志
 */
function delAll(){
    $.post(ctx+'/sys/log/removeAll',
        function (data) {
            if (data.code == 1) {
                log_datagrid.datagrid('clearSelections');//取消所有的已选择项
                log_datagrid.datagrid('load');//重新加载列表数据
                eu.showMsg(data.msg);//操作结果提示
            } else {
                eu.showAlertMsg(data.msg, 'error');
            }
        },
        'json');
}

//搜索
function search() {
    log_datagrid.datagrid('load', $.serializeObject(log_search_form));
}
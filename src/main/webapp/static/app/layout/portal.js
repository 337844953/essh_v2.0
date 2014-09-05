var $layout_portal_portal;
var protal_titles = ['&nbsp;内部通知','&nbsp;技术架构'];
$(function() {
    panels = [ {
        id : 'p1',
        title : protal_titles[0],
        height : 360,
        collapsible : true,
        href:ctx+'/portal/bug'
    }, {
        id : 'p2',
        title : protal_titles[1],
        height : 360,
        collapsible : true,
        href : ctx+'/common/layout/portal-component'
    }];

    $('#layout_portal_portal').portal({
        border : false,
        fit : true,
        onStateChange : function() {
            $.cookie('portal-state', getPortalState(), {
                expires : 7
            });
        }
    });
    var state = $.cookie('portal-state');
    if (!state) {
        state = 'p1:p2';/*冒号代表列，逗号代表行*/
    }
    addPortalPanels(state);
    $('#layout_portal_portal').portal('resize');

});

function getPanelOptions(id) {
    for ( var i = 0; i < panels.length; i++) {
        if (panels[i].id == id) {
            return panels[i];
        }
    }
    return undefined;
}
function getPortalState() {
    var aa=[];
    for(var columnIndex=0;columnIndex<2;columnIndex++) {
        var cc=[];
        var panels=$('#layout_portal_portal').portal('getPanels',columnIndex);
        for(var i=0;i<panels.length;i++) {
            cc.push(panels[i].attr('id'));
        }
        aa.push(cc.join(','));
    }
    return aa.join(':');
}
function addPortalPanels(portalState) {
    var columns = portalState.split(':');
    for (var columnIndex = 0; columnIndex < columns.length; columnIndex++) {
        var cc = columns[columnIndex].split(',');
        for (var j = 0; j < cc.length; j++) {
            var options = getPanelOptions(cc[j]);
            if (options) {
                var p = $('<div/>').attr('id', options.id).appendTo('body');
                p.panel(options);
                $('#layout_portal_portal').portal('add', {
                    panel : p,
                    columnIndex : columnIndex
                });
            }
        }
    }
}
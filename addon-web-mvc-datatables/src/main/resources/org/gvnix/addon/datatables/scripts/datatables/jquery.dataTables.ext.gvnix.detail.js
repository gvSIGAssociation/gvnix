/**
 * Datatables details functions.
 */

/**
 * Request an include on page a detail datatables content.
 */
function loadDetail(detailDatatableId, mainDatatablesId, url) {
	jQuery("#" + mainDatatablesId).tabs("destroy");
	jQuery('#' + detailDatatableId).attr('href', url);
	showDetailTabs(mainDatatablesId);
}

/**
 * generate url to obtain some detail.
 */
function getDetailUrl(mainDatatableId, baseUrl, mappedBy, rowId) {
	return baseUrl + '&' + mappedBy + '=' + rowId + '&datatablesMappedValue=' + rowId + '&_dt_parentId=' + mainDatatableId;
}

/**
 * Initialize the JQuery HTML structure to draw it as tabs.
 */
function showDetailTabs(mainDatatablesId) {
	var tabs = jQuery("#" + mainDatatablesId).tabs({
      beforeLoad: function(event, ui) {
          ui.jqXHR.error(function() {
            ui.panel.html("Error getting detail datatable from server.");
          });
      },
      active: jQuery.cookie(mainDatatablesId + "_active_panel"),
    });
	tabs.find( ".ui-tabs-nav" ).sortable({
		axis: "x",
		stop: function() {
			tabs.tabs( "refresh" );
		}
	});
	tabs.click(function(e) {
		var curTab = jQuery('.ui-tabs-active');
        curTabIndex = curTab.index();
    	jQuery.cookie(mainDatatablesId + "_active_panel", curTabIndex);		
	});
}

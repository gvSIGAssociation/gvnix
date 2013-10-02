/**
 * Datatables details functions.
 */

/**
 * Request an include on page a detail datatables content.
 */
function loadDetail(trId, detailDatatableId, mainDatatablesId, url, errorMessage, received_bodies, length) {
	$.get(url)
	.done(function(datatablesDetailContent) {
		received(detailDatatableId, mainDatatablesId, datatablesDetailContent, received_bodies, length);
	})
	.fail(function(error) {
		if (error.status != 0) {
			alert(errorMessage + ' (Error: ' + error.status + ', URL: ' + url + ').');
		}
		jQuery('#' + mainDatatablesId).hide();
	});
}

function received(bodyId, mainDatatablesId, bodyReceived, received_bodies, length) {
	console.log(received_bodies.length);
	received_bodies.push({'id': bodyId, 'body': bodyReceived});
	if (received_bodies.length == length) {
		jQuery.each(received_bodies, function (index,value){
			var $container = jQuery('#'+value.id);
			$container.html(value.body);
		});
		received_bodies.splice(0,received_bodies.length);
		var tab = jQuery('#' + mainDatatablesId);
		tab.tabs('refresh');
		tab.show();
	}
}

/**
 * Hide a detail datatables content.
 */
function hideDetail(detailDatatableId) {
	jQuery('#' + detailDatatableId).hide();
}

/**
 * generate url to obtain some detail.
 */
function getDetailUrl(baseUrl, mappedBy, rowId) {
	return baseUrl + '&' + mappedBy + '=' + rowId + '&datatablesMappedValue=' + rowId;
}

/**
 * Initialize the JQuery HTML structure to draw it as tabs.
 */
function showDetailTabs(mainDatatablesId) {
	var tabs = jQuery("#" + mainDatatablesId).tabs();
	tabs.find( ".ui-tabs-nav" ).sortable({
		axis: "x",
		stop: function() {
			tabs.tabs( "refresh" );
		}
	});
}

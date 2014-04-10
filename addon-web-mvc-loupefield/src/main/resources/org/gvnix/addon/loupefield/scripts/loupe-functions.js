/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */

/**
 * This method add event click to the loupe button
 * that displays the datatable Dialog
 * 
 * @param input 
 */
function bindLoupeClickSearch($aInput) {
	var data = $aInput.data();
	var loupeButton = $("#"+data.searchbuttonid);
	loupeButton.click(function() {
		var data = loupeButton.data();
		// Adding the div where the page is gonna be displayed if not exists
		$aInput.parent().append(
				'<div id="' + data.listselectorid
						+ '" style="display:none;"></div>');
		// Creating dialog, but not oppening
		var buttonOpts = {};
		buttonOpts[$aInput.data().acceptlabel] = function() {
			// Getting master table inside dialog
			var table = $("#" + data.listselectorid
					+ " table[class=dataTable][id]")[0];
			var tableId = table.attributes.id.value;
			// Getting datatableInstance
			var datatableInstance = $("#" + tableId)
					.dataTable();
			// Getting rowClick properties
			var datatableRowClick = datatableInstance
					.fnRowClick();
			// Getting last Clicked element
			var idLastClicked = datatableRowClick
					.fnGetLastClickedRowId();
			
			// Setting id into input field
			$("#" + $aInput.attr("id")).val(idLastClicked);

			// Remove Dialog
			$aInput.parent().remove(
					"#" + data.listselectorid);
			selectorDialog.dialog('destroy');

			// FindById to show label
			findRecordById(idLastClicked, $aInput.data().path,
					data.idlabel);
		};
		var selectorDialog = $("#" + data.listselectorid).dialog(
				{
					autoOpen : false,
					modal : true,
					resizable : false,
					width : 800,
					height : 500,
					title : "Select " + data.label,
					buttons : buttonOpts,
					close : function(event, ui) {
						selectorDialog.dialog('close');
						$aInput.parent().remove("#" + data.listselectorid);
						selectorDialog.dialog('destroy');
					}
				});

		// Adding list page to Dialog
		selectorDialog.load(data.listurl,function(a,e,i){
			setTimeout(function(){
				var table = $("#" + data.listselectorid + " table[class=dataTable][id]")[0];
				var tableId = table.attributes.id.value;
				// Getting datatableInstance
				var datatableInstance = $("#" + tableId).dataTable();
				var editingInstance = datatableInstance.fnEditing();
				
				var isAjaxDatatable = datatableInstance.fnEditing()._data.oSettings.oFeatures.bServerSide;
				
				// Setting Datatable as no editable
				editingInstance.fnSetNoEditableDatatable(isAjaxDatatable);
				
			},200);
		}).dialog('open');
		
		return false;

	});

};

/**
 * This method add event keyup to the generated input
 * to update the label with the correct data to display
 * 
 * @param $aInput
 */
function bindLoupeInputKeyUp($aInput) {
	// When the value changes, get the value and findById
	var data = $aInput.data();
	$aInput.keyup(function() {
		var id = $aInput.val();
		if (id != "" && !isNaN(parseInt(id))) {
			findRecordById(id, data.path, data.idlabel);
		} else if (id != "" && isNaN(parseInt(id))) {
			$("#" + data.idlabel).css("color", "red");
			$("#" + data.idlabel).html("No String Values");
		} else {
			$("#" + data.idlabel).html("");
		}
	});
};

/**
 * Find record using the typed id or the
 * id passed by the selected row in the datatable
 * 
 * @param id
 * @param url
 * @param loupeLabel
 */
function findRecordById(id, url, loupeLabel) {
	$.ajax({
		url : url + "?find=ById",
		data : {
			id : id
		},
		success : function(data) {
			// Detecting if not exists method to findById
			if (data.substring(0,1) == "<"){
				$("#" + loupeLabel).css("color", "red");
				data = "Ajax Error. Method findById not exists. ";
			}else if (data == "No Data Found") {
				$("#" + loupeLabel).css("color", "red");
			} else {
				$("#" + loupeLabel).css("color", "green");
			}

			$("#" + loupeLabel).html(data);
		}
	});
}

/**
 * Function to create loupe button and label
 * 
 * @param $aInput
 */
function createItems($aInput) {
	var data = $aInput.data();
	// Adding button search if not exists
	var buttonSearch = $("#"+data.searchbuttonid);
	if(buttonSearch.length == 0){
		$aInput.css("width","50%");
		$('<span style="cursor: pointer; margin-left:10px;" id="' + data.searchbuttonid
				+ '" class="glyphicon glyphicon-search" data-parent="'
				+ data.parent + '" data-listselectorid="'
				+ data.listselectorid + '" data-label="' + data.label
				+ '" data-inputid="' + data.inputid + '" data-listurl="'
				+ data.listurl + '" data-idlabel="' + data.idlabel + '"/>').insertAfter($aInput);
	}
	
	// Adding label span
	var label = $("#"+data.idlabel);
	if(label.length == 0){
		$('<label id="'+data.idlabel+'" style="margin-right:20px;"></label>').insertBefore($aInput);
	}
}

/**
 * 
 */
fnRegisterFunctionsToCallBack(function(context) {
	jQuery(".loupe_control", context).each(function(index) {
		var $input = $(this);
		var data = $input.data();
		var loupeButton = $("#"+data.searchbuttonid);
		// If loupe button not exists add events
		if(loupeButton.length == 0){
			//Creating loupe button and loupe label to show the result
			createItems($input);
			// Setting events in elements
			bindLoupeClickSearch($input);
			bindLoupeInputKeyUp($input);
		}
	});
});

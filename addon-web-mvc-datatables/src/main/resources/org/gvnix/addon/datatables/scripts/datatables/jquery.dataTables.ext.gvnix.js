/**
 * gvNIX Datatables extended initialization
 *
 * @param datatable
 *            settings
 * @param tableId
 * @param options
 *            to initialize
 * @param count
 *            of tries-to-delay-initialize
 */
function fnDatatablesExtInit(oSettings, tableId, options, count) {

	var $table = null;
	var atables = $.fn.dataTable.fnTables(true);
	for (i = 0; i < atables.length; i++) {
		$table = jQuery(atables[i]).dataTable();
		if ($table.fnSettings().sTableId == tableId) {
			break;
		}
		$table = null;
	}
	if ($table == null) {
		// dataTable instance not found
		if (count) {
			// Check number of tries
			if (count > 12) {
				log("Datatables "
						+ tableId
						+ " gvnix init ext is not loaded because it's currently not available (not visible tab)");
				return;
			}
			// increase tries count
			count++;
		} else {
			// init try count
			count = 1;
		}
		// Try to load it delayed
		log("Delay loading of '" + tableId + "':" + count);
		window.setTimeout(fnDatatablesExtInit, 100, oSettings, tableId,
				options, count);
		return;
	}
	if (options.filterOnReturn) {

		// Enable filter on return
		$table.fnFilterOnReturn();
	} else {

		// Enable filtering delay
		if (options.filteringDelay) {
			if (typeof options.filteringDelay == "number") {
				$table.fnSetFilteringDelay(options.filteringDelay);
			} else {
				try {
					$table.fnSetFilteringDelay(Number
							.parseInt(options.filteringDelay));
				} catch (e) {
					log("Error parsing filteringDelay option (using default): "
							+ e);
					$table.fnSetFilteringDelay(500);
				}
			}
		} else {
			$table.fnSetFilteringDelay(500);
		}
	}

	// Init gvNIX selection support. Note this function is in file
	// 'jquery.dataTables.ext.gvnix.selection.js', so we must check if
	// init function has been loaded
	if (options.selection && typeof ($table.fnSelection) === "function") {
		if (typeof options.selection == "object") {
			$table.fnSelection(options.selection);
		} else {
			$table.fnSelection();
		}
	}

	// Init gvNIX inline editing support. Note this function is in file
	// 'jquery.dataTables.ext.gvnix.editing.js', so we must check if
	// init function has been loaded
	if (options.editing && typeof ($table.fnEditing) === "function") {
		if (typeof options.editing == "object") {
			$table.fnEditing(options.editing);
		} else {
			$table.fnEditing();
		}
	}

	// Init gvNIX rowclick support. Note this function is in file
	// 'jquery.dataTables.ext.gvnix.rowclick.js', so we must check if
	// init function has been loaded
	if (options.rowclick && typeof ($table.fnRowClick) === "function") {
		if (typeof options.rowclick == "object") {
			$table.fnRowClick(options.rowclick);
		} else {
			$table.fnRowClick();
		}
	}

	// Init gvNIX rowOnTop support. Note this function is in file
	// 'jquery.dataTables.ext.gvnix.rowontop.js', so we must check if
	// init function has been loaded
	if (options.rowsOnTop && typeof ($table.fnRowOnTop) === "function") {
		if (typeof options.rowsOnTop == "object") {
			$table.fnRowOnTop(options.rowsOnTop);
		} else {
			$table.fnRowOnTop();
		}
	}

	if (count) {
		// If delay loading adjust column size
		$table.fnAdjustColumnSizing();
	}
	
	var st = $table.fnSettings();
	
	// Register Footer callback
	st.oApi._fnCallbackReg(st, 'aoFooterCallback', 
			function(row, data, start, end, display){
		this.fnGvNIX_FooterCallback(row, data, start, end, display);
	});
	
	// Register Draw callback
	st.oApi._fnCallbackReg(st, 'aoDrawCallback', function( oSettings ) {
		this.fnGvNIX_DrawCallback(oSettings);
	});
	
	// Register CreatedRow Callback
	st.oApi._fnCallbackReg(st, 'aoRowCreatedCallback', function(nRow, aData, iDataIndex){
		this.fnGvNIX_RowCreatedCallback(nRow, aData, iDataIndex);
	});
	
	// Calling at first time Footer Callback
	updateDatatablesFilters($table.fnSettings());
	
	// Calling at first time RowCreatedCallback
	var $tds = $table.find("td");
	var sSearch = $table.fnSettings().oPreviousSearch.sSearch;
	if($tds.length > 0 && sSearch.length > 0){
		showSearchResultsHighLighted($tds, sSearch);
	}
	// Displaying always the clicked row if row click exists
	fnScrollDatatableToRowClick($table);
	
	// Changing filter class when there's some
	// filter value
	fnChangeFilterClass($table);
}

/**
 * Display create form.
 *
 * @param sPanelId
 */
function fnDisplayCreateForm(sTableId) {
	var oTable = jQuery('#' + sTableId);
	if (oTable.length == 0) {
		throw "fnDisplayCreateForm : id not found '" + sTableId + "'";
	}
	oTable.dataTable().fnEditing().fnBeginCreate(sTableId);
}

/**
 * Start editing a row of a datatables
 *
 * @param sTableId
 * @param sRowId
 */
function fnEditDatatableRow(sTableId, sRowId) {
	var oTable = jQuery('#' + sTableId);
	if (oTable.length == 0) {
		throw "fnEditDatatableRow : id not found '" + sTableId + "'";
	}
	oTable.dataTable().fnEditing().fnEditRows(sRowId);
}

/**
 * Perform datatables AJAX request using POST method
 *
 * @param sSource
 * @param aoData
 * @param fnCallback
 * @param oSettings
 */
function doAjaxRequestByPost(sSource, aoData, fnCallback, oSettings) {
	oSettings.jqXHR = $.ajax({
		"dataType" : 'json',
		"type" : "POST",
		"url" : sSource,
		"data" : aoData,
		"success" : fnCallback
	});
}

/**
 * Try to show a message on browser JS console
 */
function log(message) {
	try {
		console.log(message);
	} catch (e) {
		// Can't do anything
	}
}

/**
 * Get the values of form elements such as input, select and textarea
 *
 * @param $control
 *            jQuery object that references DOM input, select and textarea
 * @returns input value/s
 */
function fnVal($control) {
	var nControl = $control[0];

	if (nControl.nodeName.toLowerCase() == "input") {
		return $control.val();
	}

	// Note: At present, using .val() on textarea elements strips carriage
	// return characters from the browser-reported value. When this value is
	// sent to the server via XHR however, carriage returns are preserved
	// (or added by browsers which do not include them in the raw value).
	// A workaround: http://api.jquery.com/val/
	if (nControl.nodeName.toLowerCase() == "textarea") {
		return $control.text().replace(/\r?\n/g, "\r\n");
	}

	if (nControl.nodeName.toLowerCase() == "select") {
		if (nControl.multiple) {
			// Select multiple controls return an array of values and we return
			// a comma-list string
			return $control.find("option:selected").map(function() {
				return this.value;
			}).get().join(",");
		} else {
			return $control.val();
		}
	}

	return null;
}

/**
 *
 * Hide details Enables filtration delay for keeping the browser more responsive
 * while searching for a longer keyword.
 *
 * from http://datatables.net/plug-ins/api#fnSetFilteringDelay
 */
jQuery.fn.dataTableExt.oApi.fnSetFilteringDelay = function(oSettings, iDelay) {
	var _that = this;

	if (iDelay === undefined) {
		iDelay = 250;
	}

	this
			.each(function(i) {
				$.fn.dataTableExt.iApiIndex = i;
				var $this = this, oTimerId = null, sPreviousSearch = null, anControl = $(
						'input', _that.fnSettings().aanFeatures.f);

				anControl.unbind('keyup').bind(
						'keyup',
						function() {
							var $$this = $this;

							if (sPreviousSearch === null
									|| sPreviousSearch != anControl.val()) {
								window.clearTimeout(oTimerId);
								sPreviousSearch = anControl.val();
								oTimerId = window.setTimeout(function() {
									$.fn.dataTableExt.iApiIndex = i;
									_that.fnFilter(anControl.val());
								}, iDelay);
							}
						});

				return this;
			});
	return this;
};

/**
 * This function is executed after draw
 * datatable element
 */

jQuery.fn.dataTableExt.oApi.fnGvNIX_DrawCallback = function(oSettings){
	var _that = this;

	// add fnScrollDatatableToRowClick in a timer to
	// assure it is executed after all drawCallbacks are done
	// (which can modify the row "offsetTop")

	window.setTimeout(function() {

		fnScrollDatatableToRowClick(_that);

	}, 200);

};


/**
 * This function checks filters on footerCallback
 */
jQuery.fn.dataTableExt.oApi.fnGvNIX_FooterCallback = function(nFoot, data, start, end, display){
	var _that = this;
	var oSettings = _that.fnSettings();

	if (!oSettings) {
		return;
	}
	updateDatatablesFilters(oSettings);
	
};

/**
 * This function is executed when a TR element is created
 */

jQuery.fn.dataTableExt.oApi.fnGvNIX_RowCreatedCallback = function(nRow, aData, iDataIndex){
	var _that = this;
	var st = _that.fnSettings();
	// Getting sSearch
	var sSearch = st.oPreviousSearch.sSearch;
	// Getting all td in the new row
	var oTds = jQuery(aData).children("td");
	// highlight matching results
	if(sSearch !== ""){
		showSearchResultsHighLighted(oTds, sSearch);
	}
};

/*
 * This function highlight results that match with 
 * the current search
 */
function showSearchResultsHighLighted($tds, sSearch){
	jQuery.each($tds, function(index, td){
		var $td = jQuery(td);
		var tdClass = $td.attr("class");
		// Excluding utilbox
		if(tdClass !== "utilbox" && tdClass !== "dataTables_empty" && $td.children().length == 0){
			var content = $td.html();
			var contentToLower = content.toLowerCase();
			var contentMatch = contentToLower.indexOf(sSearch.toLowerCase());
			// If content match with search
			if(contentMatch != -1){
				var toHighLightString = content.substr(contentMatch, sSearch.length);
				var highLighted = "<span class='search-match'>" + toHighLightString + "</span>";
				var re = new RegExp(toHighLightString, 'g');
				var finalContent = content.replace(re, highLighted);
				// Setting new value
				$td.html(finalContent);
			}
		}
	});
};

/**
 * This function updates datatables column filters
 * 
 * @param oSettings
 */
function updateDatatablesFilters(oSettings) {

	var filters = oSettings.aoPreSearchCols;
	var footer = jQuery(oSettings.aoFooter)[0];
	
	// If footer is defined
	if(footer !== undefined){
		
		for ( var i=0, iLen=filters.length ; i<iLen ; i++ )
	    {
			if(footer[i] !== null){
				var $cell = jQuery(footer[i].cell);
				var property = $cell.data().property;
				var filterExpression = filters[i].sSearch;
				
				if(filterExpression != "")	{

					jQuery.ajax({
						  url: "?checkFilters",
						  data: {property: property, expression: filterExpression},
						  type: "post",
						  success: function(jsonResponse) {
							  for(var i=0;i<footer.length;i++){
								  if(property == jsonResponse.property){
									  if(!jsonResponse.response){
										  $cell.find('input').css("background-color","#FA5858");
									  }else{
										  $cell.find('input').css("background-color","#ffffff");
									  }
								  }
							  }
						  },
						  error:function (xhr, ajaxOptions, thrownError) {
						  }
					});
				}else{
					$cell.find('input').css("background-color","#ffffff");
				}
			}
	    }
	}
};

/**
 *
 * This plug-in removed the default behaviour of DataTables to filter on each
 * keypress, and replaces with it the requirement to press the enter key to
 * perform the filter.
 *
 * from http://datatables.net/plug-ins/api#fnFilterOnReturn
 */
jQuery.fn.dataTableExt.oApi.fnFilterOnReturn = function(oSettings) {
	var _that = this;

	this.each(function(i) {
		$.fn.dataTableExt.iApiIndex = i;
		var $this = this;
		var anControl = $('input', _that.fnSettings().aanFeatures.f);
		anControl.unbind('keyup').bind('keypress', function(e) {
			if (e.which == 13) {
				$.fn.dataTableExt.iApiIndex = i;
				_that.fnFilter(anControl.val());
			}
		});
		return this;
	});
	return this;
};

/**
 *
 * When doing some heavy processing of your own (for example using fnOpen with
 * data loading from the server) it can be useful to make use of the
 * 'processing' indicator built-into DataTables. This plug-in function exposes
 * the internal DataTables function so it can be used for exactly this.
 *
 * from http://datatables.net/plug-ins/api#fnProcessingIndicator
 */
jQuery.fn.dataTableExt.oApi.fnProcessingIndicator = function(oSettings, onoff) {
	if (typeof (onoff) == 'undefined') {
		onoff = true;
	}
	this.oApi._fnProcessingDisplay(oSettings, onoff);
};

/**
 * Gets the outerHTML of the first element a JQuery query
 *
 * @param query
 * @returns html text (include the content node itself)
 */
function fnOuterHTML(query) {
	// If no query
	if (query === undefined) {
		return null;
	}
	// If no result
	if (!query.length) {
		return null;
	}

	var node = query[0];

	if (node.outerHTML) {
		return node.outerHTML;
	} else {
		return $(document.createElement("div")).append($(node).clone()).html();
	}
}

/**
 * By default DataTables only uses the sAjaxSource variable at initialisation
 * time, however it can be useful to re-read an Ajax source and have the table
 * update. Typically you would need to use the fnClearTable() and fnAddData()
 * functions, however this wraps it all up in a single function call.
 *
 * from http://datatables.net/plug-ins/api#fnReloadAjax
 */
$.fn.dataTableExt.oApi.fnReloadAjax = function(oSettings, sNewSource,
		fnCallback, bStandingRedraw) {
	if (sNewSource !== undefined && sNewSource !== null) {
		oSettings.sAjaxSource = sNewSource;
	}

	// Server-side processing should just call fnDraw
	if (oSettings.oFeatures.bServerSide) {
		oSettings.bAjaxDataGet = true; // force perform ajax call
		this.fnDraw();
		return;
	}

	this.oApi._fnProcessingDisplay(oSettings, true);
	var that = this;
	var iStart = oSettings._iDisplayStart;
	var aData = [];

	this.oApi._fnServerParams(oSettings, aData);

	oSettings.fnServerData.call(oSettings.oInstance, oSettings.sAjaxSource,
			aData, function(json) {
				/* Clear the old information from the table */
				that.oApi._fnClearTable(oSettings);

				/* Got the data - add it to the table */
				var aData = (oSettings.sAjaxDataProp !== "") ? that.oApi
						._fnGetObjectDataFn(oSettings.sAjaxDataProp)(json)
						: json;

				for (var i = 0; i < aData.length; i++) {
					that.oApi._fnAddData(oSettings, aData[i]);
				}

				oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();

				that.fnDraw();

				if (bStandingRedraw === true) {
					oSettings._iDisplayStart = iStart;
					that.oApi._fnCalculateEnd(oSettings);
					that.fnDraw(false);
				}

				that.oApi._fnProcessingDisplay(oSettings, false);

				/* Callback user function - for event handlers etc */
				if (typeof fnCallback == 'function' && fnCallback !== null) {
					fnCallback(oSettings);
				}
			}, oSettings);
};

/**
 * Redraw the table (i.e. fnDraw) to take account of sorting and filtering, but
 * retain the current pagination settings.
 *
 * from http://datatables.net/plug-ins/api#fnStandingRedraw
 */
$.fn.dataTableExt.oApi.fnStandingRedraw = function(oSettings) {
	
	if (oSettings.oFeatures.bServerSide === false) {
		var before = oSettings._iDisplayStart;

		oSettings.oApi._fnReDraw(oSettings);

		// iDisplayStart has been reset to zero - so lets change it back
		oSettings._iDisplayStart = before;
		oSettings.oApi._fnCalculateEnd(oSettings);
	} else {
		oSettings.bAjaxDataGet = true; // force perform ajax call
	}

	// draw the 'current' page
	oSettings.oApi._fnDraw(oSettings);
};


/**
 * Move datatable scroll to show current row-clicked
 * <p/>
 * The scroll doen't be moved if any row is in 
 * editing mode.
 *
 * @param $datatable the datatable instance
 */

function fnScrollDatatableToRowClick($datatable) {

	if(!$datatable.fnHasRowClick()){
		return false;
	}

	var hasRowInEditingMode = false;
	if ($datatable.fnHasEditing()) {
		var rowEditingClassSelector = "." + $datatable.fnEditing()._options.classForEditingRow;
		hasRowInEditingMode = $datatable.find(rowEditingClassSelector).length > 0;
	}

	var rowClickedClassSelector = "." + $datatable.fnRowClick().s.classForClickedRow;

	if(hasRowInEditingMode){
		return false;
	}

	var $body = $datatable.parent(".dataTables_scrollBody");
	$body.animate({scrollTop: 0}, 0);

    // Displaying always the clicked row

    if($datatable.find(rowClickedClassSelector).length > 0){

        var rowSelected = $datatable.find(rowClickedClassSelector);
        var scrollHeight = $body.height();
        var rowHeight = rowSelected.height();
        var rowSelectedPosition = rowSelected[0].offsetTop + rowHeight + (scrollHeight / 2);
        
        if(rowSelectedPosition > scrollHeight){
        	$body.animate({scrollTop:  rowSelectedPosition - scrollHeight});
        }

    }

}

/**
 * This method changes filter class and add
 * callback to know if filter has value
 */
function fnChangeFilterClass(table){
	
	var tableId = table.attr("id");
	var filterDiv = jQuery("#" + tableId + "_filter");
	var filterInput = filterDiv.find("input");
	
	if(filterInput.length > 0){
		// Setting class if value is not empty
		var value = filterInput.val();
		if(value !== ""){
			filterInput.addClass("filter_not_empty");
		}
		
		// Registering input click event
		filterInput.on("keyup", function(){
			var $input = jQuery(this);
			var value = $input.val();

			if(value !== ""){
				filterInput.addClass("filter_not_empty");
			}else{
				filterInput.removeClass("filter_not_empty");
			}
		});
	}
	
}


/**
 * This method returns a hashcode from
 * a string.
 * 
 * @param str String to transform
 * @return String
 */
function fnGetHashCode(str){
	var hash = 0;
    if (str.length == 0) return hash;
    for (i = 0; i < str.length; i++) {
        char = str.charCodeAt(i);
        hash = ((hash<<5)-hash)+char;
        hash = hash & hash; // Convert to 32bit integer
    }
	return hash.toString();
}

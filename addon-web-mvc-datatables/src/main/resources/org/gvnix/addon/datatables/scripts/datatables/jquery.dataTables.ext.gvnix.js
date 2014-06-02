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
		$table.fnFooterCallback(row, data, start, end, display);
	});
	
	// Register Draw callback
	st.oApi._fnCallbackReg(st, 'aoDrawCallback', function( oSettings ) {
		$table.fnDrawCallback(oSettings);
	});
	
	// Calling at first time Footer Callback
	$table.fnFooterCallback();
	
	// Displaying always the clicked row
    $table.parent(".dataTables_scrollBody").animate({scrollTop: 0}, 0);
    var rowClickedClassSelector = "." + $table.DataTable().fnRowClick().s.classForClickedRow;
	if($table.find(rowClickedClassSelector).length > 0){
		var rowSelected = $table.find(rowClickedClassSelector);
		var scrollHeight = $table.parent(".dataTables_scrollBody").height();
		var rowHeight = rowSelected.height();
		var rowSelectedPosition = rowSelected[0].offsetTop + rowHeight + (scrollHeight / 2);
		if(rowSelectedPosition > scrollHeight){
			$table.parent(".dataTables_scrollBody").animate({scrollTop:  rowSelectedPosition - scrollHeight});
		}
	}
	
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

jQuery.fn.dataTableExt.oApi.fnDrawCallback = function(oSettings){
	var rowClickedClassSelector = "." + this.DataTable().fnRowClick().s.classForClickedRow;
    var rowEditingClassSelector = "." + this.DataTable().fnEditing()._options.classForEditingRow;
    
	if(!this.find(rowEditingClassSelector).length > 0){
		this.parent(".dataTables_scrollBody").animate({scrollTop: 0}, 0);
	    // Displaying always the clicked row
	    if(this.find(rowClickedClassSelector).length > 0){
	        var rowSelected = this.find(rowClickedClassSelector);
	        var scrollHeight = this.parent(".dataTables_scrollBody").height();
	        var rowHeight = rowSelected.height();
	        var rowSelectedPosition = rowSelected[0].offsetTop + rowHeight + (scrollHeight / 2);
	        if(rowSelectedPosition > scrollHeight){
	            this.parent(".dataTables_scrollBody").animate({scrollTop:  rowSelectedPosition - scrollHeight});
	        }
	    }
	}
};

/**
 * This function checks filters on footerCallback
 */
jQuery.fn.dataTableExt.oApi.fnFooterCallback = function(row, data, start, end, display){
	var _that = this;
	var filters = _that.fnSettings().aoPreSearchCols;
	var footer = $(_that.fnSettings().aoFooter)[0];
	
	// If footer is defined
	if(footer !== undefined){
		
		for ( var i=0, iLen=filters.length ; i<iLen ; i++ )
	    {
			if(footer[i] !== null){
				var property = $(footer[i].cell).data().property;
				var filterExpression = filters[i].sSearch;
				
				if(filterExpression != "")	{
					$.ajax({
						  url: "?checkFilters",
						  data: {property: property, expression: filterExpression},
						  type: "post",
						  success: function(jsonResponse) {
							  for(var i=0;i<footer.length;i++){
								  if($(footer[i].cell).data().property == jsonResponse.property){
									  if(!jsonResponse.response){
										  $(footer[i].cell).find('input').css("background-color","#FA5858");
									  }else{
										  $(footer[i].cell).find('input').css("background-color","#ffffff");
									  }
								  }
							  }
						  },
						  error:function (xhr, ajaxOptions, thrownError) {
						  }
					});
				}else{
					$(footer[i].cell).find('input').css("background-color","#ffffff");
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
/**
 * gvNIX Datatables extended initialization
 *
 * @param datatable settings
 * @param tableId
 * @param options to initialize
 * @param count of tries-to-delay-initialize
 */
function fnDatatablesExtInit(oSettings, tableId, options, count) {

	var $table = null;
	var atables = $.fn.dataTable.fnTables(true);
	for (i = 0; i < atables.length; i++) {
		$table = jQuery(atables[i]).dataTable();
		if ($table.fnSettings().sTableId == tableId){
			break;
		}
		$table = null;
	}
	if ($table == null) {
		// dataTable instance not found
		if (count) {
			// Check number of tries
			if (count > 12) {
				log("Datatables " + tableId + " gvnix init ext is not loaded because it's currently not available (not visible tab)");
				return;
			}
			// increase tries count
			count++;
		} else {
			// init try count
			count = 1;
		}
		// Try to load it delayed
		log("Delay loading of '"+ tableId+"':"+count);
		window.setTimeout(fnDatatablesExtInit, 100, oSettings, tableId, options,count);
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

	if (count) {
		// If delay loading adjust column size
		$table.fnAdjustColumnSizing();
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
		throw "fnDisplayCreateForm : id not found '"+ sTableId + "'";
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
		throw "fnEditDatatableRow : id not found '"+ sTableId + "'";
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

				for ( var i = 0; i < aData.length; i++) {
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
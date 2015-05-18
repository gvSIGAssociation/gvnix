/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Global scope for GvNIX_RowClick */
var GvNIX_RowClick;

(function(jQuery, window, document) {

	GvNIX_RowClick = function(oSettings, oOpts) {

		// Santiy check that we are a new instance
		if (!this instanceof GvNIX_RowClick) {
			alert("Warning: GvNIX_RowClick must be initialised with the keyword 'new'");
		}

		// Public class variables * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * @namespace Settings object which contains custom information for
		 *            TableTools instance
		 */
		this.s = {

			/**
			 * Show messages on console
			 *
			 * @property debug
			 * @type boolean
			 * @default false
			 */
			"debug" : false,

			/**
			 * Css class to set
			 * <tr> on last clicked row
			 *
			 * @property classForClickedRow
			 * @type string
			 * @default 'row_selected'
			 */
			"classForClickedRow" : 'row_clicked',

			/**
			 * List of cell classes to be ignored on
			 * click binding.
			 *
			 * @property ignoreCellClasses
			 * @type array (string)
			 * @default ["utilbox"]
			 */
			"ignoreCellClasses" : ["utilbox"],

			/**
			 * Persist state on datatable store
			 *
			 * @property persistState
			 * @type boolean
			 * @default false
			 */
			"persistState" : false

		};

		/**
		 * @namespace Settings object which contains customizable information
		 *            for TableTools instance
		 */
		this._data = {

			/**
			 * Store 'this' so the instance can be retrieved from the settings
			 * object
			 *
			 * @property that
			 * @type object
			 * @default this
			 */
			"that" : this,

			/**
			 * DataTables settings objects
			 *
			 * @property dt
			 * @type object
			 * @default <i>From the oDT init option</i>
			 */
			"dt" : oSettings,

			/**
			 * Last clicked row id
			 *
			 * @property idList
			 * @type string
			 * @default null
			 */
			"lastClickedId" : null,

			/**
			 * Callback for selection change notification. The handler
			 * functions this object contains, receives following data:
			 *
			 * <code>rowClickSupport</code> (GvNIX_RowClick) instance which
			 * fires the event
			 * <code>action</code> (string) one of ['select','deselect','all','none']
			 * <code>id</code> (string) ID of row affected by given action
			 * (null for 'none','all')
			 *
			 * @property rowClickedCallbacks
			 * @type jQuery.Callbacks
			 * @default jQuery.Callbacks("unique")
			 */
			"rowClickedCallbacks" : jQuery.Callbacks("unique")
		};

		// Public class methods * * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * Retreieve the settings object from an instance
		 *
		 * @method fnSettings
		 * @returns {object} TableTools settings object
		 */
		this.fnSettings = function() {
			return this.s;
		};

		// Constructor logic
		this._fnConstruct(oOpts);

		GvNIX_RowClick._fnAddInstance(this);

		return this;
	};

	GvNIX_RowClick.prototype = {

		// Public methods * * * * * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * Try to show a message on browser JS console
		 *
		 * @param message
		 *            to show
		 */
		"log" : function(message) {
			try {
				console.log(message);
			} catch (e) {
				// Can't do anything
			}
		},

		/**
		 * Gets the row (node) from its id
		 *
		 * @param id
		 *            value
		 */
		"fnGetRowById" : function(trId) {
			var aoData = this._data.dt.aoData;
			for ( var i = 0; i < aoData.length; i++) {
				var nRow = aoData[i].nTr;
				if (nRow.id == trId) {
					return nRow;
				}
			}
			return null;
		},

		/**
		 * Redraw a row
		 *
		 * @param trId
		 *            tr.id value (could be DT_RowId on AJAX mode)
		 * @param isSelected
		 *            (optional if not set it will check)
		 */
		"fnRedrawRow" : function(trId, isLastRowClicked) {
			if (isLastRowClicked === undefined) {
				isLastRowClicked = this.fnSelected(trId);
			}
			var nRow = this.fnGetRowById(trId);
			// Check if row is loaded
			if (nRow) {
				this._fnUpdateRowTr(nRow, isLastRowClicked);
			}
		},

		/**
		 * Redraw all visible rows
		 *
		 * @param trId
		 *            tr.id value (could be DT_RowId on AJAX mode)
		 * @param isSelected
		 *            (optional if not set it will check)
		 */
		"fnRedrawVisibleRows" : function() {

			var dt = this._data.dt;
			var aoData = dt.aoData, aiDisplay = dt.aiDisplay, start = 0, end = aiDisplay.length;

			if (!dt.oFeatures.bServerSide) {
				start = dt._iDisplayStart;
				end = dt.fnDisplayEnd();
			}

			for ( var i = start; i < end; i++) {
				var nRow = aoData[aiDisplay[i]].nTr;
				this._fnUpdateRowTr(nRow, this.fnIsTheLastRowClicked(nRow.id));
			}
		},

		/**
		 * Informs if trId is selected
		 *
		 * @param trId
		 *            tr.id value (could be DT_RowId on AJAX mode)
		 */
		"fnIsTheLastRowClicked" : function(trId) {
			return this._data.lastClickedId == trId;
		},

		/**
		 * Get if there is row clicked
		 *
		 * @returns true if there is a row clicked
		 */
		"fnHasRowClicked" : function() {
			return this._data.lastClickedId != null;
		},

		/**
		 * Gets the last clicked row id
		 *
		 * @returns last clicked row id.
		 */
		"fnGetLastClickedRowId" : function() {
			return this._data.lastClickedId;
		},

		/**
		 * Set trId as last clicked
		 *
		 * @param trId
		 *            tr.id value (could be DT_RowId on AJAX mode)
		 * @param redraw
		 *            row if change state
		 * @param force
		 *            force to execute callbacks
		 * @returns true if selection has been change
		 */
		"fnSetLastClicked" : function(trId, redraw, force, showNextDetail) {
			var _d = this._data;
			if ((trId == _d.lastClickedId) && !force) {
				// is the same row: do nothing
				return false;
			} else if (_d.lastClickedId && redraw) {
				// clean previous row
				this.fnRedrawRow(_d.lastClickedId, false);
			}

			_d.lastClickedId = trId;
			if (redraw === undefined || redraw) {
				this.fnRedrawRow(trId, true);
			}
			_d.rowClickedCallbacks.fireWith(this, [ this, trId ]);
			if (this.s.persistState) {
				this.fnSaveState();
			}
			this.fnScrollDatatableBodyToClicked();
			this.fnScrollToNextDetail(showNextDetail);
			
			return true;
		},

		/**
		 * Add given function handler to {@link rowClicked} Callback
		 *
		 * @param callback function to register that can declare following
		 *            parameters:
		 *            <code>rowClickSupport</code> (GvNIX_RowClick) instance which fires the event
		 *            <code>id</code> (string) ID of row affected
		 */
		"fnAddRowClickCallback" : function(callback) {
			var _d = this._data;
			if (this.s.debug) {
				this.log("fnAddRowClickCallback: "
						+ (callback.name ? callback.name : callback));
			}
			_d.rowClickedCallbacks.add(callback);
		},

		/**
		 * Remove a rowClicked callback function
		 *
		 * @param callback
		 *            function to remove
		 */
		"fnRemoverowClickedCallback" : function(callback) {
			var _d = this._data;
			if (this.s.debug) {
				this.log("fnRemoverowClickedCallback: "
						+ (callback.name ? callback.name : callback));
			}
			_d.rowClickedCallbacks.remove(callback);
		},

		/**
		 * Load previous state of control from
		 * the cookie
		 *
		 *@param force force load
		 */
		"fnLoadState" : function(force) {
			var dt = this._data.dt;
			
			// Generating hash location
			var hashLocation = fnGetHashCode(window.location.pathname);
			// Getting statePrefix
			var statePrefix = jQuery(dt.nTable).data().stateprefix;
			
			// Generating unic sName
			var sName = hashLocation + "_";
			if(statePrefix != undefined){
				sName +=  statePrefix + "_";
			}
			sName += "gvnixRowclk-"+dt.nTable.id;

			if(!window.localStorage){
				var id = dt.oApi._fnReadCookie(sName);
				if (id) {
					this.fnSetLastClicked(id, true,true);
				}
			}else{
				var id = window.localStorage.getItem(sName);
				if (id) {
					this.fnSetLastClicked(id, true,true);
				}
			}
		},

		/**
		 * Save current state of control to
		 * the cookie
		 *
		 */
		"fnSaveState" : function(clear) {
			var _d = this._data, dt = _d.dt;

			// Generating hash location
			var hashLocation = fnGetHashCode(window.location.pathname);
			// Getting statePrefix
			var statePrefix = jQuery(dt.nTable).data().stateprefix;
			
			// Generating unic sName
			var sName = hashLocation + "_";
			if(statePrefix != undefined){
				sName +=  statePrefix + "_";
			}
			sName += "gvnixRowclk-"+dt.nTable.id;
			
			var sValue = "";
			if(clear == undefined){
				sValue = _d.lastClickedId;
			}
			

			if(!window.localStorage){
				dt.oApi._fnCreateCookie(sName,
						sValue,
						10*60, // 10 minutes
						"gvnixRowclk-",
						null
						);
			}else{
				window.localStorage.setItem(sName,sValue);
			}
		},
		
		/**
		 * This functions scrolls datatable scroll body 
		 * to clicked record
		 */
		"fnScrollDatatableBodyToClicked": function() {
			var _d = this._data;
			var s = this.s;
			
			var $table = jQuery(_d.dt.nTable);
			var clickedClassSelector = "." + s.classForClickedRow;
			$table.parent(".dataTables_scrollBody").animate({scrollTop: 0}, 0);
		    // Displaying always the clicked row
		    if($table.find(clickedClassSelector).length > 0){
		        var rowSelected = $table.find(clickedClassSelector);
		        var scrollHeight = $table.parent(".dataTables_scrollBody").height();
		        var rowHeight = rowSelected.height();
		        var rowSelectedPosition = rowSelected[0].offsetTop + rowHeight + (scrollHeight / 2);
		        if(rowSelectedPosition > scrollHeight){
		        	$table.parent(".dataTables_scrollBody").animate({scrollTop:  rowSelectedPosition - scrollHeight}, 0);
		        }
		    }
		},
		
		/**
		 * This function scrolls page to next detail when
		 * the user click a row
		 * 
		 */
		"fnScrollToNextDetail": function(showDetails) {
			if(showDetails){
				var _d = this._data;
				var s = this.s;
			
				var $table = jQuery(_d.dt.nTable);
				var tableId = $table.attr("id");
			
				var divDetails = jQuery("div[id^="+tableId+"_][id$=detail]");

				if(divDetails.length > 0){
					jQuery('html, body').animate({ scrollTop: divDetails.offset().top });
				}
			}
			
		},


		// Private methods (they are of course public in JS, but recommended as
		// private) * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * Register a Datatables fnRowDrawCallback
		 *
		 * This registers a function on datatable to draw row.
		 *
		 * Also bind click event on row to manage selection.
		 */
		"_fnRegisterDrawRowCallback" : function() {

			var dt = this._data.dt;

			dt.oApi._fnCallbackReg(dt, 'aoRowCallback', jQuery.proxy(function(nRow, aData,
					iDisplayIndex) {

				// Locate Id
				var rowId = nRow.id;
				if (!rowId) {

					// Check for DT_RowId in data
					rowId = aData.DT_RowId;
				}
				if (!rowId) {
					throw "Can't get Row id for manage selection!!";
				}

				// update row style
				this._fnUpdateRowTr(nRow, this.fnIsTheLastRowClicked(rowId));

				// Check if it's already bind click event
				this._fnBindClickEvent(nRow);
			},this), 'addrowClickSupport');
		},

		/**
		 * update graphically a row (node)
		 *
		 * @param nRow
		 * @param selectec
		 */
		"_fnUpdateRowTr" : function(nRow, selected) {
			var s = this.s, nTable = this._data.dt.nTable;
			var classToAdd = null, classToRemove = null;

			// prepare values
			if (selected) {
				// add selected class
				classToAdd = s.classForClickedRow;
			} else {
				// remove selected class
				classToRemove = s.classForClickedRow;
			}
			if (s.debug) {
				this.log("_fnUpdateRowTr: id=" + nRow.id + (classToAdd ? " +" + classToAdd :"")
						+ (classToRemove ? " -" + classToRemove : ""));
			}
			// update row values
			var $nRow = jQuery(nRow,nTable);
			if (classToAdd) {
				$nRow.addClass(classToAdd);
			}
			if (classToRemove) {
				$nRow.removeClass(classToRemove);
			}
		},

		/**
		 * Bind click event of a row to toggle select action
		 *
		 * @param nRow
		 *            (node)
		 */
		"_fnBindClickEvent" : function(nRow) {
			if (!nRow) {
				// nothing to do
				return;
			}

			if (nRow.rowClick_binded) {
				// already binded
				return;
			}
			var that = this;
			// Compound jquery curey
			var sQuery = "td";
			if (this.s.ignoreCellClasses) {
				jQuery.each(this.s.ignoreCellClasses, function (index,className){
					sQuery += ":not(." + className +")";
				});
			}
			var aTds = jQuery(sQuery,nRow);
			// bind row click
			jQuery.each(aTds, function (index,ntd){
				jQuery(ntd).dblclick(function() {
					that.fnSetLastClicked(this.parentNode.id, true, undefined, true);
				});
			});
			nRow.rowClick_binded = true;
		},

		/**
		 * Initialize component
		 */
		"_fnConstruct" : function(iSettings) {
			var s = this.s, _d = this._data, dt = _d.dt;
			// initialize settings
			if (typeof iSettings == "object") {
				if (iSettings.classForClickedRow !== undefined) {
					s.classForClickedRow = iSettings.classForClickedRow;
				}
				if (iSettings.debug !== undefined) {
					s.debug = iSettings.debug;
					if (s.debug) {
						// Regsiter a rowClicked calback to log the event
						// fire
						_d.rowClickedCallbacks.add(function(selection, id) {
							selection.log("rowClicked: "+ id);
						});
					}
				}
				if (typeof iSettings.rowClicked == "object") {
					_d.rowClickedCallbacks.add(iSettings.rowClicked);
				}
				if (iSettings.ignoreCellClasses !== undefined) {
					if (jQuery.isArray(iSettings.ignoreCellClasses)) {
						s.ignoreCellClasses = iSettings.ignoreCellClasses;
					} else if (typeof iSettings.ignoreCellClasses === "string" ){
						s.ignoreCellClasses = [iSettings.ignoreCellClasses];
					}
				}
				if (iSettings.persistState) {
					s.persistState = true;
				}
			}

			// Initialize Variables to store data
			_d.lastClickedId = null;

			// Register click on current rows
			var aoData = dt.aoData;
			for ( var i = 0; i < aoData.length; i++) {
				var nRow = aoData[i].nTr;
				this._fnBindClickEvent(nRow);
			}

			// Register Row callback without remove user configuration
			this._fnRegisterDrawRowCallback();

			// Register store state
			if (s.persistState) {
				this.fnLoadState();
			}

			// Update visible rows
			this.fnRedrawVisibleRows();
		}

	};

	// Static variables * * * * * * * * * * * * * * * * * * * * * * * * * * *

	/**
	 * Store of all instances that have been created of TableTools, so one can
	 * look up other (when there is need of a master)
	 *
	 * @property _aInstances
	 * @type Array
	 * @default []
	 * @private
	 */
	GvNIX_RowClick._aInstances = [];

	/**
	 * Store selection callbacks which are not registed yet in instances
	 * (usually because not initialiced yet). When the refered instance is
	 * register on _aInstances the related callback will be added.
	 */
	GvNIX_RowClick._aCallbacksPendingToRegister = [];

	// Static methods * * * * * * * * * * * * * * * * * * * * * * * * * * * *

	/**
	 * Store new GvNIX_RowClick instance. Also register pending callback.
	 *
	 * @method _fnAddInstance
	 * @static
	 */
	GvNIX_RowClick._fnAddInstance = function(instance) {
		GvNIX_RowClick._aInstances.push(instance);

		// Register pending callback
		var nodeId = instance._data.dt.nTable.id;
		var register = GvNIX_RowClick._aCallbacksPendingToRegister;

		var regInfo;
		// Check if there is a pending register
		for ( var i = 0, iLen = register.length; i < iLen; i++) {
			regInfo = register[i];

			// If table id match
			if (regInfo.id == nodeId) {
				// register all callback
				var callbacks = regInfo.callbacks;
				while (callbacks.length > 0) {
					instance.fnAddRowClickCallback(callbacks.pop());
				}

				// remove pending callback register for this id
				register.splice(i, 1);
				// All done
				return;
			}
		}
	};

	/**
	 * Get the instance for a table node (or id if a string is given)
	 *
	 * @method fnGetInstance
	 * @returns {Object} ID of table OR table node, for which we want the
	 *          GvNIX_Seletion instance
	 * @static
	 */
	GvNIX_RowClick.fnGetInstance = function(node) {
		var nodeId = null;
		var $node = null;
		if (typeof node != 'object') {
			nodeId = node;
			$node = jQuery("#" + node);
			if ($node.length < 1){
				return null;
			}
			node = $node[0];
		} else {
			nodeId = node.id;
			$node = jQuery(node);
		}

		if (jQuery.fn.DataTable.fnIsDataTable(node)) {
			// found node
			for ( var i = 0, iLen = GvNIX_RowClick._aInstances.length; i < iLen; i++) {
				if (GvNIX_RowClick._aInstances[i]._data.dt.nTable == node) {
					return GvNIX_RowClick._aInstances[i];
				}
			}
		}
		return null;
	};

	/**
	 * Register a RowClick Callback on table node (or id if a string is
	 * given). This works including when datatable is not ready. In this case,
	 * this will store the function and register it when selection support is
	 * initialized for related datatable.
	 *
	 * @method fnGetInstance
	 * @returns {Object} ID of table OR table node, for which we want the
	 *          GvNIX_RowClick instance
	 * @static
	 */
	GvNIX_RowClick.fnAddRowClickCallback = function(node, callback) {
		var nodeId = null;
		var $node = null;
		if (typeof node != 'object') {
			nodeId = node;
			$node = jQuery("#" + node);
			if ($node.length < 1){
				return null;
			}
			node = $node[0];
		} else {
			nodeId = node.id;
			$node = jQuery(node);
		}
		if (jQuery.fn.DataTable.fnIsDataTable(node)) {
			// found node
			var dt = $node.dataTable();

			for ( var i = 0, iLen = GvNIX_RowClick._aInstances.length; i < iLen; i++) {
				if (GvNIX_RowClick._aInstances[i]._data.dt.nTable == node) {
					// found datatable: register callback in datatable
					GvNIX_RowClick._aInstances[i]
							.fnAddRowClickCallback(callback);
					return true;
				}
			}
		}
		// Store in pending to assing
		var register = GvNIX_RowClick._aCallbacksPendingToRegister;

		// Check if there is a pending register
		for ( var i = 0, iLen = register.length; i < iLen; i++) {
			if (register[i].id == nodeId) {
				register[i].callbacks.push(callback);
				return false;
			}
		}
		// nothing registered yet. create a new register
		register.push({
			'id' : nodeId,
			'callbacks' : [ callback ]
		});
		return false;
	};

	/**
	 * Name of this class
	 *
	 * @constant CLASS
	 * @type String
	 * @default TableTools
	 */
	GvNIX_RowClick.prototype.CLASS = "GvNIX_RowClick";

	/**
	 * TableTools version
	 *
	 * @constant VERSION
	 * @type String
	 * @default See code
	 */
	GvNIX_RowClick.VERSION = "${gvnix.version}";
	GvNIX_RowClick.prototype.VERSION = GvNIX_RowClick.VERSION;

	/** TODO Add as datatable feature * */

})(jQuery, window, document);

/** *********************************************** */

/**
 *
 * Gets/initialize gvnix RowClick support on a datatables
 *
 * @param oSettings
 * @param iSettings
 * @return GvNIX_RowClick object
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
jQuery.fn.dataTableExt.oApi.fnRowClick = function(oSettings,
		iSettings) {

	var rowClickSupport = oSettings.GvNIX_RowClick_support;

	if (rowClickSupport === undefined) {
		rowClickSupport = new GvNIX_RowClick(oSettings, iSettings);
	} else {
		// TODO adjust settings on already initialized selection support
	}

	oSettings.GvNIX_RowClick_support = rowClickSupport;

	return rowClickSupport;
};

/**
*
* Checks if  gvnix RowClick support is initialized on a datatables
*
* @param oSettings
* @param iSettings
* @return GvNIX_RowClick object
* @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
*/
jQuery.fn.dataTableExt.oApi.fnHasRowClick = function(oSettings,
		iSettings) {
	
	if (!oSettings) {
		return false;
	}

	var rowClickSupport = oSettings.GvNIX_RowClick_support;

	if (rowClickSupport === undefined) {
		return false;
	} else {
		return true;
	}
};

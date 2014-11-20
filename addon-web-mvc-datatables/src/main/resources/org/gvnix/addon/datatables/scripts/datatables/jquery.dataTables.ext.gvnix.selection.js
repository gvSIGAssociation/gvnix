/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana
 * Copyright (C) 2013 Generalitat Valenciana
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

/* Global scope for GvNIX_Selection */
var GvNIX_Selection;

(function(jQuery, window, document) {

	GvNIX_Selection = function(oSettings, oOpts) {

		// Santiy check that we are a new instance
		if (!this instanceof GvNIX_Selection) {
			alert("Warning: GvNIX_Selection must be initialised with the keyword 'new'");
		}

		// Public class variables * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * @namespace Settings object which contains custom information for
		 *            TableTools instance
		 */
		this.s = {

			/**
			 * Allow multiple row selection
			 *
			 * @property multiRow
			 * @type boolean
			 * @default false
			 */
			"multiRow" : false,

			/**
			 * Show messages on console
			 *
			 * @property debug
			 * @type boolean
			 * @default false
			 */
			"debug" : false,

			/**
			 * Css class of checkbox to manage selection
			 *
			 * @property checkColumnClass
			 * @type string
			 * @default null
			 */
			"checkColumnClass" : null,

			/**
			 * Css class to set
			 * <tr> on selected rows
			 *
			 * @property classForSelectedRow
			 * @type string
			 * @default 'row_selected'
			 */
			"classForSelectedRow" : 'row_selected',

			/**
			 * Css class to set
			 * <tr> on not-selected rows
			 *
			 * @property classForNotSelectedRow
			 * @type string
			 * @default null
			 */
			"classForNotSelectedRow" : null,

			/**
			 * Show a information message on datatables info label. Ex: '<br/>
			 * Selected _SEL-COUNT_ rows, _SEL-VISIBLE-COUNT_ on this page'
			 *
			 * @property _infoMessage
			 * @type string
			 * @default null
			 */
			"infoMessage" : null
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
			 * save user-defined fnInfoCallBack before replace it for set
			 * infoMessage.
			 *
			 * @property classForNotSelectedRow
			 * @type function
			 * @default null
			 */
			"user_fnInfoCallBack" : null,

			/**
			 * List of ids. This list could be selected or not-selected ids
			 * depending on idListSelected value.
			 *
			 * @property idList
			 * @type array
			 * @default []
			 */
			"idList" : null,
			/**
			 * Informs if items from _idList are selected (true) or not-selected
			 * (false) idListSelected value.
			 *
			 * @property idListSelected
			 * @type boolean
			 * @default true
			 */
			"idListSelected" : true,

			/**
			 * Flag to mark all-items-selected.
			 *
			 * @property selectedAll
			 * @type boolean
			 * @default false
			 */
			"selectedAll" : false,

			/**
			 * Callback for selection change notification. The handler
			 * functions this object contains, receives following data:
			 *
			 * <code>selectionSupport</code> (GvNIX_Selection) instance which
			 * fires the event
			 * <code>action</code> (string) one of ['select','deselect','all','none']
			 * <code>id</code> (string) ID of row affected by given action
			 * (null for 'none','all')
			 *
			 * @property selectionChangeCallbacks
			 * @type jQuery.Callbacks
			 * @default jQuery.Callbacks("unique")
			 */
			"selectionChangeCallbacks" : jQuery.Callbacks("unique")
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

		GvNIX_Selection._fnAddInstance(this);

		return this;
	};

	GvNIX_Selection.prototype = {

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
		"fnRedrawRow" : function(trId, isSelected) {
			if (isSelected === undefined) {
				isSelected = this.fnIsSelected(trId);
			}
			var nRow = this.fnGetRowById(trId);
			// Check if row is loaded
			if (nRow) {
				this._fnUpdateRowTr(nRow, isSelected);
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
				this._fnUpdateRowTr(nRow, this.fnIsSelected(nRow.id));
			}
		},

		/**
		 * Informs how many rows are selected and visible
		 */
		"fnVisibleRowsSelecteds" : function() {
			var dt = this._data.dt, count = 0;
			var aoData = dt.aoData, aiDisplay = dt.aiDisplay, start = 0, end = aiDisplay.length;

			if (!dt.oFeatures.bServerSide) {
				start = dt._iDisplayStart;
				end = dt.fnDisplayEnd();
			}

			for ( var i = start; i < end; i++) {
				var nRow = aoData[aiDisplay[i]].nTr;
				if (this.fnIsSelected(nRow.id)) {
					count++;
				}
			}
			return count;
		},

		/**
		 * Informs if trId is selected
		 *
		 * @param trId
		 *            tr.id value (could be DT_RowId on AJAX mode)
		 */
		"fnIsSelected" : function(trId) {
			var _d = this._data;

			if (_d.selectedAll) {
				return true;
			}
			return this._fnIdInList(trId) ? _d.idListSelected
					: !_d.idListSelected;
		},

		/**
		 * Informs if all rows are selected
		 *
		 */
		"fnIsAllSelected" : function() {
			if (this._data.selectedAll){
				return true;
			}
			return this.fnSelectionCount() == this._data.dt.fnRecordsTotal();
		},

		/**
		 * Toggle trId selection status
		 *
		 * @param trId
		 *            tr.id value (could be DT_RowId on AJAX mode)
		 */
		"fnToggleSelect" : function(trId) {
			var debug = this.s.debug;
			if (this.fnIsSelected(trId)) {
				if (debug) {
					this.log("fnToggleSelect: " + trId + " --> deselected");
				}
				this.fnDeselect(trId);
			} else {
				if (debug) {
					this.log("fnToggleSelect: " + trId + " --> selected");
				}
				this.fnSelect(trId);
			}
		},

		/**
		 * Get selection count
		 *
		 * @returns number of row selected
		 */
		"fnSelectionCount" : function() {
			var _d = this._data, totalRecords = _d.dt.fnRecordsTotal();

			if (_d.selectedAll) {
				return totalRecords;
			}
			var idLength = _d.idList.length;
			return _d.idListSelected ? idLength : totalRecords - idLength;
		},

		/**
		 * Get if there is at least one row selected
		 *
		 * @returns true if there is any row selected
		 */
		"fnHasSelection" : function() {
			var _d = this._data, dt = _d.dt;
			if (_d.selectedAll) {
				return true;
			}
			var listLength = _d.idList.length;
			return _d.idListSelected ? listLength > 0
					: (dt.fnRecordsTotal() - listLength) > 0;
		},

		/**
		 * Gets an array of all selected ids
		 *
		 * WARNING: not supported if bServerSide datatables is set. Use
		 * fnGetSelectionInfo in that cases (AJAX mode).
		 *
		 * @returns an array of selected id.
		 */
		"fnGetSelectedIds" : function() {
			var _d = this._data, dt = _d.dt;
			if (dt.oFeatures.bServerSide) {
				this
						.log("fnGetSelectedIds: method not supported in bServerSide datatable mode");
				throw "fnGetSelectedIds: method not supported in bServerSide datatable mode";
			}
			var aoData = dt.aoData, result = [];
			if (_d.selectedAll) {
				for ( var i = 0; i < aoData.length; i++) {
					var nRow = aoData[i].nTr;
					result.push(nRow.id);
				}
			} else if (_d.idListSelected) {
				result = _d.idList.slice();

			} else {
				for ( var i = 0; i < aoData.length; i++) {
					var id = aoData[i].nTr.id;
					if (!this._fnIdInList(id)) {
						result.push(id);
					}
				}
			}
			return result;
		},

		/**
		 * Gets an array of all selected IDs of rows which is currently on
		 * display on the page.
		 *
		 * @returns an array of selected IDs
		 */
		"fnGetDisplaySelectedIds" : function() {
			var _d = this._data, dt = _d.dt;
			var aoData = dt.aoData, aiDisplay = dt.aiDisplay, start = 0, end = aiDisplay.length, result = [];

			if (!dt.oFeatures.bServerSide) {
				start = dt._iDisplayStart;
				end = dt.fnDisplayEnd();
			}

			for ( var i = start; i < end; i++) {
				var nRow = aoData[aiDisplay[i]].nTr;
				if (this.fnIsSelected(nRow.id)) {
					result.push(nRow.id);
				}
			}
			return result;
		},

		/**
		 * Return an Object with selection information:
		 * <ul>
		 * <li><code>idList</code>(array of objects): List of refered ide</li>
		 * <li><code>idListSelected</code>(boolean) if true
		 * <code>idList</code> contains <em>selected</em> id, otherwise
		 * <code>idList</code> contains <em>not-selected</em> ids</li>
		 * <li><code>all</code>(boolean): if true all elements all selected
		 * (previous values are empty)</li>
		 * </ul>
		 */
		"fnGetSelectionInfo" : function() {
			var _d = this._data;
			if (_d.selectedAll) {
				return {
					"all" : true,
					"idList" : new Array(),
					"idListSelected" : false
				};
			} else {
				return {
					"all" : false,
					"idList" : _d.idList.slice(),
					"idListSelected" : _d.idListSelected
				};
			}
		},

		/**
		 * Set trId as selected
		 *
		 * @param trId
		 *            tr.id value (could be DT_RowId on AJAX mode)
		 * @param redraw
		 *            row if change state
		 * @returns true if selection has been change
		 */
		"fnSelect" : function(trId, redraw) {
			var _d = this._data, s = this.s;

			if (_d.selectedAll) {
				// Already selected
				return false;
			}
			var changed = false;
			var index = jQuery.inArray(trId, _d.idList);
			if (index === -1) {
				// not in list
				if (_d.idListSelected) {
					// before add, check for multi options
					if (!s.multiRow) {
						// check for previso selection
						var count = this.fnSelectionCount();
						if (count > 1) {
							this.fnSelectNone(redraw, false);
						} else if (count == 1) {
							this.fnDeselect(_d.idList[0], redraw);
						}
					}
					// add to "selected" list
					_d.idList.push(trId);
					changed = true;
				}
			} else {
				// in list
				if (!_d.idListSelected) {
					// before add, check for multi options
					if (!s.multiRow) {
						// check for previous selection
						if (this.fnHasSelection()) {
							this.fnSelectNone(redraw, false);
						}
					}
					// remove from "not-selected" list
					_d.idList.splice(index, 1);
					changed = true;
				}
			}
			if (changed) {
				if (redraw === undefined || redraw) {
					this.fnRedrawRow(trId, true);
					this._fnUpdateInfo();
				}
				_d.selectionChangeCallbacks.fireWith(this, [ this, 'select',
						trId ]);
			}
			
			// Save selected rows
			this.fnSaveState();
			
			return changed;
		},

		/**
		 * Set trId as no-selected
		 *
		 * @param trId
		 *            tr.id value (could be DT_RowId on AJAX mode)
		 * @param redraw
		 *            row if change state
		 * @returns true if selection has been change
		 */
		"fnDeselect" : function(trId, redraw) {
			var _d = this._data;
			if (_d.selectedAll) {
				// Toggle select all and use id list as not-selected
				_d.selectedAll = false;
				_d.idListSelected = false;
				_d.idList = [ trId ];
				if (redraw === undefined || redraw) {
					this.fnRedrawRow(trId);
					this._fnUpdateInfo();
				}
				_d.selectionChangeCallbacks.fireWith(this, [ this, 'deselect',
						trId ]);
				
				// Save selected rows
				this.fnSaveState();
				
				return true;
			}
			var changed = false;
			var index = jQuery.inArray(trId, _d.idList);
			if (index === -1) {
				// not in list
				if (!_d.idListSelected) {
					// add to "not-selected" list
					_d.idList.push(trId);
					changed = true;
				}
				if (this.fnSelectionCount() == 0) {
					// reset flag and list
					_d.idList = [];
					_d.idListSelected = true;
				}
			} else {
				// in list
				if (_d.idListSelected) {
					// remove from "selected" referred list
					_d.idList.splice(index, 1);
					changed = true;
				}
			}
			if (changed) {
				if (redraw === undefined || redraw) {
					this.fnRedrawRow(trId);
					this._fnUpdateInfo();
				}
				_d.selectionChangeCallbacks.fireWith(this, [ this, 'deselect',
						trId ]);
			}
			
			// Save selected rows
			this.fnSaveState();
			
			return changed;
		},

		/**
		 * Set all rows as selected
		 *
		 * @param redraw
		 *            row if change state
		 * @returns true if selection has been change
		 */
		"fnSelectAll" : function(redraw) {
			var _d = this._data;
			if (_d.selectedAll) {
				return false;
			}
			_d.selectedAll = true;
			_d.idListSelected = true;
			_d.idList = [];
			if (redraw === undefined || redraw) {
				this.fnRedrawVisibleRows();
				this._fnUpdateInfo();
			}
			_d.selectionChangeCallbacks.fireWith(this, [ this, 'all', null ]);
			
			// Save selected rows
			this.fnSaveState();
			
			return true;
		},

		/**
		 * Set all rows as no-selected
		 *
		 * @param redraw
		 *            row if change state
		 * @param updateInfo
		 *            if redraw
		 * @returns true if selection has been change
		 */
		"fnSelectNone" : function(redraw, updateInfo) {
			var _d = this._data;
			if (!_d.selectedAll) {
				if (_d.idList.length == 0) {
					return false;
				}
			}
			_d.selectedAll = false;
			_d.idListSelected = true;
			_d.idList = [];
			if (redraw === undefined || redraw) {
				this.fnRedrawVisibleRows();
				if (updateInfo === undefined || updateInfo) {
					this._fnUpdateInfo();
				}
			}
			_d.selectionChangeCallbacks.fireWith(this, [ this, 'none', null ]);
			
			// Save selected rows
			this.fnSaveState();
			
			return true;
		},

		/**
		 * Add given function handler to {@link selectionChange} Callback
		 *
		 * @param callback function to register that can declare following
		 *            parameters:
		 *            <code>selectionSupport</code> (GvNIX_Selection) instance which fires the event
		 *            <code>action</code> (string) one of ['select','deselect','all','none']
		 *            <code>id</code> (string) ID of row affected by given action (null for
		 *            'none','all')
		 */
		"fnAddSelectionChangeCallback" : function(callback) {
			var _d = this._data;
			if (this.s.debug) {
				this.log("fnAddSelectionChangeCallback: "
						+ (callback.name ? callback.name : callback));
			}
			_d.selectionChangeCallbacks.add(callback);
		},

		/**
		 * Remove a selectionChange callback function
		 *
		 * @param callback
		 *            function to remove
		 */
		"fnRemoveSelectionChangeCallback" : function(callback) {
			var _d = this._data;
			if (this.s.debug) {
				this.log("fnRemoveSelectionChangeCallback: "
						+ (callback.name ? callback.name : callback));
			}
			_d.selectionChangeCallbacks.remove(callback);
		},

		// Private methods (they are of course public in JS, but recommended as
		// private) * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * Informs if trId is selected
		 *
		 * @param trId
		 *            tr.id value (could be DT_RowId on AJAX mode)
		 */
		"_fnIdInList" : function(trId) {
			return jQuery.inArray(trId, this._data.idList) !== -1;
		},

		/**
		 * Register a Datatables fnRowDrawCallback
		 *
		 * This registers a function on datatable to draw row.
		 *
		 * Also bind click event on row to manage selection.
		 */
		"_fnRegisterDrawSelectionRowCallback" : function() {

			var that = this, dt = this._data.dt;

			dt.oApi._fnCallbackReg(dt, 'aoRowCallback', function(nRow, aData,
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
				that._fnUpdateRowTr(nRow, that.fnIsSelected(rowId));

				// Check if it's already bind click event
				that._fnBindClickEvent(nRow);
			}, 'addSelectionSupport');
		},

		/**
		 * Datatables fnInfoCallback
		 *
		 * This function will register a callback on datatables to draw the
		 * table to add 'infoMessage' to datatable info label.
		 *
		 * This concatenate to the end of 'sPre' (message generated by
		 * datatables or custom user function) the value of 'infoMessage' after
		 * replacing '_SEL-VISIBLE-COUNT_' and '_SEL-COUNT_' variables.
		 */
		"_fnRegisterSelectionInfoCallback" : function() {

			var that = this, dt = this._data.dt;

			if (!this.s.infoMessage) {
				return;
			}
			if (dt.oLanguage.fnInfoCallback !== null) {
				this._data.user_fnInfoCallBack = dt.oLanguage.fnInfoCallback;
			}

			dt.oLanguage.fnInfoCallback = function(oSettings, iStart, iEnd,
					iMax, iTotal, sPre) {
				var usr_callbck = that._data.user_fnInfoCallBack;

				if (usr_callbck) {
					sPre = usr_callbck.call(oSettings.oInstance, oSettings,
							iStart, iEnd, iMax, iTotal, sPre);
				}
				var iSelCount = that.fnSelectionCount();
				var message = that.s.infoMessage;
				if (message && iSelCount) {
					sPre = sPre
							+ message.replace(/_SEL-VISIBLE-COUNT_/g,
									that.fnVisibleRowsSelecteds()).replace(
									/_SEL-COUNT_/g, iSelCount);
				}
				return sPre;
			};
		},

		/**
		 * Update datatables information message if its needed
		 */
		"_fnUpdateInfo" : function() {
			if (this.s.infoMessage) {
				this._data.dt.oApi._fnUpdateInfo(this._data.dt);
			}
		},

		/**
		 * update graphically a row (node)
		 *
		 * @param nRow
		 * @param selectec
		 */
		"_fnUpdateRowTr" : function(nRow, selected) {
			var s = this.s, nTable = this._data.dt.nTable;
			var classToAdd = null, classToRemove = null, checkValue = null;

			// prepare values
			if (selected) {
				// add selected class
				classToAdd = s.classForSelectedRow;
				// check for not-select class
				classToRemove = s.classForNotSelectedRow ? s.classForNotSelectedRow
						: null;
				// check if input must be checked
				checkValue = s.checkColumnClass ? true : null;
			} else {
				// remove selected class
				classToRemove = s.classForSelectedRow;
				// check for not-select class
				classToAdd = s.classForNotSelectedRow ? s.classForNotSelectedRow
						: null;
				// check if input must be unchecked
				checkValue = s.checkColumnClass ? false : null;
			}
			if (s.debug) {
				this.log("_fnUpdateRowTr: id=" + nRow.id + " +" + classToAdd
						+ " -" + classToRemove + " check:" + checkValue);
			}
			// update row values
			var $nRow = jQuery(nRow,nTable);
			if (classToAdd) {
				$nRow.addClass(classToAdd);
			}
			if (classToRemove) {
				$nRow.removeClass(classToRemove);
			}
			if (checkValue != null) {
				var checkbox = jQuery("input." + s.checkColumnClass
						+ ":checkbox", $nRow);
				checkbox.prop('checked', checkValue);
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

			var that = this, s = this.s;

			if (nRow.selection_binded) {
				// already binded
				return;
			}
			if (s.checkColumnClass) {
				// bind checkbox
				var checkbox = jQuery("input." + s.checkColumnClass
						+ ":checkbox", nRow);
				checkbox.on('change', function() {
					var id = jQuery(this).closest('tr').prop('id');
					if (this.checked) {
						that.fnSelect(id);
					} else {
						that.fnDeselect(id);
					}
				});

			} else {
				// bind row click
				jQuery(nRow).on('click', function() {
					that.fnToggleSelect(this.id);
				});
			}
			nRow.selection_binded = true;
		},

		/**
		 * Initialize component
		 */
		"_fnConstruct" : function(iSettings) {
			var s = this.s, _d = this._data, dt = _d.dt;
			// initialize settings
			if (typeof iSettings == "object") {
				if (iSettings.multiRow !== undefined) {
					s.multiRow = iSettings.multiRow;
				}
				if (iSettings.checkColumnClass !== undefined) {
					s.checkColumnClass = iSettings.checkColumnClass;
				}
				if (iSettings.classForSelectedRow !== undefined) {
					s.classForSelectedRow = iSettings.classForSelectedRow;
				}
				if (iSettings.classForNotSelectedRow !== undefined) {
					s.classForNotSelectedRow = iSettings.classForNotSelectedRow;
				}
				if (iSettings.infoMessage !== undefined) {
					s.infoMessage = iSettings.infoMessage;
				}
				if (iSettings.debug !== undefined) {
					s.debug = iSettings.debug;
					if (s.debug) {
						// Regsiter a selectionChange calback to log the event
						// fire
						_d.selectionChangeCallbacks.add(function(selection,
								action, id) {
							selection.log("selectionChange: [" + action + "]"
									+ (id ? "id: '" + id + "'" : ""));
						});
					}
				}
				if (typeof iSettings.selectionChange == "object") {
					_d.selectionChangeCallbacks.add(iSettings.selectionChange);
				}
			}

			// Initialize Variables to store data
			_d.idList = [];
			_d.idListSelected = true;
			_d.selectedAll = false;

			// Register click on current rows
			var aoData = dt.aoData;
			for ( var i = 0; i < aoData.length; i++) {
				var nRow = aoData[i].nTr;
				this._fnBindClickEvent(nRow);
			}

			// Register Row callback without remove user configuration
			this._fnRegisterDrawSelectionRowCallback();

			// Register callback to update info label
			this._fnRegisterSelectionInfoCallback();

			// Update visible rows
			this.fnRedrawVisibleRows();
			
			// Load current state
			this.fnLoadState();
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
			sName += "gvnixRowSelected-"+dt.nTable.id;
			
			var sValue = "";
			if(clear == undefined){
				var selectionInfo = this.fnGetSelectionInfo();
				sValue = dt.oApi._fnJsonString(selectionInfo);
			}
			

			if(!window.localStorage){
				dt.oApi._fnCreateCookie(sName,
						sValue,
						10*60, // 10 minutes
						"gvnixRowSelected-",
						null
						);
			}else{
				window.localStorage.setItem(sName,sValue);
			}
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
			sName += "gvnixRowSelected-"+dt.nTable.id;

			if(!window.localStorage){
				var ids = dt.oApi._fnReadCookie(sName);
				if(ids !== ""){
					var object = JSON.parse(ids);
					if(object !== null && object.all){
						this.fnSelectAll();
					}else if(object !== null && object.idListSelected){
						for(i in object.idList){
							this.fnSelect(object.idList[i], true,true);
						}
					}else if(object !== null){
						this.fnSelectAll();
						for(i in object.idList){
							this.fnDeselect(object.idList[i], true,true);
						}
					}
				}
			}else{
				var ids = window.localStorage.getItem(sName);
				if(ids !== ""){
					var object = JSON.parse(ids);
					if(object !== null && object.all){
						this.fnSelectAll();
					}else if(object !== null && object.idListSelected){
						for(i in object.idList){
							this.fnSelect(object.idList[i], true,true);
						}
					}else if(object !== null){
						this.fnSelectAll();
						for(i in object.idList){
							this.fnDeselect(object.idList[i], true,true);
						}
					}
				}
			}
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
	GvNIX_Selection._aInstances = [];

	/**
	 * Store selection callbacks which are not registed yet in instances
	 * (usually because not initialiced yet). When the refered instance is
	 * register on _aInstances the related callback will be added.
	 */
	GvNIX_Selection._aSelectionCallbacksPendingToRegister = [];

	// Static methods * * * * * * * * * * * * * * * * * * * * * * * * * * * *

	/**
	 * Store new GvNIX_Selection instance. Also register pending callback.
	 *
	 * @method _fnAddInstance
	 * @static
	 */
	GvNIX_Selection._fnAddInstance = function(instance) {
		GvNIX_Selection._aInstances.push(instance);

		// Register pending callback
		var nodeId = instance._data.dt.nTable.id;
		var register = GvNIX_Selection._aSelectionCallbacksPendingToRegister;

		var regInfo;
		// Check if there is a pending register
		for ( var i = 0, iLen = register.length; i < iLen; i++) {
			regInfo = register[i];

			// If table id match
			if (regInfo.id == nodeId) {
				// register all callback
				var callbacks = regInfo.callbacks;
				while (callbacks.length > 0) {
					instance.fnAddSelectionChangeCallback(callbacks.pop());
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
	GvNIX_Selection.fnGetInstance = function(node) {
		if (typeof node != 'object') {
			node = jQuery("#" + node);
		}

		if ($.fn.DataTable.fnIsDataTable(node)) {
			throw "Datatable not found: " + node;
		}
		var dt = node.dataTable();

		for ( var i = 0, iLen = GvNIX_Selection._aInstances.length; i < iLen; i++) {
			if (GvNIX_Selection._aInstances[i]._data.dt == dt) {
				return GvNIX_Selection._aInstances[i];
			}
		}
		return null;
	};

	/**
	 * Register a SelectionChange Callback on table node (or id if a string is
	 * given). This works including when datatable is not ready. In this case,
	 * this will store the function and register it when selection support is
	 * initialized for related datatable.
	 *
	 * @method fnGetInstance
	 * @returns {Object} ID of table OR table node, for which we want the
	 *          GvNIX_Seletion instance
	 * @static
	 */
	GvNIX_Selection.fnAddSelectionChangeCallback = function(node, callback) {
		var nodeId;
		if (typeof node != 'object') {
			nodeId = node;
			node = jQuery("#" + node);
		} else {
			nodeId = node.id;
		}

		if (jQuery.fn.DataTable.fnIsDataTable(node)) {
			// found node
			var dt = node.dataTable();

			for ( var i = 0, iLen = GvNIX_Selection._aInstances.length; i < iLen; i++) {
				if (GvNIX_Selection._aInstances[i]._data.dt == dt) {
					// found datatable: register callback in datatable
					GvNIX_Selection._aInstances[i]
							.fnAddSelectionChangeCallback(callback);
					return true;
				}
			}
		}
		// Store in pending to assing
		var register = GvNIX_Selection._aSelectionCallbacksPendingToRegister;

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
	GvNIX_Selection.prototype.CLASS = "GvNIX_Selection";

	/**
	 * TableTools version
	 *
	 * @constant VERSION
	 * @type StrFing
	 * @default See code
	 */
	GvNIX_Selection.VERSION = "1.4.0.RELEASE";
	GvNIX_Selection.prototype.VERSION = GvNIX_Selection.VERSION;

	/** TODO Add as datatable feature * */

})(jQuery, window, document);

/** *********************************************** */

/**
 *
 * Gets/initialize gvnix Selection support on a datatables
 *
 * @param oSettings
 * @param iSelectionSettings
 * @return GvNIX_Selection object
 * @author gvNIX Team
 */
jQuery.fn.dataTableExt.oApi.fnSelection = function(oSettings,
		iSelectionSettings) {

	var selectionSupport = oSettings.GvNIX_Selection_support;

	if (oSettings.GvNIX_Selection_support === undefined) {
		selectionSupport = new GvNIX_Selection(oSettings, iSelectionSettings);
	} else {
		// TODO adjust settings on already initialized selection support
	}

	oSettings.GvNIX_Selection_support = selectionSupport;

	return selectionSupport;
};

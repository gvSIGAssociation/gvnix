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

/* Global scope for GvNIX_Editing */
var GvNIX_Editing;

/*
 * Prefixes used:
 *   _ - private
 *   $ - jQuery object
 *   n - node
 *   o - object
 *   a - array
 *   s - string
 *   b - boolean
 *   f - float
 *   i - integer
 *   fn - function
 */
(function(jQuery, window, document) {

	GvNIX_Editing = function(oSettings, oOpts) {

		// Santiy check that we are a new instance
		if (!this instanceof GvNIX_Editing) {
			alert("Warning: GvNIX_Editing must be initialised with the keyword 'new'");
		}

		// Public class variables * * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * @namespace Apply this general edit options
		 */
		this._options = {

			/**
			 * Allow create rows
			 *
			 * @property create
			 * @type boolean
			 * @default true
			 */
			"create" : true,

			/**
			 * Allow update rows
			 *
			 * @property update
			 * @type boolean
			 * @default true
			 */
			"update" : true,

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
			 * <tr> on editing rows
			 *
			 * @property classForEditingRow
			 * @type string
			 * @default 'row_editing'
			 */
			"classForEditingRow" : 'row_editing',

			/**
			 * Css class to set
			 * <tr> on editing rows which has binding errors
			 *
			 * @property classForEditingRowError
			 * @type string
			 * @default 'row_editing'
			 */
			"classForEditingRowError" : 'row_editing_error',

			/**
			 * Show a information message on datatables info label. Ex: '<br/>
			 * Editing _EDT-COUNT_ rows, _EDT-VISIBLE-COUNT_ on this page'
			 *
			 * @property infoMessage
			 * @type string
			 * @default null
			 */
			"infoMessage" : "<br/>Editing _EDT-COUNT_ rows, _EDT-VISIBLE-COUNT_ on this page",

			/**
			 * Function to call when a binding error found.
			 *
			 * This function will receive:
			 *
			 * <code>editingSupport</code> (GvNIX_Editing) instance which fires the event
			 * <code>cellNode</code> (node) td node related to binding error
			 * <code>property</code> (string) refered property
			 * <code>errorMessage</code> (string) binding error message
			 */
			"bindingErrorDrawer" : null,

			/**
			 * ID of DIV which contains gvNIX Datatables tools
			 *
			 * @property toolbarId
			 * @type string
			 * @default null
			 */
			"toolbarId" : null,

			/**
			 * Show last created row at first row on first
			 * table refresh (true by default)
			 */
			"showCreatedRowFirst": true
		};

		/**
		 * @namespace Settings object which contains customizable information
		 *            for this instance
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
			 * DataTables settings object. Holds all the information needed for a given table
			 * API: http://datatables.net/docs/DataTables/1.9.4/DataTable.models.oSettings.html
			 *
			 * @property oSettings
			 * @type object
			 * @default <i>From the oDT init option</i>
			 */
			"oSettings" : oSettings,

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
			 * Information of rows which are in editing mode. This Map
			 * holds as key the ROW_ID and as value an instance of
			 * {@link GvNIX_Editing.models.oEditingRow}
			 *
			 * @property oEditingRows
			 * @type object
			 * @default empty
			 */
			"oEditingRows" : {},

			/**
			 * Information of rows which are in creating mode. This Map
			 * holds as key the ROW_ID and as value an instance of
			 * {@link GvNIX_Editing.models.oCreatingRow}
			 *
			 * @property oCreatingRows
			 * @type object
			 * @default empty
			 */
			"oCreatingRows" : {},

			/**
			 * The panel that contains the create table
			 *
			 * @type object
			 * @default empty
			 */
			"oCreatePanel" : {},

			/**
			 * The create table
			 *
			 * @type object
			 * @default empty
			 */
			"oCreateTable" : {},

			/**
			 * Array with one element for each entity property. Relates each
			 * column of the create table with the entity property which value
			 * is shown in that column. Example: aColumnField[0] = "firstName"
			 * means column 0 is shown the value of the "firstName" field
			 */
			"aCreateColumnField" : [],

			/**
			 * Array with one element for each entity property. Relates
			 * each column with the entity property which value is shown in
			 * that column. Example: aColumnField[0] = "firstName" means
			 * column 0 is shown the value of the "firstName" field
			 */
			"aColumnField" : [],

			/**
			 * Callback for start editing notification. The handler
			 * functions this object contains, receives following data:
			 *
			 * <code>editingSupport</code> (GvNIX_Editing) instance which fires the event
			 * <code>action</code> (string)
			 * <code>selectedIds</code> (array) IDs of rows affected by given action
			 *
			 * @property beginEditingCallbacks
			 * @type jQuery.Callbacks
			 * @default jQuery.Callbacks("unique")
			 */
			"beginEditingCallbacks" : jQuery.Callbacks("unique"),

			/**
			 * Callback for finishEditing
			 *
			 * Callback will receive:
			 * <code>editingSupport</code> (GvNIX_Editing) instance which fires the event
			 *
			 * @property finishEditingCallbacks
			 * @type jQuery.Callbacks
			 * @default jQuery.Callbacks("unique")
			 */
			"finishEditingCallbacks" : jQuery.Callbacks("unique"),

			/**
			 * Callback for beforeUpdateValue
			 *
			 * Callback will change receive:
			 * <code>editingSupport</code> (GvNIX_Editing) instance which fires the event
			 * <code>id</code> (string) id affected by action
			 * <code>propertyName</code> property to change
			 * <code>previousValue</code> previous property value
			 * <code>newValue</code> new property value
			 *
			 * To cancel value change callback function
			 * must throw a exception
			 *
			 * @property beforeUpdateValueCallbacks
			 * @type jQuery.Callbacks
			 * @default jQuery.Callbacks("unique")
			 */
			"beforeUpdateValueCallbacks" : jQuery.Callbacks("unique"),

			/**
			 * Callback for afterUpdateValue
			 *
			 * Callback will change receive:
			 * <code>editingSupport</code> (GvNIX_Editing) instance which fires the event
			 * <code>id</code> (string) id affected by action
			 * <code>propertyName</code> property to change
			 * <code>previousValue</code> previous property value
			 * <code>newValue</code> new property value
			 * To cancel value change callback function
			 * must throw a exception
			 *
			 * @property afterUpdateValueCallbacks
			 * @type jQuery.Callbacks
			 * @default jQuery.Callbacks("unique")
			 */
			"afterUpdateValueCallbacks" : jQuery.Callbacks("unique"),

			/**
			 * jQuery instance to submit button
			 */
			"$submitButton" : null,

			/**
			 * jQuery instance to cancel button
			 */
			"$cancelButton" : null,
		};

		// Public class methods * * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * Retrieve the edit options object from an instance
		 *
		 * @method fnOptions
		 * @returns {object} settings object
		 */
		this.fnOptions = function() {
			return this._options;
		};

		// Constructor logic
		this._fnConstruct(oOpts);

		// TBC
		GvNIX_Editing._fnAddInstance(this);

		return this;
	};

	GvNIX_Editing.prototype = {

		// Public methods * * * * * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * Show error message on browser JS console
		 *
		 * @param message to show
		 */
		"error" : function(message) {
			try {
				console.log(message);
			} catch (e) {
				// Can't do anything
			}
		},

		/**
		 * Show debug message on browser JS console if this._options.debug is
		 * true, otherwise doesn't show the message
		 *
		 * @param message to show
		 */
		"debug" : function(message) {
			try {
				if (this._options.debug) {
					console.log(message);
				}
			} catch (e) {
				// Can't do anything
			}
		},

		/**
		 * Gets the row (node) from its id
		 *
		 * @param id value
		 */
		"fnGetRowById" : function(trId) {
			var aoData = this._data.oSettings.aoData;
			for ( var i = 0; i < aoData.length; i++) {
				var nRow = aoData[i].nTr;
				if (nRow.id == trId) {
					return nRow;
				}
			}
			return null;
		},

		/**
		 * Gets the cretion row (node) from its id
		 *
		 * @param the numer of the row
		 */
		"fnGetCreationRowById" : function(numRow) {
			var createTable = this._data.oCreateTable;
			var $trs = jQuery('>tbody >tr', createTable);
			var row = $trs[numRow];
			if (row !== undefined && row) {
				return row;
			}
			return null;
		},

		/**
		 * Redraw a row
		 *
		 * @param trId tr.id value (could be DT_RowId on AJAX mode)
		 */
		"fnRedrawRow" : function(trId) {
			var nRow = this.fnGetRowById(trId);
			// Check if row is loaded
			if (nRow) {
				this._fnUpdateRowTr(nRow);
			}
		},

		/**
		 * Redraw all visible rows
		 */
		"fnRedrawVisibleRows" : function() {

			var st = this._data.oSettings;
			var aoData = st.aoData, aiDisplay = st.aiDisplay,
			start = 0, end = aiDisplay.length;
			var oTable = st.oInstance;

			// As, usually, we need to adjust column size (cells sizes
			// when it inserts or remove input) we just call to fnAdjustColumnSizing
			// which perform a redraw of every cell. So we don't need redraw cells
			// by hand
			this._fnUpdateInfo();
			oTable.fnAdjustColumnSizing();
		},

		/**
		 * Informs how many rows are editing and visible
		 */
		"fnVisibleRowsEditing" : function() {
			var st = this._data.oSettings, count = 0;
			var aoData = st.aoData, aiDisplay = st.aiDisplay,
			start = 0, end = aiDisplay.length;

			if (!this.fnHasEditingRow()) {
				return 0;
			}

			if (!st.oFeatures.bServerSide) {
				start = st._iDisplayStart;
				end = st.fnDisplayEnd();
			}

			for ( var i = start; i < end; i++) {
				var nRow = aoData[aiDisplay[i]].nTr;
				if (this.fnIsEditing(nRow.id)) {
					count++;
				}
			}
			return count;
		},

		/**
		 * Informs if trId is editing mode
		 *
		 * @param trId
		 *            tr.id value (could be DT_RowId on AJAX mode)
		 */
		"fnIsEditing" : function(trId) {
			if (!this.fnHasEditingRow()) {
				return false;
			}
			return this._data.oEditingRows[trId] && true;
		},

		/**
		 * Get row-in-editing-mode count
		 *
		 * @returns number of row in editing mode
		 */
		"fnInlineEditingCount" : function() {
			return Object.keys(this._data.oEditingRows).length;
		},

		/**
		 * Get if there is at least one row in editing mode
		 *
		 * @returns true if there is any row in editing mode
		 */
		"fnHasEditingRow" : function() {
			return !jQuery.isEmptyObject(this._data.oEditingRows);
		},

		/**
		 * Replace the content of each cell in the given row or selected rows
		 * to be made editable with edit controls requested to server by
		 * AJAX.
		 *
		 * Process flow:
		 * 1. Do a request to get one update form template for each row
		 * 2. Parse the result
		 * 3. Push parsed controls in _data.aEditingControls and editing data
		 *    in _data.oEditingRows
		 * 4. Replace current values by edit controls in editing rows
		 * 5. Redraw the table
		 *
		 * @param trId tr.id value or DT_RowId on AJAX mode (also accepts an array).
		 * 		This param is optional, if no provided the method will start editing for
		 * 		selected rows
		 * @returns true start editing row
		 */
		"fnEditRows" : function(trId) {
			var oSettings = this._data.oSettings, _d = this._data;
			var oTable = oSettings.oInstance; // The DataTables object
			var appCtx = this._options.applicationContext;

			if (appCtx === undefined) {
				this.error("[fnEditRows] No application context given, nothing to do.");
				return false;
			}

			var selectedIds = [];
			if (trId !== undefined) {
				if (jQuery.isArray(trId)) {
					[].push.apply(selectedIds, trId);
				} else {
					selectedIds.push(trId);
				}
			}
			else {

				// Get the table GvNIX_Selection object
				var $selection = oTable.fnSelection();

				var selCount = $selection.fnSelectionCount();
				if (selCount == 0) {
					if (this._options.debug) {
						this.debug("[fnEditRows] No rows selected, nothing to do.");
					}
					return false;
				}

				selectedIds = $selection.fnGetDisplaySelectedIds();
			}

			// Remove already editing rows
			selectedIds = jQuery.grep(selectedIds, function(rowId) {
				return !_d.oEditingRows[rowId];
			});

			if (selectedIds.length === 0) {
				// nothing to do
				return false;
			}

			// Notify start editing callback
			try {
				_d.beginEditingCallbacks.fireWith(this, [this, "update", selectedIds ]);
			} catch (e) {
				// Canceled editing
				return false;
			}

			// Request for update forms
			var jqxhr = jQuery.ajax(appCtx + "/datatables/updateforms", {
				traditional: true,
				data: { id: selectedIds },
				dataType: 'json'
			});

			jqxhr.done( jQuery.proxy( function(response) {
				var _d = this._data;
				var oTable = _d.oSettings.oInstance;

				// With proxy(), 'this' refers to the object encapsulates
				// this function.
				for ( var i = 0; i < response.length; i++) {
					var rowId = response[i].DT_RowId;
					var nRow = this.fnGetRowById( rowId );
					var iRow = nRow._DT_RowIndex;
					var aRowData = oTable.fnGetData( nRow );

					// Create jQuery object for easier access to update form components
					var sUpdateForm = response[i].form;
					var htmlUpdateForm = jQuery.parseHTML(sUpdateForm );
					var $updateForm = jQuery( htmlUpdateForm );

					// Set row in editing mode by storing the data edit format
					// in _data.oEditingRows
					var oEditRow = jQuery.extend( true, {}, GvNIX_Editing.models.oEditingRow );
					oEditRow.iRowIndex = iRow;
					oEditRow.sRowId = rowId;

					// Init data containers
					oEditRow.oEditingData.id = rowId;

					// Iterate over column fields to get edit controls from
					// entire update form
					var aFields = _d.aColumnField;
					for ( var j = 0; j < aFields.length; j++) {
						var colIdx = j;
						var property = aFields[colIdx];

						// property is undefined for action columns
						if(property !== undefined) {
							property = this._fnUnscapePropertyName(property);

							// Roo property id has the pattern "... _Vet_firstName_id"
							var $ctrlGroup = $updateForm.find("div[id $= '_" + property.replace(".","_") + "_id'].control-group");
							var $editCtrls = $ctrlGroup.find("div.controls");
							var $inputCtrl = $editCtrls.find(":input");
							if ($inputCtrl.length == 0) {
								this.error("[fnEditRows] Input control for property '" + property + "' not found.");
								continue;
							}

							for(var k = 0; k < $inputCtrl.length; k++){
								
								// Update input class
								if (jQuery($inputCtrl[k]).attr('type') == 'checkbox') {
									$editCtrls.attr('class', $editCtrls.attr('class') + ' checkbox');
								} else {
									var inputClass = jQuery($inputCtrl[k]).attr('class');
									if (inputClass !== undefined && inputClass) {
										inputClass = inputClass + ' form-control input-sm';
									} else {
										inputClass = 'form-control input-sm';
									}
									jQuery($inputCtrl[k]).attr('class', inputClass);
								}
								
								// Make a new unique ID for the input to avoid
								// collisions with other elements with same ID
								jQuery($inputCtrl[k]).attr('id', '_' + $form.attr('id') + jQuery($inputCtrl[k]).attr('id') + '_edit_' + rowId);
								
								var value = fnVal( jQuery($inputCtrl[k]) );
								
								// Store current value to be able to recover if user
								// cancels editing
								var originalData = aRowData[property];
								if (originalData !== undefined) {
									oEditRow.aOriginalData[ colIdx ] = originalData;
								}
								
								// Store value received from server in array of
								// values by column index
								oEditRow.aEditingData[ colIdx ] = value;

								// Store value received from server in Map of
								// values by property name
								oEditRow.oEditingData[ property ] = value;
								
								// Store edit controls
								oEditRow.aEditingControls[ colIdx ] = fnOuterHTML($editCtrls); // node HTML
							}
							
						} else {
							// Store non-property values using index
							oEditRow.aOriginalData[ colIdx ] = aRowData[ colIdx ];
							oEditRow.aEditingControls[ colIdx ] = "";
						}
					}

					_d.oEditingRows[ rowId ] = oEditRow;


					// We need all values in received from to complete the
					// the request to send to server (by example: version, other required fields, etc...)
					jQuery.each($updateForm.find("form").find(":input"), function (index, input) {
						var $input = jQuery(input);
						var name = $input.attr('name');
						if (name !== undefined && name) {
							oEditRow.oAllItemData[name] = fnVal($input);
						}
					});
				}

				// Redraw the table
				this.fnRedrawVisibleRows();

				this.fnUpdateEditingTools();
				
				// Initializing Loupe components
				setTimeout(function(){
					oTable.find(".loupe_control").each(function(index) {
						new GvNIX_Loupe(jQuery(this));
					});
					oTable.find(".row_editing").each(function(index){
						// Bind events for update inputs, focus cursor and initialize components
						this.editing_binded = false;
						oTable.fnEditing()._fnBindRowEvents(this);
					});
					
				},100);
				
			}, this) );

			return true;
		},

		/**
		 * Write the original value to the TD cell node, effectively replacing
		 * the edit control that we inserted in the fnEditRows function with
		 * the original cell value stored in _data.oEditingRows[ROW_ID].aOriginalData
		 *
		 * @param trId tr.id value (could be DT_RowId on AJAX mode)
		 * @param redraw row if change state
		 * @returns true cancel is done
		 */
		"fnCancelRows" : function(trId, redraw) {
			var oEditingRows = this._data.oEditingRows;

			var editingIds = [];
			if (trId != null && trId !== undefined) {
				if (jQuery.isArray(trId)) {
					[].push.apply(selectedIds, trId);
				} else {
					selectedIds.push( trId );
				}
			}
			else {
				// Iterate over editing rows and get editing row IDs
				jQuery.each( oEditingRows, function( key, value ) {
					editingIds.push( key );
				});
			}

			if (editingIds.length === 0) {
				// nothing to do
				return false;
			}


			for ( var i = 0; i < editingIds.length; i++) {
				var nRow = this.fnGetRowById( editingIds[i] );
				this._fnCancelRow( nRow );
			}

			// Redraw the table
			if (redraw || redraw === undefined) {
				this.fnRedrawVisibleRows();
				this.fnUpdateEditingTools();
			}
		},

		/**
		 * Cancel all current editing Rows
		 *
		 * @param redraw (default true)
		 * @returns true if any row has been canceled
		 */
		"fnCancelAll" : function(redraw) {
			var _d = this._data;

			return this.fnCancelRows(null,redraw);
		},

		/**
		 * Revert all changes of current editing Rows
		 *
		 * @returns true if any row has been canceled
		 */
		"fnRevertAll" : function() {
			var oEditingRows = this._data.oEditingRows;
			var oTable = this._data.oSettings.oInstance;
			var aProperties = this._data.aColumnField;

			if (jQuery.isEmptyObject(oEditingRows)) {
				return;
			}
			jQuery.each(oEditingRows, jQuery.proxy( function (rowId, oEditingRow) {
				var nRow = this.fnGetRowById(rowId);

				if (jQuery.isEmptyObject(oEditingRow)) {
					this.error("[fnRevertAll] oEditingRow for row '" + rowId + "' must not be null.");
					return;
				}
				// Set original value, stored in 'aOriginalData', in each cell of given row
				var $tds = jQuery('>td', nRow);
				$tds.each(function(index) {
					var $input = jQuery(":input",$tds[index]);
					$input.val(oEditingRow.aOriginalData[index]);
				});
			},this));
			return true;
		},

		/**
		 * Finish editing of all rows in editing mode
		 */
		"fnFinishEditingAll" : function() {
			this.fnFinishEditing(null);
		},

		/**
		 * Finish editing of row
		 *
		 * @param trId of editing row. Accepts an array or a single row id. if null
		 * 		finish all editing rows
		 * @returns true if all rows are successful saved
		 */
		"fnFinishEditing" : function(trId) {
			var _d = this._data, oTable = _d.oSettings.oInstance;
			var oEditingRows = _d.oEditingRows;
			var appCtx = this._options.applicationContext;
			var $this = this;

			if(appCtx === undefined) {
				this.error("[fnEditRows] No application context given, nothing to do.");
				return false;
			}

			var oEditingRows = _d.oEditingRows;

			var editingIds = [];
			if (trId != null && trId !== undefined) {
				if (jQuery.isArray(trId)) {
					[].push.apply(selectedIds, trId);
				} else {
					selectedIds.push( trId );
				}
			}
			else {
				// Iterate over editing rows and get editing row IDs
				jQuery.each( oEditingRows, function( key, value ) {
					editingIds.push( key );
				});
			}

			if (editingIds.length === 0) {
				// nothing to do
				return false;
			}

			// show processing
			oTable.fnProcessingIndicator(true);


			// Prepare data for server update request
			var dataToSend = this._fnPrepareDataToSaveRow(editingIds);

			// prepare Request
			var jqxhr = jQuery.ajax(appCtx, {
				contentType: "application/json",
				//traditional: true,
				handleAs : "json",
				data: JSON.stringify(dataToSend),
				type: "PUT",
				dataType: 'json'
			});

			// Do request to server
			jqxhr.done( jQuery.proxy( function(response) {
				var _d = this._data, oEditingRows = _d.oEditingRows;
				var oTable = _d.oSettings.oInstance;

				// With proxy(), 'this' refers to the object encapsulates
				// this function.
				if (response.status == 'SUCCESS') {
					// Remove editing Rows from registry
					jQuery.each(editingIds, jQuery.proxy(function (index, nRow) {
						// load the row data with received data
						this._fnLoadNewRowData(nRow, response.value[index]);
						delete oEditingRows[nRow];
					},this));

					// Hide accept and cancel buttons if no more editing rows
					this.fnUpdateEditingTools()

					// Redraw table
					// XXX Redraw table or not (updated rows already are updated)
					oTable.fnStandingRedraw();

					try {
						_d.finishEditingCallbacks.fireWith(this, [ this ]);
					} catch (e) {
						this.error("[fnFinishEditing] error on finishEditingCallbacks: "+ e);
					}

				} else {
					// there are errors
					if (response.exceptionMessage){
						// TODO i18n
						showMessage("Error",response.exceptionMessage);
					}
					if (response.bindingResult) {
						jQuery.each(response.bindingResult,jQuery.proxy( function (index, oErrors) {
							var rowId = editingIds[index];
							var oEditingRow = oEditingRows[rowId];
							oEditingRow.oLastEditingErrors = oErrors;

							this._fnShowBindingErrors(oEditingRow, oErrors);
						},this));

					}
				}
			},this))
			.always(jQuery.proxy(function () {
				// Always hide processing indicator
				this._data.oSettings.oInstance.fnProcessingIndicator(false);
				},this))
			.fail(jQuery.proxy(function (jqXHR, textStatus, errorThrown) {
				// TODO i18n
				this.error(textStatus +": " + errorThrown);
				showMessage("Error updating data", textStatus +": " + errorThrown);
			},this));
			return true;
		},
		
		/**
		 * Hide toolbar (ONLY HIDES)
		 */
		"fnHideToolbar" : function() {
			// Hidding Toolbar
			var sTableId = this._data.oSettings.sTableId;
			setTimeout(function(){
				var toolBarLinks = jQuery("#"+ sTableId + "_gvnix_toolbar a");
				toolBarLinks.hide();
			},100);
		},
		
		/**
		 * Hide util buttons
		 */
		"fnHideUtils" : function(){
			var sTableId = this._data.oSettings.sTableId;
			setTimeout(function(){
				// Hidding td
				jQuery("#"+ sTableId + " .utilbox").hide();
				// Hidding th
				jQuery("#"+ sTableId + "_wrapper .dataTables_scroll .dataTables_scrollHead th.utilbox").hide();
			},100);
		},
		
		/** 
		 * Hide Details (ONLY HIDES)
		 */
		
		"fnHideDetails": function() {
			var sTableId = this._data.oSettings.sTableId;
			var details = jQuery("div[id$="+sTableId+"_detail]");
			details.each(function(index, detail){
				jQuery("#"+detail.id).hide();
			});
		},

		/**
		 * Begin create rows
		 *
		 * @param id the identifier
		 */
		"fnBeginCreate" : function(id) {
			var createPanel = jQuery('#' + id + 'CreateForm');
			if (createPanel.length == 0) {
				throw "fnDisplayCreateForm : id not found '" + id + "CreateForm'";
			}
			
			var _d = this._data;

			// Request for create form
			var appCtx = this._options.applicationContext;
			var jqxhr = jQuery.ajax(appCtx + "/datatables/createform", {
				traditional : true,
				dataType : 'json'
			});

			jqxhr.done(jQuery.proxy(function(response) {

				// Create jQuery object for easier access to create form components
				var sCreateForm = response[0].form;
				var htmlCreateForm = jQuery.parseHTML(sCreateForm);
				var $createForm = jQuery(htmlCreateForm);
				
				// Create empty form
				var $form = $createForm.find("form");
				var formHtml = '<form id="' + $form.attr('id') + 'CreateForm"'
						+ ' class="' + $form.attr('class') + ' form-inline create-form"'
						+ ' enctype="' + $form.attr('enctype')
						+ '" method="' + $form.attr('method') + '">'
						+ '</form>';
				var formHtmlParsed = jQuery.parseHTML(formHtml);
				var $emptyForm = jQuery(formHtmlParsed);

				var oCreateRow = jQuery.extend( true, {}, GvNIX_Editing.models.oCreatingRow );
				var rowId = 0; // Currently only create one row
				oCreateRow.sRowId = rowId;
				
				// Init data containers
				oCreateRow.oCreatingData.id = rowId;
				
				// Iterate over column fields to get create controls from
				// entire create form
				var aFields = _d.aColumnField;
				var aHeaderCells = [];
				var aFormCells = [];
				for (var colIdx = 0; colIdx < aFields.length; colIdx++) {
					var property = aFields[colIdx];

					// property is undefined for action columns
					if (property !== undefined) {
						property = this._fnUnscapePropertyName(property);
						
						// Roo property id has the pattern "... _Entity_propertyName_id"
						var $ctrlGroup = $createForm.find("div[id $= '_" + property.replace(".","_") + "_id'].control-group");
						var $createCtrls = $ctrlGroup.find("div.controls");
						var $input = $createCtrls.find(":input");
						if ($input.length == 0) {
							this.error("[fnDisplayCreateForm] Input control for property '" + property + "' not found.");
							continue;
						}

						// Store property name in array of values by column index
						_d.aCreateColumnField.push(property);
						
						// Create header cell
						var headerCell = '<th>' + _d.oSettings.aoColumns[colIdx].sTitle + '</th>';
						aHeaderCells.push(headerCell);

						aFormCells.push("<td>");
						
						for(var i = 0; i < $input.length; i++){
							// Add proper CSS classes to contained and input
							var divClass = 'controls';
							if (jQuery($input[i]).attr('type') == 'checkbox') {
								divClass = divClass + ' checkbox';
							} else {
								var inputClass = jQuery($input[i]).attr('class');
								if (inputClass !== undefined && inputClass) {
									inputClass = inputClass + ' form-control input-sm';
								} else {
									inputClass = 'form-control input-sm';
								}
								jQuery($input[i]).attr('class', inputClass);
							}

							// Make a new unique ID for the input to avoid
							// collisions with other elements with same ID
							jQuery($input[i]).attr('id', '_' + $form.attr('id') + jQuery($input[i]).attr('id') + '_create_' + rowId);
							
							
							
							var formCell = "";
							
							if(i == 0){
								formCell = '<div class="' + divClass + '">';
							}
							
							formCell+= fnOuterHTML(jQuery($input[i]));
							
							if(i == $input.length){
								formCell+= '</div>';
							}
							
							aFormCells.push(formCell);
							
							formCell = "";
						}
						
						aFormCells.push("</td>");
					}
				}
				
				// Add a column to send button
				aHeaderCells.push('<th></th>');
				var oOpts = this._options;
				var sSubmitBtnId = id + '_submit_btn';
				var submitBtnHtml = '<td><a href="#" id="' + sSubmitBtnId + '"'
						+ ' alt="' + oOpts.submitBtnLabel + '"' + ' title="'
						+ oOpts.submitBtnLabel + '" class="btn btn-primary btn-sm">'
						+ oOpts.submitBtnLabel + '</a></td>';
				aFormCells.push(submitBtnHtml);

				// Store create row
				_d.oCreatingRows[rowId] = oCreateRow;
				
				// We need all values in received from to complete the
				// request to send to server (by example: version, other
				// required fields, etc...)
				jQuery.each($createForm.find("form").find(":input"), function (index, input) {
					var $input = jQuery(input);
					var name = $input.attr('name');
					if (name !== undefined && name) {
						oCreateRow.oAllItemData[name] = fnVal($input);
					}
				});
				
				// Make the create table
				var createTable = '<table class="table table-condensed"><thead><tr>';
				for (var i = 0; i < aHeaderCells.length; i++) {
					createTable = createTable + aHeaderCells[i];
				}
				createTable = createTable + '</thead><tbody><tr id="' + rowId + '">';
				for (var i = 0; i < aFormCells.length; i++) {
					createTable = createTable + aFormCells[i];
				}
				
				$emptyForm.append(createTable);
				createPanel.append(fnOuterHTML($emptyForm));
				
				// Store createPanel and createTable into data
				_d.oCreatePanel = jQuery(createPanel);
				_d.oCreateTable = jQuery(jQuery('>form >table', createPanel));
				
				// Display createPanel
				createPanel.show();
				
				jQueryInitializeComponents(_d.oCreatePanel);
				
				// Handler submit button
				jQuery('#' + sSubmitBtnId).click( function() {
					jQuery('#' + id).dataTable().fnEditing().fnSendCreationForm(rowId);
					return false;
				});
				
				// When all items are generated, build aCreatingData
				
				// Getting new created form and fields
				var newCreatedForm = jQuery("#" + $form.attr('id')+"CreateForm");
				var fieldsWithName = newCreatedForm.find("div.controls :input[name]");
				
				for (var colIdx = 0; colIdx < fieldsWithName.length; colIdx++) {
					var property = fieldsWithName[colIdx].name;

					// Getting field value
					if(jQuery(fieldsWithName[colIdx]).prop("type") !== "checkbox"){
						var value = fieldsWithName[colIdx].value; 
					}else{
						var value = jQuery(fieldsWithName[colIdx]).prop("checked"); // Not the same for checkboxes
					}
					
					// Store value received from server in array of values by column index
					oCreateRow.aCreatingData.push(value);
					
					// Store value received from server in Map of values by property name
					oCreateRow.oCreatingData[property] = value;
					
					// Store current value to be able to recover when redraw the create form
					oCreateRow.aOriginalData.push(value);
					oCreateRow.oOriginalData[property] = value;
				}
				
				// Bind events for create inputs, focus cursor and initialize components
				this._fnBindCreateRowEvents(this.fnGetCreationRowById(oCreateRow.sRowId));
				
			}, this));
			
			return true;
		},

		/**
		 * Send creation form info
		 *
		 * @param trId of editing row. Accepts an array or a single row id. if null
		 * 		finish all creating rows
		 * @returns true if the data of the form is successful saved
		 */
		"fnSendCreationForm" : function(trId) {
			var _d = this._data, oTable = _d.oSettings.oInstance;
			var appCtx = this._options.applicationContext;
			if( appCtx === undefined) {
				this.error("[fnSendCreationForm] No application context given, nothing to do.");
				return false;
			}

			var creatingIds = [];
			if (trId != null && trId !== undefined) {
				if (jQuery.isArray(trId)) {
					[].push.apply(creatingIds, trId);
				} else {
					creatingIds.push(trId);
				}
			}
			else {
				// Iterate over creating rows and get creating row IDs
				jQuery.each(oCreatingRows, function(key, value) {
					creatingIds.push(key);
				});
			}
			if (creatingIds.length === 0) {
				return false; // nothing to do
			}

			// show processing
			oTable.fnProcessingIndicator(true);
			
			// Prepare data for server update request
			var dataToSend = this._fnPrepareDataToCreate(creatingIds);
			
			// Prepare Request
			var jqxhr = jQuery.ajax(appCtx, {
				contentType: "application/json",
				//traditional: true,
				handleAs : "json",
				data: JSON.stringify(dataToSend),
				type: "POST",
				dataType: 'json'
			});

			// Do request to server
			jqxhr.done(jQuery.proxy(function(response) {
				var _d = this._data, oCreatingRows = _d.oCreatingRows;
				var oTable = _d.oSettings.oInstance;
				var _o = this._options;
				
				// With proxy(), 'this' refers to the object encapsulates
				// this function.
				if (response.status == 'SUCCESS') {
					var oCreateRow = oCreatingRows[trId];
					// Set original values
					var aOriginalData = oCreateRow.aOriginalData;
					var aCreatingData = oCreateRow.aCreatingData;
					jQuery.each(aOriginalData, function(index) {
						aCreatingData[index] = aOriginalData[index];
					});
					oCreateRow.aCreatingData = aCreatingData;
					var oOriginalData = oCreateRow.oOriginalData;
					var oCreatingData = oCreateRow.oCreatingData;
					jQuery.each(oOriginalData, function(key, value) {
						oCreatingData[key] = value;
					});
					oCreateRow.oCreatingData = oCreatingData;
					
					// Remove error class from row
					var nRow = this.fnGetCreationRowById(trId);
					jQuery(nRow).removeClass(this._options.classForEditingRowError);
					
					// Set original values, stored in 'aOriginalData', in each
					// cell of given row
					var $tds = jQuery(':input', nRow);
					$tds.each(function(index) {
						var $input = jQuery($tds[index]);
						var originalValue = oCreateRow.aOriginalData[index];
						if($input.prop("type") !== "checkbox"){
							$input.val(originalValue);
						}else{
							$input.prop("checked", false); // Not the same for checkbox inputs
						}
						
						// Focus on the first input
						if (index === 0) {
							$input.focus();
						}
						
						// Remove error messages from cells
						var $div = jQuery("div", $input.parent().parent());
						var $span = jQuery("span.errors", $div);
						if ($span.length) {
							$span.remove();
						}
					});

					// handle showCreatedRowFirst
					if (_o.showCreatedRowFirst && oTable.fnHasRowOnTop()) {
						// fnSetRowsOnTop will refresh the table, so we shouldn't redraw the table
						oTable.fnRowOnTop().fnSetRowsOnTop(response.oid,true);
					} else {
						// Redraw table
						oTable.fnStandingRedraw();
					}

				} else {
					
					// there are errors
					if (response.exceptionMessage){
						showMessage("Error",response.exceptionMessage); // TODO i18n
					}
					if (response.bindingResult) {
						jQuery.each(response.bindingResult,jQuery.proxy(function(index, oErrors) {
							var rowId = creatingIds[index];
							var oCreatingRow = oCreatingRows[rowId];
							oCreatingRow.oLastCreatingErrors = oErrors;
							this._fnShowBindingCreateErrors(oCreatingRow, oErrors);
						}, this));
					}
				}
			}, this))
			.always(jQuery.proxy(function () {
				// Always hide processing indicator
				this._data.oSettings.oInstance.fnProcessingIndicator(false);
				}, this))
			.fail(jQuery.proxy(function (jqXHR, textStatus, errorThrown) {
				this.error(textStatus +": " + errorThrown); // TODO i18n
				showMessage("Error updating data", textStatus +": " + errorThrown);
			}, this));

			return true;
		},

		// Callback components ---

		/**
		 * Add a beginEditing callback
		 *
		 * Callback will receive:
		 * <code>editingSupport</code> (GvNIX_Editing) instance which fires the event
		 * <code>action</code> (string) ['update','create']
		 * <code>selectedIds</code> (array) IDs of rows affected by given action if (action == 'update')
		 *
		 * @param callback
		 *            function to register
		 */
		"fnAddBeginEditingCallbacks" : function(callback) {
			var _d = this._data;
			this.debug("[fnAddBeginEditingCallbacks] " + (callback.name ? callback.name : callback ));

			_d.beginEditingCallbacks.add(callback);
		},

		/**
		 * Remove a beginEditing callback function
		 *
		 * @param callback
		 *            function to remove
		 */
		"fnRemoveBeginEditingCallback" : function(callback) {
			var _d = this._data;
			this.debug("[fnRemoveBeginEditingCallback] " + (callback.name ? callback.name : callback ));

			_d.beginEditingCallbacks.remove(callback);
		},

		/**
		 * Add a finishEditing callback
		 *
		 * Callback will receive:
		 * <code>editingSupport</code> (GvNIX_Editing) instance which fires the event
		 *
		 * @param callback
		 *            function to register
		 */
		"fnAddFinishEditingCallbacks" : function(callback) {
			var _d = this._data;
			this.debug("[fnAddFinishEditingCallbacks] " + (callback.name ? callback.name : callback ));

			_d.finishEditingCallbacks.add(callback);
		},

		/**
		 * Remove a beginEditing callback function
		 *
		 * @param callback function to remove
		 */
		"fnRemoveFinishEditingCallback" : function(callback) {
			var _d = this._data;
			this.debug("[fnRemoveFinishEditingCallback] " + (callback.name ? callback.name : callback ));

			_d.finishEditingCallbacks.remove(callback);
		},

		/**
		 * Add a beforeUpdateValue callback
		 *
		 * Callback will receive:
		 * <code>editingSupport</code> (GvNIX_Editing) instance which fires the event
		 * <code>id</code> (string) id affected by action
		 * <code>propertyName</code> property to change
		 * <code>previousValue</code> previous property value
		 * <code>newValue</code> new property value
		 *
		 * @param callback
		 *            function to register
		 */
		"fnAddBeforeUpdateValueCallbacks" : function(callback) {
			var _d = this._data;
			this.debug("[fnAddBeforeUpdateValueCallbacks] " + (callback.name ? callback.name : callback ));

			_d.beforeUpdateValueCallbacks.add(callback);
		},

		/**
		 * Remove a beforeUpdateValue callback function
		 *
		 * @param callback
		 *            function to remove
		 */
		"fnRemoveBeforeUpdateValueCallback" : function(callback) {
			var _d = this._data;
			this.debug("[fnRemoveBeforeUpdateValueCallback] " + (callback.name ? callback.name : callback ));

			_d.beforeUpdateValueCallbacks.remove(callback);
		},

		/**
		 * Add a afterUpdateValue callback
		 *
		 * Callback will receive:
		 * <code>editingSupport</code> (GvNIX_Editing) instance which fires the event
		 * <code>id</code> (string) id affected by action
		 * <code>propertyName</code> property to change
		 * <code>previousValue</code> previous property value
		 * <code>newValue</code> new property value
		 *
		 * @param callback
		 *            function to register
		 */
		"fnAddAfterUpdateValueCallbacks" : function(callback) {
			var _d = this._data;
			this.debug("[fnAddAfterUpdateValueCallbacks] " + (callback.name ? callback.name : callback ));

			_d.afterUpdateValueCallbacks.add(callback);
		},

		/**
		 * Remove a afterUpdateValue callback function
		 *
		 * @param callback
		 *            function to remove
		 */
		"fnRemoveAfterUpdateValueCallbacks" : function(callback) {
			var _d = this._data;
			this.debug("[fnRemoveAfterUpdateValueCallbacks] " + (callback.name ? callback.name : callback ));

			_d.afterUpdateValueCallbacks.remove(callback);
		},

		// UI components ---
		
		/**
		 * This method sets Datatable as no editable
		 * hidding all editable elements
		 * 
		 * <b>Note:</b> Not works on Datatable
		 * mode show
		 * 
		 */
		"fnSetNoEditableDatatable" : function(delay) {
			var oTable = this._data.oSettings.oInstance;
			var eTable = oTable.fnEditing();
			// Used to works with DOM and AJAX Datatables
			if(delay){
				oTable.on('draw',function(){
					eTable.fnHideToolbar();
					eTable.fnHideUtils();
					eTable.fnHideDetails();
				});
			}else{
				eTable.fnHideToolbar();
				eTable.fnHideDetails();
			}
		},

		/**
		 * Show editing buttons in given toolbar panel
		 */
		"fnInitEditingTools": function () {
			var sTableId = this._data.oSettings.sTableId;
			var oOpts = this._options;
			var sToolbarId = oOpts.toolbarId;
			var _d = this._data;

			// Toolbar ---
            var $toolbar = jQuery( '#' + sToolbarId );

            // If toolbar does not exist, create it
            if($toolbar.length == 0) {
            	jQuery('#' + sTableId + '_wrapper')
            		.prepend('<div id="' + sToolbarId
            				+ '" class="gvnix_dataTables_toolbar"></div>');
            	$toolbar = jQuery( '#' + sToolbarId );
            }

			// --- Update tools ---
			if (oOpts.update) {
				var sUpdateBtnId = sToolbarId + '_update_btn';
				var updateBtn = '<a href="#" id="' + sUpdateBtnId + '"'
						+ ' alt="' + oOpts.updateBtnLabel + '"' + ' title="'
						+ oOpts.updateBtnLabel + '" class="'
						+ oOpts.updateBtnClass + '">' + '</a>';
				var sSubmitBtnId = sToolbarId + '_submit_btn';
				var submitBtn = '<a href="#" id="' + sSubmitBtnId + '"'
						+ ' alt="' + oOpts.submitBtnLabel + '"' + ' title="'
						+ oOpts.submitBtnLabel + '" class="'
						+ oOpts.submitBtnClass + '">' + '</a>';
				var sCancelBtnId = sToolbarId + '_cancel_btn';
				var cancelBtn = '<a href="#" id="' + sCancelBtnId + '"'
						+ ' alt="' + oOpts.cancelBtnLabel + '"' + ' title="'
						+ oOpts.cancelBtnLabel + '" class="'
						+ oOpts.cancelBtnClass + '">' + '</a>';

				_d.$submitButton = jQuery($toolbar.append(submitBtn).find(
						'#' + sSubmitBtnId));
				_d.$cancelButton = jQuery($toolbar.append(cancelBtn).find(
						'#' + sCancelBtnId));
				$toolbar.append(updateBtn);

				// Press btn handlers
				// Note inside handlers we cannot use 'this' to refer to
				// GvNIX_Editing instance because the handler are in
				// context of controls created above, updateBtn, submitBtn, etc
				jQuery('#' + sSubmitBtnId).click( function() {
					jQuery('#' + sTableId).dataTable().fnEditing().fnFinishEditingAll();
					return false;
				});
				jQuery('#' + sCancelBtnId).click( function() {
					jQuery('#' + sTableId).dataTable().fnEditing().fnCancelAll();
					return false;
				});
				jQuery('#' + sUpdateBtnId).click( function() {
					jQuery('#' + sTableId).dataTable().fnEditing().fnEditRows();
					return false;
				});
			}

			// --- Create tools ---
			if (oOpts.create) {
				// TODO
			}

			// --- Delete tools ---
			if (oOpts['delete']) {
				// TODO
			}

			this.fnUpdateEditingTools();
		},

		/**
		 * Hide editing tools panel
		 */
		"fnHideEditingTools" : function() {
			this._data.$submitButton.hide();
			this._data.$cancelButton.hide();
		},

		/**
		 * Update editing tools
		 */
		"fnUpdateEditingTools" : function() {
			if (this.fnHasEditingRow()) {
				this._data.$submitButton.show();
				this._data.$cancelButton.show();
			} else {
				this._data.$submitButton.hide();
				this._data.$cancelButton.hide();

			}
			this._fnUpdateInfo();
		},

		// Private methods (they are of course public in JS, but recommended as
		// private) * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * Unscape nested property chars on property names:
		 * replaces "_~~_" by "."
		 *
		 * @para propertyName to unscape
		 */
		"_fnUnscapePropertyName" : function(propertyName) {
			return propertyName.replace("_~~_",".");
		},

		/**
		 * Prepares an array of data to send to server
		 *
		 * @param trIds Array of trId to prepare
		 */
		"_fnPrepareDataToSaveRow" : function(trIds) {
			var oEditingRows = this._data.oEditingRows;

			var requestData = [];
			// For each ids
			jQuery.each(trIds, function(index, key) {
				var item = {};
				// get editedRow info
				var oEditingRow = oEditingRows[key];
				// Store original values from received form
				jQuery.each(oEditingRow.oAllItemData, function (property,value){
					item[property] = value;
				});
				// Update the edited values
				jQuery.each(oEditingRow.oEditingData, function (property,value){
					item[property] = value;
				});
				// Store item values
				requestData.push(item);
			});
			return requestData;
		},
		
		/**
		 * Prepares an array of data to send to server to create a new value
		 *
		 * @param trIds Array of trId to prepare
		 */
		"_fnPrepareDataToCreate" : function(trIds) {
			var oEditingRows = this._data.oCreatingRows;

			var requestData = [];
			// For each ids
			jQuery.each(trIds, function(index, key) {
				var item = {};
				// get editedRow info
				var oEditingRow = oEditingRows[key];
				// Store original values from received form
				jQuery.each(oEditingRow.oAllItemData, function (property, value){
					item[property] = value;
				});
				// Update the edited values
				jQuery.each(oEditingRow.oCreatingData, function (property, value){
					item[property] = value;
				});
				// Store item values
				requestData.push(item);
			});
			return requestData;
		},

		/**
		 * Show binding error of a oEditingRow
		 *
		 * @param oEditingRow
		 * @param oErrors of the refered row
		 */
		"_fnShowBindingErrors" : function(oEditingRow, oErrors) {
			var aColumnField = this._data.aColumnField;
			var fnErrorDrawer = this._options.bindingErrorDrawer;
			var nRow = this.fnGetRowById(oEditingRow.sRowId);

			jQuery(nRow).addClass(this._options.classForEditingRowError);
			var $tds = jQuery('>td', nRow);
			jQuery.each(oErrors, jQuery.proxy(function (property, errorMessage) {
				var colIndex = aColumnField.indexOf(property);
				if (colIndex > -1) {
					if (fnErrorDrawer) {
						try {
							fnErrorDrawer(this,$tds[colIndex], property,  errorMessage);
						} catch (e) {
							this.error("[_fnShowBindingErrors] error calling configured binding-error drawer: { property:'"+  property + "' errorMessage: '" + errorMessage +"'} exception: "+ e);
						}
					} else {
						var $div = jQuery("div", $tds[colIndex]);
						var $span = jQuery("span.errors", $div);
						if (!$span.length) {
							$div.append('<span class="errors">' + errorMessage + '</span>');
						} else {
							$span.html(errorMessage);
						}
					}
				} else {
					this.error("[_fnShowBindingErrors] find binding error of a proprerty ("+ property+ ") which has no column on table: '" + errorMessage+ "'");
				}
			}, this));
		},

		/**
		 * Show binding error of a oCreatingRow
		 *
		 * @param oCreatingRow
		 * @param oErrors of the refered row
		 */
		"_fnShowBindingCreateErrors" : function(oCreatingRow, oErrors) {
			var aCreateColumnField = this._data.aCreateColumnField;
			var fnErrorDrawer = this._options.bindingErrorDrawer;
			var nRow = this.fnGetCreationRowById(oCreatingRow.sRowId);

			jQuery(nRow).addClass(this._options.classForEditingRowError);
			var $tds = jQuery('>td', nRow);
			
			// Remove previous error messages
			$tds.each(function(index) {
				var $div = jQuery("div", $tds[index]);
				var $span = jQuery("span.errors", $div);
				if ($span.length) {
					$span.remove();
				}
			});

			// Put error messages in the right cells
			jQuery.each(oErrors, jQuery.proxy(function (property, errorMessage) {
				if (errorMessage instanceof Object){
					prefix = property;					
					var properties = Object.keys(errorMessage);
					jQuery.each(properties, jQuery.proxy(function (index, propertyOfErrorMessage) {
						var message = errorMessage[propertyOfErrorMessage];
						this._fnPutErrorMessages(propertyOfErrorMessage, message, prefix, aColumnField, fnErrorDrawer, $tds);	
					}, this));									
				}else{
					this._fnPutErrorMessages(property, errorMessage, null, aColumnField, fnErrorDrawer, $tds);
				}
			}, this));
		},
		
        /**
         * Put error messages in the right cells
         *
         * @param property that has the error.
         * @param errorMessage of the refered row.
         * @param prefix class to which it belongs if it comes from an embedded in another class.
         * @param columnField column fields.
         * @param fnErrorDrawer __options.bindingErrorDrawer (See bindingErrorDrawer).
         * @param $tds editing row td list (jQuery Objects).
         */
        "_fnPutErrorMessages" : function(property, errorMessage, prefix, columnField, fnErrorDrawer, $tds) {                        
            if (prefix != null){
                    var propertyAux = prefix.concat("_~~_").concat(property);
                    property = prefix.concat(".").concat(property);                                
            }
            // Put error messages in the right cells
            var colIndex = columnField.indexOf(property);
            if (colIndex < 0){
                    colIndex = columnField.indexOf(propertyAux);
            }
            if (colIndex > -1) {
                    if (fnErrorDrawer) {
                            try {
                                    fnErrorDrawer(this,$tds[colIndex], property,  errorMessage);
                            } catch (e) {
                                    this.error("[_fnShowBindingCreateErrors] error calling configured binding-error drawer: { property: '"+  property + "' errorMessage: '" + errorMessage +"'} exception: "+ e);
                            }
                    } else {
                            var $div = jQuery("div", $tds[colIndex]);
                            var $span = jQuery("span.errors", $div);
                            if (!$span.length) {
                                    $div.append('<span class="errors">' + errorMessage + '</span>');
                            } else {
                                    $span.html(errorMessage);
                            }
                    }
            } else {
                    this.error("[_fnShowBindingErrors] find binding error of a proprerty ("+ property+ ") which has no column on table: '" + errorMessage+ "'");
            }
    	},

        


		/**
		 * For each cell which was edited in given row or editing rows, replaces
		 * the edit control that we inserted in the _fnUpdateRowTr function with
		 * the original cell value stored in _data.oEditingRows[ROW_ID].aOriginalData
		 *
		 * @param nRow The TR node for the row to be edited
		 */
		"_fnCancelRow" : function(nRow) {
			var oEditingRows = this._data.oEditingRows;
			var oTable = this._data.oSettings.oInstance;

			if (jQuery.isEmptyObject(oEditingRows)) {
				return;
			}

			var oEditingRow = oEditingRows[nRow.id];

			delete oEditingRows[nRow.id];

			if (jQuery.isEmptyObject(oEditingRow)) {
				this.error("[_fnCancelRow] oEditingRow for row '" + nRow.id + "' must not be null.");
				return;
			}

			// Set original value, stored in 'aOriginalData', in each cell of given row
			var $tds = jQuery('>td', nRow);
			$tds.each(function(index) {
				$tds[index].innerHTML = oEditingRow.aOriginalData[index];
			});

			this._fnCleanRowMarks(nRow);
		},

		/**
		 * Informs if trId is editing
		 *
		 * @param trId tr.id value (could be DT_RowId on AJAX mode)
		 */
		"_fnIdIsEditing" : function(trId) {
			return this._data.oEditingRows[trId] != null;
		},

		/**
		 * Register a Datatables fnRowDrawCallback
		 *
		 * This registers a function on datatable to draw row.
		 */
		"_fnRegisterDrawEditRowCallback" : function() {

			var that = this, st = this._data.oSettings;

			st.oApi._fnCallbackReg(st, 'aoRowCallback', function(
					nRow, aData, iDisplayIndex) {
				// Locate Id
				var rowId = nRow.id;
				if (!rowId) {
					// Check for DT_RowId in data
					rowId = aData.DT_RowId;
				}
				if (!rowId) {
					throw "Can't get Row id for manage in-line update!!";
				}

				// update row content if it's in editing mode
				that._fnUpdateRowTr(nRow);

				// Check if it's already bind click event
				that._fnBindRowEvents(nRow);

			}, 'manageInlineEditing');
		},

		/**
		 * Datatables fnInfoCallback
		 *
		 * This function registers a callback on datatables to draw the table
		 * to add 'infoMessage' to datatable info label.
		 *
		 * This concatenate to the end of 'sPre' (message generated by
		 * datatables or custom user function) the value of 'infoMessage' after
		 * replacing '_EDT-VISIBLE-COUNT_' and '_EDT-COUNT_' variables.
		 */
		"_fnRegisterEditingInfoCallback" : function() {

			var st = this._data.oSettings;

			if (!this._options.infoMessage) {
				return;
			}
			if (st.oLanguage.fnInfoCallback !== null) {
				this._data.user_fnInfoCallBack = st.oLanguage.fnInfoCallback;
			}

			st.oLanguage.fnInfoCallback = jQuery.proxy(function(oSettings,
					iStart, iEnd, iMax, iTotal, sPre) {
				var usr_callbck = this._data.user_fnInfoCallBack;

				if (usr_callbck) {
					sPre = usr_callbck.call(oSettings.oInstance, oSettings,
							iStart, iEnd, iMax, iTotal, sPre);
				}
				var iEdCount = this.fnInlineEditingCount();
				var message = this._options.infoMessage;
				if (message && iEdCount) {
					sPre = sPre
							+ message.replace(/_EDT-VISIBLE-COUNT_/g,
									this.fnVisibleRowsEditing()).replace(
									/_EDT-COUNT_/g, iEdCount);
				}
				return sPre;
			}, this);
		},

		/**
		 * Update datatables information message if its needed
		 */
		"_fnUpdateInfo" : function() {
			if (this._options.infoMessage) {
				this._data.oSettings.oApi._fnUpdateInfo(this._data.oSettings);
			}
		},

		/**
		 * Update graphically a row (node)
		 *
		 * If row is in editing mode this loads the row contents based on
		 * the information stored in _data.oEditingRows.
		 *
		 * @param nRow
		 */
		"_fnUpdateRowTr" : function(nRow) {
			var oEditingRows = this._data.oEditingRows;

			if (jQuery.isEmptyObject(oEditingRows)) {
				return;
			}

			var $tds = jQuery('>td', nRow);
			var oEditingRow = oEditingRows[nRow.id];
			if (!oEditingRow) {
				return;
			}

			this.debug("[_fnUpdateRowTr] redraw row '" + nRow.id + "'.");
			if (jQuery.isEmptyObject(oEditingRow)) {
				this.error("[_fnUpdateRowTr] oEditingRow for row '" + nRow.id + "' must not be null.");
				return;
			}

			$tds.each(function(index) {
				var sHtml = oEditingRow.aEditingControls[index];
				if (!sHtml) {
					// ignore cell
					return;
				}
				if (!nRow.initialized_editing_controls) {
					$tds[index].innerHTML = sHtml;
					jQueryInitializeComponents($tds[index]);
				}
				jQuery(":input",$tds[index]).val(oEditingRow.aEditingData[index]);
			});
			jQuery(nRow).addClass(this._options.classForEditingRow);
			nRow.initialized_editing_controls = true;

			var oErrors = oEditingRow.oLastEditingErrors;
			if (!jQuery.isEmptyObject(oErrors)) {
				this._fnShowBindingErrors(oEditingRow, oErrors);
			}
			this._fnBindRowEvents(nRow);
		},

		"_fnLoadNewRowData" : function(sRowId, value) {
			var oEditingRows = this._data.oEditingRows;
			var aProperties = this._data.aColumnField;

			var nRow = this.fnGetRowById(sRowId);
			if (!nRow) {
				// Row not loaded: nothing to do
				return;
			}

			var $tds = jQuery('>td', nRow);
			var oEditingRow = oEditingRows[nRow.id];

			if (jQuery.isEmptyObject(value)) {
				this.error("[_fnLoadNewRowData] value for row '" + nRow.id + "' must not be null.");
				return;
			}

			$tds.each(function(index) {
				var property = aProperties[index];
				var $td = jQuery($tds[index]);
				if (property) {
					$td.html(value[aProperties[index]]);
				} else {
					// if not a property column use original data
					$td.html(oEditingRow.aOriginalData[index]);
				}
			});

			this._fnCleanRowMarks(nRow);
		},

		/**
		 * Clean al editing row marks (css, and node attributes.
		 *
		 * @param nRow
		 *            row to clean
		 */
		"_fnCleanRowMarks" : function(nRow) {
			// Remove binding mark
			delete nRow.editing_binded;
			delete nRow.initialized_editing_controls;

			// Remove editing css classes
			var _o = this._options;
			var $Row = jQuery(nRow);
			$Row.removeClass(_o.classForEditingRow);
			$Row.removeClass(_o.classForEditingRowError);
		},

		/**
		 * Bind double-click event of a row to toggle select action
		 *
		 * @param nRow
		 */
		"_fnBindDoubleClickEvent" : function(nRow) {

			if (!nRow) {
				// nothing to do
				return;
			}

			var that = this, s = this._options;

			if (nRow.dblclick_binded) {
				// already binded
				return;
			}

			// bind row click
			jQuery(nRow).on('dblclick', function() {
				that.fnEditRows(this.id);
			});

			nRow.dblclick_binded = true;
		},

		/**
		 * Bind events required to manage a row editing.
		 *
		 * The most important is register 'onChange' callback
		 * on every input.
		 *
		 * @param nRow
		 *            (node)
		 */
		"_fnBindRowEvents" : function(nRow) {
			var _d = this._data;
			if (!nRow) {
				// nothing to do
				return;
			}

			var rowId = nRow.id;
			if (_d.oEditingRows[rowId] == null) {
				// No edited row: do nothing
				return;
			}

			if (nRow.editing_binded) {
				// already binded
				return;
			}

			// Iterate over row TDs to be sure index (i) match the column index
			var anTd = jQuery('>td', nRow);
			for ( var i = 0; i < anTd.length; i++) {
				var property = _d.aColumnField[i];

				// ignore non-property cells
				if (!property) {
					continue;
				}

				var $input = jQuery(':input', anTd[i]);
				$input.change( /* Event params*/ {'colIdx': i, 'rowId': rowId, 'property' : property},
						jQuery.proxy( function(event){
					var _d = this._data;

					// prepare values needed
					var oEventParams = event.data;
					var $input = jQuery(event.currentTarget);
					var value = $input.val(); // Use .val() to get new value
					var property = oEventParams.property;
					var oEditRow = _d.oEditingRows[oEventParams.rowId];
					var previousValue = oEditRow.oEditingData[property];
					var rowId = oEventParams.rowId;

					// Clone the callbacks instances to call it
							var beforeCallbaks = jQuery.extend(true, {}, _d.beforeUpdateValueCallbacks);
							var afterCallbaks = jQuery.extend(true, {},_d.afterUpdateValueCallbacks);
					try {
						// Call beforeUpdateValue callbacks
						// parameters: this, rowId, propertyName, previousValue, newvalue
						beforeCallbaks.fireWith(this, [ this, rowId, property, previousValue, value]);

						// beforeCallbacks doesn't throw any error, update values
						this.debug("[inputChange] {" + rowId + "[" + property + "]} <-- '"+ value + "'");
						oEditRow.aEditingData[oEventParams.colIdx] = value;
						oEditRow.oEditingData[property] = value;

						// Call afterUpdateValue
						// parameters: this, rowId, propertyName, previousValue, newvalue
						afterCallbaks.fireWith(this, [ this, rowId, property, previousValue, value]);

						oEventParams.isDirty = true;
					} catch (e) {
						// calbacks throw event, cancel change
						$input.val(previousValue);
						this.debug("[inputChange]  Canceled cell change {" + rowId + "[" + property + "]} <-- '"+ value + "' : exception : " + e);
					}

					return;
				}, /* proxy context */this));
			}
			nRow.editing_binded = true;
		},

		/**
		 * Bind events required to manage a row creating.
		 *
		 * The most important is register 'onChange' callback
		 * on every input.
		 *
		 * @param nRow
		 *            (node)
		 */
		"_fnBindCreateRowEvents" : function(nRow) {
			var _d = this._data;
			if (!nRow) {
				// nothing to do
				return;
			}

			var rowId = nRow.id;
			if (_d.oCreatingRows[rowId] == null) {
				// No created row: do nothing
				return;
			}

			// Iterate over row TDs to be sure index (i) match the column index
			var anTd = jQuery('>td', nRow);
			for (var i = 0; i < anTd.length; i++) {
				var property = _d.aCreateColumnField[i];

				// ignore non-property cells
				if (!property) {
					continue;
				}

				var $input = jQuery(':input', anTd[i]);
				
				// Focus cursor
				/*if (i == 0) {
					$input.focus();
				}*/
				
				$input.change( /* Event params*/ {'colIdx': i, 'rowId': rowId, 'property' : property},
						jQuery.proxy( function(event){
					var _d = this._data;
					// prepare values needed
					var oEventParams = event.data;
					var $input = jQuery(event.currentTarget);
					if($input.prop("type") !== "checkbox"){
						var value = $input.val(); // Use .val() to get new value
					}else{
						var value = $input.prop("checked"); // val is not correct for checkbox inputs
					}
					var property = oEventParams.property;
					var oCreatRow = _d.oCreatingRows[oEventParams.rowId];
					var previousValue = oCreatRow.oCreatingData[property];
					var rowId = oEventParams.rowId;
					try {
						this.debug("[inputChange] {" + rowId + " [" + property + "]} <-- '"+ value + "'");
						oCreatRow.aCreatingData[oEventParams.colIdx] = value;
						oCreatRow.oCreatingData[property] = value;
						oEventParams.isDirty = true;
					} catch (e) {
						// calbacks throw event, cancel change
						$input.val(previousValue);
						this.debug("[inputChange]  Canceled cell change {" + rowId + "[" + property + "]} <-- '"+ value + "' : exception : " + e);
					}
					return;
				}, this));
			}
		},

		/**
		 * Initialize component
		 */
		"_fnConstruct" : function(iSettings) {
			var opt = this._options, _d = this._data, oSettings = _d.oSettings;

			// Initialize settings
			if (typeof iSettings == "object") {

				jQuery.extend(opt, iSettings);

				if (opt.debug) {

					// Register a callback to log the event fire
					_d.beginEditingCallbacks.add( function (editing, action, id){
						editing.debug("[Begin inline editing] " + action + (id ? "ID: '" +id + "'" : ""));
					});
					_d.finishEditingCallbacks.add(function(editing) {
						editing.debug("[Finish inline editing]");
					});
					_d.beforeUpdateValueCallbacks.add(function(editing, id, propertyName, previousValue, newValue) {
						editing.debug("[Before update value in inline editing] ID: '"+ id + "' propertyName: '" + propertyName + "' value: '" + previousValue + "' --> '" + newValue + "'");
					});
					_d.afterUpdateValueCallbacks.add(function(editing, id, propertyName, previousValue, newValue) {
						editing.debug("[After update value in inline editing] ID: '" + id + "' propertyName: '" + propertyName + "' value: '" + previousValue + "' --> '" + newValue + "'");
					});
				}
				// TODO load callbacks from settings ??
			}

			// Initialize variables to store data
			_d.oEditingRows = {};

			_d.aColumnField = [];
			$thead = jQuery(oSettings.nTHead);
			$thead.find("th").each(function(index, value) {
				var $th = jQuery(value);
				var property = $th.data("property");

				_d.aColumnField[index] = property;

				var i = index;
			});

			// Register click on current rows
			var aoData = oSettings.aoData;
			for ( var i = 0; i < aoData.length; i++) {
				var nRow = aoData[i].nTr;
				this._fnBindRowEvents(nRow);
				// this._fnBindDoubleClickEvent(nRow);
			}

			// Register Row callback without remove user configuration
			this._fnRegisterDrawEditRowCallback();

			// Register callback to update info label
			this._fnRegisterEditingInfoCallback();

			// Initialize tools
			this.fnInitEditingTools();

			// Update visible rows
			this.fnRedrawVisibleRows();
		}

	};

	// Static variables * * * * * * * * * * * * * * * * * * * * * * * * * * *

	/**
	 * Store of all instances that have been created of this plugin, so one can
	 * look up other (when there is need of a master)
	 *
	 * @property _aInstances
	 * @type Array
	 * @default []
	 * @private
	 */
	GvNIX_Editing._aInstances = [];

	// Static methods * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

	/**
	 * Store new GvNIX_Editing instance.
	 * Also register pending callback.
	 * @method _fnAddInstance
	 * @static
	 */
	GvNIX_Editing._fnAddInstance = function ( instance )
	{
		GvNIX_Editing._aInstances.push(instance);
	};

	/**
	 * Get the instance for a table node (or id if a string is given)
	 * @method fnGetInstance
	 * @returns {Object} ID of table OR table node, for which we want the GvNIX_Seletion instance
	 * @static
	 */
	GvNIX_Editing.fnGetInstance = function(node) {
		if (typeof node != 'object') {
			node = jQuery("#" + node);
		}

		if ($.fn.DataTable.fnIsDataTable(node)) {
			throw "Datatable not found: " + node;
		}
		var dt = node.dataTable();

		for ( var i=0, iLen=GvNIX_Editing._aInstances.length ; i<iLen ; i++ )
		{
			if ( GvNIX_Editing._aInstances[i]._data.dt == dt )
			{
				return GvNIX_Editing._aInstances[i];
			}
		}
		return null;
	};

	/**
	 * Template object for the way in which plugin holds information about
	 * created/edited row.
	 * @namespace
	 */
	GvNIX_Editing.models = {};
	GvNIX_Editing.models.oEditingRow = {

		/**
		 * Hold the row identifier
		 * 
		 * @type integer
		 * @default null
		 */
		"iRowIndex" : null,

		/**
		 * Hold the row identifier
		 * 
		 * @type string
		 * @default null
		 */
		"sRowId" : null,

		/**
		 * Informs if any row data has been modified.
		 * 
		 * @type boolean
		 * @default false
		 */
		"dirty" : false,

		/**
		 * Data from the original data source for the row. This is an array of
		 * values with one element for each column.
		 * 
		 * Use this values to cancel editing, that is, on cancel just put this
		 * values in given row.
		 * 
		 * @type array
		 * @default empty
		 */
		"aOriginalData" : [],

		/**
		 * Data being edited. This is an array of values with one element for
		 * each column.
		 * 
		 * @type array
		 * @default empty
		 */
		"aEditingData" : [],

		/**
		 * Data being edited. This is a Map of values with one element for
		 * entity property.
		 * 
		 * @type object
		 * @default empty
		 */
		"oEditingData" : {},

		/**
		 * Errors found on the last save request
		 * 
		 * @type object
		 * @default empty
		 */
		"oLastEditingErrors" : {},

		/**
		 * Editing components. This is an array of HTML components with one
		 * element for each column.
		 * 
		 * @type array
		 * @default empty
		 */
		"aEditingControls" : [],

		/**
		 * All data received from server about element. This includes
		 * non-visible columns which are required to update a item (by example
		 * version field). This values will be added to update server request.
		 */
		"oAllItemData" : {},
	};
	GvNIX_Editing.models.oCreatingRow = {

		/**
		 * Hold the row identifier
		 * 
		 * @type string
		 * @default null
		 */
		"sRowId" : null,

		/**
		 * Data from the original data source for the row. This is an array of
		 * values with one element for each column.
		 * 
		 * Use this values to redraw the create table.
		 * 
		 * @type array
		 * @default empty
		 */
		"aOriginalData" : [],

		/**
		 * Data being created. This is an array of values with one element for
		 * each column.
		 * 
		 * @type array
		 * @default empty
		 */
		"aCreatingData" : [],

		/**
		 * Data from the original data source for the row. This is a Map of
		 * values with one element for entity property.
		 * 
		 * @type object
		 * @default empty
		 */
		"oOriginalData" : {},

		/**
		 * Data being created. This is a Map of values with one element for
		 * entity property.
		 * 
		 * @type object
		 * @default empty
		 */
		"oCreatingData" : {},

		/**
		 * Errors found on the last save request
		 * 
		 * @type object
		 * @default empty
		 */
		"oLastCreatingErrors" : {},

		/**
		 * All data received from server about element. This includes
		 * non-visible columns which are required to create a item (by example
		 * version field). This values will be added to create server request.
		 */
		"oAllItemData" : {},
	};

	/**
	 * Name of this class
	 * @constant CLASS
	 * @type String
	 * @default GvNIX_Editing
	 */
	GvNIX_Editing.prototype.CLASS = "GvNIX_Editing";

	/**
	 * gvNIX version
	 * @constant VERSION
	 * @type String
	 * @default See code
	 */
	GvNIX_Editing.VERSION = "1.3.1-RELEASE";
	GvNIX_Editing.prototype.VERSION = GvNIX_Editing.VERSION;

	/** TODO Add as datatable feature * */

})(jQuery, window, document);

/** *********************************************** */

/**
 * Gets/initialize gvNIX editing support on a datatables
 *
 * @param oSettings
 * @param iSettings
 * @return GvNIX_Editing object
 * @author gvNIX Team
 */
jQuery.fn.dataTableExt.oApi.fnEditing = function(oSettings, iSettings) {

	// TODO: Inicializar toolbar, etc from here

	var editing = oSettings.GvNIX_Editing_support;

	if (oSettings.GvNIX_Editing_support === undefined) {
		editing = new GvNIX_Editing(oSettings, iSettings);
	}
	else {
		// TODO adjust settings on already initialized selection support
	}

	oSettings.GvNIX_Editing_support = editing;

	return editing;
};

/**
*
* Checks if gvnix Editing support is initialized on a datatables
*
* @param oSettings
* @param iSettings
* @return boolean
* @author gvNIX Team
*/
jQuery.fn.dataTableExt.oApi.fnHasEditing = function(oSettings,
		iSettings) {
	
	if (!oSettings) {
		return false;
	}
	
	var editing = oSettings.GvNIX_Editing_support;
	
	if (editing === undefined) {
		return false;
	} else {
		return true;
	}
};

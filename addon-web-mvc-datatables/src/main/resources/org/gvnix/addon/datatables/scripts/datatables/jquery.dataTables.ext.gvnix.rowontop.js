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

/* Global scope for GvNIX_RowOnTop */
var GvNIX_RowOnTop;

(function(jQuery, window, document) {

	GvNIX_RowOnTop = function(oSettings, oOpts) {

		// Santiy check that we are a new instance
		if (!this instanceof GvNIX_RowOnTop) {
			alert("Warning: GvNIX_RowOnTop must be initialised with the keyword 'new'");
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
			 * <tr> on all row-on-top
			 *
			 * @property classForRowOnTop
			 * @type string
			 * @default 'row_on_top'
			 */
			"classForRowOnTop" : 'row_on_top',

			/**
			 * Do a row-ClickShow on last row-on-top
			 * @property classForClickedRow
			 * @type string
			 * @default 'row_selected'
			 */
			"doRowClickOnCreatedRow": true

		};

		/**
		 * @namespace Settings object which contains customizable information
		 *            for every plugin instance
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

		GvNIX_RowOnTop._fnAddInstance(this);

		return this;
	};

	GvNIX_RowOnTop.prototype = {

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
		 * Informs if trId is selected
		 *
		 * @param trId
		 *            tr.id value (could be DT_RowId on AJAX mode)
		 */
		"fnIsRowOnTop" : function(trId) {
			return jQuery.inArray(trId,this._data.asRowOnTopIds) >-1;
		},

		/**
		 * Get if there any row-on-top
		 *
		 * @returns true if there is a row clicked
		 */
		"fnHasRowOnTop" : function() {
			return this._data.asRowOnTopIds && this._data.asRowOnTopIds.length;
		},

		/**
		 * Gets the ids of row-on-top
		 *
		 * @returns row-on-top id array.
		 */
		"fnGetRowOnTopIds" : function() {
			return this._data.asRowOnTopIds;
		},

		/**
		 * Set trId to show on top datatables in
		 * next draw
		 *
		 * @param atrId
		 *            tr.id value or values (could be DT_RowId on AJAX mode)
		 * @param redraw
		 *            force to redraw datatable now
		 */
		"fnSetRowsOnTop" : function(atrId, redraw) {
			var _d = this._data;
			var oTable = _d.dt.oInstance;

			if (jQuery.isArray(atrId)) {
				_d.asRowOnTopIds = this._fnToStringArray(atrId);
			} else if (!atrId) {
				_d.asRowOnTopIds = [];
			} else {
				_d.asRowOnTopIds = [atrId];
			}
			if (redraw) {
				oTable.fnStandingRedraw();
			}
		},

		// Private methods (they are of course public in JS, but recommended as
		// private) * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * Tranfroms an array of objects to a 
		 * String array
		 */
		"_fnToStringArray" : function(aObject) {
			var result = [];
			for (var i=0;i<aObject.length;i++) {
				result.push(aObject[i]+"");
			}
			return result;
		},

		/**
		 * Register a Datatables fnRowDrawCallback
		 *
		 * This registers a function on datatable to draw row.
		 *
		 */
		"_fnRegisterDrawRowCallback" : function() {

			var that = this, _d = this._data, dt = _d.dt;
			var sClassToAdd = this.s.classForRowOnTop;
			var oTable = dt.oInstance;

			dt.oApi._fnCallbackReg(dt, 'aoRowCallback', jQuery.proxy(function(nRow, aData,
					iDisplayIndex) {

				// Locate Id
				var rowId = nRow.id;
				if (!rowId) {

					// Check for DT_RowId in data
					rowId = aData.DT_RowId;
				}
				if (!rowId) {
					throw "Can't get Row id for manage row-on-top!!";
				}

				var $Row =jQuery(nRow);
				// update row style if it's a row-on-top and css class is set
				if (_d.asRowOnTopIds && _d.asRowOnTopIds.length
						&& sClassToAdd && !(that.s.doRowClickOnCreatedRow &&
						oTable.fnHasRowClick())) {
					if (jQuery.inArray(nRow.id, _d.asRowOnTopIds) >-1) {
						$Row.addClass(sClassToAdd);
					}
				}


			},this), 'rowOnTopSupport');
		},

		/**
		 * Registers datatables server-params callback to include
		 * the <code>dtt_row_on_top_ids</code> param.
		 * This allow server request handler to identify which rows
		 * must be get on top of the table.
		 */
		"_fnRegisterServerParamsCallback" : function() {

			var that = this, st = this._data.dt;

			st.oApi._fnCallbackReg(st, 'aoServerParams', function(
					aoData ) {
				var aIds = that._data.asRowOnTopIds;
				if (aIds && aIds.length){
					// Add ids to request
					for (var i=0;i<aIds.length;i++) {
					  aoData.push({ "name": "dtt_row_on_top_ids", "value": aIds[i] } );
					}
				}

			}, 'rowOnTopSupport');
		},

		/**
		 * Register datatable draw callback to clean the row-on-top value.
		 * This callback is called when datatables is already drawn.
		 */
		"_fnRegisterDrawCallback" : function() {

			var that = this, st = this._data.dt;
			var oTable = st.oInstance;

			st.oApi._fnCallbackReg(st, 'aoDrawCallback', function(
					oSettings ) {
				var aIds = that._data.asRowOnTopIds;
				if (aIds && aIds.length){
					var sLastId = aIds[aIds.length-1];
					// Clean ShowIdAsFirst
					that._data.asRowOnTopIds = [];

					if (that.s.doRowClickOnCreatedRow &&
							oTable.fnHasRowClick()) {
						oTable.fnRowClick().fnSetLastClicked(sLastId,true,true);
					}
				}

			}, 'rowOnTopSupport');
		},

		/**
		 * Initialize component
		 */
		"_fnConstruct" : function(iSettings) {
			var s = this.s, _d = this._data, dt = _d.dt;
			var oTable = dt.oInstance;
			var forceRedraw = false;
			// initialize settings
			if (typeof iSettings == "object") {
				if (iSettings.classForRowOnTop !== undefined) {
					s.classForRowOnTop = iSettings.classForRowOnTop;
				}
				if (iSettings.debug !== undefined) {
					s.debug = iSettings.debug;
				}
				if (iSettings.doRowClickOnCreatedRow !== undefined) {
					s.doRowClickOnCreatedRow = iSettings.doRowClickOnCreatedRow;
				}

				if (iSettings.asRowOnTopIds) {
					if (jQuery.isArray(iSettings.asRowOnTopIds)) {
						_d.asRowOnTopIds = this._fnToStringArray(iSettings.asRowOnTopIds);
					} else {
						_d.asRowOnTopIds = [iSettings.asRowOnTopIds+""];
					}
					forceRedraw = true;
				}
			}

			// Register Row callback without remove user configuration
			this._fnRegisterDrawRowCallback();

			// Register ServerParams callback to add parameter to server request
			this._fnRegisterServerParamsCallback();

			// Register Draw callback to clean row-on-top after first draw finished
			this._fnRegisterDrawCallback();

			if (forceRedraw) {
				// Redraw the datatables
				oTable.fnStandingRedraw();
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
	GvNIX_RowOnTop._aInstances = [];

	/**
	 * Store selection callbacks which are not registed yet in instances
	 * (usually because not initialiced yet). When the refered instance is
	 * register on _aInstances the related callback will be added.
	 */
	GvNIX_RowOnTop._aCallbacksPendingToRegister = [];

	// Static methods * * * * * * * * * * * * * * * * * * * * * * * * * * * *

	/**
	 * Store new GvNIX_RowOnTop instance. Also register pending callback.
	 *
	 * @method _fnAddInstance
	 * @static
	 */
	GvNIX_RowOnTop._fnAddInstance = function(instance) {
		GvNIX_RowOnTop._aInstances.push(instance);
	};

	/**
	 * Get the instance for a table node (or id if a string is given)
	 *
	 * @method fnGetInstance
	 * @returns {Object} ID of table OR table node, for which we want the
	 *          GvNIX_Seletion instance
	 * @static
	 */
	GvNIX_RowOnTop.fnGetInstance = function(node) {
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
			for ( var i = 0, iLen = GvNIX_RowOnTop._aInstances.length; i < iLen; i++) {
				if (GvNIX_RowOnTop._aInstances[i]._data.dt.nTable == node) {
					return GvNIX_RowOnTop._aInstances[i];
				}
			}
		}
		return null;
	};

	/**
	 * Name of this class
	 *
	 * @constant CLASS
	 * @type String
	 * @default TableTools
	 */
	GvNIX_RowOnTop.prototype.CLASS = "GvNIX_RowOnTop";

	/**
	 * TableTools version
	 *
	 * @constant VERSION
	 * @type String
	 * @default See code
	 */
	GvNIX_RowOnTop.VERSION = "${gvnix.version}";
	GvNIX_RowOnTop.prototype.VERSION = GvNIX_RowOnTop.VERSION;

	/** TODO Add as datatable feature * */

})(jQuery, window, document);

/** *********************************************** */

/**
 *
 * Gets/initialize gvnix row-on-top support on a datatables
 *
 * @param oSettings
 * @param iSettings
 * @return GvNIX_RowOnTop object
 * @author gvNIX Team
 */
jQuery.fn.dataTableExt.oApi.fnRowOnTop = function(oSettings,
		iSettings) {

	var rowOnTopSupport = oSettings.GvNIX_RowOnTop_support;

	if (rowOnTopSupport === undefined) {
		rowOnTopSupport = new GvNIX_RowOnTop(oSettings, iSettings);
	} else {
		// TODO adjust settings on already initialized selection support
	}

	oSettings.GvNIX_RowOnTop_support = rowOnTopSupport;

	return rowOnTopSupport;
};

/**
*
* Checks if  gvnix RowOnTop support is initialized on a datatables
*
* @param oSettings
* @param iSettings
* @return GvNIX_RowOnTop object
* @author gvNIX Team
*/
jQuery.fn.dataTableExt.oApi.fnHasRowOnTop = function(oSettings,
		iSettings) {
	
	if (!oSettings) {
		return false;
	}

	var rowOnTopSupport = oSettings.GvNIX_RowOnTop_support;

	if (rowOnTopSupport === undefined) {
		return false;
	} else {
		return true;
	}
};
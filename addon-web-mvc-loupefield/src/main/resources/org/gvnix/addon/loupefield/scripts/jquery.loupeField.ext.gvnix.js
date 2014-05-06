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

/* Global scope for GvNIX_Loupe */
var GvNIX_Loupe;

(function(jQuery, window, document) {
	
	GvNIX_Loupe = function($aInput) {
		// Santiy check that we are a new instance
		if (!this instanceof GvNIX_Loupe) {
			alert("Warning: GvNIX_Loupe must be initialised with the keyword 'new'");
		}
		
		var inputData = $aInput.data();
		
		// Public class variables * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * Initial configuration
		 */
		this.s = {
			/**
			 * Input Id
			 */
			"id":  $aInput.attr('id'),
			
			/**
			 * Input Class
			 */
			"class": "form-control input-sm loupe_control",
			
			/**
			 * Input type
			 */
			"type": "text",
			
			/**
			 * Is disabled or not
			 */
			"disabled": $aInput.attr('disabled'),
			
			/**
			 * Entity field name
			 */
			"field": inputData.field,
			
			/**
			 * Field name
			 */
			"name": inputData.name,
			
			/**
			 * Primary Key Entty Field
			 */
			"pkfield": inputData.pkfield,
			
			/**
			 * Max results to display
			 */
			"max": inputData.max,
			
			/**
			 * Caption to show (use conversion service if null)
			 */
			"caption": inputData.caption,
			
			/**
			 * Additional Fields to search
			 */
			"additionalfields": inputData.additionalfields,
			
			/**
			 * Path where the list.jspx is
			 */
			"listpath": inputData.listpath,
			
			/**
			 * Search Button id
			 */
			"searchbuttonid": inputData.searchbuttonid,
			
			/**
			 * Initial Base Filter
			 */
			"basefilter": inputData.basefilter,
			
			/**
			 * OnSet Function
			 */
			"onset": inputData.onset,
			
			/**
			 * OnDraw Function
			 */
			"ondraw": inputData.ondraw,
			
			/**
			 * Parent item
			 */
			"parent": inputData.parent,
			
			/**
			 * List Selector id
			 */
			"listselectorid": inputData.listselectorid,
			
			/**
			 * Label
			 */
			"label": inputData.label,
			
			/**
			 * Input id
			 */
			"inputid": inputData.inputid,
			
			/**
			 * List Url
			 */
			"listurl": inputData.listurl,
			
			/**
			 * Path
			 */
			"path": inputData.path,
			
			/**
			 * Application URL
			 */
			"controllerurl": inputData.controllerurl,
			
			/**
			 * Error label id
			 */
			"idlabel": inputData.idlabel,
			
			/**
			 * Accept label message i18n
			 */
			"acceptlabel": inputData.acceptlabel,
			
			/**
			 * View More label message i18n
			 */
			"viewmorelabel": inputData.viewmorelabel
		}
		
		/**
		 * Configuration to use and replace
		 */
		this._data = {
				/**
				 * Input Id
				 */
				"id":  $aInput.attr('id'),
				
				/**
				 * Input Class
				 */
				"class": "form-control input-sm loupe_control",
				
				/**
				 * Input type
				 */
				"type": "text",
				
				/**
				 * Is disabled or not
				 */
				"disabled": $aInput.attr('disabled'),
				
				/**
				 * Entity field name
				 */
				"field": inputData.field,
				
				/**
				 * Field name
				 */
				"name": inputData.name,
				
				/**
				 * Primary Key Entty Field
				 */
				"pkfield": inputData.pkfield,
				
				/**
				 * Max results to display
				 */
				"max": inputData.max,
				
				/**
				 * Caption to show (use conversion service if null)
				 */
				"caption": inputData.caption,
				
				/**
				 * Additional Fields to search
				 */
				"additionalfields": inputData.additionalfields,
				
				/**
				 * Path where the list.jspx is
				 */
				"listpath": inputData.listpath,
				
				/**
				 * Search Button id
				 */
				"searchbuttonid": inputData.searchbuttonid,
				
				/**
				 * Initial Base Filter
				 */
				"basefilter": inputData.basefilter,
				
				/**
				 * OnSet Function
				 */
				"onset": inputData.onset,
				
				/**
				 * OnDraw Function
				 */
				"ondraw": inputData.ondraw,
				
				/**
				 * Parent item
				 */
				"parent": inputData.parent,
				
				/**
				 * List Selector id
				 */
				"listselectorid": inputData.listselectorid,
				
				/**
				 * Label
				 */
				"label": inputData.label,
				
				/**
				 * Input id
				 */
				"inputid": inputData.inputid,
				
				/**
				 * List Url
				 */
				"listurl": inputData.listurl,
				
				/**
				 * Path
				 */
				"path": inputData.path,
				
				/**
				 * Application URL
				 */
				"controllerurl": inputData.controllerurl,
				
				/**
				 * Error label id
				 */
				"idlabel": inputData.idlabel,
				
				/**
				 * Accept label message i18n
				 */
				"acceptlabel": inputData.acceptlabel,
				
				/**
				 * View More label message i18n
				 */
				"viewmorelabel": inputData.viewmorelabel
			}
		
		this.fnSettings = function(){
			return this.s;
		}
		
		// Constructor
		this._fnConstruct();
		
		GvNIX_Loupe._fnAddInstance(this);
		
		return this;
	};
	
	GvNIX_Loupe.prototype = {
			
			/**
			 * Adding callback when set id to hidden input
			 */
			"_fnSetItemCallback": jQuery.Callbacks("unique"),
			
			/**
			 * Adding callback when paints item
			 */
			"_fnDrawItemCallback": jQuery.Callbacks("unique"),
			
			/**
			 * Method to initialize Loupe Fields and create callbacks
			 */
			"_fnConstruct" : function(instance){
					var data = this._data;
					var loupeButton = $("#" + data.searchbuttonid);
					// If loupe button not exists add events
					if (loupeButton.length == 0) {
						// Creating loupe button and loupe label to show the result
						this._fnCreateItems();
						// Deleting Binding Elements and using his values
						this._fnUseBindingElementsIfNecessary();
						// Setting events in elements
						this._fnBindLoupeClickSearch();
						this._fnBindLoupeInputKeyUp();
						this._fnOnLostFocusLoupeInput();
					}
			},
			
			/**
			 * This method add event click to the loupe button that displays the datatable
			 * Dialog
			 */
			"_fnBindLoupeClickSearch" : function(){
				var instance = this;
				var data = this._data;
				var input = $("#" + data.id);
				var loupeButton = $("#" + data.searchbuttonid);
				loupeButton
						.click(function() {
							// Removing callbacks
							instance._fnSetItemCallback.empty();
							
							// Adding the div where the page is gonna be displayed if not
							// exists
							input.parent().append(
									'<div id="' + data.listselectorid
											+ '" style="display:none;"></div>');
							// Creating dialog, but not oppening
							var buttonOpts = {};
							buttonOpts[data.acceptlabel] = function() {
								// Getting master table inside dialog
								var table = $("#" + data.listselectorid
										+ " table[class=dataTable][id]")[0];
								var tableId = table.attributes.id.value;
								// Getting datatableInstance
								var datatableInstance = $("#" + tableId).dataTable();
								// Getting rowClick properties
								var datatableRowClick = datatableInstance.fnRowClick();
								// Getting last Clicked element
								var idLastClicked = datatableRowClick
										.fnGetLastClickedRowId();

								// Remove Dialog
								input.parent().remove("#" + data.listselectorid);
								selectorDialog.dialog('destroy');

								// Remove Errors
								$("#" + data.idlabel).html("");

								// FindById to show label
								instance._fnFindRecordById(idLastClicked, instance);
							};
							var selectorDialog = $("#" + data.listselectorid).dialog({
								autoOpen : false,
								modal : true,
								resizable : false,
								width : 800,
								height : 500,
								title : "Select " + data.label,
								buttons : buttonOpts,
								close : function(event, ui) {
									selectorDialog.dialog('close');
									input.parent().remove("#" + data.listselectorid);
									selectorDialog.dialog('destroy');
									// Hidding dropdown div
									$("#" + data.name + "_dropdown_div").html("");
								}
							});
							
							// Using base filters to filter datatable when
							// opens
							var baseFilters = instance._fnReplaceAllBaseFilters
							(data.basefilter,"||","&");

							// Adding list page to Dialog
							selectorDialog
									.load(
											data.controllerurl + "?selector&path=" + data.listpath+ "&" + baseFilters,
											function(a, e, i) {
												setTimeout(
														function() {
															var table = $("#"
																	+ data.listselectorid
																	+ " table[class=dataTable][id]")[0];
															var tableId = table.attributes.id.value;
															// Getting datatableInstance
															var datatableInstance = $(
																	"#" + tableId)
																	.dataTable();
															var editingInstance = datatableInstance
																	.fnEditing();

															// Initialize Row Click event
															datatableInstance.fnRowClick();

															// Filtering by current value
															var currentValue = input.val();
															
															datatableInstance
																	.fnFilter(currentValue);

															var isAjaxDatatable = datatableInstance
																	.fnEditing()._data.oSettings.oFeatures.bServerSide;

															// Setting Datatable as no
															// editable
															editingInstance
																	.fnSetNoEditableDatatable(isAjaxDatatable);

														}, 200);
											}).dialog('open');

							return false;

						});
			},
			
			/**
			 * This method add event keyup to the generated input to update the label with
			 * the correct data to display
			 */
			"_fnBindLoupeInputKeyUp": function(){
				// When the value changes, get the value and find by all fields
				var instance = this;
				var data = this._data;
				var input = $("#" + data.id);
				var timeOutClear = true;
				input.keyup(function(a) {
					// Hidding dropdown when press Esc
					if(a.key == "Esc"){
						setTimeout(function(){
							// Hidding dropdown div
							$("#" +data.name + "_dropdown_div").html("");
							timeOutClear = true;
						},100);
						timeOutClear = false;
					}
					if (timeOutClear) {
						setTimeout(function() {
							var text = input.val();
							if (text != "") {
								$("#" + data.idlabel).html("");
								GvNIX_Loupe.prototype._fnFindRecordByAll(text, instance);
							} else {
								$("#" + data.idlabel).html("");
								$("#" + data.name + "_dropdown_div").html('');
							}
							timeOutClear = true;
						}, 500)
						timeOutClear = false;
					}
				});
			},
			
			/** 
			 * On Los focus event
			 */
			"_fnOnLostFocusLoupeInput": function(){
				var instance = this;
				var data = this._data;
				var input = $("#" + data.id);
				input.focusout(function(e){
					setTimeout(function(){
						// Hidding dropdown div
						$("#" + data.name + "_dropdown_div").html("");
					},100);
				})
			},
			
			/**
			 * Find record using input value
			 * */
			"_fnFindRecordByAll": function(search, instance) {
				var data = instance._data;
				var input = $("#" + data.id);
				var baseFilter = {};
				if(data.basefilter != ""){
					var baseFilterParams = data.basefilter.split("||");
					for(i in baseFilterParams){
						var param = baseFilterParams[i];
						var values = param.split("=");
						baseFilter[values[0]] = values[1];
					}
				}
				var params = jQuery.extend({
						_search_ : search,
						_pkField_ : data.pkfield,
						_max_ : data.max,
						_caption_ : data.caption,
						_additionalFields_ : data.additionalfields,
						_field_ : data.field
					}, baseFilter);
				
				$.ajax({
					url : data.controllerurl + "?findUsingAjax",
					data : params,
					success : function(object) {
						instance._fnCreateDropDownList(input, object, data.pkfield, instance);
					},
					error : function(object) {
						var error = object.responseJSON[0].Error;

						$("#" + data.name + "_dropdown_div").html("");

						$("#" + data.idlabel).css("color", "red");
						$("#" + data.idlabel).html(error);
					}
				});
			},
			
			/**
			 * Find record using only id but returning all object fields
			 */
			"_fnFindRecordById": function(id, instance) {
				var data = this._data;
				var input = $("#" + data.id);
				var baseFilter = {};
				if(data.basefilter != ""){
					var baseFilterParams = data.basefilter.split("||");
					for(i in baseFilterParams){
						var param = baseFilterParams[i];
						var values = param.split("=");
						baseFilter[values[0]] = values[1];
					}
				}			
				var params = jQuery.extend({
						_id_ : id,
						_pkField_ : data.pkfield,
						_max_ : data.max,
						_caption_ : data.caption,
						_additionalFields_ : data.additionalfields,
						_field_ : data.field
					}, baseFilter);
				
				$.ajax({
					url : data.controllerurl + "?findUsingAjax",
					data : params,
					success : function(element) {
						// Getting object
						var object = element[0];
						
						// Creating callbacks
						var onSetNameFunction = data.onset; 
						
						if(onSetNameFunction !== ""){
							callbackFunctions = {};
							callbackFunctions[onSetNameFunction] = function(){
								if(typeof window[onSetNameFunction] == "function"){
									window[onSetNameFunction](input.attr('id'),instance,object);
								}else if ( window.console && console.log ){
									console.log( onSetNameFunction + "is not defined" );
								}
								
							}
							instance._fnSetItemCallback.add(callbackFunctions[onSetNameFunction]);
						}
						
						input.val(object.__caption__);
						$("#" + data.name + "_loupe_hidden").val(object[data.pkfield]);
						// Hidding dropdown div
						$("#" + data.name + "_dropdown_div").html("");
						
						// Fire Callback function
						instance._fnSetItemCallback.fire(onSetNameFunction);
						
						// Removing callbacks
						instance._fnSetItemCallback.empty();

					},
					error : function(element) {
						var error = element.responseJSON[0].Error;

						$("#" + data.name + "_dropdown_div").html("");

						$("#" + data.label).css("color", "red");
						$("#" + data.label).html(error);
						
						// Removing callbacks
						instance._fnSetItemCallback.empty();
					}
				});
			},
			
			/**
			 * Function to create loupe button and label
			 * */
			"_fnCreateItems": function createItems() {
				var data = this._data;
				var input = $("#" + data.id);
				// Adding button search if not exists
				var buttonSearch = $("#" + data.searchbuttonid);
				if (buttonSearch.length == 0) {
					input.css("width", "40%");
					$('<span style="cursor: pointer; margin-left:10px;" id="'+ data.searchbuttonid + '" class="glyphicon glyphicon-search" />').insertAfter(input);
				}
				
				// Hidding button if disabled
				if(typeof data.disabled !== "undefined"){
					$("#" + data.searchbuttonid).hide();
				}

				// Adding label errors span
				var label = $("#" + data.idlabel);
				if (label.length == 0) {
					$('<label id="' + data.idlabel + '" style="margin-left:20px;"></label>')
							.insertAfter($("#" + data.searchbuttonid));
				}

				// Adding DropDown list div

				var dropDownDiv = $("#" + data.name + "_dropdown_div");
				if (dropDownDiv.length == 0) {
					$(
							"<div style='position:absolute;width:38%;z-index:1;' id='"
									+ data.name + "_dropdown_div'></div>").insertAfter(
							$("#" + data.idlabel));
				}

				// Adding hidden input
				var hiddenInput = $("#" + data.name + "_loupe_hidden");
				if (hiddenInput.length == 0) {
					$(
							'<input id="' + data.name
									+ '_loupe_hidden" type="hidden" name="' + data.name
									+ '">').insertBefore(input);
				}
			},
			
			/**
			 * This method use binding elements to set values in Loupe fields
			 * 
			 * If not ahs value, delete field to prevent errors
			 */
			"_fnUseBindingElementsIfNecessary": function() {
				var instance = this;
				var data = this._data;
				var bindElement = $("#" + data.name + "_loupe_hidden_bind");
				if(bindElement.length > 0){
					var bindValue = bindElement.val();
					// If not has value, remove element
					if (bindValue == "") {
						bindElement.remove();
					} else {
						// Find by id in bind element
						this._fnFindRecordById(bindValue, instance)
						// Remove bind element
						bindElement.remove();
					}
				}
			},
			
			/**
			 * 
			 * This method creates DropdownList to display search results
			 */
			"_fnCreateDropDownList": function (input, data, pkField, instance) {
				
				//Cleaning callbacks
				instance._fnSetItemCallback.empty();
				instance._fnDrawItemCallback.empty();
				
				var inputData = input.data();
				var inputId = input.attr('id');
				var htmlToAdd = "";
				var dataToAdd = "";
				var viewMoreCode = inputData.viewmorelabel;
				
				// Creating callbacks
				var onSetNameFunction = inputData.onset; 
				var onDrawNameFunction = inputData.ondraw;
				callbackFunctions = {};
				if(onSetNameFunction !== ""){
					callbackFunctions[onSetNameFunction] = function(functionName, item){
						window[onSetNameFunction](input.attr('id'),instance,data[item]);
					}
					instance._fnSetItemCallback.add(callbackFunctions[onSetNameFunction]);
				}
				
				if(onDrawNameFunction !== ""){
					var itemToDraw = "";
					callbackFunctions[onDrawNameFunction] = function(functionName, item){
						itemToDraw = window[onDrawNameFunction](data[item]);
					}
					instance._fnDrawItemCallback.add(callbackFunctions[onDrawNameFunction]);
				}
				
				for (i in data) {
					var item = data[i];
					if(onDrawNameFunction !== ""){
						instance._fnDrawItemCallback.fire(onDrawNameFunction,[i]);
					}else{
						itemToDraw = "";
					}

					
					// Draw Caption by default
					var toDraw = "<span style='font-weight:bold;'>"+ item.__caption__+ "</span>";
					
					// If has draw function, draw the return of the function
					if(itemToDraw !== ""){
						toDraw = itemToDraw;
					}
					
					htmlToAdd += 
						"<div id='" + inputData.name + "_dropdown_div_itemid_" + item[pkField]+ "'"
						+ " style='background-color:#ffffff;cursor:pointer;padding:10px;border:1px solid #CCCCCC;border-top:0px;'>" 
							+ toDraw +
						"</div>";
					
					htmlToAdd += 
						"<script>" + 
							"$('#" + inputData.name + "_dropdown_div_itemid_" + item[pkField] + "').on"
								+ "('click'," + "function(e){" 
									+ "$('#" + inputId + "').val('"+ item.__caption__ + "');" 
									+ "$('#" + inputData.name+ "_dropdown_div').html('');" 
									+ "$('#" + inputData.name+ "_loupe_hidden').val(" + item[pkField] + ");" 
									+ "GvNIX_Loupe.prototype._fnSetItemCallback.fire('" + onSetNameFunction + "',['" + i +"']);"
									+ "GvNIX_Loupe.prototype._fnSetItemCallback.empty();"
									+ "});"
							+ "$('#" + inputData.name + "_dropdown_div_itemid_"+ item[pkField] + "').mouseover" +
									"(" + "function(e){"
										+ " $(this).css('background-color','#91B9DB');" + 
								"});"
							+ "$('#" + inputData.name + "_dropdown_div_itemid_"+ item[pkField] + "').mouseout" +
									"(" + "function(e){"
										+ "$(this).css('background-color','#ffffff');" 
								+ "});"
						+ "</script>";
				}
				
				// Deleting draw callbacks
				instance._fnDrawItemCallback.empty();
				
				htmlToAdd += 
						"<div id='" + inputData.name + "_dropdown_div_itemid_view_more"
						+ "' style='background-color:#ffffff;text-align:center;cursor:pointer;padding:10px;border:1px solid #CCCCCC;border-top:0px;'>" +
								"<span style='font-weight:italic-bold;text-align:center;'>"
									+ viewMoreCode + "" +
								"</span>" +
						"</div>";
				htmlToAdd += 
						"<script>" + 
							"$('#" + inputData.name+ "_dropdown_div_itemid_view_more').on" +
									"('click',function(e){" 
										+ "$('#" + inputData.searchbuttonid+ "').trigger('click');"
									 + "});" 
							+ "$('#" + inputData.name + "_dropdown_div_itemid_view_more').mouseover" +
									"(function(e){"
										+ " $(this).css('background-color','#91B9DB');" + 
									"});" 
							+ "$('#"+ inputData.name + "_dropdown_div_itemid_view_more').mouseout" +
									"(function(e){" 
										+ "$(this).css('background-color','#ffffff');" +
									"});" +
						"</script>";
				$("#" + inputData.name + "_dropdown_div").html(htmlToAdd);
			},
			
			/**
			 * Method to replace base filters ||
			 */
			"_fnReplaceAllBaseFilters" : function(text, search, newstring){
				    while (text.toString().indexOf(search) != -1)
				        text = text.toString().replace(search,newstring);
				    return text;
			},
			
			/**
			 * Method to get base filter of an instance
			 */
			"fnGetBaseFilter": function(){
				return this._data.basefilter;
			},
			
			/**
			 * Method to set base filter of an instance
			 */
			"fnSetBaseFilter": function(baseFilter){
				this._data.basefilter = baseFilter;
			},
			
			/**
			 * Method to enable or disable Loupe Field
			 */
			"fnEnableLoupe": function(enable){
				var instance = this;
				// Getting input
				var inputId = instance.s.id;
				var input = $("#" + inputId);
				// Enabling input
				input.prop('disabled', !enable);
				// Getting button
				var buttonId = instance.s.searchbuttonid;
				var button = $("#" + buttonId);
				// Showing / Hidding input
				if(enable){
					button.show();
				}else{
					button.hide();
				}
				// Cleaning value
				instance.fnCleanLoupe();
			},
			
			/**
			 * Method to set value to the instance
			 */
			"fnSetValue": function(id){
				var instance = this;
				// Getting hidden input
				var hiddenId = instance.s.field + "_loupe_hidden";
				// Setting id
				$("#" + hiddenId).val(id);
				// Searching by id
				instance._fnFindRecordById(id, instance);
			},
			
			/**
			 * Method to clean current value in loupeField
			 */
			"fnCleanLoupe": function(){
				var instance = this;
				// Getting hidden input
				var hiddenId = instance.s.field + "_loupe_hidden";
				// Setting val to null
				$("#" + hiddenId).val(null);
				// Getting input
				var inputId = instance.s.id;
				var input = $("#" + inputId);
				input.val("");
			}
			
			
	};
	
	// Static variables * * * * * * * * * * * * * * * * * * * * * * * * * * *

	/**
	 * Store of all instances that have been created of Loupe, so one can
	 * look up other (when there is need of a master)
	 *
	 * @property _aInstances
	 * @type Array
	 * @default []
	 * @private
	 */
	GvNIX_Loupe._aInstances = [];
	
	/**
	 * Function to add new instances 
	 */
	GvNIX_Loupe._fnAddInstance = function(instance){
		GvNIX_Loupe._aInstances.push(instance);
	}
	
	/**
	 * Function to get Loupe Instance
	 */
	GvNIX_Loupe.fnGetInstance = function(id){
		//Getting real id
		var compositeId = "_"+id+"_loupe_input_id";

		//Getting all instances
		var instances = GvNIX_Loupe._aInstances;
		
		// Iterating instances and returning the correct one
		for(i in instances){
			var instance = instances[i];
			var settings = instance.s;
			if(settings.id == compositeId){
				return instances[i];
			}
		}
	}
	
	
})(jQuery, window, document);

// Registering events
fnRegisterFunctionsToCallBack(function(context){
	//GvNIX_Loupe.prototype._fnConstruct(context);
	jQuery(".loupe_control", context).each(function(index) {
		new GvNIX_Loupe($(this));
	});
});
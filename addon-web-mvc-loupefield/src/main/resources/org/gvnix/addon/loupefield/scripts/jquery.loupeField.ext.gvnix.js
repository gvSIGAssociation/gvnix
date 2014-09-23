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
					//Getting sufix
					var sufix = data.id.substr(data.inputid.length);
					var loupeButton = $("#" + data.searchbuttonid + sufix);
					// If loupe button not exists add events
					if (loupeButton.length == 0) {
						// Creating loupe button and loupe label to show the result
						this._fnCreateItems(sufix);
						// Deleting Binding Elements and using his values
						this._fnUseBindingElementsIfNecessary(sufix);
						// Setting events in elements
						this._fnBindLoupeClickSearch(sufix);
						this._fnBindLoupeInputKeyUp(sufix);
						this._fnOnLostFocusLoupeInput(sufix);
					}
			},
			
			/**
			 * This method add event click to the loupe button that displays the datatable
			 * Dialog
			 */
			"_fnBindLoupeClickSearch" : function(sufix){
				var instance = this;
				var data = this._data;
				var input = $("#" + data.id);
				var loupeButton = $("#" + data.searchbuttonid + sufix);
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

								// FindById to show label
								instance._fnFindRecordById(idLastClicked, instance, sufix);
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
			"_fnBindLoupeInputKeyUp": function(sufix){
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
							$("#" +data.name + "_dropdown_div" + sufix).html("");
							timeOutClear = true;
						},100);
						timeOutClear = false;
					}
					if (timeOutClear) {
						setTimeout(function() {
							var text = input.val();
							if (text != "") {
								GvNIX_Loupe.prototype._fnFindRecordByAll(text, instance, sufix);
							} else {
								$("#" + data.name + "_dropdown_div" + sufix).html('');
								// White Background
								$("#" + data.inputid + sufix).css("background","#ffffff");
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
			"_fnOnLostFocusLoupeInput": function(sufix){
				var instance = this;
				var data = this._data;
				var input = $("#" + data.id);
				input.focusout(function(e){
					setTimeout(function(){
						// Hidding dropdown div
						$("#" + data.name + "_dropdown_div" + sufix).html("");
					},500);
				})
			},
			
			/**
			 * Find record using input value
			 * */
			"_fnFindRecordByAll": function(search, instance, sufix) {
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
						$("#" + data.inputid + sufix).css("background", "#ffffff");
						
						instance._fnCreateDropDownList(input, object, data.pkfield, instance, sufix);
					},
					error : function(object) {
						var error = object.responseJSON[0].Error;

						$("#" + data.name + "_dropdown_div" + sufix).html("");

						$("#" + data.inputid + sufix).css("background", "#FA6161");
					}
				});
			},
			
			/**
			 * Find record using only id but returning all object fields
			 */
			"_fnFindRecordById": function(id, instance, sufix) {
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
									console.log("[ERROR] : " + onSetNameFunction + " is not defined at 'loupe-callbacks.js'" );
								}
								
							}
							instance._fnSetItemCallback.add(callbackFunctions[onSetNameFunction]);
						}
						
						input.val(object.__caption__);
						$("#" + data.name + "_loupe_hidden" + sufix).val(object[data.pkfield]);
						$("#" + data.name + "_loupe_hidden" + sufix).trigger('change');
						// Hidding dropdown div
						$("#" + data.name + "_dropdown_div" + sufix).html("");
						// Background white
						$("#" + data.inputid + sufix).css("background", "#ffffff");
						
						// Fire Callback function
						instance._fnSetItemCallback.fire(onSetNameFunction);
						
						// Removing callbacks
						instance._fnSetItemCallback.empty();

					},
					error : function(element) {
						var error = element.responseJSON[0].Error;

						$("#" + data.name + "_dropdown_div").html("");

						// Removing callbacks
						instance._fnSetItemCallback.empty();
						
						// Background red
						$("#" + data.inputid + sufix).css("background", "#FA6161");
						
					}
				});
			},
			
			/**
			 * Function to create loupe button and label
			 * */
			"_fnCreateItems": function createItems(sufix) {
				var data = this._data;
				var input = $("#" + data.id);
				// Adding button search if not exists
				var buttonSearch = $("#" + data.searchbuttonid + sufix);
				if (buttonSearch.length == 0) {
					input.css("width", "80%");
					$('<span style="cursor: pointer; margin-left:10px;" id="'+ data.searchbuttonid + sufix + '" class="glyphicon glyphicon-search" />').insertAfter(input);
				}
				
				// Hidding button if disabled
				if(typeof data.disabled !== "undefined"){
					$("#" + data.searchbuttonid + sufix).hide();
				}

				// Adding DropDown list div

				var dropDownDiv = $("#" + data.name + "_dropdown_div" + sufix);
				if (dropDownDiv.length == 0) {
					$(
							"<div style='position:absolute;z-index:1;' id='"
									+ data.name + "_dropdown_div"+ sufix +"'></div>").insertAfter($("#" + data.searchbuttonid + sufix));
				}

				// Adding hidden input
				var hiddenInput = $("#" + data.name + "_loupe_hidden" + sufix);
				if (hiddenInput.length == 0) {
					$(
							'<input id="' + data.name
									+ '_loupe_hidden'+ sufix +'" type="hidden" name="' + data.name
									+ '">').insertBefore(input);
				}
				
				setTimeout(function(){
					$("#" + data.name + "_dropdown_div"+ sufix).width(input.width() + 50);
				},100);
			},
			
			/**
			 * This method use binding elements to set values in Loupe fields
			 * 
			 * If not ahs value, delete field to prevent errors
			 */
			"_fnUseBindingElementsIfNecessary": function(sufix) {
				var instance = this;
				var data = this._data;
				var bindElement = $(":input[id='"+data.name+"_loupe_hidden_bind"+sufix+"']");
				if(bindElement.length > 0){
					var bindValue = bindElement.val();
					// If not has value, remove element
					if (bindValue == "") {
						bindElement.remove();
					} else {
						// Find by id in bind element
						this._fnFindRecordById(bindValue, instance, sufix);
						// Remove bind element
						bindElement.remove();
					}
				}
			},
			
			/**
			 * 
			 * This method creates DropdownList to display search results
			 */
			"_fnCreateDropDownList": function (input, data, pkField, instance, sufix) {
				
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
						if(typeof window[onSetNameFunction] == "function"){
							window[onSetNameFunction](input.attr('id'),instance,data[item]);
						}else if ( window.console && console.log ){
							console.log("[ERROR] : " + onSetNameFunction + " is not defined at 'loupe-callbacks.js'" );
						}
					}
					instance._fnSetItemCallback.add(callbackFunctions[onSetNameFunction]);
				}
				
				if(onDrawNameFunction !== ""){
					var itemToDraw = "";
					callbackFunctions[onDrawNameFunction] = function(functionName, item){
						if(typeof window[onDrawNameFunction] == "function"){
							itemToDraw = window[onDrawNameFunction](data[item]);
						}else if ( window.console && console.log ){
							console.log("[ERROR] : " + onDrawNameFunction + " is not defined at 'loupe-callbacks.js'" );
						}
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
									+ "$('#" + inputData.name+ "_loupe_hidden"+ sufix +"').val(" + item[pkField] + ");"
									+ "$('#" + inputData.name+ "_loupe_hidden"+ sufix +"').trigger('change');"
									+ "GvNIX_Loupe.prototype._fnSetItemCallback.fire('" + onSetNameFunction + "',['" + i +"']);"
									+ "GvNIX_Loupe.prototype._fnSetItemCallback.empty();"
									+ "});"
							+ "$('#" + inputData.name + "_dropdown_div_itemid_"+ item[pkField] + "').mouseover" +
									"(" + "function(e){"
										+ " $(this).css('border-left','4px solid #428BCA');" + 
								"});"
							+ "$('#" + inputData.name + "_dropdown_div_itemid_"+ item[pkField] + "').mouseout" +
									"(" + "function(e){"
										+ "$(this).css('border-left','1px solid #CCCCCC');" 
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
										+ "$('#" + inputData.searchbuttonid + sufix + "').trigger('click');"
									 + "});" 
							+ "$('#" + inputData.name + "_dropdown_div_itemid_view_more').mouseover" +
									"(function(e){"
										+ " $(this).css('border-bottom','4px solid #428BCA');" + 
									"});" 
							+ "$('#"+ inputData.name + "_dropdown_div_itemid_view_more').mouseout" +
									"(function(e){" 
										+ "$(this).css('border-bottom','1px solid #CCCCCC');" +
									"});" +
						"</script>";
				$("#" + inputData.name + "_dropdown_div" + sufix).html(htmlToAdd);
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
				instance._fnFindRecordById(id, instance, "");
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
	 * Function to get Loupe Instance using field and current instance
	 */
	GvNIX_Loupe.fnGetInstance = function(currentFieldId, field){
		//Getting current form
		var form = $("#" + currentFieldId).closest("form");
		if(form !== null){
			// Getting related field in the current form
			var relatedField = form.find("input[data-field='"+field+"']");
			if(relatedField !== null && relatedField.data() !== null){
				// Getting related field data
				var relatedFieldData = relatedField.data();
				// Getting related field id
				var relatedFieldId = relatedFieldData.inputid;
				
				// Adding sufix
				if(currentFieldId.search("_create") !== -1){
					relatedFieldId += currentFieldId.substr(currentFieldId.search("_create"));
				}else if(currentFieldId.search("_update") !== -1){
					relatedFieldId += currentFieldId.substr(currentFieldId.search("_update"));
				}
				
				//Getting all instances
				var instances = GvNIX_Loupe._aInstances;
				
				// Iterating instances and returning the correct one
				for(i in instances){
					var instance = instances[i];
					var settings = instance.s;
					if(settings.id == relatedFieldId){
						return instances[i];
					}
				}
			}else if ( window.console && console.log ){
				console.log("[ERROR] Cannot locate loupe field '"+field+"' in current form.");
			}
		}else if ( window.console && console.log ){
			console.log("[ERROR] Can not locate current form.");
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
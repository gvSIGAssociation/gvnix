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
			"viewmorelabel": inputData.viewmorelabel,

			/**
			 * Related field
			 */
			"related": inputData.related,

			/**
			 * Name of parameter utilized to identify the related field
			 */
			"relatedparam": inputData.relatedparam,

			/**
			 * Size of modal
			 */
			"modalwidth": inputData.modalwidth,

			/**
			 * Hide elements with class utilbox on Datatables list
			 */
			"hideutilbox" : inputData.hideutilbox,

			/**
			 * OnAccept Function
			 */
			"onaccept": inputData.onaccept,

			/**
			 * Field used to search (primary element of caption)
			 */
			"searchfield": inputData.searchfield,

			/**
			 * Value of searchfield
			 */
			"searchvalue": inputData.searchvalue,

			/**
			 * Indicates if the loupe has been initialized
			 */
			"initialized": false,

			/**
			 * Additional Fields to return by ajax but don't search by them
			 */
			"returnfields": inputData.returnfields,

			/**
			 * Label of the title of the modal
			 */
			"labeltitlemodal": inputData.labeltitlemodal
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
			 * jQuery input object
			 */
			"$input" : $aInput,

			/**
			 * jQuery container object
			 */
			"$container" : $aInput.parent(),

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
				"viewmorelabel": inputData.viewmorelabel,

				/**
				 * Related field
				 */
				"related": inputData.related,

				/**
				 * Name of parameter utilized to identify the related field
				 */
				"relatedparam": inputData.relatedparam,

				/**
				 * Size of modal
				 */
				"modalwidth": inputData.modalwidth,

				/**
			 * Hide elements with class utilbox on Datatables list
				 */
			"hideutilbox" : inputData.hideutilbox,

				/**
				 * OnAccept Function
				 */
				"onaccept": inputData.onaccept,


				/**
				 * Field used to search (primary element of caption)
				 */
				"searchfield": inputData.searchfield,

				/**
				 * Value of searchfield
		         */
				"searchvalue": inputData.searchvalue,

				/**
				 * Indicates if the loupe has been initialized
				 */
				"initialized": false,

				/**
				 * Additional Fields to return by ajax but
				 * don't search by them
				 */
				"returnfields": inputData.returnfields,

				/**
				 * Label of the title of the modal
				 */
				"labeltitlemodal": inputData.labeltitlemodal,

				/**
				 * Set loupe field as required or not
				 */
			"required" : inputData.required === null ? 'false'
					: inputData.required
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
			 * Adding callback when push accept button in modal
			 */
			"_fnAcceptItemCallback": jQuery.Callbacks("unique"),

			/**
			 * Method to initialize Loupe Fields and create callbacks
			 */
			"_fnConstruct" : function(instance){
					var data = this._data;
					data.initialized = false;
					//Getting sufix
					var sufix = data.id.substr(data.inputid.length);
			var loupeButton = data.$container.find(".loupe-button");
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
						this._fnOnKeyDownLoupeInput(sufix);
					}
			},

			/**
			 * This method add event click to the loupe button that displays the datatable
			 * Dialog
			 */
			"_fnBindLoupeClickSearch" : function(sufix){
				var instance = this;
				var data = this._data;
				var $input = data.$input;
				var $container = data.$container;
				var loupeButton = $container.find(".loupe-button");
				loupeButton
						.click(function() {
							// Removing callbacks
							instance._fnSetItemCallback.empty();

						var finalListSelectorId = $input.id + '_' + data.listselectorid;
						// Adding the div where the page is gonna be displayed
						// if not
							// exists
						$container.append('<div id="'+ finalListSelectorId
								+ '" class="loupe-listselector" style="display:none;"></div>');
							// Creating dialog, but not oppening
							var buttonOpts = {};
							buttonOpts[data.acceptlabel] = function() {
								// Getting master table inside dialog
							var table = jQuery("#" + finalListSelectorId
										+ " table[class*=dataTable][id]")[0];
								var tableId = table.attributes.id.value;
								// Getting datatableInstance
							var datatableInstance = jQuery("#" + tableId)
									.dataTable();
								// Getting rowClick properties
								var datatableRowClick = datatableInstance.fnRowClick();
								// Getting last Clicked element
								var idLastClicked = datatableRowClick
										.fnGetLastClickedRowId();

								// Creating callback
								var onAcceptNameFunction = data.onaccept;

								if(onAcceptNameFunction !== ""){
									callbackFunctions = {};
									callbackFunctions[onAcceptNameFunction] = function(){
										if(typeof window[onAcceptNameFunction] == "function"){
										idLastClicked = window[onAcceptNameFunction]
												(
														datatableRowClick._data.dt.aoData,
														idLastClicked,
														data.pkfield);
									} else {
										logMessage("[ERROR] : "
														+ onAcceptNameFunction
														+ " is not defined at 'loupe-callbacks.js'");
										}

									}
									instance._fnAcceptItemCallback.add(callbackFunctions[onAcceptNameFunction]);
								}

								// Fire Callback function
								instance._fnAcceptItemCallback.fire(onAcceptNameFunction);

								// Removing callbacks
								instance._fnAcceptItemCallback.empty();

								//Clear searchvalue
								instance._data['searchvalue'] = "";

								// Remove Dialog
							jQuery("#" + finalListSelectorId).remove();

								// FindById to show label
								instance._fnFindRecordById(idLastClicked, instance, sufix);
							};
						var selectorDialog = jQuery("#" + finalListSelectorId)
								.dialog(
										{
								autoOpen : false,
								modal : true,
								resizable : false,
								width : data.modalwidth,
								height : 500,
								title : data.labeltitlemodal,
								buttons : buttonOpts,
								close : function(event, ui) {
									selectorDialog.dialog('close');
												jQuery("#" + finalListSelectorId).remove();
									// Hidding dropdown div
												$input
														.parent().find(
																".dropdown_div")
														.html("");
								}
							});
							// Using base filters to filter datatable when
							// opens
							var baseFilters = instance._fnReplaceAllBaseFilters
							(data.basefilter,"||","&");
							//Adding filters from related elements
							if(data.relatedparam != ""){
								var relatedFields = data.related.split(",");
								var relatedParams = data.relatedparam.split(",");
								for(i in relatedFields){
								var value = jQuery(
										"#_" + relatedFields[i] + "_id").val();
								baseFilters = baseFilters + "&"
										+ relatedParams[i] + "=" + value;
								}
							}

							// Adding list page to Dialog
							selectorDialog
									.load(
											data.controllerurl + "?selector&path=" + data.listpath+ "&" + baseFilters,
											function(a, e, i) {
												setTimeout(
														function() {
														var table = jQuery("#" + finalListSelectorId
																	+ " table[class*=dataTable][id]")[0];
														if (!table) {
															logMessage("[Error] datatables for selector ('" + finalListSelectorId + "') not found.");
															return;
														}
															var tableId = table.attributes.id.value;
														// Getting
														// datatableInstance
														var datatableInstance = jQuery(
																	"#" + tableId)
																	.dataTable();
															var editingInstance = datatableInstance
																	.fnEditing();

															// Initialize Row Click event
															datatableInstance.fnRowClick();

															// Filtering by current value or searchField
															var currentValue;
															if(data.searchvalue != undefined && data.searchvalue != "" && data.searchvalue != "undefined"){
																currentValue = data.searchvalue;
															}else{
															currentValue = $input
																	.val();
															}

															datatableInstance
																	.fnFilter(currentValue);


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
			var $input = data.$input;
			var $container = data.$container;
				var timeOutClear = true;
			$input.keyup(function(a) {
					// Hidding dropdown when press Esc
					if(a.key == "Esc"){
						setTimeout(function(){
							// Hidding dropdown div
						$container(".dropdown_div")
								.html("");
							timeOutClear = true;
						},500);
						timeOutClear = false;
					}
					if (timeOutClear) {
						setTimeout(function() {
							var text = $input.val();
							if (text != "") {
								GvNIX_Loupe.prototype._fnFindRecordByAll(text, instance, sufix);
							} else {
							$container.find(
									".dropdown_div")
									.html('');
								// White Background
							$container.css(
									"background", "#ffffff");
							}
							timeOutClear = true;
						}, 500)
						timeOutClear = false;
					}
				});
			},

			/**
			 * On Lost focus event
			 */
			"_fnOnLostFocusLoupeInput": function(sufix){
				var instance = this;
				var data = this._data;
				var input = data.$input;
				var container = input.parent();
				input.focusout(function(e){
					setTimeout(function(){
					// Hidding dropdown div
					container.find(".dropdown_div").html("");
					var searchValue;
					var inputValue = input.val();
					// Check for any input value
					if(inputValue.length > 0){
						// Check if search field is defined
						if(data.searchfield  && data.searchfield != ""){
							if(data.searchvalue && data.searchvalue != "" && data.searchvalue != "undefined"){
								// Get stored search value
								searchValue = data.searchvalue;
							}else{
								// Get current input value
								searchValue = inputValue;
							}
							if (searchValue && searchValue != "") {
								// Launch find by field
								instance._fnFindRecordByField(searchValue, instance, sufix, data.searchfield);
							}
						}else{
							// Getting selected Id from hidden input
							var hiddenInputs = container.find(".loupe-hiddeninput");
							if(hiddenInputs.length > 0){
								for(x = 0; x < hiddenInputs.length; x++){
									var hiddenInput = hiddenInputs[x];
									// Check if hidden input has value and is not hidden input for binding
									if(hiddenInput.value != "" && hiddenInput.id.indexOf("hidden_bind") == -1){
										// Launch find by id
										instance._fnFindRecordById(hiddenInput.value, instance, sufix);
									}
								}
							}
						}
					}else { // Input is clear
						// Clean hidden input.
						container.find(".loupe-hiddeninput").val("");
						// Reset background to white color
						input.css("background", "#ffffff");
						// Usually, hidden inputs will be present, but to prevent errors,
						// we are going to set valueInput to undefined.
						searchValue = undefined;
					}
				},500);

				})
			},

			/**
			 * On keyDown event
			 */
			"_fnOnKeyDownLoupeInput": function(sufix){
				var instance = this;
				var data = this._data;
			var input = data.$input;
				input.keydown(function(e){
					setTimeout(function(){
							//Clean search value
							data.searchvalue = "";
					},500);
				})
			},

			/**
			 * Find record using input value
			 * */
			"_fnFindRecordByAll": function(search, instance, sufix) {
				var data = instance._data;
			var input = data.$input;
				var baseFilter = {};

				//Adding filters from related elements
				if(data.relatedparam != ""){
					var relatedFields = data.related.split(",");
					var relatedParams = data.relatedparam.split(",");
					for(i in relatedFields){
					var value = jQuery("#_" + relatedFields[i] + "_id").val();
						baseFilter[relatedParams[i]] = value;
					}
				}

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
						_returnFields_ : data.returnfields,
						_field_ : data.field
					}, baseFilter);

			jQuery.ajax({
					url : data.controllerurl + "?findUsingAjax",
					data : params,
					success : function(object) {
					input.css("background",
							"#ffffff");

						instance._fnCreateDropDownList(input, object, data.pkfield, instance, sufix);
					},
					error : function(object) {
						var error = object.responseJSON[0].Error;
					var container = data.$container;
					container.find(".dropdown_div").html("");
					container.find(".loupe-hiddeninput").val("");
					input.css("background",
							"#FA6161");
					}
				});
			},

			/**
			 * Find record using only id but returning all object fields
			 */
			"_fnFindRecordById": function(id, instance, sufix) {
				var data = this._data;
			var input = data.$input;
			var container = data.$container;
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
						_returnFields_ : data.returnfields,
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
										window[onSetNameFunction](input
												.attr('id'), instance, object);
									} else {
										logMessage("[ERROR] : "
														+ onSetNameFunction
														+ " is not defined at 'loupe-callbacks.js'");
								}

							}
							instance._fnSetItemCallback.add(callbackFunctions[onSetNameFunction]);
						}
						input.val(object.__caption__);

                        var inputValue = undefined;
                        if(data.searchfield != undefined) {
                            var searchFieldSplit = data.searchfield.split(",");
                            inputValue = object[searchFieldSplit[0]];
                        }else{
                            inputValue = object.__caption__;
                        }

						instance._data['searchvalue'] = inputValue;

							var hiddeninput = data.$container.find(".loupe-hiddeninput");
							hiddeninput
									.val(object[data.pkfield]);
							hiddeninput
									.trigger('change');
						// Hidding dropdown div
							container.find(".dropdown_div")
									.html("");
						// Background white
							input.css(
									"background", "#ffffff");

						// Fire Callback function
						instance._fnSetItemCallback.fire(onSetNameFunction);

						// Removing callbacks
						instance._fnSetItemCallback.empty();

						// Set initialized true because is necessary put this
						// after first call to findById, because this variable
						// is used on _fnSetItemCallback
						data.initialized = true;

					},

					error : function(element) {
						var error = element.responseJSON[0].Error;

							container.find(".dropdown_div").html("");
							container.find(".loupe-hiddeninput").val("");

						// Removing callbacks
						instance._fnSetItemCallback.empty();

						// Background red
							input.css(
									"background", "#FA6161");

					}
				});
			},


			/**
			 * Find record by field but returning all object fields
			 */
		"_fnFindRecordByField" : function(valueInput, instance, sufix,
				searchField) {
				var data = this._data;
			var input = data.$input
			var parent = data.$container;
				var baseFilter = {};

				//Adding filters from related elements
				if(data.relatedparam != ""){

					var relatedFields = data.related.split(",");
					var relatedParams = data.relatedparam.split(",");
					for(i in relatedFields){
					var value = jQuery("#_" + relatedFields[i] + "_id").val();
						baseFilter[relatedParams[i]] = value;
					}
				}

				if(data.basefilter != ""){
					var baseFilterParams = data.basefilter.split("||");
					for(i in baseFilterParams){
						var param = baseFilterParams[i];
						var values = param.split("=");
						baseFilter[values[0]] = values[1];
					}
				}


				var pkField = data.pkfield;
				var additionalfieldsAux = data.additionalfields;
				if(searchField != ""){
                    var searchFieldSplit = searchField.split(",");
					pkField = searchFieldSplit[0];
					if(additionalfieldsAux!=""){
						additionalfieldsAux = additionalfieldsAux + "," + data.pkfield;
					}else{
						additionalfieldsAux = data.pkfield;
					}
				}

				var params = jQuery.extend({
						_id_ : valueInput,
						_pkField_ : pkField,
						_max_ : data.max,
						_caption_ : data.caption,
						_additionalFields_ : additionalfieldsAux,
						_returnFields_ : data.returnfields,
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
										window[onSetNameFunction](input
												.attr('id'), instance, object);
									} else {
										logMessage("[ERROR] : "
														+ onSetNameFunction
														+ " is not defined at 'loupe-callbacks.js'");
								}

							}
							instance._fnSetItemCallback.add(callbackFunctions[onSetNameFunction]);
						}
						input.val(object.__caption__);

                        var inputValue = undefined;
                        if(data.searchfield != undefined) {
                            var searchFieldSplit = data.searchfield.split(",");
                            inputValue = object[searchFieldSplit[0]];
                        }else{
                            inputValue = object.__caption__;
                        }

						instance._data['searchvalue'] = inputValue;

							parent.find(".loupe-hiddeninput")
									.val(object[data.pkfield]);
							parent.find(".loupe-hiddeninput")
									.trigger('change');
						// Hidding dropdown div
							parent.find(".dropdown_div")
									.html("");
						// Background white
							input.css(
									"background", "#ffffff");

						// Fire Callback function
						instance._fnSetItemCallback.fire(onSetNameFunction);

						// Removing callbacks
						instance._fnSetItemCallback.empty();

					},
					error : function(element) {
                        var error = "";
                        if(element.responseJSON != undefined || element.responseJSON != null){
                            error = element.responseJSON[0].Error;
                        }else if(element.responseText != undefined || element.responseText != null){
                            error = element.responseText;
                        }

							parent.find(".loupe-hiddeninput").val("");
							parent.find(".dropdown_div").html("");

						// Creating callbacks
						var onSetNameFunction = data.onset;

						if(onSetNameFunction !== ""){
							callbackFunctions = {};
							callbackFunctions[onSetNameFunction] = function(){
								if(typeof window[onSetNameFunction] == "function"){
							window[onSetNameFunction]();
									} else {
										logMessage("[ERROR] : "
														+ onSetNameFunction
														+ " is not defined at 'loupe-callbacks.js'");
								}

							}
							instance._fnSetItemCallback.add(callbackFunctions[onSetNameFunction]);
						}

						// Fire Callback function
						instance._fnSetItemCallback.fire(onSetNameFunction);

						// Removing callbacks
						instance._fnSetItemCallback.empty();

						// Background red
							input.css(
									"background", "#FA6161");


						//clean hidden value
						instance.fnCleanHiddenValueLoupe();

					}
				});
			},

			/**
			 * Function to create loupe button and label
			 * */
			"_fnCreateItems": function createItems(sufix) {
				var data = this._data;
			var $input = data.$input;
			var container = data.$container;

				// Adding button search if not exists
			var buttonSearch = container.find(".loupe-button");
				if (buttonSearch.length == 0) {
				buttonSearch = jQuery(
						'<span style="cursor: pointer;" id="'
								+ data.searchbuttonid
								+ sufix
								+ '" class="input-group-addon glyphicon glyphicon-search loupe-button" />')
						.insertAfter($input);
				}

				// Hidding button if disabled
				if(typeof data.disabled !== "undefined"){
				container.find(".loupe-button").hide();
				}

				// Adding DropDown list div

			var dropDownDiv = container.find(".dropdown_div");
				if (dropDownDiv.length == 0) {
				jQuery(
						"<div style='position:absolute;z-index:1;' class='dropdown_div' id='"
								+ data.name + "_dropdown_div" + sufix
								+ "'></div>").insertBefore(
										data.$container.find(".loupe-button" ));
				}

				// Adding hidden input
			var hiddenInput = container.find(".loupe-hiddeninput");
				if (hiddenInput.length == 0) {
					var class_hidden = "loupe-hiddeninput";
					if (data.required){
					class_hidden = class_hidden+ " include-to-validate";
					}

				jQuery(
						'<input id="' + data.name + '_loupe_hidden' + sufix
								+ '" type="hidden" name="' + data.name
								+ '" required="' + data.required + '" class="'
								+ class_hidden + '">').insertAfter($input);

				}

				setTimeout(function(){
				container.find(".dropdown_div").width(
						$input.width() + 50);
				},500);
			},

			/**
			 * This method use binding elements to set values in Loupe fields
			 *
			 * If not has value, delete field to prevent errors
			 */
			"_fnUseBindingElementsIfNecessary": function(sufix) {
				var instance = this;
				var data = this._data;
			var $container = data.$container;
			var bindElement = $container.find(".loupe_hidden_id");
                // Getting again with other jQuery selector
				if(bindElement.length > 0){
					var bindValue = bindElement.val();
					// If not has value, remove element
					if (bindValue == "") {
						bindElement.remove();
						// Set initialized true because is necessary put this
						// after first call to findById, because this variable
						// is used on _fnSetItemCallback
						data.initialized = true;
					} else {
						// Find by id in bind element
						this._fnFindRecordById(bindValue, instance, sufix);
						// Remove bind element
						bindElement.remove();
					}
				}else{
					// Set initialized true because is necessary put this
					// after first call to findById, because this variable
					// is used on _fnSetItemCallback
					data.initialized = true;
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
			var $input = this._data.$input;
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
						window[onSetNameFunction](input.attr('id'), instance,
								data[item]);
					} else {
						logMessage("[ERROR] : " + onSetNameFunction
								+ " is not defined at 'loupe-callbacks.js'");
						}
					}
					instance._fnSetItemCallback.add(callbackFunctions[onSetNameFunction]);
				}

				if(onDrawNameFunction !== ""){
					var itemToDraw = "";
					callbackFunctions[onDrawNameFunction] = function(functionName, item){
						if(typeof window[onDrawNameFunction] == "function"){
							itemToDraw = window[onDrawNameFunction](data[item]);
					} else {
						logMessage("[ERROR] : " + onDrawNameFunction
								+ " is not defined at 'loupe-callbacks.js'");
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

				htmlToAdd += "<div id='"
						+ inputData.name
						+ "_dropdown_div_itemid_"
						+ item[pkField]
						+ "'"
						+ " style='background-color:#ffffff;cursor:pointer;padding:10px;border:1px solid #CCCCCC;border-top:0px;'>"
						+ toDraw + "</div>";

                    var inputValue = undefined;
				if (inputData.searchfield != undefined && inputData.searchfield != "") {
                        var searchFieldSplit = inputData.searchfield.split(",");
                        inputValue = item[searchFieldSplit[0]];
                    }else{
                        inputValue = item.__caption__;
                    }

				htmlToAdd += "<script>" + "jQuery('#"
						+ inputData.name
						+ "_dropdown_div_itemid_"
						+ item[pkField]
						+ "').on"
						+ "('click',"
						+ "function(e){"
						+ "jQuery('#"
						+ inputId
						+ "').val('"
						+ item.__caption__
						+ "');"
						+ "var loupeInstance = GvNIX_Loupe.fnGetInstance('"
						+ inputId
						+ "','"
						+ inputData.field
						+ "');"
						+ "loupeInstance._data['searchvalue'] = '"
						+ inputValue
						+ "';"
						+ "jQuery('#' + loupeInstance.s.id).data().searchvalue = '"
						+ inputValue
						+ "';"
						+ "jQuery('#"
						+ inputData.name
						+ "_loupe_hidden"
						+ sufix
						+ "').val("
						+ item[pkField]
						+ ");"
						+ "jQuery('#"
						+ inputData.name
						+ "_loupe_hidden"
						+ sufix
						+ "').trigger('change');"
						+ "GvNIX_Loupe.prototype._fnSetItemCallback.fire('"
						+ onSetNameFunction
						+ "',['"
						+ i
						+ "']);"
									+ "GvNIX_Loupe.prototype._fnSetItemCallback.empty();"
									+ "});"
						+ "jQuery('#"
						+ inputData.name
						+ "_dropdown_div_itemid_"
						+ item[pkField]
						+ "').mouseover"
						+ "("
						+ "function(e){"
						+ " jQuery(this).css('border-left','4px solid #428BCA');"
								+ "});"
						+ "jQuery('#"
						+ inputData.name
						+ "_dropdown_div_itemid_"
						+ item[pkField]
						+ "').mouseout"
						+ "("
						+ "function(e){"
						+ "jQuery(this).css('border-left','1px solid #CCCCCC');"
						+ "});" + "</script>";
				}

				// Deleting draw callbacks
				instance._fnDrawItemCallback.empty();

			htmlToAdd += "<div id='"
					+ inputData.name
					+ "_dropdown_div_itemid_view_more"
					+ "' style='background-color:#ffffff;text-align:center;cursor:pointer;padding:10px;border:1px solid #CCCCCC;border-top:0px;-moz-border-radius:0 0 10px 10px;-webkit-border-radius:0 0 10px 10px;border-radius:0 0 10px 10px;'>"
					+ "<span style='font-weight:italic-bold;text-align:center;'>"
					+ viewMoreCode + "" + "</span>" + "</div>";
			htmlToAdd += "<script>" + "jQuery('#" + inputData.name
					+ "_dropdown_div_itemid_view_more').on"
					+ "('click',function(e){" + "jQuery('#"
					+ inputData.searchbuttonid + sufix + "').trigger('click');"
					+ "});" + "jQuery('#" + inputData.name
					+ "_dropdown_div_itemid_view_more').mouseover"
					+ "(function(e){"
					+ " jQuery(this).css('border-bottom','4px solid #428BCA');"
					+ "});" + "jQuery('#" + inputData.name
					+ "_dropdown_div_itemid_view_more').mouseout"
					+ "(function(e){"
					+ "jQuery(this).css('border-bottom','1px solid #CCCCCC');"
					+ "});" + "</script>";
			$input.parent().find(".dropdown_div").html(
					htmlToAdd);
				// Checks if a scroll container contains the loupe field.
				// In that case, scrolls the loupe field to show its result list.
				var container = jQuery(".dataTables_scrollBody");
			var insideContainer = container.find(input);
				if(container.length && insideContainer.length){
					container.animate({
					scrollTop : input.offset().top
							- container.offset().top + container.scrollTop()
					}, 200);
				}

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
			var input = data.$input;
				// Enabling input
				input.prop('disabled', !enable);
				// Getting button
				var buttonId = instance.s.searchbuttonid;
			var button = this._data.$container.find(".loupe-button");
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
				// Setting id
				this._data.$container.find(".loupe-hiddeninput").val(id);
				// Searching by id
				instance._fnFindRecordById(id, instance, "");
			},

			/**
			 * Method to clean current value in loupeField
			 */
			"fnCleanLoupe": function(){
				var instance = this;
				// Getting hidden input
			var input = instance._data.$input;
				// Setting val to null
			input.parent().find(".loupe-hiddeninput").val(null);
				// Getting input
				var inputId = instance.s.id;
			var input = this._data.$input;
				input.val("");
			},

			/**
			 * Method to clean current hidden value in loupeField
			 */
			"fnCleanHiddenValueLoupe": function(){
				var instance = this;
				// Getting hidden input
			var input = instance._data.$input;
				// Setting val to null
			input.parent().find(".loupe-hiddeninput").val(null);
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
		var curField = jQuery("#" + currentFieldId);
		if (curField == null || curField.length == 0) {
				logMessage("[ERROR] element '" + currentFieldId
						+ "' not found.");
		}
		var form = curField.closest("form");
		if (form == null || form.length == 0) {
			if (curField[0].form) {
				form = jQuery(curField[0].form);
			} else {
				return GvNIX_Loupe._fnGetInstance(curField);
			}
		}
		if(form !== null){
			// Getting related field in the current form
			var relatedField = form.find("input[data-field='"+field+"']");
			if(relatedField !== null && relatedField.data() !== null){
				return GvNIX_Loupe._fnGetInstance(relatedField);
			} else {
				logMessage("[ERROR] Cannot locate loupe field '" + field
						+ "' in current form.");
			}
		} else {
			logMessage("[ERROR] Can not locate current form.");
	}
	},

	/**
	 * Gets the loupe object instance for a jQuery-input object
	 *
	 * @param $input
	 * @returns
	 */
	GvNIX_Loupe._fnGetInstance = function($input) {
		// Getting related field id
		var relatedFieldId = $input.data().inputid;

		// Getting all instances
		var instances = GvNIX_Loupe._aInstances;

		// Iterating instances and returning the correct one
		var currentInstance = null;
		for (i in instances) {
			var instance = instances[i];
			var settings = instance.s;
			if (settings.id.indexOf(relatedFieldId) != -1) {
				currentInstance = instances[i];
				break;
			}
		}
		return currentInstance;
	}

})(jQuery, window, document);

/**
 * Shows message in log console (if any)
 */
function logMessage(message){
	if (window.console && console.log) {
			console.log(message);
	}
}

// Registering events
fnRegisterFunctionsToCallBack(function(context){
	//GvNIX_Loupe.prototype._fnConstruct(context);
	jQuery(".loupe_control", context).each(function(index) {
		new GvNIX_Loupe(jQuery(this));
	});
});

//Clean hide element of loupe
function cleanHiddenInputLoupe(inputId, hiddenInputId){
	var valueOfInput = jQuery("#"+inputId).val();
	if(valueOfInput == ""){
		jQuery("#"+hiddenInputId).val("");
	}
}

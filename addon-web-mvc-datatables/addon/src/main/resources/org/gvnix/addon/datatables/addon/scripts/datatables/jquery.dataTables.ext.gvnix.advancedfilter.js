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

/* Global scope for GvNIX_Advanced_Filter */
var GvNIX_Advanced_Filter;

(function(jQuery, window, document) {

	GvNIX_Advanced_Filter = function($aInput) {
		// Santiy check that we are a new instance
		if (!this instanceof GvNIX_Advanced_Filter) {
			alert("Warning: GvNIX_Advanced_Filter must be initialised with the keyword 'new'");
		}

		var inputData = $aInput.data();

		// Public class variables * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * Initial configuration
		 */
		this.s = {
				/**
				 * Input Obj
				 */
				"input":  $aInput,

                /**
				 * Datatable Parent Id
				 */
				"parentId":  inputData.dataTableId,

                /**
				 * Datatable Parent Path
				 */
				"parentPath":  inputData.dataTablePath,

				/**
				 * Last selected tab
				 */
				"tabSelectionLast": 1,

				/**
				 * Previous selected tab
				 */
				"tabSelectionPrevious": -1,

				/**
				 * Ratio button checked
				 */
				"selectedRadioOption": "",

				/**
				 * Tabs Div Root
				 */
				"tabsRoot": "",

				/**
				 * Dialog type
				 */
				"type": "",

				/**
				 *  Button Find i18n label
				 */
				"buttonFind" : "",

				/**
				 *  All is null i18n label
				 */
				"allIsNull" : "",

				/**
				 *  All not null i18n label
				 */
				"allNotNull" : "",

				/**
				 *  False i18n label
				 */
				"booleanFalse" : "",

				/**
				 *  True i18n label
				 */
				"booleanTrue" : "",

				/**
				 *  Date between i18n label
				 */
				"dateBetween" : "",

				/**
				 *  Date i18n label
				 */
				"dateDate" : "",

				/**
				 *  Day i18n label
				 */
				"dateDay" : "",

				/**
				 *  Month i18n label
				 */
				"dateMonth" : "",

				/**
				 *  Year i18n label
				 */
				"dateYear" : "",

				/**
				 *  Between i18n label
				 */
				"numberBetween" : "",

				/**
				 *  Contains i18n label
				 */
				"stringContains" : "",

				/**
				 *  Ends i18n label
				 */
				"stringEnds" : "",

				/**
				 *  Is empty i18n label
				 */
				"stringIsEmpty" : "",

				/**
				 *  Is not empty i18n label
				 */
				"stringIsNotEmpty" : "",

				/**
				 *  Starts i18n label
				 */
				"stringStarts" : "",

				/**
				 *  Date i18n Pattern
				 */
				"datePattern" : "",

				/**
				 *  All is null i18n label
				 */
				"helpAllIsNull" : "",

				/**
				 *  All not null help i18n label
				 */
				"helpAllNotNull" : "",

				/**
				 *  False help i18n label
				 */
				"helpBooleanFalse" : "",

				/**
				 *  True help i18n label
				 */
				"helpBooleanTrue" : "",

				/**
				 *  Date between help i18n label
				 */
				"helpDateBetween" : "",

				/**
				 *  Date help i18n label
				 */
				"helpDateDate" : "",

				/**
				 *  Day help i18n label
				 */
				"helpDateDay" : "",

				/**
				 *  Month help i18n label
				 */
				"helpDateMonth" : "",

				/**
				 *  Year help i18n label
				 */
				"helpDateYear" : "",

				/**
				 *  Between help i18n label
				 */
				"helpNumberBetween" : "",

				/**
				 *  Equal help i18n label
				 */
				"helpNumberEq" : "",

				/**
				 *  Not Equal help i18n label
				 */
				"helpNumberNeq" : "",

				/**
				 *  Greater than help i18n label
				 */
				"helpNumberGt" : "",

				/**
				 *  Lesser than help i18n label
				 */
				"helpNumberLt" : "",

				/**
				 *  Greater or Equal help i18n label
				 */
				"helpNumberGoe" : "",

				/**
				 *  Lesser or Equal help i18n label
				 */
				"helpNumberLoe" : "",

				/**
				 *  Contains help i18n label
				 */
				"helpStringContains" : "",

				/**
				 *  Ends help i18n label
				 */
				"helpStringEnds" : "",

				/**
				 *  Is empty help i18n label
				 */
				"helpStringIsEmpty" : "",

				/**
				 *  Is not empty help i18n label
				 */
				"helpStringIsNotEmpty" : "",

				/**
				 *  Starts help i18n label
				 */
				"helpStringStarts" : "",
		};

		/**
		 * Configuration to use and replace
		 */
		this._data = {
				/**
				 * Input Obj
				 */
				"input":  $aInput,

                /**
				 * Datatable Parent Id
				 */
				"parentId":  inputData.dataTableId,

                /**
				 * Datatable Parent Path
				 */
				"parentPath":  inputData.dataTablePath,

				/**
				 * Last selected tab
				 */
				"tabSelectionLast": "1",

				/**
				 * Previous selected tab
				 */
				"tabSelectionPrevious": "-1",

				/**
				 * Ratio button checked
				 */
				"selectedRadioOption": "",

				/**
				 * Tabs Div Root
				 */
				"tabsRoot": "",

				/**
				 * Dialog type
				 */
				"type": "",

				/**
				 *  Button Find i18n label
				 */
				"buttonFind" : "",

				/**
				 *  All is null i18n label
				 */
				"allIsNull" : "",

				/**
				 *  All not null i18n label
				 */
				"allNotNull" : "",

				/**
				 *  False i18n label
				 */
				"booleanFalse" : "",

				/**
				 *  True i18n label
				 */
				"booleanTrue" : "",

				/**
				 *  Date between i18n label
				 */
				"dateBetween" : "",

				/**
				 *  Date i18n label
				 */
				"dateDate" : "",

				/**
				 *  Day i18n label
				 */
				"dateDay" : "",

				/**
				 *  Month i18n label
				 */
				"dateMonth" : "",

				/**
				 *  Year i18n label
				 */
				"dateYear" : "",

				/**
				 *  Between i18n label
				 */
				"numberBetween" : "",

				/**
				 *  Contains i18n label
				 */
				"stringContains" : "",

				/**
				 *  Ends i18n label
				 */
				"stringEnds" : "",

				/**
				 *  Is empty i18n label
				 */
				"stringIsEmpty" : "",

				/**
				 *  Is not empty i18n label
				 */
				"stringIsNotEmpty" : "",

				/**
				 *  Starts i18n label
				 */
				"stringStarts" : "",

				/**
				 *  Date i18n Pattern
				 */
				"datePattern" : "",

				/**
				 *  All is null i18n label
				 */
				"helpAllIsNull" : "",

				/**
				 *  All not null help i18n label
				 */
				"helpAllNotNull" : "",

				/**
				 *  False help i18n label
				 */
				"helpBooleanFalse" : "",

				/**
				 *  True help i18n label
				 */
				"helpBooleanTrue" : "",

				/**
				 *  Date between help i18n label
				 */
				"helpDateBetween" : "",

				/**
				 *  Date help i18n label
				 */
				"helpDateDate" : "",

				/**
				 *  Day help i18n label
				 */
				"helpDateDay" : "",

				/**
				 *  Month help i18n label
				 */
				"helpDateMonth" : "",

				/**
				 *  Year help i18n label
				 */
				"helpDateYear" : "",

				/**
				 *  Between help i18n label
				 */
				"helpNumberBetween" : "",

				/**
				 *  Equal help i18n label
				 */
				"helpNumberEq" : "",

				/**
				 *  Not Equal help i18n label
				 */
				"helpNumberNeq" : "",

				/**
				 *  Greater than help i18n label
				 */
				"helpNumberGt" : "",

				/**
				 *  Lesser than help i18n label
				 */
				"helpNumberLt" : "",

				/**
				 *  Greater or Equal help i18n label
				 */
				"helpNumberGoe" : "",

				/**
				 *  Lesser or Equal help i18n label
				 */
				"helpNumberLoe" : "",

				/**
				 *  Contains help i18n label
				 */
				"helpStringContains" : "",

				/**
				 *  Ends help i18n label
				 */
				"helpStringEnds" : "",

				/**
				 *  Is empty help i18n label
				 */
				"helpStringIsEmpty" : "",

				/**
				 *  Is not empty help i18n label
				 */
				"helpStringIsNotEmpty" : "",

				/**
				 *  Starts help i18n label
				 */
				"helpStringStarts" : "",
		};

		this.fnSettings = function(){
			return this.s;
		};

		// Constructor
		this._fnConstruct();

		GvNIX_Advanced_Filter._fnAddInstance(this);

		return this;
	};

	GvNIX_Advanced_Filter.prototype = {

			/**
			 * Method to initialize filter and create callbacks
			 */
			"_fnConstruct" : function(){

				// Recover input object
				var currentInput = jQuery(this._data.input);
				var parentInput = currentInput.parent();

				// AJAX Calls to get data
				this._fnGetColumnTypeAJAX(jQuery(currentInput).parent().parent().attr('data-property'));
				this._fnGetLocaleAJAX();

				// Creating and setting up new properties
				this._fnCreateIcon(parentInput);
				this._fnCreateWizardDialog(parentInput);
				this._fnCheckIfNotEmpty(currentInput);

			},
			/**
			 * This method creates an icon to open advanced filter dialog
			 *
			 * @param parent
			 */
			"_fnCreateIcon" : function(parent){
				parent.append('<a id="' + this._data.parentId + '_' + jQuery(parent).parent().attr('data-property')  + '_selection_open_wizard" class="icon open_wizard" style="margin-bottom: -4px;"></a>');
			},


			/**
			 * This method sets initial dialog configuration
			 *
			 * @param parent
			 */
			"_fnCreateWizardDialog" : function(parent){
				var wizard_button = parent.find("a");

				var data = this._data;
				var that = this;

				wizard_button.click(function() {

					// Adding the DIV where the page is gonna be displayed if not exists
					if(parent.find("div").attr("id") === undefined){
						parent.append('<div id="' + that._data.parentId + '_' + jQuery(parent).parent().attr('data-property') + '_dialog" style="display:none;"></div>');
					}


					// Creating dialog, but not opening
					var buttonOpts = {};
					buttonOpts[data.buttonFind] = function() {
						// Remove Dialog
						that._fnReturnValue();
						jQuery("#" + that._data.parentId + "_" + jQuery(parent).parent().attr('data-property') + "_dialog").html("");
						selectorDialog.dialog('destroy');
					};

					// Draw UI inside dialog
					var dialogDiv = jQuery(parent.find("div"));
					if(dialogDiv !== undefined){
						if(that._data.type != undefined){
                            that._fnCreateDialogTypifiedUI(dialogDiv, that._data.type);
                        } else {
                            that._fnCreateDialogBaseUI(dialogDiv);
                        }
					}

					// Setting up dialog
					var selectorDialog = jQuery("#" + that._data.parentId + '_' + jQuery(parent).parent().attr('data-property') + '_dialog').dialog({
						autoOpen : false,
						overflow:"auto",
						modal : true,
						resizable : false,
						width : 500,
						height : "auto",
						maxHeight : 600,
						title : "",
						buttons : buttonOpts,
						close : function(event, ui) {
							that._fnReturnValue();
							selectorDialog.dialog('close');
							jQuery("#" + that._data.parentId + '_' + jQuery(parent).parent().attr('data-property') + '_dialog').html("");
							selectorDialog.dialog('destroy');
						}
					});

					// Adding list page to Dialog
					selectorDialog.dialog('open');
				});
			},

			/**
			 * This method draws UI components inside advanced filter dialog following the needed type of operations
			 *
			 * @param parent
			 * @param type
			 */
			"_fnCreateDialogTypifiedUI" : function(parent,type){
				// Saving instance
				var that = this;

				// Adding tabs container
				parent.append('<div id="tabs"></div>');
				var tabsDiv = parent.find('div');
				this._data.tabsRoot = tabsDiv;

				// Actions for each tab
				switch(type) {
				case "boolean":
					this._fnCreateDialogBooleanUI(tabsDiv);
					tabsDiv.append('<div style="margin-bottom: 10px;margin-top: 15px;"><hr/></div>');
					this._fnCreateDialogGeneralUI(tabsDiv);
					break;
				case "date":
					this._fnCreateDialogDateUI(tabsDiv);
					tabsDiv.append('<div style="margin-bottom: 10px;margin-top: 15px;"><hr/></div>');
					this._fnCreateDialogGeneralUI(tabsDiv);
					break;
				case "number":
					this._fnCreateDialogNumberUI(tabsDiv);
					tabsDiv.append('<div style="margin-bottom: 10px;margin-top: 15px;"><hr/></div>');
					this._fnCreateDialogGeneralUI(tabsDiv);
					break;
				case "string":
				default:
					this._fnCreateDialogStringUI(tabsDiv);
					tabsDiv.append('<div style="margin-bottom: 10px;margin-top: 15px;"><hr/></div>');
					this._fnCreateDialogGeneralUI(tabsDiv);
					break;
				}

				// Disables all fields
				this._fnDisableFields(tabsDiv);

				// Adds action elements
				this._fnCreateRadioInputAction(tabsDiv);

				// Refilling Input
				this._fnFormRefilling();
			},

			/**
			 * This method draws a base UI components inside advanced filter dialog
			 *
			 * @param parent
			 */
			"_fnCreateDialogBaseUI" :function(parent){
				// Saving instance
				var that = this;

				// Adding tabs container
				parent.append('<div id="tabs"></div>');
				var tabsDiv = parent.find('div');
				tabsDiv.append('<ul></ul>');
				this._data.tabsRoot = tabsDiv;

				// Adding tab headers
				var tabsHeader = tabsDiv.find('ul');
				var headers = ["Todo","Booleano","Fecha","Número","Texto"];

				// Filling headers and building tab content
				for(var i = 0; i < headers.length; i++ ){
					tabsHeader.append('<li><a href="#tabs-' + (i+1) + '">' + headers[i] + '</a></li>');
					tabsDiv.append('<div id="tabs-' + (i+1) + '"></div>' );

					var parentDiv = jQuery(tabsDiv.find('div[id^=tabs]')[i]);

					// Actions for each tab
					switch(i) {
					case 0:
						this._fnCreateDialogAllUI(parentDiv);
						break;
					case 1:
						this._fnCreateDialogBooleanUI(parentDiv);
						break;
					case 2:
						this._fnCreateDialogDateUI(parentDiv);
						break;
					case 3:
						this._fnCreateDialogNumberUI(parentDiv);
						break;
					case 4:
						this._fnCreateDialogStringUI(parentDiv);
						break;

					}

					// Save last and previous tab
					var header = jQuery(tabsHeader.find('a')[i]);
					header.click(function() {
						// Getting tab selected
						var clickedTab = jQuery(this).attr('href');
						clickedTab = clickedTab.substring(clickedTab.length-1,clickedTab.length);

						// Updating values
						that._data.tabSelectionPrevious = that._data.tabSelectionLast;
						that._data.tabSelectionLast = clickedTab;

						// Checking tab changes
						if(clickedTab === "1" && that._data.selectedRadioOption.substring(0, 3) !== "gen"){
							var input = jQuery(this).parent().parent().parent().find('input[type=radio][id="general_' + that._data.selectedRadioOption  + '"]');
							input.trigger( "click" );
						} else if(clickedTab !== "1" &&
								that._data.selectedRadioOption.substring(0, 3) === "gen" &&
								that._data.selectedRadioOption.substring(0, 17) !== "general_radio_all"){
							var input = jQuery(this).parent().parent().parent().find('input[type=radio][id="' + that._data.selectedRadioOption.substring(8,that._data.selectedRadioOption.length)  + '"]');
							input.trigger( "click" );
						}

					});

				}
				// Disables all fields
				this._fnDisableFields(tabsDiv);

				// Adds action elements
				this._fnCreateRadioInputAction(tabsDiv);
				this._fnCreateNoRadioInputAction(tabsDiv);

				// Adding script to create tabs
				parent.append('<script> jQuery(function() { jQuery( "#tabs" ).tabs(); }); </script>');
			},

			/**
			 * This method disables all fields except radio buttons
			 *
			 * @param parent
			 */
			"_fnDisableFields" : function(parent) {
				var inputList = jQuery(parent.find("input[type!='radio']"));
				for(var i = 0; i < inputList.length; i++){
					var input = jQuery(inputList[i]);
					input.attr('disabled','disabled');
				}
			},

			/**
			 * This method sets action for all dialog inputs that are not radio
			 *
			 * @param parent
			 */

			"_fnCreateNoRadioInputAction" : function(parent){
				var inputList = jQuery(parent.find("input[type!='radio']"));
				for(var i = 0; i < inputList.length; i++){
					var input = jQuery(inputList[i]);
					input.change(function() {
						var input2Change = "";
						if(this.id.substring(0, 3) === "gen" &&
								this.id.substring(0, 17) !== "general_radio_all"){
							input2Change = this.id.substring(8,this.id.length);
						} else if(this.id.substring(0, 3) !== "gen"){
							input2Change = "general_" + this.id;
						}
						if(input2Change != "")
							jQuery(this).parent().parent().parent().parent().find('input[id=' + input2Change +']').val(this.value);
					});
				}
			},


			/**
			 * This method sets action for all dialog radio inputs
			 *
			 * @param parent
			 */
			"_fnCreateRadioInputAction" : function(parent){
				// Saving instance
				var that = this;

				// Getting list of radio button
				var inputList = jQuery(parent.find("input[type='radio']"));

				// Adding same action for all radio button
				for(var i = 0; i < inputList.length; i++){
					var input = jQuery(inputList[i]);
					if(input.attr("type") === "radio"){
						input.click(function() {
							// Disable previous fields
							var selectedFields = jQuery(this).parent().parent().parent().parent().find('input[id^=' +
									that._data.selectedRadioOption+ '][type!=radio]');
							for(var j = 0; j < selectedFields.length; j++){
								jQuery(selectedFields[j]).attr('disabled','disabled');
							}
							// Enable current fields
							selectedFields = jQuery(this).parent().find("input[type!=radio]");
							for(var j = 0; j < selectedFields.length; j++){
								jQuery(selectedFields[j]).removeAttr('disabled');
							}
							// Save current option
							that._data.selectedRadioOption = this.id;
						});
					}
				}
			},


			/**
			 * This method creates code for boolean tab
			 *
			 * @param parent
			 */
			"_fnCreateDialogBooleanUI" : function(parent, isGeneral){
				var sufix = "";
				if(isGeneral === true){
					sufix = "general_"
				}

				// Getting UI root
				parent.append('<div id="boolean' + sufix + '"></div>');
				var booleanParent = jQuery(parent.find('div[id^=boolean' + sufix +']'))

				// Setting UI inputs
				booleanParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_boolean_true" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px">' + this._data.booleanTrue + '<span class="wizard-help" title="' + this._data.helpBooleanTrue + '">[?]</span></div>' +
				'<div style="clear:both"></div></div>');
				booleanParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_boolean_false" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px">' + this._data.booleanFalse + '<span class="wizard-help" title="' + this._data.helpBooleanFalse + '">[?]</span></div>' +
				'<div style="clear:both"></div></div>');


			},

			/**
			 * This method creates code for date tab
			 *
			 * @param parent
			 */
			"_fnCreateDialogDateUI" : function(parent, isGeneral){
				// Before append check if it's called from general tab
				var sufix = "";
				if(isGeneral === true){
					sufix = "general_"
				}

				// Getting UI root
				parent.append('<div id="date' + sufix + '"></div>');
				var dateParent = jQuery(parent.find('div[id^=date' + sufix +']'))

				// Setting UI inputs
				dateParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_date_date" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">' + this._data.dateDate + '<span class="wizard-help" title="' + this._data.helpDateDate + '">[?]</span></div>' +
						'<input type="text" id="' + sufix +'radio_date_date_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				dateParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_date_between" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">' + this._data.dateBetween + '<span class="wizard-help" title="' + this._data.helpDateBetween + '">[?]</span></div>' +
						'<input type="text" id="' + sufix +'radio_date_between_from_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 10px;">;</div>' +
						'<input type="text" id="' + sufix +'radio_date_between_to_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				dateParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_date_day" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">' + this._data.dateDay + '<span class="wizard-help" title="' + this._data.helpDateDay + '">[?]</span></div>' +
						'<input type="number" id="' + sufix +'radio_date_day_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" min="1" max="31" value="1" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				dateParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_date_month" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">' + this._data.dateMonth + '<span class="wizard-help" title="' + this._data.helpDateMonth + '">[?]</span></div>' +
						'<input type="number" id="' + sufix +'radio_date_month_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" min="1" max="12" value="1" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				dateParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_date_year" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">' + this._data.dateYear + '<span class="wizard-help" title="' + this._data.helpDateYear + '">[?]</span></div>' +
						'<input type="number" id="' + sufix +'radio_date_year_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" value="2014" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				dateParent.append('<script>jQuery(function() {' +
						'jQuery( "#' + sufix +'radio_date_date_input" ).datepicker();' +
						'jQuery( "#' + sufix +'radio_date_between_from_input" ).datepicker();' +
						'jQuery( "#' + sufix +'radio_date_between_to_input" ).datepicker();' +
				' });</script>');
			},

			/**
			 * This method creates code for number tab
			 *
			 * @param parent
			 */
			"_fnCreateDialogNumberUI" : function(parent, isGeneral){
				var sufix = "";
				if(isGeneral === true){
					sufix = "general_"
				}

				// Getting UI root
				parent.append('<div id="number' + sufix + '"></div>');
				var numberParent = jQuery(parent.find('div[id^=number' + sufix +']'))

				// Setting UI inputs
				numberParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_number_between" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">' + this._data.numberBetween + '<span class="wizard-help" title="' + this._data.helpNumberBetween + '">[?]</span></div>' +
						'<input type="number" value="0" id="' + sufix +'radio_number_between_from_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 10px;">;</div>' +
						'<input type="number" value="0"id="' + sufix +'radio_number_between_to_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				numberParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_number_eq" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">=<span class="wizard-help" title="' + this._data.helpNumberEq + '">[?]</span></div>' +
						'<input type="number" value="0" id="' + sufix +'radio_number_eq_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				numberParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_number_neq" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">!=<span class="wizard-help" title="' + this._data.helpNumberNeq + '">[?]</span></div>' +
						'<input type="number" value="0" id="' + sufix +'radio_number_neq_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				numberParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_number_gt" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">><span class="wizard-help" title="' + this._data.helpNumberGt + '">[?]</span></div>' +
						'<input type="number" value="0" id="' + sufix +'radio_number_gt_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				numberParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_number_lt" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;"><<span class="wizard-help" title="' + this._data.helpNumberLt + '">[?]</span></div>' +
						'<input type="number" value="0" id="' + sufix +'radio_number_lt_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				numberParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_number_goe" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">>=<span class="wizard-help" title="' + this._data.helpNumberGoe + '">[?]</span></div>' +
						'<input type="number" value="0" id="' + sufix +'radio_number_goe_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				numberParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_number_loe" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;"><=<span class="wizard-help" title="' + this._data.helpNumberLoe + '">[?]</span></div>' +
						'<input type="number" value="0" id="' + sufix +'radio_number_loe_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');


				var numberInputs = numberParent.find('input[id^='+ sufix +'radio_number][type=number]')
				for(var i = 0; i < numberInputs.length; i++){
					jQuery(numberInputs[i]).change(function(){
						if(this.value === "") this.value = "0";
					});
				}
			},

			/**
			 * This method creates code for string tab
			 *
			 * @param parent
			 */
			"_fnCreateDialogStringUI" : function(parent, isGeneral){
				var sufix = "";
				if(isGeneral === true){
					sufix = "general_"
				}

				// Getting UI root
				parent.append('<div id="string' + sufix + '"></div>');
				var stringParent = jQuery(parent.find('div[id^=string' + sufix +']'))

				// Setting UI inputs
				stringParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_string_contains" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">' + this._data.stringContains + '<span class="wizard-help" title="' + this._data.helpStringContains + '">[?]</span></div>' +
						'<input type="text" id="' + sufix +'radio_string_contains_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				stringParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_string_ends" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">' + this._data.stringEnds + '<span class="wizard-help" title="' + this._data.helpStringEnds + '">[?]</span></div>' +
						'<input type="text" id="' + sufix +'radio_string_ends_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				stringParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_string_starts" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">' + this._data.stringStarts + '<span class="wizard-help" title="' + this._data.helpStringStarts + '">[?]</span></div>' +
						'<input type="text" id="' + sufix +'radio_string_starts_input" name="advanced_filter" style="float: left; margin-left: 10px; width: 100px;" class="form-control input-sm">' +
				'<div style="clear:both"></div></div>');
				stringParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_string_isempty" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">' + this._data.stringIsEmpty + '<span class="wizard-help" title="' + this._data.helpStringIsEmpty + '">[?]</span></div>' +
				'<div style="clear:both"></div></div>');
				stringParent.append('<div style="margin: 4px;"><input type="radio" id="' + sufix +'radio_string_isnotempty" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px; width: 150px;">' + this._data.stringIsNotEmpty + '<span class="wizard-help" title="' + this._data.helpStringIsNotEmpty + '">[?]</span></div>' +
				'<div style="clear:both"></div></div>');
			},

			/**
			 * This method creates code for general tab
			 *
			 * @param parent
			 */
			"_fnCreateDialogGeneralUI" : function(parent){
				// Getting UI root
				parent.append('<div id="general"></div>');
				var generalParent = jQuery(parent.find('div[id^=general]'));

				// Setting UI inputs
				generalParent.append('<div><input type="radio" id="general_radio_all_isnull" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px">' + this._data.allIsNull + '<span class="wizard-help" title="' + this._data.helpAllIsNull + '">[?]</span></div>' +
				'<div style="clear:both"></div></div>');
				generalParent.append('<div><input type="radio" id="general_radio_all_isnotnull" name="advanced_filter" style="float: left; margin: 3px;">' +
						'<div style="float:left; margin-left: 10px; margin-top: 6px">' + this._data.allNotNull + '<span class="wizard-help" title="' + this._data.helpAllNotNull + '">[?]</span></div>' +
				'<div style="clear:both"></div></div>');
			},

			/**
			 * This method creates code for general tab
			 *
			 * @param parent
			 */
			"_fnCreateDialogAllUI" : function(parent){
				this._fnCreateDialogGeneralUI(parent);
				parent.append('<div style="margin-bottom: 10px;margin-top: 15px;"><hr/></div>');
				this._fnCreateDialogBooleanUI(parent,true);
				parent.append('<div style="margin-bottom: 10px;margin-top: 15px;"><hr/></div>');
				this._fnCreateDialogDateUI(parent,true);
				parent.append('<div style="margin-bottom: 10px;margin-top: 15px;"><hr/></div>');
				this._fnCreateDialogNumberUI(parent,true);
				parent.append('<div style="margin-bottom: 10px;margin-top: 15px;"><hr/></div>');
				this._fnCreateDialogStringUI(parent,true);

			},

			/**
			 * Sets selected advanced filter from wizard inside datatable input
			 *
			 */
			"_fnReturnValue" : function() {
				// Checking if there's selection
				if(this._data.selectedRadioOption !== ""){
					// Recovering optional values
					var outputValues = jQuery(this._data.tabsRoot.find('input[id=' + this._data.selectedRadioOption + ']')).parent().find('input[type!=radio]');

					var outputFormatted = "";

					// Getting tag
					var tag = jQuery(jQuery(this._data.tabsRoot.find('input[id=' + this._data.selectedRadioOption + ']')).parent().find('div')[0]).text()

					// Remove [?]
					if(tag.indexOf('[') !== -1)
						tag = tag.substring(0,tag.indexOf('['));

					// If there are parameters
					if(outputValues.length > 0 ) {
						// Adding tag
						if(tag.length > 2 ) outputFormatted += tag + "(";
						else outputFormatted += tag;

						// Adding parameters
						for(var i = 0; i < outputValues.length; i++){
							outputFormatted += outputValues[i].value;
							outputFormatted += ";";
						}
						// Removing last semicolon and closing
						outputFormatted = outputFormatted.substring(0,outputFormatted.length-1);
						if(tag.length > 2 ) outputFormatted +=")";
					} else {
						// Adding tag
						outputFormatted += tag;
					}

					var result = jQuery(this._data.input).val(outputFormatted)
					result.trigger("keyup");
					jQuery(this._data.input).removeClass("search_init");

				}
			},

			/**
			 * This method gets field type to apply the filter
			 *
			 * @param name
			 */

			"_fnGetColumnTypeAJAX" : function(name){
				// Saving instance
				var that = this;

				var params = {
					_columnName_: name
				};
            
				$.ajax({
					type: "POST",
					url : that._data.parentPath + "?getColumnType",
					data : params,
					success : function(object) {
						// Sets column type
						that._data.type = object.columnType;
					},
					error : function(object) {
						console.log("[ERROR] AJAX type request failed.")
					}
				});
			},

			/**
			 * This method gets i18n data to apply inside dialog
			 *
			 */

			"_fnGetLocaleAJAX" : function(){
				// Saving instance
				var that = this;

				// Check cookies for locale
				var locale = $.cookie("locale");

				// If there's no cookie check browser for locale
				if(locale === undefined)
					locale = window.navigator.language.substring(0,2) || window.navigator.userLanguage.substring(0,2);

				var params = {
						"_locale_": locale
					};

				$.ajax({
					type: "POST",
					url : that._data.parentPath + "?geti18nText",
					data: params,
					success : function(object) {
						// Setting i18n strings
						that._data.buttonFind = object.button_find;
						that._data.allIsNull = object.all_isnull;
						that._data.allNotNull = object.all_notnull;
						that._data.booleanFalse = object.boolean_false;
						that._data.booleanTrue = object.boolean_true;
						that._data.dateBetween = object.date_between;
						that._data.datePattern = object.date_pattern;
						that._data.dateDate = object.date_date;
						that._data.dateDay = object.date_day;
						that._data.dateMonth = object.date_month;
						that._data.dateYear = object.date_year;
						that._data.numberBetween = object.number_between;
						that._data.stringContains = object.string_contains;
						that._data.stringEnds = object.string_ends;
						that._data.stringIsEmpty = object.string_isempty;
						that._data.stringIsNotEmpty = object.string_isnotempty;
						that._data.stringStarts = object.string_starts;
						that._data.helpAllIsNull = object.help_all_isnull;
						that._data.helpAllNotNull = object.help_all_notnull;
						that._data.helpBooleanFalse = object.help_boolean_false;
						that._data.helpBooleanTrue = object.help_boolean_true;
						that._data.helpDateBetween = object.help_date_between;
						that._data.helpDateDate = object.help_date_date;
						that._data.helpDateDay = object.help_date_day;
						that._data.helpDateMonth = object.help_date_month;
						that._data.helpDateYear = object.help_date_year;
						that._data.helpNumberBetween = object.help_number_between;
						that._data.helpNumberEq = object.help_number_eq;
						that._data.helpNumberNeq = object.help_number_neq;
						that._data.helpNumberGt = object.help_number_gt;
						that._data.helpNumberLt = object.help_number_lt;
						that._data.helpNumberGoe = object.help_number_goe;
						that._data.helpNumberLoe = object.help_number_loe;
						that._data.helpStringContains = object.help_string_contains;
						that._data.helpStringEnds = object.help_string_ends;
						that._data.helpStringIsEmpty = object.help_string_isempty;
						that._data.helpStringIsNotEmpty = object.help_string_isnotempty;
						that._data.helpStringStarts = object.help_string_starts;
					},
					error : function(object) {
						console.log("[ERROR] AJAX i18n request failed.")
					}
				});
			},

			"_fnCheckIfNotEmpty" : function(input) {
				input.keyup(function(){
					if(this.value !== "") jQuery(this).addClass("filter_not_empty");
					else jQuery(this).removeClass("filter_not_empty");
				});
			},

			"_fnFormRefilling" : function(parent) {
				var parentInputValue = this._data.input.val();

				// We need pattern matching here to enter to next code
				if(parentInputValue.match("[=><!][=]?[0-9]+(\.)?[0-9]*") != null || parentInputValue.match("[A-Z]*") != null ){
					var tag = "";
					var values = "";

					// Check if input contains '(' ')'
					if(parentInputValue.indexOf('(') > -1){ // TAG(value) or TAG(value1;value2)
						var inputValue = parentInputValue.split('(');
						tag = inputValue[0];
						values = inputValue[1].substring(0,inputValue[1].length-1); // remove last ')'
						values = values.split(";"); // splittig for 2-valued entries
					} else { // TAGvalue or TAG
						if (parentInputValue.match("[=><!][=]?") !== null) { // > = ! <
							tag = parentInputValue.match("[=><!][=]?")[0];
							values = parentInputValue.substring(tag.length,parentInputValue.length);
						} else { // [A-Z]*
							tag = parentInputValue;
						}
					}

					// Rebuild data to select inputs and fill fields
					switch(tag){
					case "=" :
						if((this._data.input.parent().find('input[id^=radio_number_eq]').length === 2) && !(values instanceof Array)) {
							jQuery(this._data.input.parent().find('input[id^=radio_number_eq]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_number_eq]')[1]).val(values);
						}
						break;
					case "!=" :
						if((this._data.input.parent().find('input[id^=radio_number_neq]').length === 2) && !(values instanceof Array)) {
							jQuery(this._data.input.parent().find('input[id^=radio_number_neq]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_number_neq]')[1]).val(values);
						}
						break
					case "<" :
						if((this._data.input.parent().find('input[id^=radio_number_lt]').length === 2) && !(values instanceof Array)) {
							jQuery(this._data.input.parent().find('input[id^=radio_number_lt]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_number_lt]')[1]).val(values);
						}
						break;
					case ">" :
						if((this._data.input.parent().find('input[id^=radio_number_gt]').length === 2) && !(values instanceof Array)) {
							jQuery(this._data.input.parent().find('input[id^=radio_number_gt]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_number_gt]')[1]).val(values);
						}
						break;
					case "<=" :
						if((this._data.input.parent().find('input[id^=radio_number_loe]').length === 2) && !(values instanceof Array)) {
							jQuery(this._data.input.parent().find('input[id^=radio_number_loe]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_number_loe]')[1]).val(values);
						}
						break;
					case ">=" :
						if((this._data.input.parent().find('input[id^=radio_number_goe]').length === 2) && !(values instanceof Array)) {
							jQuery(this._data.input.parent().find('input[id^=radio_number_goe]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_number_goe]')[1]).val(values);
						}
						break;
					case this._data.allIsNull :
						if((this._data.input.parent().find('input[id^=general_radio_all_isnull]').length === 1)) {
							jQuery(this._data.input.parent().find('input[id^=general_radio_all_isnull]')[0]).trigger("click");
						}
						break;
					case this._data.allNotNull :
						if((this._data.input.parent().find('input[id^=general_radio_all_isnotnull]').length === 1)) {
							jQuery(this._data.input.parent().find('input[id^=general_radio_all_isnotnull]')[0]).trigger("click");
						}
						break;
					case this._data.booleanFalse :
						if((this._data.input.parent().find('input[id^=radio_boolean_false]').length === 1)) {
							jQuery(this._data.input.parent().find('input[id^=radio_boolean_false]')[0]).trigger("click");
						}
						break;
					case this._data.booleanTrue :
						if((this._data.input.parent().find('input[id^=radio_boolean_true]').length === 1)) {
							jQuery(this._data.input.parent().find('input[id^=radio_boolean_true]')[0]).trigger("click");
						}
						break;
					case this._data.dateBetween :
						if((this._data.input.parent().find('input[id^=radio_date_between]').length === 3) && values.length > 1) {
							jQuery(this._data.input.parent().find('input[id^=radio_date_between]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_date_between]')[1]).val(values[0]);
							jQuery(this._data.input.parent().find('input[id^=radio_date_between]')[2]).val(values[1]);
						}
						break;
					case this._data.dateDate :
						if((this._data.input.parent().find('input[id^=radio_date_date]').length === 2) && values.length === 1) {
							jQuery(this._data.input.parent().find('input[id^=radio_date_date]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_date_date]')[1]).val(values[0]);
						}
						break;
					case this._data.dateDay :
						if((this._data.input.parent().find('input[id^=radio_date_day]').length === 2) && values.length === 1) {
							jQuery(this._data.input.parent().find('input[id^=radio_date_day]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_date_day]')[1]).val(values[0]);
						}
						break;
					case this._data.dateMonth :
						if((this._data.input.parent().find('input[id^=radio_date_month]').length === 2) && values.length === 1) {
							jQuery(this._data.input.parent().find('input[id^=radio_date_month]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_date_month]')[1]).val(values[0]);
						}
						break;
					case this._data.dateYear :
						if((this._data.input.parent().find('input[id^=radio_date_year]').length === 2) && values.length === 1) {
							jQuery(this._data.input.parent().find('input[id^=radio_date_year]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_date_year]')[1]).val(values[0]);
						}
						break;
					case this._data.numberBetween :
						if((this._data.input.parent().find('input[id^=radio_number_between]').length === 3) && values.length > 1) {
							jQuery(this._data.input.parent().find('input[id^=radio_number_between]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_number_between]')[1]).val(values[0]);
							jQuery(this._data.input.parent().find('input[id^=radio_number_between]')[2]).val(values[1]);
						}
						break;
					case this._data.stringContains :
						if((this._data.input.parent().find('input[id^=radio_string_contains]').length === 2) && values.length === 1) {
							jQuery(this._data.input.parent().find('input[id^=radio_string_contains]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_string_contains]')[1]).val(values[0]);
						}
						break;
					case this._data.stringEnds :
						if((this._data.input.parent().find('input[id^=radio_string_ends]').length === 2) && values.length === 1) {
							jQuery(this._data.input.parent().find('input[id^=radio_string_ends]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_string_ends]')[1]).val(values[0]);
						}
						break;
					case this._data.stringIsEmpty :
						if((this._data.input.parent().find('input[id^=radio_string_isempty]').length === 1)) {
							jQuery(this._data.input.parent().find('input[id^=radio_string_isempty]')[0]).trigger("click");
						}
						break;
					case this._data.stringIsNotEmpty :
						if((this._data.input.parent().find('input[id^=radio_string_isnotempty]').length === 1)) {
							jQuery(this._data.input.parent().find('input[id^=radio_string_isnotempty]')[0]).trigger("click");
						}
						break;
					case this._data.stringStarts :
						if((this._data.input.parent().find('input[id^=radio_string_starts]').length === 2) && values.length === 1) {
							jQuery(this._data.input.parent().find('input[id^=radio_string_starts]')[0]).trigger("click");
							jQuery(this._data.input.parent().find('input[id^=radio_string_starts]')[1]).val(values[0]);
						}
						break;
					default:
						break;

					}
				}
			}
	};

	// Static variables * * * * * * * * * * * * * * * * * * * * * * * * * * *

	/**
	 * Store of all instances that have been created of AdvancedFilter, so one can
	 * look up other (when there is need of a master)
	 *
	 * @property _aInstances
	 * @type Array
	 * @default []
	 * @private
	 */
	GvNIX_Advanced_Filter._aInstances = [];

	/**
	 * Function to add new instances
	 */
	GvNIX_Advanced_Filter._fnAddInstance = function(instance){
		GvNIX_Advanced_Filter._aInstances.push(instance);
	};

	/**
	 * Function to get Advanced Filter using field and current instance
	 */
	GvNIX_Advanced_Filter.fnGetInstance = function(input){
		//Getting all instances
		var instances = GvNIX_Advanced_Filter._aInstances;

		// Iterating instances and returning the correct one
		for(i in instances){
			var instance = instances[i];
			var settings = instance.s;
			if(settings.input == input){
				return instances[i];
			}
		}
	};


})(jQuery, window, document);

/*
 * STATIC METHODS
 */

//Registering events
fnRegisterFunctionsToCallBack(function(context){
    setTimeout(function(){
        jQuery(".search_init , .dandelion_text_filter", context).each(function(index) {
	        // Checking that not exists another advanced search
	        if(jQuery(this).parent().find('a').length == 0){
			    new GvNIX_Advanced_Filter(jQuery(this));
	        }
		});
    }, 500);
});
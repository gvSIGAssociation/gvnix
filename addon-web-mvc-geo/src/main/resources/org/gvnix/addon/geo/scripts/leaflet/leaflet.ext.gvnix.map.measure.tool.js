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

/* Global scope for GvNIX_Map_Measure_Tool */
var GvNIX_Map_Measure_Tool;

(function(jQuery, window, document) {
	
	GvNIX_Map_Measure_Tool = function($aTool) {
		// Santiy check that we are a new instance
		if (!this instanceof GvNIX_Map_Measure_Tool) {
			alert("Warning: GvNIX_Map_Measure_Tool must be initialised with the keyword 'new'");
		}
		
		// Public class variables * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * Initial configuration
		 */
		this.s = {
			/**
			 * div Id
			 */
			"id":  $aTool.attr("id"),
			
			/**
			 * div Class
			 */
			"class": "mapviewer_measure_tool",
			
			/**
			 * Button item
			 */
			"button": $aTool,

			/**
	        * Map instance                        
	        */
	        "map": null,
	        
	        /**
	         * Toolbar instance
	         */
	        "toolBar": null
	        
		};
		
		/**
		 * Configuration to use and replace
		 */
		this._data = {
			/**
			 * div Id
			 */
			"id":  $aTool.attr("id"),
			
			/**
			 * div Class
			 */
			"class": "mapviewer_measure_tool",
			
			/**
			 * Button item
			 */
			"button": $aTool,

			/**
	        * Map instance                        
	        */
	        "map": null,
	        
	        /**
	         * Toolbar instance
	         */
	        "toolBar": null
			
		};
		
		this.fnSettings = function(){
			return this.s;
		};
		
		// Constructor
		this._fnConstruct();
		
		GvNIX_Map_Measure_Tool._fnAddInstance(this);
		
		return this;
	};
	
	GvNIX_Map_Measure_Tool.prototype = {
			
			/**
			 * Method to initialize Map and create callbacks
			 */
			"_fnConstruct" : function(){
				var instance = this;
				var mapControl = jQuery(".mapviewer_control");
				if(mapControl){
					// Registering map instance
					this._data.map = GvNIX_Map_Leaflet.fnGetInstance(mapControl.attr("id"));
					// Registering toolbar instance
					this._data.toolBar = mapControl.parent();
					// Adding click event to button
					this._data.button.on('click', function(){
						instance.fnOnClickTool(this, instance);
					});
					// Adding deselect event
					this._data.button.on("deselectCurrentTool", function(event){
						// Calling instance deactivate method
						var instanceId = this.id;
						var toolInstance = GvNIX_Map_Measure_Tool.fnGetInstance(instanceId);
						toolInstance.fnDeactivateTool(jQuery(this));
					});
				}else{
					alert("Warning: GvNIX_Map_Measure_Tool must be initialised with map element present");
				}
			},
			
			/**
			 * Action to do when clicks tool button
			 */
			"fnOnClickTool": function onClickTool(element, instance){
				// Getting tool
				var tool = jQuery(element);
				// Getting map instance
				var map = instance.fnGetMapObject();
				var response = true;
				if(!tool.hasClass("mapviewer_tool_selected")){
					// Selecting tool
					response = instance.fnSetCurrentTool(tool);
					if(response){
						// Changing cursor 
						jQuery(this._data.map._data.map._container).css({"cursor" : "url('resources/images/measure_icon.png'), default"});
						// Generating new measuringTool 
						instance._data.measuringTool = new L.MeasuringTool(map);
						// Enable measuring tool
						instance._data.measuringTool.enable();
					}
				}
			},
			
			
			/**
            * Function to get map item
            **/
            "fnGetMapObject": function getMapObject(){
                    return this._data.map._data.map;
            },
            
            /**
			 * Function to select current tool and deselect other selected tools
			 * 
			 * @param tool
			 */
			"fnSetCurrentTool": function selectCurrentTool(){
				// Removing previous selected element
				var response = true;
				var currentTool = this._data.map._data.currentTool;
				if( currentTool !== "" && currentTool !== undefined){
					// Calling event of current tool to deselect
					jQuery(currentTool).trigger("deselectCurrentTool").data("response");
					// If trigger returns false, not change tool
					response = jQuery(currentTool).data().response;
					if(response){
						currentTool.removeClass("mapviewer_tool_selected");
					}
						
				}
				// If the previous tool allows the tool change, save the new tool as current tool
				// and set this tool as active
				if(response){
					this._data.map._data.currentTool = jQuery(this._data.button);
					jQuery(this._data.button).addClass("mapviewer_tool_selected");
				}
				
				return response;
			},
			
			/**
			 * Function to deactivate selected tool 
			 * 
			 * @param tool
			 */
			"fnDeactivateTool": function deselectCurrentTool(element){
				var response = confirm("Do you want to get out of the current edition? You will lose all changes made ");
				if(response){
					// Changing icon to default
					jQuery(this._data.map._data.map._container).css({"cursor" : "url('resources/images/cursor_hand.png'), default"});
					element.removeClass("mapviewer_tool_selected");
					// Disable measuring tool
					this._data.measuringTool.disable();
				}
				element.data("response", response);
			},
			
            /**
             * Function to get toolBar item
             **/
             "fnGetToolbarObject": function getMapObject(){
                     return this._data.toolBar;
             }
			
			
			
			
	};
	
	// Static variables * * * * * * * * * * * * * * * * * * * * * * * * * * *

	/**
	 * Store of all instances that have been created of Leaflet_Map, so one can
	 * look up other (when there is need of a master)
	 *
	 * @property _aInstances
	 * @type Array
	 * @default []
	 * @private
	 */
	GvNIX_Map_Measure_Tool._aInstances = [];
	
	/**
	 * Function to add new instances 
	 */
	GvNIX_Map_Measure_Tool._fnAddInstance = function(instance){
		GvNIX_Map_Measure_Tool._aInstances.push(instance);
	};
	
	/**
	 * Function to get Map Instance using field and current instance
	 */
	GvNIX_Map_Measure_Tool.fnGetInstance = function(id){
		//Getting all instances
		var instances = GvNIX_Map_Measure_Tool._aInstances;
		
		// Iterating instances and returning the correct one
		for(i in instances){
			var instance = instances[i];
			var settings = instance.s;
			if(settings.id == id){
				return instances[i];
			}
		}
	};
	
	
})(jQuery, window, document);
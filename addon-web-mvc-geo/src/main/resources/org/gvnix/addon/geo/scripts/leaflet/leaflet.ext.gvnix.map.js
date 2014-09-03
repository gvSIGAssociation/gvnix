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

/* Global scope for GvNIX_Map_Leaflet */
var GvNIX_Map_Leaflet;

(function(jQuery, window, document) {
	
	GvNIX_Map_Leaflet = function($aDiv) {
		// Santiy check that we are a new instance
		if (!this instanceof GvNIX_Map_Leaflet) {
			alert("Warning: GvNIX_Map_Leaflet must be initialised with the keyword 'new'");
		}
		
		var divData = $aDiv.data();
		
		// Public class variables * * * * * * * * * * * * * * * * * * * * * *

		/**
		 * Initial configuration
		 */
		this.s = {
			/**
			 * div Id
			 */
			"id":  $aDiv.attr('id'),
			
			/**
			 * div Class
			 */
			"class": "mapviewer_control",
			
			/**
			 * Center point location of the Map
			 */
			"center": divData.center,
			
			/**
			 * Map Zoom
			 */
			"zoom": divData.zoom,
			
			/**
			 * Max Zoom allowed on Map
			 */
			"maxZoom": divData.maxzoom,
			
			/**
			 * URL Server to load map
			 */
			"url": divData.url,

	        /**
	        * Map instance                        
	        */
	        "map": null,
	        
	        /**
	         * Map Layers
	         */
	        "layers": [],
	        
	        /**
	         * Tile layers
	         */
	        "tileLayers": [],
	        
	        /**
	         * WMS layers
	         */
	        "wmsLayers": [],
	        
	        /**
	         * Toolbar object
	         */
	        "toolBar": null,
	        
	        /**
	         * Control Layers object
	         */
	        "controlLayers": null,
	        
	        /**
	         * Control Toolbar object
	         */
	        "controlToolbar": null
	        
		};
		
		/**
		 * Configuration to use and replace
		 */
		this._data = {
				/**
				 * Input Id
				 */
				"id":  $aDiv.attr('id'),
				
				/**
				 * Input Class
				 */
				"class": "mapviewer_control",
				
				/**
				 * Center point location of the Map
				 */
				"center": divData.center,
				
				/**
				 * Map Zoom
				 */
				"zoom": divData.zoom,
				
				/**
				 * Max Zoom allowed on Map
				 */
				"maxZoom": divData.maxzoom,
				
				/**
				 * URL Server to load map
				 */
				"url": divData.url,

                /**
                * Map instance                        
                */
                "map": null,
                
                /**
    	         * Map Layers
    	         */
    	        "layers": [],
    	        
    	        /**
    	         * Tile layers
    	         */
    	        "tileLayers": [],
    	        
    	        /**
    	         * WMS layers
    	         */
    	        "wmsLayers": [],
    	        
    	        /**
    	         * Toolbar object
    	         */
    	        "toolBar": null,
    	        
    	        /**
    	         * Control Layers object
    	         */
    	        "controlLayers": null,
    	        
    	        /**
    	         * Control Toolbar object
    	         */
    	        "controlToolbar": null
    	        
			};
		
		this.fnSettings = function(){
			return this.s;
		};
		
		// Constructor
		this._fnConstruct();
		
		GvNIX_Map_Leaflet._fnAddInstance(this);
		
		return this;
	};
	
	GvNIX_Map_Leaflet.prototype = {
			
			/**
			 * Method to initialize Map and create callbacks
			 */
			"_fnConstruct" : function(instance){
					var data = this._data;
					// Initialize map
					this._fnInitializeMap(data.id, data.center, data.zoom, data.maxZoom, data.url);
					// Initialize toolbar
					this._fnInitializeToolBar();
			},
			
			/**
			 * Function to initialize Map
			 * */
			"_fnInitializeMap": function initializeMap(divId, center, zoom, maxZoom, url) {
				// Saving instance
				var instance = this;
				
				// Getting center LatLng
                var latLng = center.split(",");
                var lat = latLng[0];
                var lng = latLng[1];
                
				// Creating Map
				this._data.map = L.map(divId, {
	  				center: [lat , lng], 
	  				zoom: zoom,
	  		        zoomControl: false
				});
				
				// Adding zoom control on top right position
				new L.Control.Zoom({ position: 'topright' }).addTo(this._data.map);
				// Adding Zoomslider
				//L.control.zoomslider({position: 'topright'}).addTo(this._data.map);
				
				// Adding layers control
				this._fnAddLayersControl(this._data.map);
				
				// Configuring map
				L.tileLayer(url, {
	  			    attribution: '<a href="http://www.gvnix.org">gvNIX</a>',
	  			    maxZoom: maxZoom
	  			}).addTo(this._data.map);
				
				// Adding events to reload data
				this._data.map.on("moveend", function(event){
					var layers = instance._data.layers;
					instance._fnReloadDataByCheckedLayers(instance, layers);
				});

			},
			
			/**
			 * Function to add layer control with toc layers
			 */
			"_fnAddLayersControl": function addLayersControl(map){
				// Getting generated toc HTML
				var tocLayers = jQuery("#" + this._data.id + "_toc_layers");
				var html = tocLayers.html();
				if(jQuery(html).children().length > 0){
					// Generating controlLayers item
					this._data.controlLayers = L.control.htmlLayers();
					// Adding controlLayers to map
					this._data.controlLayers.addTo(this._data.map);
					// Removing hidden tocLayer
					tocLayers.html("");
					// Adding HTML toc to controlLayer
					this._data.controlLayers._setHtmlContent(html);
				}
				
			},
			
			/**
			 * Function to add toolbar control with tools
			 */
			"_fnAddToolbarControl": function addToolbarControl(map){
				// Generating controlLayers item
				this._data.controlToolbar = L.control.htmlToolbar();
				// Adding controlLayers to map
				this._data.controlToolbar.addTo(this._data.map);
				
				// Getting generated toolbar HTML
				var toolbar = jQuery(".mapviewer_tools_bar");
				var toolbarHTML = toolbar.html();
				// Removing hidden toolbar
				toolbar.html("");
				// Adding HTML toolbar to toolbarControl
				this._data.controlToolbar._setHtmlContent(toolbarHTML);
			},
			
			/**
			 * Function to register entity layer 
			 * 
			 * @param oLayer
			 */
			"fnRegisterLayer": function registerLayer(oLayer){
				// Getting EntityLayer configuration
				var checkBox = oLayer.checkBox;
				var data = checkBox.data();
				// Generating correct path
				data.path = data.path.replace("/","");
				// Getting loading icon 
				oLayer.loadingIcon = jQuery("#"+checkBox.attr("id")+"_loading_icon");
				
				// Check if layer exists yet
				var currentLayers = this._data.layers;
				var exists = false;
				for(i in currentLayers){
					var currentLayer = currentLayers[i];
					if(oLayer.checkBox.data() == currentLayer.checkBox.data()){
						exists = true;
						break;
					}
				}
				
				if(!exists){
					// Adding layer to _data object
					currentLayers.push(oLayer);
					// Adding span click event
					this._fnRegisterSpanEvents(checkBox);
					// Add onChange event for all layers checkbox
					this._fnRegisterCheckboxEvents(checkBox, oLayer);
					// TODO: Check which layers needs to load. 
					// Force change event at all register layer
					oLayer.checkBox.trigger("change");
				}
			},
			
			/**
			 * Function to register field entity layer 
			 * 
			 * @param oLayer
			 */
			"fnRegisterFieldLayer": function registerFieldLayer(oLayer){
				// Getting instance
				var instance = this;
				// Getting parent layer
				var entityCheckbox = oLayer.checkBox.parent().parent().parent().children().get(0);
				var registeredLayers = this._data.layers;
				jQuery.each(registeredLayers, function(index, layer){
					// If gets parent layer configuration, add fields config
					if(layer.checkBox.attr("id") == entityCheckbox.id){
						if(layer.fieldsConfig == undefined){
							layer.fieldsConfig = [];
						}
						// Checking if layer exists yet
						var exists = false;
						for(i in layer.fieldsConfig){
							var item = layer.fieldsConfig[i].checkBox.attr("id");
							var checkBoxToAdd = oLayer.checkBox.attr("id");
							if(item == checkBoxToAdd){
								exists = true;
								break;
							}
						}
						
						// If not exists, add and generate necessary items
						if(!exists){
							// Creating layerGroup for every field
	        				var layerGroup = new L.MarkerClusterGroup({
	        					showCoverageOnHover: false, 
	        					removeOutsideVisibleBounds : true,
	        				    iconCreateFunction: function(cluster) {
	        				    	var childCount = cluster.getChildCount();
	        				    	// Modifying object of child markers
	        				    	var markerChilds = cluster.getAllChildMarkers();
	        				    	if(markerChilds.length > 0){
	        				    		var child = markerChilds[0];
	        				    		var currentIcon = child.options.icon;
	        				    		var groupIcon = L.AwesomeMarkers.icon({
	            	    				    icon: currentIcon.options.icon,
	            	    				    prefix: currentIcon.options.prefix,
	            	    				    groupedMarkers: childCount,
	            	    				    markerColor: currentIcon.options.markerColor,
	            	    				    iconColor: currentIcon.options.iconColor
	                        			});
	        				    		return groupIcon;
	        				    	}
	        				    }
	        				});
	        				//var layerGroup = new L.LayerGroup();
	        				oLayer.layerGroup = layerGroup;
	        				layer.fieldsConfig.push(oLayer);

	        				// Add onClick event for all layers span
	        				instance._fnRegisterSpanFieldsEvents(oLayer.checkBox);
	        				// Add onChange event for all layers checkbox
	        				instance._fnRegisterCheckboxFieldsEvents(oLayer.checkBox, oLayer, layer);
						}
					}
				});
			},
			
			
			/**
			 * Function to register tile layer 
			 * 
			 * @param oTileLayer
			 */
			"fnRegisterTileLayer": function registerTileLayer(oTileLayer){
				// Saving instance
				var instance = this;
				// Getting map
				var map = this.fnGetMapObject();
				// Getting tilelayer configuration
				var checkBox = oTileLayer.checkBox;
				var span = oTileLayer.span;
				
				// Checking if tile layers exists
				var exists = false;
				for(i in instance._data.tileLayers){
					var item =instance._data.tileLayers[i];
					var tileLayerToAdd = checkBox.attr("id");
					if(item == tileLayerToAdd){
						exists = true;
						break;
					}
				}
				
				// If exists, do nothing
				if(!exists){
					// Adding span event
					jQuery(span).click(function(){
						if(jQuery(checkBox).prop("checked")){
							jQuery(checkBox).prop("checked", false);
							jQuery(checkBox).trigger("change");
						}else{
							jQuery(checkBox).prop("checked", true);
							jQuery(checkBox).trigger("change");
						}
					});
					// Adding checkbox event
					jQuery(checkBox).change(function(){
						if(jQuery(this).prop('checked')){
	    		    		if(map){
	    		    			var tileLayer = L.tileLayer(oTileLayer.url);
	    						tileLayer.setOpacity(oTileLayer.opacity);
	    						if(oTileLayer.index !== ""){
	    							tileLayer.setZIndex(oTileLayer.index);
	    						}
	    						oTileLayer.markerGroup.addLayer(tileLayer);
	    						oTileLayer.markerGroup.addTo(map);
	    		    		}else{
	    		    			alert("Map is not defined!");
	    		    		}
	    				}else{
	    					oTileLayer.markerGroup.clearLayers();
	    				}
					});
					
					// Saving current tile layer
					instance._data.tileLayers.push(checkBox.attr("id"));
				}
				
			},
			
			
			/**
			 * Function to register wms layer 
			 * 
			 * @param oWmsLayer
			 */
			"fnRegisterWmsLayer": function registerWmsLayer(oWmsLayer){
				// Saving instance
				var instance = this;
				// Getting map
				var map = this.fnGetMapObject();
				// Getting wmsLayer configuration
				var checkBox = oWmsLayer.checkBox;
				var span = oWmsLayer.span;
				
				// Checking if tile layers exists
				var exists = false;
				for(i in instance._data.wmsLayers){
					var item =instance._data.wmsLayers[i];
					var wmsLayerToAdd = checkBox.attr("id");
					if(item == wmsLayerToAdd){
						exists = true;
						break;
					}
				}
				
				// If exists, do nothing
				if(!exists){
					// Adding span event
					jQuery(span).click(function(){
						if(jQuery(checkBox).prop("checked")){
							jQuery(checkBox).prop("checked", false);
							jQuery(checkBox).trigger("change");
						}else{
							jQuery(checkBox).prop("checked", true);
							jQuery(checkBox).trigger("change");
						}
					});
					// Adding checkbox event
					jQuery(checkBox).change(function(){
	    				if(jQuery(this).prop('checked')){
	    		    		if(map){
	    		    			var wmsLayer = L.tileLayer.wms(oWmsLayer.url, {
	    		    			    layers: oWmsLayer.layers,
	    		    			    format: oWmsLayer.format,
	    		    			    transparent: oWmsLayer.transparent,
	    		    			    attribution: oWmsLayer.attribution,
	    		    			    styles: oWmsLayer.styles,
	    		    			    version: oWmsLayer.version,
	    		    			    crs: oWmsLayer.crs
	    		    			});
	    		    			wmsLayer.setOpacity(oWmsLayer.opacity);
	    						if(oWmsLayer.index !== ""){
	    							wmsLayer.setZIndex(oWmsLayer.index);
	    						}
	    						oWmsLayer.markerGroup.addLayer(wmsLayer);
	    						oWmsLayer.markerGroup.addTo(map);
	    		    		}else{
	    		    			alert("Map is not defined!");
	    		    		}
	    				}else{
	    					oWmsLayer.markerGroup.clearLayers();
	    				}
	    			});	
					
					// Saving current tile layer
					instance._data.wmsLayers.push(checkBox.attr("id"));
				}
				
			},
			
			/**
			 * Function to add onClick event to span layer
			 * @param oCheckBox
			 */
			"_fnRegisterSpanEvents": function registerSpanEvents(oCheckBox){
				var spanLayer = jQuery("#"+oCheckBox.attr("id")+"_span");
				spanLayer.click(function(){
          			if(oCheckBox.prop("checked")){
          				oCheckBox.prop("checked", false);
          			}else{
          				oCheckBox.prop("checked", true);
          			}
          			oCheckBox.trigger("change");
          		});
			},
			
			/**
			 * Function to add onChange event to checkboxes layers
			 * 
			 * @param oCheckbox
			 */
			"_fnRegisterCheckboxEvents": function registerCheckboxEvent(oCheckbox, oLayer){
				// Getting mapInstance
				var instance = this;
				var map = instance._data.map;
				// When checkbox changes
				oCheckbox.change(function(){
					// Getting checkbox id and children layers
					var currentLayerId = jQuery(this).attr("id");
					var childrenLayers = jQuery("input[id^="+currentLayerId+"]");
					// If checkbox is checked
					if(jQuery(this).prop('checked')){
						// Show loading icon
						instance.fnShowLoadingIcon(oLayer);
						// Clearing child layers if exists
						var fieldLayers = oLayer.fieldsConfig;
						if(fieldLayers !== undefined){
							// Clear all layers to be sure that not displays more than one
							for(var i=0;i<fieldLayers.length;i++){
								fieldLayers[i].layerGroup.clearLayers();
							}
						}
						// Parent checbox must check childrens and enable
						jQuery.each(childrenLayers, function(index, children){
							if(children.id !== currentLayerId){
								jQuery(children).prop('checked', true);
							}
						});
						// Getting checkbox data
		    			var data = jQuery(this).data();
		    			// Getting results to display on the map
		    			instance.fnGetResultList(data, oLayer);
					}else{
						// Parent checbox must uncheck childrens when parent checkbox 
						// is unchecked
						jQuery.each(childrenLayers, function(index, children){
							if(children.id !== currentLayerId){
								jQuery(children).prop('checked', false);
							}
						});
						// Clear all field layers
						var fieldsConfig = oLayer.fieldsConfig;
						if(fieldsConfig != undefined){
							for(var i=0; i < fieldsConfig.length;i++){
								fieldsConfig[i].layerGroup.clearLayers();
							}
						}
					}
				});
			},
			
			/**
			 * Function to add onClick event to span field entity layers
			 * @param oCheckBox
			 */
			"_fnRegisterSpanFieldsEvents": function registerSpanFieldsEvents(oCheckBox){
				var spanLayer = jQuery("#"+oCheckBox.attr("id")+"_span");
				spanLayer.click(function(){
          			if(oCheckBox.prop("checked")){
          				oCheckBox.prop("checked", false);
          			}else{
          				oCheckBox.prop("checked", true);
          			}
          			oCheckBox.trigger("change");
          		});
			},
			
			/**
			 * Function to add onChange event to checkboxes field entity layers
			 * 
			 * @param oCheckbox
			 */
			"_fnRegisterCheckboxFieldsEvents": function registerCheckboxEvent(oCheckbox, oLayer, oParentLayer){
					// Getting instance
					var instance = this;
					var map = instance._data.map;
					// Getting the field layers and adding event
					// When checkbox field layers changes
					var checkBoxId = oCheckbox.attr("id");
					var checkBox = jQuery("#" + checkBoxId);
					checkBox.change(function(){
						var data = jQuery(this).data();
						if(jQuery(this).prop('checked')){
							var fieldLayers = oParentLayer.fieldsConfig;
							// Clear all layers to be sure that not displays more than one
							for(var i=0;i<fieldLayers.length;i++){
								fieldLayers[i].layerGroup.clearLayers();
							}
							// Force check master checkbox
							oParentLayer.checkBox.prop("checked",true);
							// If oLayer has data loaded
							if(oParentLayer.data !== undefined){
								// Display records using oLayer data
								instance._fnDisplayRecordsOnMap(oParentLayer.data, map, oParentLayer);
							}else{
								instance.fnGetResultList(oParentLayer.checkBox.data(), oParentLayer);
							}
							
						}else{
							oLayer.layerGroup.clearLayers();
						}
					});
			},
			
			/**
			 * Function to initialize Tool Bar
			 * 
			 * @param divId
			 * */
			"_fnInitializeToolBar": function initializeToolBar() {
				// Saving instance
				var instance = this;
				// Saving toolbar on map instance
				this._data.toolBar = jQuery(".mapviewer_tools_bar");
				// Adding default hand tool
				if(this._data.toolBar.length > 0){
					this._fnAddDefaultHandTool();
				}
				
				// Adding toolbar control
				this._fnAddToolbarControl(this._data.map);
				
				var defaultTool = jQuery("#default_tool");
				
				// Saving hand as current tool
				this._data.currentTool = defaultTool;
				
				// Adding click event
				defaultTool.click(function(){
					if(!jQuery(this).hasClass("mapviewer_tool_selected")){
						// Removing previous selected element
						var response = true;
						var currentTool = instance._data.currentTool;
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
							instance._data.currentTool = jQuery(this);
							jQuery(this).addClass("mapviewer_tool_selected");
						}
						
						return response;
					}
				});
				// Adding deactivate event
				defaultTool.on("deselectCurrentTool", function(event){
					// Calling instance deactivate method
					var currentTool = jQuery("#" + this.id);
					currentTool.removeClass("mapviewer_tool_selected");
					currentTool.data("response", true);
				});
			},
			
			/**
			 * Function to add default hand tool
			 */
			"_fnAddDefaultHandTool": function addDefaultTool(){
				// Adding button element
				this._data.toolBar.prepend('<i id="default_tool" class="toolbar_button fa fa-hand cursor_hand_icon mapviewer_tool_selected" >&nbsp;&nbsp;&nbsp;&nbsp;</i>');
				// Modify icon to use hand icon
				jQuery(this._data.map._container).css({"cursor" : "url('resources/images/cursor_hand.png'), default"});
			},

			/**
            * Function to get map item
            **/
            "fnGetMapObject": function getMapObject(){
                    return this._data.map;
            },
            
            /**
             * Function to get toolBar item
             **/
             "fnGetToolbarObject": function getMapObject(){
                     return this._data.toolBar;
             },
            
            /**
             * Function to show loading icon of a getted layer
             * 
             * @param oLayer
             */
            "fnShowLoadingIcon": function showLoadingIcon(oLayer){
            	oLayer.loadingIcon.show();
            },
            
            /**
             * Function to hide loading icon of a getted layer
             * 
             * @param oLayer
             */
            "fnHideLoadingIcon": function hideLoadingIcon(oLayer){
            	oLayer.loadingIcon.hide();
            },
            
            /**
             * Function to get all results to display on the map
             * 
             * Recives the entity necessary data to call controller function
             * using AJAX
             */
            "fnGetResultList": function getResultList(oData, oLayer){
            	// Getting map object
            	var map = this._data.map;
            	// Getting boudingBox polygon
            	var boundingBox = this.fnGetMapBoundingBox(map);
            	
            	// Getting result entities
    			var jqxhr = jQuery.ajax(oData.path + "?entityMapList", {
    				contentType: "application/json",
    				data: boundingBox,
    				handleAs : "json",
    				type: "POST",
    				dataType: 'json'
    			});
    			// When results are getted, display results on map
    			jqxhr.done(jQuery.proxy(function(response) {
    				// Saving results on layer
    				oLayer.data = response;
    				// Displaying records on map
    				this._fnDisplayRecordsOnMap(response, map, oLayer);
    			}, this));
            },
            
            /**
             * Function to get map bounding box 
             * 
             * @param map
             */
            "fnGetMapBoundingBox": function getBoundingBox(map){
            	// Getting bounds
            	var bounds = map.getBounds();
            	
            	// Getting points
            	var northWest = bounds.getNorthWest().lng + " " + bounds.getNorthWest().lat;
            	var northEast = bounds.getNorthEast().lng + " " + bounds.getNorthEast().lat;
            	var southEast = bounds.getSouthEast().lng + " " + bounds.getSouthEast().lat;
            	var southWest = bounds.getSouthWest().lng + " " + bounds.getSouthWest().lat;
            	
            	// Construct points
            	var points = northWest + ", " + northEast + ", " + southEast + ", " + southWest + ", " + northWest;
            	
            	return points;
            },
            
            /**
             * 
             * Function to reload data of all layers
             * 
             * @param instance
             * @param layers
             */
            "_fnReloadDataByCheckedLayers": function reloadDataByCheckedLayer(instance, layers){
            	for(layer in layers){
					var oLayer = layers[layer];
					// If current layer is checked, get info
					var isChecked = oLayer.checkBox.prop("checked");
					if(isChecked){
						instance.fnGetResultList(oLayer.checkBox.data(), oLayer);
					}
					
				}
            },
            
            /**
             * This method displays records on currect map using entity configuration
             * 
             * @param oRecords Result of the AJAX method
             * @param oMap Map object where display records
             * @param layerGroup layer group where add records
             */
            "_fnDisplayRecordsOnMap": function displayRecordsOnMap(oRecords, oMap, oLayer){
            	// Getting instance
            	var instance = this;
            	// Getting all fields config
            	var fieldsConfig = oLayer.fieldsConfig;
            	var fieldsConfigLength = fieldsConfig.length;
            	// Getting all records
        		jQuery.each(oRecords, function(index, item){
        			for(var i = 0;i<fieldsConfigLength;i++){
        				// Getting field configuration
        				var fieldConfig = fieldsConfig[i];
        				// Getting checkbox and checkboxdata
        				var fieldLayerCheckbox = fieldConfig.checkBox;
        				var checkboxData = fieldLayerCheckbox.data();
        				// Checking if is necessary to display this field
        				var checkboxId = fieldLayerCheckbox.attr("id");
        				var fieldLayerCheckbox = jQuery("#" + checkboxId);
        				if(fieldLayerCheckbox.prop("checked")){
        					// Getting layer group
            				var layerGroup = fieldConfig.layerGroup;
            				// Checking that display element exists
                			var fieldToDisplayValue = item[checkboxData.field];
                			
                			if( fieldToDisplayValue !== undefined){
                				// Format WKT correctly
                    			var wkt = formatWkt(fieldToDisplayValue);
                    			if(wkt){
                    				// Creating marker using WKT
                        			var marker = omnivore.wkt.parse(wkt);
                        			// Adding icon if necessary
                        			jQuery.each(marker._layers, function(index, layer){
                        				if(layer.setIcon != undefined){
                        					var iconMarker = L.AwesomeMarkers.icon({
                    	    				    icon: checkboxData.icon,
                    	    				    prefix: checkboxData.iconlibrary,
                    	    				    markerColor: checkboxData.markercolor,
                    	    				    iconColor: checkboxData.iconcolor
                                			});
                        					layer.setIcon(iconMarker);
                        				}else{
                        					layer.options.color = checkboxData.markercolor;
                        				}
                        				
                        			});
                        			// Adding popup info to marker
                        			var info = createMarkerInfo(item, oLayer.checkBox.data());
                        			if(info){ 
                        				marker.bindPopup(info);
                        			}
                        			
                        			// Generating unique for layer id
                        			var idValue = fieldConfig.checkBox.data().field + "_" + item[oLayer.checkBox.data().pk];
                        			var markerLayer = marker.getLayers()[0];
                        			markerLayer.options.markerId = idValue;
                        			
                        			// Checking if current id exists on map. If exists, not add again
                        			var currentMarkers = layerGroup.getLayers();
                        			var exists = false;
                        			for(x in currentMarkers){
                        				var markerId = currentMarkers[x].options.markerId;
                        				if(markerId == idValue){
                        					exists = true;
                        				}
                        			}
                        			if(!exists){
                        				// Adding marker to layerGroup
                        				layerGroup.addLayer(marker);
                        			}
                    			}
                			}else{
                				alert("ERROR. Error getting field '"+oLayer.fieldToDisplay+"'" +
                						" on entity '/"+checkboxData.path+"'. Field doesn't exists.");
                				return false;
                			}
                			// Adding groupLayer to map
            				layerGroup.addTo(oMap);
        				}
        			}
				});
        		
        		// Hiding loading icon
        		this.fnHideLoadingIcon(oLayer);
        		
            },
            
            /**
             * Function to clean records out of current bounding box
             * 
             * @param oRecords
             * @param layerGroup
             */
            /*"_fnCleanRecordsNotDisplayed": function cleanRecordsNotDisplayed(oRecords, layerGroup, fieldName, pk){
            	var currentMarkersOnLayer = layerGroup.getLayers();
            	// Checking that exists current markers
            	if(currentMarkersOnLayer.length > 0){
            		// Getting every markers
            		for(i in currentMarkersOnLayer){
            			var exists = false;
            			var marker = currentMarkersOnLayer[i];
            			var markerId = marker.options.markerId;
            			// Checking if current marker exists on records
            			for(x in oRecords){
            				var currentRecord = oRecords[x];
            				var idValue = fieldName + "_" + currentRecord[pk];
            				// If exists, mark as exists
            				if(markerId == idValue){
            					exists = true;
            				}
            			}
            			
            			// If not exists, remove marker
                		if(!exists){
                			layerGroup.removeLayer(marker);
                		}
            		}
            	}
            }*/
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
	GvNIX_Map_Leaflet._aInstances = [];
	
	/**
	 * Function to add new instances 
	 */
	GvNIX_Map_Leaflet._fnAddInstance = function(instance){
		GvNIX_Map_Leaflet._aInstances.push(instance);
	};
	
	/**
	 * Function to get Map Instance using field and current instance
	 */
	GvNIX_Map_Leaflet.fnGetInstance = function(id){
		//Getting all instances
		var instances = GvNIX_Map_Leaflet._aInstances;
		
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

/*
 * STATIC METHODS
 */

// This method formats wkt geometry to works correctly on omnivore plugin
function formatWkt(geometry){
	if(geometry != null){
		var wkt = geometry.replace("( ","(");
		return wkt.replace(" )",")");
	}else{
		return null;
	}
}

// This function manipulate created map image canvas
function manipulateCanvasFunction(savedMap) {
	var map = jQuery(".mapviewer_control");
	if(map.length > 0){
		var mapId = map.attr("id");
		// Getting map instance
		var instance = GvNIX_Map_Leaflet.fnGetInstance(mapId);
		var map = instance.fnGetMapObject();
		var img = document.createElement('img');
	    var dimensions = map.getSize();
	    img.width = dimensions.x;
	    img.height = dimensions.y;
	    img.src = savedMap.toDataURL();
	    // Opening new tab with image
	    var w = window.open();
	    jQuery(w.document.body).html(img);
	    // Printing image
	    w.print();
	    // After print, close new window
	    w.close();
	}
}

// This method returns marker info
function createMarkerInfo(item, layerData){
	if(item){
		var info = "";
		for(i in item){
			info+="<b>"+i+":</b> "+item[i]+"<br/>";
		}
		info+="<br/>";
		// Adding show button
		info+="<div style='text-align:right;'><a class='icon show_entity' target='_blank' href='"+layerData.path+"/"+item[layerData.pk]+"'></a>";
		// Adding Edit button
		info+="<a class='icon update_entity' target='_blank' href='"+layerData.path+"/"+item[layerData.pk]+"?form'></a></div>";
		return info;
	}else{
		return null;
	}
}

// Registering events
fnRegisterFunctionsToCallBack(function(context){
	jQuery(".mapviewer_control", context).each(function(index) {
		new GvNIX_Map_Leaflet(jQuery(this));
	});
});
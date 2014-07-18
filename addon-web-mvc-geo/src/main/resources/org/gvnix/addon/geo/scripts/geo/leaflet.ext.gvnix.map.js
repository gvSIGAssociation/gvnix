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
			"url": divData.url
			
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
				"url": divData.url
				
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
			 * Adding callback when Map is ready to use
			 */
			"_fnOnMapReadyCallback": jQuery.Callbacks("unique"),
			
			/**
			 * Method to initialize Map and create callbacks
			 */
			"_fnConstruct" : function(instance){
					var data = this._data;
					// Initialize callback with necessary configuration
					this._fnInitializeMap(data.id, data.center, data.zoom, data.maxZoom, data.url);
			},
			
			/**
			 * Function to initialize Map
			 * */
			"_fnInitializeMap": function initializeMap(divId, center, zoom, maxZoom, url) {

				// Getting center LatLng
                var latLng = center.split(",");
                var lat = latLng[0];
                var lng = latLng[1];
                
				// Creating Map
				var map = L.map(divId, {
	  				center: [lat , lng], 
	  				zoom: zoom
				});
				
				// Adding callback to map when ready
				//map.whenReady(this._fnOnMapReadyCallback(divId));
				
				// Configuring map
				L.tileLayer(url, {
	  			    attribution: '<a href="http://www.gvnix.org">gvNIX</a>',
	  			    maxZoom: maxZoom
	  			}).addTo(map);
				
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

// Registering events
fnRegisterFunctionsToCallBack(function(context){
	jQuery(".mapviewer_control", context).each(function(index) {
		new GvNIX_Map_Leaflet(jQuery(this));
	});
});
/**
 * Here you can configure extra configuration to send to dataTables
 */

{
	
	"fnStateSave": function(oSettings, oData) {
		// Saving oSettings on oData
		oData.oSettings = {};
		// Creating aoColumns
        var columns = [];
        for(i in oSettings.aoColumns){
            var column = { "bSortable" : oSettings.aoColumns[i].bSortable, "sName": oSettings.aoColumns[i].sName, "mData": oSettings.aoColumns[i].mData, "bSearchable": oSettings.aoColumns[i].bSearchable, "aDataSort": oSettings.aoColumns[i].aDataSort };
            columns.push(column);
        }
        oData.oSettings.aoColumns = columns;
		// Adding iDraw
		oData.oSettings.iDraw = oSettings.iDraw;
		// Adding _iDisplayStart
		oData.oSettings._iDisplayStart = oSettings._iDisplayStart;
		// Adding _iDisplayLength
		oData.oSettings._iDisplayLength = oSettings._iDisplayLength;
		// Adding oFeatures
		oData.oSettings.oFeatures = oSettings.oFeatures;
		// Adding oPreviousSearch
		oData.oSettings.oPreviousSearch = oSettings.oPreviousSearch;
		// Adding aoPreSearchCols
		oData.oSettings.aoPreSearchCols = oSettings.aoPreSearchCols;
		// Adding aaSortingFixed
		oData.oSettings.aaSortingFixed = oSettings.aaSortingFixed;
		// Adding aaSorting
		oData.oSettings.aaSorting = oSettings.aaSorting;
		
		// Saving if is filtered
		var generalSearch = oData.oSearch.sSearch;
		var filteredByColumn = false;
		for(i in oSettings.aoPreSearchCols){
			if(oSettings.aoPreSearchCols[i].sSearch !== ""){
				filteredByColumn = true;
				break;
			}
		}
		
		if(generalSearch !== "" || filteredByColumn){
			oData.isFiltered = true;
		}else{
			oData.isFiltered = false;
		}
		
		// Generating hash location
		var hashLocation = fnGetHashCode(window.location.pathname);
		// Getting statePrefix
		var statePrefix = this.data().stateprefix;
		// Generating unic sName
		var sName = hashLocation + "_";
		if(statePrefix != undefined){
			sName +=  statePrefix + "_";
		}
		sName += oSettings.sCookiePrefix + oSettings.sInstance;
		// Getting state information
		var sValue = this.oApi._fnJsonString(oData);
		var iSecs = oSettings.iCookieDuration;
		var sBaseName = oSettings.sCookiePrefix;
		var fnCallback = oSettings.fnCookieCallback;

		// If localStorage not exists, create Cookie
		if (!window.localStorage) {
			this.oApi._fnCreateCookie(sName, sValue, iSecs, sBaseName, fnCallback);
		}else{
			window.localStorage.setItem(sName,sValue);
		}
	},
	
	"fnStateLoad": function(oSettings) {
		// Generating hash location
		var hashLocation = fnGetHashCode(window.location.pathname);
		// Getting statePrefix
		var statePrefix = this.data().stateprefix;
		// Generating unic sName
		var sName = hashLocation + "_";
		if(statePrefix != undefined){
			sName +=  statePrefix + "_";
		}
		sName += oSettings.sCookiePrefix + oSettings.sInstance;
		// If localStorage not exists, create Cookie
		if(!window.localStorage){
			var sData = this.oApi._fnReadCookie(sName);
		}else{
			var sData = window.localStorage.getItem(sName);
		}
		
		var oData;
		
		try {
			oData = (typeof $.parseJSON === 'function') ? 
				$.parseJSON(sData) : eval( '('+sData+')' );
		} catch (e) {
			oData = null;
		}

		return oData;
	}
	
}
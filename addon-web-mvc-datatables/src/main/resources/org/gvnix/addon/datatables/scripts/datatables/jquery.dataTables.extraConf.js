/**
 * Here you can configure extra configuration to send to dataTables
 */

{
	
	"fnStateSave": function(oSettings, oData) {
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
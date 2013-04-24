/**
 * Initializes custom api function 
 * 
 * @param tableId
 */
function datatables_init_custom_api_functions(tableId,options){
	var tableObj = $('#'+tableId);
	var $table = tableObj.dataTable();
	
	if (options.filterOnReturn){
		// Enable filter on return
		$table.fnFilterOnReturn();
	} else {
		// Enable filtering delay
		if (options.filteringDelay) {
			try {
				$table.fnSetFilteringDelay(Number.parseInt(options.filteringDelay));
			} catch (e) {
				log("Error parsing filteringDelay option (using default): " +e);
				$table.fnSetFilteringDelay(500);
			}
		} else {
			$table.fnSetFilteringDelay(500);
		}
	}
}

/**
 * Perform datatables AJAX request using POST method
 * 
 * @param sSource
 * @param aoData
 * @param fnCallback
 * @param oSettings
 */
function doAjaxRequestByPost ( sSource, aoData, fnCallback, oSettings ) {
    oSettings.jqXHR = $.ajax( {
        "dataType": 'json',
        "type": "POST",
        "url": sSource,
        "data": aoData,
        "success": fnCallback
    });
}

/**
 * Try to show a message on browser JS console
 */
function log(message){
	try {
		console.log(message);
	} catch (e) {
		// Can't do anything
	}
}

/**
 * 
 * Hide details	Enables filtration delay for keeping the browser 
 * more responsive while searching for a longer keyword.
 * 
 * from http://datatables.net/plug-ins/api#fnSetFilteringDelay
 */
jQuery.fn.dataTableExt.oApi.fnSetFilteringDelay = function ( oSettings, iDelay ) {
    var _that = this;
 
    if ( iDelay === undefined ) {
        iDelay = 250;
    }
      
    this.each( function ( i ) {
        $.fn.dataTableExt.iApiIndex = i;
        var
            $this = this,
            oTimerId = null,
            sPreviousSearch = null,
            anControl = $( 'input', _that.fnSettings().aanFeatures.f );
          
            anControl.unbind( 'keyup' ).bind( 'keyup', function() {
            var $$this = $this;
  
            if (sPreviousSearch === null || sPreviousSearch != anControl.val()) {
                window.clearTimeout(oTimerId);
                sPreviousSearch = anControl.val(); 
                oTimerId = window.setTimeout(function() {
                    $.fn.dataTableExt.iApiIndex = i;
                    _that.fnFilter( anControl.val() );
                }, iDelay);
            }
        });
          
        return this;
    } );
    return this;
};


/**
 * 
 * This plug-in removed the default behaviour of DataTables to filter 
 * on each keypress, and replaces with it the requirement to press 
 * the enter key to perform the filter.
 * 
 * from http://datatables.net/plug-ins/api#fnFilterOnReturn
 */
jQuery.fn.dataTableExt.oApi.fnFilterOnReturn = function (oSettings) {
    var _that = this;
  
    this.each(function (i) {
        $.fn.dataTableExt.iApiIndex = i;
        var $this = this;
        var anControl = $('input', _that.fnSettings().aanFeatures.f);
        anControl.unbind('keyup').bind('keypress', function (e) {
            if (e.which == 13) {
                $.fn.dataTableExt.iApiIndex = i;
                _that.fnFilter(anControl.val());
            }
        });
        return this;
    });
    return this;
};


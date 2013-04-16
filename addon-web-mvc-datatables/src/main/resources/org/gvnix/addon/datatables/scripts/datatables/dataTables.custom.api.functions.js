/**
 * Initializes custom api function 
 * 
 * @param tableId
 */
function datatables_init_custom_api_functions(tableId){
	var $table = $('#'+tableId).datatable();
	
	// Enable filtering delay
	$table.fnSetFilteringDelay(500);
	
	// Enable filter on return
	// $table.fnFilterOnReturn();
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


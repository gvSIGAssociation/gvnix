/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */

/* Default settings
================================================== */

var options = {
    debug : false
};

/* Creating Callbacks
 ================================================= */

var functionCallbacks = jQuery.Callbacks("unique");
var callbackFunctionsNames = [];

/* JS Utilities
================================================== */

/**
 * Show your debug messages.
 * Your message can be delimited by begin and end String
 * @param msg Debug message or object
 * @param beg String to mark the begin 
 * @param end String to mark the end
 * @param indentation Number of empty chars to left-indent the message 
 */
function debug(msg, beg, end, indentation) {
  try {
    if(options.debug && isNotEmpty(msg)) {
      var indent = new Array(indentation).join(' ');

      // mark the begin
      if( isNotEmpty(beg) ) {
        console.log(beg);
      }

      // Log the message/object
      if(jQuery.isPlainObject( msg )) {
          console.log(msg);
      }
      else {
        console.log(indent + msg);
      }

      // mark the end
      if( isNotEmpty(end) ) {
        console.log(end);
      }
    }
  }
  catch (e) {
    // do nothing, console.log is undefined
  }
}

/**
 * Convert JavaScript data structures into JSON text.
 * 
 * @param obj
 * @returns
 */
function toJson(obj) {
  return JSON.stringify(obj);
}

/**
 * Checks if an object in JavaScript is undefined
 * 
 * @param obj
 * @returns {Boolean}
 */
function isUndefined(obj) {
  if(typeof obj === "undefined") {
    return true;
  }
  return false;
}

/**
 * Checks if an object in JavaScript is defined
 * 
 * @param obj
 * @returns {Boolean}
 */
function isDefined(obj) {
  return !isUndefined(obj);
}

/**
 * Checks if an object in JavaScript is undefined or null
 * 
 * @param obj
 * @returns {Boolean}
 */
function isNull(obj) {
  if( typeof obj === "undefined" || obj == null ) {
    return true;
  }
  return false;
}

/**
 * Checks if an object in JavaScript is defined and not null
 * 
 * @param obj
 * @returns {Boolean}
 */
function isNotNull(obj) {
  return !isNull(obj);
}

/**
 * Checks if an object in JavaScript is undefined, null or empty string
 * 
 * @param obj
 * @returns {Boolean}
 */
function isEmpty(obj) {
  if(jQuery.isPlainObject(obj)) {
    return jQuery.isEmptyObject(obj);
  }
  else if(jQuery.isArray(obj)) {
    return 0 === obj.length;
  }
  else if ( typeof obj === "string" ) {
    return (isNull(obj) || 0 === obj.length);
  }
  return isNull(obj);
}

/**
 * Checks if an object in JavaScript is undefined, null or empty string
 * 
 * @param obj
 * @returns {Boolean}
 */
function isNotEmpty(obj) {
  return !isEmpty(obj);
}

/**
 * Locale of the browser.
 */
function getLocale() {
  var locale = window.navigator.userLanguage || window.navigator.language;
  return locale;
}

/**
 * Language of the browser.
 */
function getLanguage() {
  var locale = getLocale();

  if( isEmpty(locale) ) {
    return "";
  }

  var language = locale.substring(0, 2);
  return language; 
}

/* jQuery Utilities
================================================== */

/** 
 * Convert Java's SimpleDateFormat to jQuery UI datepicker formatDate.
 * Takes a Java pattern 
 * (http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html) 
 * and turns it into the expected jQueryUI formatDate 
 * (http://api.jqueryui.com/datepicker/).
 * 
 * Function authors: Laurent Picquet, Danny 
 */
function jQueryDateFormat(pattern){

  debug("gvNIX :: Java DateFormat :: " + pattern);

  // Year
  if(pattern.search(/y{3,}/g) >= 0) {       // yyyy to yy
    pattern = pattern.replace(/y{3,}/g, "yy");
  }
  else if(pattern.search(/y{2}/g) >=0) {    // yy to y
    pattern = pattern.replace(/y{2}/g, "y");
  }

  // Month
  if(pattern.search(/M{4,}/g) >=0) {        // MMMM to MM
    pattern = pattern.replace(/M{4,}/g, "MM");
  }
  else if(pattern.search(/M{3}/g) >=0) {    // MMM to M
    pattern = pattern.replace(/M{3}/g, "M");
  }
  else if(pattern.search(/M{2}/g) >=0) {    // MM to mm
    pattern = pattern.replace(/M{2}/g, "mm");
  }
  else if(pattern.search(/M{1}/g) >=0) {    // M to m
    pattern = pattern.replace(/M{1}/g, "m");
  }

  // Day
  if(pattern.search(/D{2,}/g) >=0) {        // DD to oo
    pattern = pattern.replace(/D{2,}/g, "oo");
  }
  else if(pattern.search(/D{1}/g) >=0) {    // D to o
    pattern = pattern.replace(/D{1}/g, "o");
  }

  // Day in week
  if(pattern.search(/E{4,}/g) >=0) {        // EEEE to DD
    pattern = pattern.replace(/E{4,}/g, "DD");
  }
  else if(pattern.search(/E{2,3}/g) >=0) {  // EEE to D
    pattern = pattern.replace(/E{2,3}/g, "D");
  }

  // Note there is no need to convert "day in month" because jQuery
  // format and Java DateFormat patterns are the same

  debug("gvNIX :: jQueryDateFormat :: " + pattern);
  return pattern;
}
/**
 * Show a message to user
 * 
 * @param title for dialog
 * @param message to show
 * @param targetId (optional) if set put the message as contents of targetId
 * 		otherwise this function creates a jQueryUI dialog and show it
 */
function showMessage(title, message,targetId) {
	jQuery('<div title="'+title+'">'+message+'</div>').dialog();
}

/* Application initialization
================================================== */

function jQueryInitializeComponents(context) {
	if (!context || context === undefined) {
		context = document;
	}
    // Notes:
    // * About jQuery .data() vs .attr() performance : http://jsperf.com/jquery-data-vs-attr

    var lang = getLanguage();
    debug("gvNIX :: Navigator lang :: " + lang);

    // Date inputs init
    jQuery(".datepicker",context).each(function( index ) {
      var $input = jQuery(this);
      var pattern = $input.attr("data-dateformat");

      if(isNotEmpty(pattern)) {
        $input.datepicker({ dateFormat: jQueryDateFormat( pattern ) });
      }
      else {
        $input.datepicker();
      }
    });

    // TinyMCE editors init
    jQuery(".tinymce",context).each(function( index ) {
      var $textarea = jQuery(this);
      $textarea.tinymce({
        changeMonth: true,
        changeYear: true,
        theme : "modern",
        menubar : false
      });
    });

    // Validation defaults
    jQuery.validator.setDefaults({ 
      ignoreTitle: true 
    });

    // Form validation init
    jQuery("form.validate",context).each(function( index ) {
      var $form = $(this);

      // see options at http://docs.jquery.com/Plugins/Validation/validate
      $form.validate({
        errorElement: "span",
        errorClass: "errors",
        errorPlacement: function(error, element) {
          error.insertAfter(element);
        }
      });

      // Iterate form inputs to set validation rules and messages
      $form.find("input,textarea,select").each(function( index ) {
        var $input = $(this);
        var name = $input.attr("name");
        var data = $input.data();

        debug(data, "gvNIX :: " + name + " :: ");

        // this input validation rules
        var rules = {
            required: data.required,
            messages: {
              required: data.missing,
              remote: data.invalid
            }
        };

        if( isNotEmpty(data.minlength) ) {
          rules["minlength"] = data.minlength;
        }

        if( isNotEmpty(data.maxlength) ) {
          rules["maxlength"] = data.maxlength;
        }

        if( isNotEmpty(data.regex) ) {
          rules["pattern"] = data.regex;
        }

        if( isNotEmpty(data.mindecimal) ) {
          rules["min"] = data.mindecimal;
        }

        if( isNotEmpty(data.maxdecimal) ) {
          rules["max"] = data.maxdecimal;
        }

        $input.rules("add", rules);
      });
    });

    // Tooltip for all input ant textarea elements. Show text in data-prompt attribute
    jQuery("input,textarea,select",context).each(function( index ) {
      var $input = $(this);

      $input.tooltip({ 
        position: { my: "left+15 center", at: "right center" }
      });
    });

    //Calling all callback functions
    callbackFunctionsNames.forEach(function(name){
    	functionCallbacks.fire(name,[context]);
    });
}


/**
 * Function to add callbacks
 */
function fnRegisterFunctionsToCallBack(callback){
	functionCallbacks.add(callback);
	callbackFunctionsNames.push(callback.name);
}

!function ($) {

  $(function(){
	  jQueryInitializeComponents();
  });

}(window.jQuery);

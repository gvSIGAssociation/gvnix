/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
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
 * Convert Java's SimpleDateFormat to momentJS formatDate.
 * Takes a Java pattern
 * (http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html)
 * and turns it into the expected momentJS formatDate
 * (http://momentjs.com/docs/#/parsing/string-format/).
 *
 * @param pattern SimpleDateFormat pattern
 * @return moment pattern (if 'pattern' is ommited return defautl pattern)
 */
function momentDateFormat(pattern) {

	debug("gvNIX :: Java DateFormat :: " + pattern);

	if (pattern) {
		// Year
		if (pattern.search(/y{3,}/g) >= 0) {
			pattern = pattern.replace(/y{3,}/g, "YYYY"); // yyyy to yy
		} else if (pattern.search(/y{2}/g) >= 0) { // yy to YY
			pattern = pattern.replace(/y{2}/g, "YY");
		}

		// Day
		if (pattern.search(/d{2,}/g) >= 0) { // dd to DD
			pattern = pattern.replace(/d{2,}/g, "DD");
		} else if (pattern.search(/d{1}/g) >= 0) { // d to D
			pattern = pattern.replace(/d{1}/g, "D");
		} else if (pattern.search(/D{1,}/g) >= 0) { // D,DD, DDD to DDD
			pattern = pattern.replace(/D{1,}/g, "DDD");
		}

		// Day in week
		if (pattern.search(/E{4,}/g) >= 0) { // EEEE to dddd
			pattern = pattern.replace(/E{4,}/g, "dddd");
		} else if (pattern.search(/E{2,3}/g) >= 0) { // EEE to ddd
			pattern = pattern.replace(/E{2,3}/g, "ddd");
		}

		// Day in week (number)
		if (pattern.search(/F{1}/g) >= 0) { // F to e
			pattern = pattern.replace(/F{1}/g, "e");
		}

		// week of the year
		if (pattern.search(/w{1,}/g) >= 0) { // ww to WW
			pattern = pattern.replace(/w{1,}/g, "WW");
		}
	} else {
		var pattern = "YYYY/MM/DD HH:mm";
	}


  debug("gvNIX :: momentDateFormat :: " + pattern);
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

/**
 * Informs if date format (momentJS) includes date information
 *
 * @param format string
 * @returns true if !format or format contains ('YQDMdw')
 */
function isDateFormatDate(format) {
    if (!format) {
       return true;
	}
    return format.search(/[YQDMdw]/) > -1;
}

/**
 * Informs if date format (ISO 8601) includes time information
 *
 * @param format string
 * @returns true if !format or format contains ('HmAasSZ')
 */
function isDateFormatTime(format) {
    if (!format) {
       return true;
	}
    return format.search(/[HhmAasSZ]/) > -1;
    ;
}

/**
 * Select the most switchable time format for time selectod
 * related to requiered format
 *
 * @param format
 * @returns time format
 */
function getSelectorTimeFormat(format) {
	//
	if (format.search(/h{1,2}/) > -1 && format.search(/[aA]/) > -1) {
		if (format.search(/[A]/) > -1) {
			return "hh:mm A";
		} else {
			return "hh:mm a";
		}
	}
	return "HH:mm";
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
    jQuery(".datetimepicker",context).each(function( index ) {
      var $input = jQuery(this);
      var pattern = $input.attr("data-dateformat");
      var timeStep = $input.attr("data-timestep");
      try {
        timeStep = parseInt(timeStep);
      } catch (e) {
        timeStep = 5;
      }

      if(isNotEmpty(pattern)) {
        var momentPattern = momentDateFormat(pattern);
        $input.datetimepicker({format: momentPattern,
            datepicker: isDateFormatDate(momentPattern),
            timepicker: isDateFormatTime(momentPattern),
            step: timeStep,
            formatDate: "YYYY/MM/DD",
            formatTime : getSelectorTimeFormat(momentPattern)});
      }
      else {
        var momentPattern = momentDateFormat();
        $input.datetimepicker({step: timeStep,
            format: momentPattern,
            formatDate: "YYYY/MM/DD",
            formatTime : "HH:mm" });
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

        if ( isNotEmpty(data.dateformat)) {
          rules["dateformat"] = momentDateFormat(data.dateformat);
          rules["messages"] = {
            'dateformat' : data.invalid
          };
        } else if ($input.hasClass("datetimepicker")){
          rules["dateformat"] = "ANY";
          rules["messages"] = {
            'dateformat' : data.invalid
          };
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

    functionCallbacks.fire(context);

}

/**
 * Initialize jQuery Validator methods
 */
function initializeValidations() {
	/**
	 * Date/time validation with format
	 *
	 * @name jQuery.validator.methods.number
	 * @type Boolean
	 */
	jQuery.validator.addMethod("dateformat", function(value, element, params) {
		if (this.optional(element)) {
			return true;
		}
		if (params == "ANY") {
			return  moment(value).isValid();
		} else {
			return  moment(value,params, true).isValid();
		}
	}, "Please enter a correct date/time");

     /**
	 * Validator for loupefield input
	 *
	 * @name jQuery.validator.methods.loupefield
	 * @type Boolean
	 */
	jQuery.validator.addMethod("loupefield", function(value, element){
		var data = jQuery(element).data();
		var loupeInstance = GvNIX_Loupe.fnGetInstance(element.id, data.field);
		return !loupeInstance.fnHasError();
	}, "Please select a valid register");

     /**
	 * Replaces the standar number validation to support number with comma.
	 *
	 * @name jQuery.validator.methods.number
	 * @type Boolean
	 */
	jQuery.validator.addMethod("number", function(value, element) {
		var localizedValue = jQuery.parseNumber(value, {locale: getLanguage(), strict: true});
		return this.optional(element) || !isNaN(localizedValue);
	}, "Please enter a valid number");

	/**
	 * Replaces the standar min validation to support number with comma.
	 *
	 * @name jQuery.validator.methods.number
	 * @type Boolean
	 */
	jQuery.validator.addMethod("min", function(value, element, params) {
		var localizedValue = jQuery.parseNumber(value, {locale: getLanguage()});
		return this.optional(element) || localizedValue >= params;
	}, jQuery.validator.format("Please enter a value greater than or equal to {0}."));

	/**
	 * Replaces the standar max validation to support number with comma.
	 *
	 * @name jQuery.validator.methods.number
	 * @type Boolean
	 */
	jQuery.validator.addMethod("max", function(value, element, params) {
		var localizedValue = jQuery.parseNumber(value, {locale: getLanguage()});
		return this.optional(element) || localizedValue >= params;
	}, jQuery.validator.format("Please enter a value less than or equal to {0}."));

	/**
	 * Replaces the standar range validation to support number with comma.
	 *
	 * @name jQuery.validator.methods.number
	 * @type Boolean
	 */
	jQuery.validator.addMethod("range", function(value, element, params) {
		var localizedValue = jQuery.parseNumber(value, {locale: getLanguage()});
		return this.optional(element) ||( localizedValue >= param[0] && localizedValue <= param[1] );
	}, jQuery.validator.format("Please enter a value between {0} and {1}."));
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
	  initializeValidations();
	  jQueryInitializeComponents();
  });

}(window.jQuery);

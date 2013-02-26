/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Quick links action handlers for relationship add-on
 */

dojo.require("dojox.encoding.base64");

/*
 * Show a javascript confirmation message before delete.
 */
function gvnix_delete_confirm(msg, element, compositePkField) {

  if (gvnix_any_selected(element)) {
      var r = confirm(msg);
      if (r == true) {
          gvnix_delete(element, compositePkField);
      }
  } else {
      alert(GVNIX_MSG_SELECT_ROW_TO_DELETE);
  }
}

/*
 * Check if there is any row selected
 */
function gvnix_any_selected(element) {
    var forms = dojo.query('input[id^="gvnix_checkbox_' + element + '"]');
    var any = false;
    forms.forEach(function(node, index, arr) {
      // If selected row
      if (node.checked == true) {
        any = true;
      }
    });
    return any;
}

/*
 * Activate all selected rows and submit delete form.
 */
function gvnix_delete(element, compositePkField) {

  gvnix_copy_values(element, compositePkField, "update");

    // Get all row checkbox
    var forms = dojo.query('input[id^="gvnix_checkbox_' + element + '"]');
    var any = false;
    forms.forEach(function(node, index, arr) {

        // If selected row
        if (node.checked == true) {

          // Get all form elements from this row
          var elems = dojo.query('*[id^="_' + element + '[' + node.value + ']_"]');
          elems.forEach(function(node, index, arr) {

              // Enable this row form elements (will be submitted)
              node.disabled = false;
              any = true;

          });
        }
    });

    // If any row selected
    if (any == true) {

        // Get delete form
        var forms = dojo.query('form[id^="gvnix_form_' + element + '"]');

        // Set required REST values to delete and submit
        forms[0].method = 'POST';
        forms[0].elements['_method'].value = 'DELETE';
        forms[0].submit();
    }
}

/*
 * Select/unselect all checkbox fields when select/unselect select all checkbox.
 */
function gvnix_select_all(element) {

    // Get select all checkbox
    var selectAll = dojo.query('input[id="gvnix_select_all_' + element + '"]');

    // Get all rows checkbox
    var checkboxes = dojo.query('input[id^="gvnix_checkbox_' + element + '"]');
    checkboxes.forEach(function(node, index, arr) {

        // Select/unselect checkbox as select all checkbox
        if (selectAll[0].checked == true) {

            node.checked = true;
        }
        else {

            node.checked = false;
        }

    });
}

/*
 * Activate all selected rows form elements and show update confirmation controls.
 */
function gvnix_edit(element) {

    // Get all rows checkbox
    var forms = dojo.query('input[id^="gvnix_checkbox_' + element + '"]');
    var any = false;
    forms.forEach(function(node, index, arr) {

        // If selected row
        if (node.checked == true) {

            // Get all form elements from this row
            var elems = dojo.query('*[id^="_' + element + '[' + node.value + ']_"]');
            elems.forEach(function(node, index, arr) {

                // Enable this row form elements (will be submitted)
                node.disabled = false;
                any = true;

            });
        }
    });

    // Make visible the update confirmation controls
    if (any == true) {
        var controlUpdateDiv = dojo.byId('gvnix_control_update_'+element);
        controlUpdateDiv.style.display = "block";
    }
}

/*
 * Show all form add rows and show add confirmation controls.
 */
function gvnix_create(element) {

    // Get all add rows and make it visible
    var rows = dojo.query('tr[id^="gvnix_add_row_' + element + '"]');
    rows.forEach(function(node, index, arr) {

        node.style.display = "table-row";
    });

    // Make visible the create confirmation controls
    var controlAddDiv = dojo.byId('gvnix_control_add_'+element);
    controlAddDiv.style.display = "block";
}

/*
 * Disable all not selected rows (not submitted) and submit add form.
 */
function gvnix_add(element, compositePkField) {

  // Before form submit, copy each visible field value to it related hidden field
  gvnix_copy_values(element, compositePkField, "create");

  // Get all inputs: if some row's inputs has value, submit row
  var inputs = dojo.query('input[type="text"][id$="_create"]');

  inputs.forEach(function(input) {
    checkselected = false;
    // If value and not checkbox type (always has value)
    if (input.value != '' && input.type != 'checkbox') {
      checkselected = true;
    }
    else {
      // If input has value, activate it
      input.disabled = true;
    }
    if (checkselected) {
      // Mark checkbox for row sumit
      indexPatt = /\[.*\]/;
      itemIndex = input.id.match(indexPatt);
      itemCheckbox = dojo.byId('gvnix_checkbox_add_' + element + itemIndex[0]);
      itemCheckbox.disabled = false;
    }
  });

  var hiddenChecks = dojo.query('input[id^="gvnix_checkbox_add_' + element + '"]');
  hiddenChecks.forEach(function(input) {
    if (input.disabled) {
      indexPatt = /\[.*\]/;
      itemIndex = input.id.match(indexPatt);
      var trToRemove = dojo.byId('gvnix_add_row_' + element + itemIndex[0]);
      trToRemove.parentNode.removeChild(trToRemove);
    }
  });

  // Get add form and submit them
  var forms = dojo.query('form[id^="gvnix_add_form_' + element + '"]');
  forms[0].submit();
}

/*
 * Submit update form.
 */
function gvnix_update(element, compositePkField) {

  // Before form submit, copy each visible field value to it related hidden field
  gvnix_copy_values(element, compositePkField, "update");

  // Get update form and submit them
  var forms = dojo.query('form[id^="gvnix_form_' + element + '"]');
  forms[0].submit();
}

/*
 * Copy each visible field value to it related hidden field.
 */
function gvnix_copy_values(element, compositePkField, mode) {

  // Get hidden input types
  var hiddens = dojo.query('input[id^="' + element + '"]');
  hiddens.forEach(function(hidden, index, arr) {

	// Get field value to submit from dojo visible field and set it on dojo related hidden field value
	hidden.value = gvnix_get_value("_" + hidden.id);

	// When field is part of a composite primary key, encode them into a unique hidden field for this register 
	if (compositePkField != '' && hidden.name.indexOf("." + compositePkField) != -1) {
		gvnix_encode_pk(hidden.id.substring(0, hidden.id.length - "_id_xxxxxx".length), mode);
	}
  });
}

/*
 * Get field value related with some field.
 * 
 * Obtain field value is different according with dojo field type. 
 */
function gvnix_get_value(inputId) {

    // Get different representations of visible input type related to hidden type
    var input = dojo.byId(inputId);
    var inputDijit = dijit.byId(inputId);
    
    if (input == null) {
    	return;
    }
    else if (input.type == 'checkbox') {

    	// If checkbox type, convert possible null value to unchecked value
    	var press = input.getAttribute("aria-pressed");
		if (press == null) {
			press = 'off';
    	}
	    return press;
    }
    else if (input.getAttribute("aria-autocomplete") == "list") {
	
		// If dojo autocomplete list type, get the real value (input value is only for visualization)
    	return inputDijit.get("value");
    }
    else if (input.selectedIndex != null) {
    
    	// If select, get the real value from selected option value
    	return input.options[input.selectedIndex].value;
    }
    else {
	    // Set visible value to hidden input
    	return input.value;
    }
}

/*
 * Get composite primary key values and encode it in one field.
 */
function gvnix_encode_pk(prefix, mode) {
  var obj = new Object();
  dojo.query("*[id^=\"_" + prefix + ".\"]").forEach(function(node, index, nodelist){
	if (node.id.substring(node.id.length - mode.length, node.id.length) == mode) {
	  obj[node.id.substring(prefix.length + 2, node.id.length - 10)] = gvnix_get_value(node.id);
	}
  });
  var json = dojo.toJson(obj);
  var tokArr = [];
  for (var i = 0; i < json.length; i++) {
    tokArr.push(json.charCodeAt(i));
  }
  var encoded = dojox.encoding.base64.encode(tokArr);
  var id = dojo.byId(prefix + '_id_' + mode);
  if (id != null) {
	  id.value = encoded;
  }
}

/*
 * Enable update form if any row selected.
 */
function gvnix_edit_ward(element) {
  if ( gvnix_any_selected(element) && !gvNixEditMode ) {
      gvNixChangesControl();
      gvnix_edit(element);
  } else {
	  alert(GVNIX_MSG_SELECT_ROW_TO_UPDATE);
  }
}

/*
 * Enable create form if we're not in edit mode.
 */
function gvnix_create_ward(element) {
  if ( !gvNixEditMode ) {
      gvNixChangesControl();
      gvnix_create(element);
  }
}

/*
 * Go to some URL to create item.
 */
function gvnix_create_item(url) {
  location.href = url;
}

/*
 * Get selected checkbox and reload page with this one identifier value placed in a URL param.
 */
function gvnix_edit_item(detailPath, element, urlParams, compositePkField, idField) {
	
  if ( !gvnix_any_selected(element)){
	  alert(GVNIX_MSG_SELECT_ROW_TO_UPDATE);
	  return;
  }

  // Before edit, copy each visible field value to it related hidden field
  gvnix_copy_values(element, compositePkField, "update");

  var url = detailPath + "/";
  var checkBoxes = dojo.query('input[id^="gvnix_checkbox_' + element + '"]');
  var nodeChecked;
  checkBoxes.forEach(function(node, index, arr) {
    if (node.checked == true) {
      if (nodeChecked != undefined) {
        alert(GVNIX_MSG_UPDATE_ONLY_ONE_ROW);
        return;
      }
      nodeChecked = node;
    }
  });
  if (compositePkField != "") {
    var pkId = element + '[' + nodeChecked.value + ']_' + compositePkField + '_id_update';
  } else {
    var pkId = '_' + element + '[' + nodeChecked.value + ']_' + idField + '_id_update';
  }
  url += dojo.byId(pkId).value;
  location.href = url + urlParams;
}

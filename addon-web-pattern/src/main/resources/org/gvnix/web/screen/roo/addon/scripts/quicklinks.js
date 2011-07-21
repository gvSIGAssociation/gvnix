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

/*
 * Show a javascript confirmation message before delete.
 */
function gvnix_delete_confirm(msg, element) {

  if (gvnix_any_selected(element)) {
      var r = confirm(msg);
      if (r == true) {
          gvnix_delete(element);
      }
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
function gvnix_delete(element) {

  gvnix_copy_values(element);

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

        var forms2 = dojo.query('div[id^="gvnix_control_update"]');
        forms2.forEach(function(node2, index2, arr2) {

            node2.style.display = "block";
        });
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
    var forms2 = dojo.query('div[id^="gvnix_control_add"]');
    forms2.forEach(function(node2, index2, arr2) {

        node2.style.display = "block";
    });

  // Fix element height adding 137px to its previous height definition
    var divToOverflow = dojo.query('#relations > div[class*="dijitTabPaneWrapper"]')[0];
    var divHeight = dojo.style(divToOverflow, 'height');
    dojo.style(divToOverflow, 'height', divHeight + 137 + "px");
    divHeight = dojo.style(dojo.byId('relations'), 'height');
    dojo.style(dojo.byId('relations'), 'height', divHeight + 137 + "px");

}

/*
 * Disable all not selected rows (not submitted) and submit add form.
 */
function gvnix_add(element) {

  // Before form submit, copy each visible field value to it related hidden field
  gvnix_copy_values(element);

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
function gvnix_update(element) {

  // Before form submit, copy each visible field value to it related hidden field
  gvnix_copy_values(element);

  // Get update form and submit them
  var forms = dojo.query('form[id^="gvnix_form_' + element + '"]');
  forms[0].submit();
}

/*
 * Copy each visible field value to it related hidden field.
 */
function gvnix_copy_values(element) {

  // Get hidden input types
  var hiddens = dojo.query('input[id^="' + element + '"]');
  hiddens.forEach(function(hidden, index, arr) {

    // Get different representations of visible input type related to hidden type
  var inputId = "_" + hidden.id;
    var input = dojo.byId(inputId);
    var inputDijit = dijit.byId(inputId);

    if (input.type == 'checkbox') {

      // If checkbox type, convert possible null value to unchecked value
      var press = input.getAttribute("aria-pressed");
      if (press == null) {
        press = 'off';
      }
      hidden.value = press;
    }
    else if (input.getAttribute("aria-autocomplete") == "list") {

      // If select type, get the real value (input value is only for visualization)
      hidden.value = inputDijit.get("value");
    }
    else {
      // Set visible value to hidden input
      hidden.value = input.value;
    }
  });
}

/*
 * Enable update form if any row selected
 */
function gvnix_edit_ward(element) {
  if ( gvnix_any_selected(element) ) {
      gvNixChangesControl();
      gvnix_edit(element);
  }
}

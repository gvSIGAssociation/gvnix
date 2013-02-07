/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
 * Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.web.menu.roo.addon;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.Completion;
import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides conversion to and from {@link PageMenuEntry}, with full support for
 * using IDs to identify a menu entry related to a web page.
 * 
 * @author Jose Manuel Vivó ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Enrique Ruiz ( eruiz at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class MenuEntryConverter implements Converter<MenuEntry> {

    @Reference private MenuEntryOperations operations;

    /**
     * Check if given type can be converted by this Converter
     * 
     * @param requiredType Can this type be converted?
     * @param optionContext
     */
    public boolean supports(Class<?> requiredType, String optionContext) {
        return MenuEntry.class.isAssignableFrom(requiredType);
    }

    /**
     * Convert given ID to {@link PageMenuEntry}
     * 
     * @param value Page ID
     * @param requiredType [Not used]
     * @param optionContext [Not used]
     */
    public MenuEntry convertFromText(String value, Class<?> requiredType,
            String optionContext) {
        return new MenuEntry(value);
    }

    /**
     * Get all values
     * 
     * @param completions
     * @param requiredType
     * @param existingData
     * @param optionContext
     * @param target
     */
    public boolean getAllPossibleValues(List<Completion> completions,
            Class<?> requiredType, String existingData, String optionContext,
            MethodTarget target) {
        Document document = operations.getMenuDocument();

        // make the root element of the menu the one with the menu identifier
        // allowing for different decorations of menu
        Element rootElement = XmlUtils.findFirstElement("//*[@id='_menu']",
                (Element) document.getFirstChild());

        List<Element> elements = null;

        if (MenuEntryOperations.CATEGORY_MENU_ITEM_PREFIX.equals(optionContext)) {
            elements = XmlUtils.findElements(
                    "//*[starts-with(@id, '".concat(
                            MenuEntryOperations.CATEGORY_MENU_ITEM_PREFIX)
                            .concat("')]"), rootElement);
        }
        // Get all elements that have the id attribute
        else {
            elements = XmlUtils.findElements("//*[@id]", rootElement);
        }

        for (Element element : elements) {
            completions.add(new Completion(element.getAttribute("id")));
        }

        return false;
    }

}

/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures     
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
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
package org.gvnix.dynamiclist.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.gvnix.dynamiclist.exception.DynamiclistException;
import org.gvnix.dynamiclist.service.DynamiclistService;
import org.gvnix.dynamiclist.util.TagConstants;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

/**
 * DynamiclistService implementation
 * 
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 *
 */
@Repository
@Transactional(readOnly = true)
public class DynamiclistServiceImpl implements DynamiclistService{
	
    /*
     * (non-Javadoc)
     * @see org.gvnix.dynamiclist.service.DynamiclistService#searchMetaFields(java.lang.Class, org.springframework.ui.Model)
     */
	public void searchMetaFields(Class<?> objectClass, Model model) throws DynamiclistException {
    	
		Field[] fields = objectClass.getFields();
		String[] fieldsNames_ = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			fieldsNames_[i] = fields[i].getName();			
		}
		
		List<String> fieldsNames = new ArrayList<String>();
		List<Type> fieldsTypes = new ArrayList<Type>();
		Method[] methods = objectClass.getMethods();
		for (Method method : methods) {
			if (method.getName().startsWith("get") && !method.getName().contains("getClass")){
				fieldsNames.add(method.getName().substring(3));
				fieldsTypes.add(method.getGenericReturnType());
			}
		}
				
		model.addAttribute(TagConstants.META_FIELDS_NAMES, fieldsNames);
		model.addAttribute(TagConstants.META_FIELDS_TYPES, fieldsTypes);
    }
}

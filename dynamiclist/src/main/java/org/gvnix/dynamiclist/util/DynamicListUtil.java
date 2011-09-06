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
package org.gvnix.dynamiclist.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.gvnix.dynamiclist.dto.DynamiclistConfig;
import org.gvnix.dynamiclist.exception.DynamiclistException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 * 
 * Utils for dynamiclist CustomTags.
 *
 */
public class DynamicListUtil {

	/**
	 * Get all attributes of a generic object included in param config. 
	 * 
	 * @param config
	 * @return map of all attributes of the 'ClassObjectName'
	 * @throws DynamiclistException
	 */
	public static Map<String, String> allAttributesObject (DynamiclistConfig config) throws DynamiclistException {
		Map<String, String> map = new HashMap<String, String>();
		
		Class<?> objectClass; 
		try {
			objectClass = Class.forName(config.getClassObjectName());
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new DynamiclistException(0, e.toString(), "DynamicListUtil", "allAttributesObject");
		}
			
		List<String> fieldsNames = new ArrayList<String>();			
		Method[] methods = objectClass.getMethods();
		for (Method method : methods) {
			if (method.getName().startsWith("get") && !method.getName().contains("getClass")){
				fieldsNames.add(normalizeAttribute(method.getName().substring(3)));					
			}
		}		
				
		//String notDisplayColumns = "addresses, mobile,    sEx";		
		String[] arrayNotDisplay = StringUtils.split(config.getGlobalConfig().getHiddenProperties(), ',');
				
		for (int i = 0; i < arrayNotDisplay.length; i++) {
			String notDisplayColumn = arrayNotDisplay[i].trim();
			for (String fieldName : fieldsNames) {
				if (!StringUtils.upperCase(fieldName).equals(StringUtils.upperCase(notDisplayColumn))) {
					map.put(normalizeAttribute(objectClass.getSimpleName()) + "." + fieldName, fieldName);
				}
			}
		}
		
		return map;
	}
	
	/**
	 * Get the actual attributes of a generic object included in param config.
	 * 
	 * @param config
	 * @return map of attributes of the 'ClassObjectName'
	 * @throws DynamiclistException
	 */
	public static Map<String, String> actualAttributesObject (DynamiclistConfig config) throws DynamiclistException {
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		Class<?> objectClass; 
		try {
			objectClass = Class.forName(config.getClassObjectName());
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new DynamiclistException(0, e.toString(), "DynamicListUtil", "allAttributesObject");
		}
			
		List<String> fieldsNames = new ArrayList<String>();			
		Method[] methods = objectClass.getMethods();
		for (Method method : methods) {
			if (method.getName().startsWith("get") && !method.getName().contains("getClass")){
				fieldsNames.add(normalizeAttribute(method.getName().substring(3)));					
			}
		}
		
		for (String actual : config.getActualFieldsNames()) {
			for (String fieldName : fieldsNames) {
				if (StringUtils.upperCase(fieldName).equals(StringUtils.upperCase(actual))) {
					map.put(normalizeAttribute(objectClass.getSimpleName()) + "." + fieldName, fieldName);
				}
			}
		}
		return map;
	}
	
	/**
	 * Get the actual hidden attributes of a generic object included in param config.
	 * 
	 * @param config
	 * @return map of attributes of the 'ClassObjectName'
	 * @throws DynamiclistException
	 */
	public static Map<String, String> actualAttributesNoVisibleObject(DynamiclistConfig config) throws DynamiclistException {
		Map<String, String> actualMap = actualAttributesObject(config);
		Map<String, String> allMap = allAttributesObject(config);
		
		Collection<String> actualkeys = actualMap.keySet();
		for (String key : actualkeys) {
			if (allMap.containsKey(key)){
				allMap.remove(key);
			}
		}
		return allMap;
	}
	
	/**
	 * Get all attributes of a generic object included in param config, used in popup Filter.
	 * 
	 * @param config
	 * @return map of all attributes of the 'ClassObjectName'
	 * @throws DynamiclistException
	 */
	public static Map<String, String> allAttributesObjectForFilter (DynamiclistConfig config) throws DynamiclistException {
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> mapAllAttributes = allAttributesObject(config);
		
		Class<?> objectClass; 
		try {
			objectClass = Class.forName(config.getClassObjectName());
		
			Set<String> keys = mapAllAttributes.keySet();
			for (String key : keys) {
				
				String value = mapAllAttributes.get(key);
				Type type = objectClass.getMethod("get" + StringUtils.capitalize(mapAllAttributes.get(key)), null).getGenericReturnType();
				value += "|" + getTypeBasic(type);
				map.put(key, value);
			}
			
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new DynamiclistException(0, e.toString(), "DynamicListUtil", "allAttributesObjectForFilter");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new DynamiclistException(0, e.toString(), "DynamicListUtil", "allAttributesObjectForFilter");
		}
		
		return map;
	}
	
	/**
	 * Get the basic type of a intance of class Type, used in dynamiclist.
	 * 
	 * <p>Possibilities: DATE, NUMBER or TEXT</p>
	 * 
	 * @param type
	 * @return
	 */
	public static String getTypeBasic (Type type) {
		String typeBasic = TagConstants.TYPE_BASIC_TEXT;
		String name = type.toString();
		if (name.contains("String") || name.contains("Character")) {
			typeBasic = TagConstants.TYPE_BASIC_TEXT;
		} else if (name.contains("Integer") || name.contains("Number") 
				|| name.contains("Long") || name.contains("Double") 
				|| name.contains("Float")){
			typeBasic = TagConstants.TYPE_BASIC_NUMBER;
		} else if (name.contains("Date")){
			typeBasic = TagConstants.TYPE_BASIC_DATE;
		}		
		return typeBasic;
	}
	
		
	/**
	 * Converts the primer char of a String to lower case 
	 * 
	 * @param attribute
	 * @return
	 */
	public static String normalizeAttribute(String str){
		return StringUtils.lowerCase(str.subSequence(0, 1).toString()) + str.substring(1);
	}
	
	/**	  
	 * Get the human orderby of a orderby in language JPQL.
	 * 
	 * @param orderBy
	 * @return
	 */
	public static String getHumanOrderBy (String orderBy, String simpleClassName, HttpServletRequest request) {	
		StringBuffer human = new StringBuffer("");
		
		if (StringUtils.isNotEmpty(orderBy)) {
			String[] orders = orderBy.split(",");
			for (int i = 0; i < orders.length; i++) {
				String order = orders[i];
				String[] aux = StringUtils.split(order);
				human.append(Messages.getMessage(normalizeAttribute(simpleClassName) + "." + aux[0], request));
				human.append(" = ");
				if (aux.length > 0){
					human.append(aux[1]);
				} else {
					human.append("DESC");
				}
				
				if (i < orders.length-1){
					human.append(", ");
				}
			}
		}
		return human.toString();
	}
	
	/**
	 * Get collection of maps with the data for the exporting to a spreadsheet. 
	 * 
	 * @param dataList
	 * @param metaFields
	 * @return Data collection for the export.
	 * @throws DynamiclistException
	 */
	public static Collection<Map<String, String>> getDataCollectionExport(Collection<?> dataList, Collection<String> metaFields) 
		throws DynamiclistException {    	
		
		Collection<Map<String, String>> dataExport = new ArrayList<Map<String, String>>();				
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TagConstants.DATE_FORMAT_DEFAULT_EXPORT);				
		try {			
			
			for (Object dataObject : dataList){				
				Map<String, String> map = new HashMap<String, String>();
				for (String metaField : metaFields){
					Method method = dataObject.getClass().getMethod("get" + StringUtils.capitalize(metaField));
					String value;
					if (method.getGenericReturnType() == Date.class) {
						value = simpleDateFormat.format(method.invoke(dataObject));
					} else {
						value = method.invoke(dataObject).toString();
					}
					map.put(metaField, value);
				}
				dataExport.add(map);
			}		
		} catch (NoSuchMethodException e) {
			throw new DynamiclistException(1, "Error, problems on method of classObject define of DynamiclistConfig [" + e.getMessage() + "]",
					"DynamiclistServiceImpl", "searchMetaFieldsWidth");
		} catch (InvocationTargetException e) {
			throw new DynamiclistException(1, "Error InvocationTargetException, problems on invocation method of classObject define of DynamiclistConfig [" + e.getMessage() + "]",
					"DynamiclistServiceImpl", "searchMetaFieldsWidth");
		} catch (IllegalAccessException e) {
			throw new DynamiclistException(1, "Error IllegalAccessException, problems on invocation method of classObject define of DynamiclistConfig [" + e.getMessage() + "]",
					"DynamiclistServiceImpl", "searchMetaFieldsWidth");
		}		
		return dataExport;		
    }
	
	/**
	 * Get user login of SPRING_SECURITY AUTHENTICATION
	 * 
	 * @param request HttpServletRequest
	 * @return String
	 */
	public static String getLoginUser(HttpServletRequest request){
		//SPRING_SECURITY_LAST_USERNAME		
		String loginUser = (String)request.getSession().getAttribute(TagConstants.AUTHENTICATION_USERNAME);
		if (StringUtils.isEmpty(loginUser)){
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			loginUser = ((User)(authentication.getPrincipal())).getUsername();
		} 
		return loginUser;
	} 
	
	
}
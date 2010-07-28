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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gvnix.dynamiclist.dto.DynamiclistConfig;
import org.gvnix.dynamiclist.exception.DynamiclistException;
import org.gvnix.dynamiclist.jpa.bean.GlobalConfig;
import org.gvnix.dynamiclist.jpa.bean.UserConfig;
import org.gvnix.dynamiclist.jpa.bean.UserFilter;
import org.gvnix.dynamiclist.jpa.dao.DynamiclistDao;
import org.gvnix.dynamiclist.jpa.dao.GlobalConfigDao;
import org.gvnix.dynamiclist.jpa.dao.GlobalFilterDao;
import org.gvnix.dynamiclist.jpa.dao.UserConfigDao;
import org.gvnix.dynamiclist.jpa.dao.UserFilterDao;
import org.gvnix.dynamiclist.service.DynamiclistService;
import org.gvnix.dynamiclist.util.DynamicListUtil;
import org.gvnix.dynamiclist.util.TagConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 * DynamiclistService implementation
 * 
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 *
 */
@Service
public class DynamiclistServiceImpl implements DynamiclistService{
	
	@Autowired
    protected DynamiclistDao dynamiclistDao = null;
	
	@Autowired
    protected GlobalConfigDao globalConfigDao = null;
	
	@Autowired
    protected GlobalFilterDao globalFilterDao = null;
	
	@Autowired
    protected UserConfigDao userConfigDao = null;
	
	@Autowired
    protected UserFilterDao userFilterDao = null;
	
	
	private Log log = LogFactory.getLog(this.getClass());
	
	
	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.service.DynamiclistService#searchMetaFields(org.gvnix.dynamiclist.dto.DynamiclistConfig)
	 */
	public Collection<String> searchMetaFields(DynamiclistConfig config) throws DynamiclistException {    	
		
		List<String> finalFieldsNames = new ArrayList<String>();
		
		//GET fieldsNames
		if (config.getActualFieldsNames() == null || config.getActualFieldsNames().size() == 0) {
			//obtenemos los nombres de todas las propiedades de la clase
			List<String> fieldsNames = new ArrayList<String>();		
			
			try {
				Method[] methods = Class.forName(config.getClassObjectName()).getMethods();
				for (Method method : methods) {
					if (method.getName().startsWith("get") && !method.getName().contains("getClass")){
						fieldsNames.add(DynamicListUtil.normalizeAttribute(method.getName().substring(3)));
						//fieldsTypes.add(method.getGenericReturnType());
					}
				}
			} catch (ClassNotFoundException e) {				 
				 throw new DynamiclistException(1, "Error, problems on classObject define of DynamiclistConfig [" + config.getClassObjectName() + "]",
							"DynamiclistServiceImpl", "searchMetaFields");
			}
			
			//recorremos las propiedades visibles de la case desde el config y las comparamos con los nombres de las propiedades
			//si existe la añadimos en finalFieldsNames
			String[] arrayDisplay = StringUtils.split(config.getGlobalConfig().getEntityProperties(), ',');
			for (int i = 0; i < arrayDisplay.length; i++) {
				String displayColumn = arrayDisplay[i].trim();
				for (String fieldName : fieldsNames) {
					if (StringUtils.upperCase(fieldName).equals(StringUtils.upperCase(displayColumn))) {
						finalFieldsNames.add(fieldName);						
						break;
					}
				}
			}
			config.setActualFieldsNames(finalFieldsNames);
						
		} else {			
			
			finalFieldsNames.addAll(config.getActualFieldsNames());
			
			if (StringUtils.isNotEmpty(config.getActualGroupBy())){				
				if (finalFieldsNames.contains(config.getActualGroupBy())){
					finalFieldsNames.remove(config.getActualGroupBy());
				}
				//fieldName of groupBy in the first position
				finalFieldsNames.add(0, config.getActualGroupBy());
			}			
		}
						
		return finalFieldsNames;		
    }

	
	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.service.DynamiclistService#search(java.lang.Class, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest, org.springframework.ui.Model)
	 */
	public void search(Class<?> objectClass, Integer page, Integer size, String groupBy, String orderBy, String orderByColumn, 
			HttpServletRequest request, Model model) throws DynamiclistException {				
		DynamiclistConfig config = getDynamiclistConfig(objectClass, request);
		
		// --- orderBy
		String order = "";		
		//ordernación desde una columna del listado
		//TODO: delete actualOrderByColumn of config
		if (StringUtils.isNotEmpty(orderByColumn)){
			order = orderByColumn;
			//update orderBycolumn in dynamiclistConfig
			config.setActualOrderByColumn(orderByColumn);			
		} else if (StringUtils.isNotEmpty(config.getActualOrderByColumn())) {
			order = config.getActualOrderByColumn();
		} 		
		//ordenacion desde el popup orderBy
		if (StringUtils.isNotEmpty(orderBy) && !orderBy.equalsIgnoreCase(TagConstants.FALSE)){			
			if (StringUtils.isNotBlank(order)){
				order = order + ", " + orderBy;
			} else {
				order = orderBy;
			}
			//update orderBy in dynamiclistConfig
			config.setActualOrderBy(orderBy);
		} else if (StringUtils.isNotEmpty(orderBy) && orderBy.equalsIgnoreCase(TagConstants.FALSE)){
			//update orderBy in dynamiclistConfig
			config.setActualOrderBy(null);
		} else if (StringUtils.isNotEmpty(config.getActualOrderBy())){
			if (StringUtils.isNotBlank(order)){
				order = order + ", " + config.getActualOrderBy();
			} else {
				order = config.getActualOrderBy();
			}
		}
		
		// --- groupBy
		if (StringUtils.isNotEmpty(groupBy)) {
			if (groupBy.equalsIgnoreCase(TagConstants.FALSE)) {					
				//update groupBy in dynamiclistConfig
				config.setActualGroupBy(null);				
				
			} else {										
				if (StringUtils.isNotEmpty(order)){
					order = groupBy + ", " + order;
				} else {
					order = groupBy;
				}
				//update groupBy in dynamiclistConfig
				config.setActualGroupBy(groupBy);
			}
		} else {
			if (StringUtils.isNotEmpty(config.getActualGroupBy())){
				if (StringUtils.isNotEmpty(order)){
					order = config.getActualGroupBy() + ", " + order;
				} else {
					order = config.getActualGroupBy();
				}
			}
			groupBy = config.getActualGroupBy();
		}		
		
		// --- filters
		String filter = "";
		if (StringUtils.isNotEmpty(config.getActualWhereFilter())){
			filter = config.getActualWhereFilter();
		}
		
		// --- add GlobalConfig or UserConfig if exist
		String finalFilter = this.addFilterGlobalUserConfig(filter, config);
		String finalOrder = this.addOrderGlobalUserConfig(order, config);
		
		
		Integer countEntities = dynamiclistDao.countEntities(objectClass.getSimpleName(), finalFilter);
		int sizeNo = size == null ? TagConstants.SIZE_PAGE_DEFAULT : size.intValue();
		float nrOfPages = (float) countEntities / sizeNo;		
				
		model.addAttribute(TagConstants.PAGE_NAME, page == null ? 1 : page.intValue());
		model.addAttribute(TagConstants.MAX_PAGES_NAME, (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? 
				nrOfPages + 1 : nrOfPages));
		model.addAttribute(TagConstants.COUNTLIST_NAME, countEntities);
		model.addAttribute(TagConstants.GROUPBY, groupBy);

		//get MetaFields
		Collection<String> metaFields = searchMetaFields(config);
		//Collection<Ingeger> metaFieldsWidth = searchMetaFieldsWidth(metaFields, config.getClassObjectName());
		
		model.addAttribute(TagConstants.META_FIELDS_NAMES, metaFields);
		//config.setActualFieldsNames(metaFields);
		
		// set dynamiclistConfig in session
		request.getSession().setAttribute(TagConstants.DYNAMICLIST_CONFIG, config);
		
		Collection<?> entities = dynamiclistDao.findEntities(objectClass.getSimpleName(), 
				page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo, finalFilter, finalOrder);		
		model.addAttribute(TagConstants.LIST, entities);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.service.DynamiclistService#searchExport(java.lang.String, org.gvnix.dynamiclist.dto.DynamiclistConfig)
	 */
	public Collection<?> searchExport(String ClassObjectSimpleName, DynamiclistConfig config) throws DynamiclistException {
		
		String filter = this.addFilterGlobalUserConfig(config.getActualWhereFilter(), config);
		String order = this.addOrderGlobalUserConfig(config.getActualOrderBy(), config);
		
		return dynamiclistDao.findEntities(config.getClassObjectSimpleName(), 0, 2000, filter, order);
		
	}
	
	/* (non-Javadoc)
	 * @see org.gvnix.dynamiclist.service.DynamiclistService#deleteUserFilter(java.lang.Integer)
	 */
	public void deleteUserFilter(Integer id) throws DynamiclistException {
		UserFilter filter = userFilterDao.findUserFilterById(id);		
		userFilterDao.delete(filter);		
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.service.DynamiclistService#saveUserFilter(org.gvnix.dynamiclist.jpa.bean.UserFilter)
	 */
	public UserFilter saveUserFilter(UserFilter userFilter) throws DynamiclistException {
		return userFilterDao.save(userFilter);
	}


	/* (non-Javadoc)
	 * @see org.gvnix.dynamiclist.service.DynamiclistService#deleteUserConfig(org.gvnix.dynamiclist.jpa.bean.UserConfig)
	 */
	public void deleteUserConfig(UserConfig userConfig)	throws DynamiclistException {		
		userConfigDao.delete(userConfig);
	}


	/* (non-Javadoc)
	 * @see org.gvnix.dynamiclist.service.DynamiclistService#saveUserConfig(org.gvnix.dynamiclist.jpa.bean.UserConfig)
	 */
	public UserConfig saveUserConfig(UserConfig userConfig) throws DynamiclistException {
		return userConfigDao.save(userConfig);
	}


	/* (non-Javadoc)
	 * @see org.gvnix.dynamiclist.service.DynamiclistService#searchUserFilters(java.lang.String, java.lang.String)
	 */
	public Collection<UserFilter> searchUserFilters(String entity, String idUser)
			throws DynamiclistException {
		return userFilterDao.findUserFilterByEntityAndIdUser(entity, idUser);		
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.gvnix.dynamiclist.service.DynamiclistService#searchMetaFieldsWidth(java.util.Collection, java.lang.String)
	 */
	public Map<String, Integer> searchMetaFieldsWidth(Collection<String> metaFields, String className) throws DynamiclistException {    	
		Map<String, Integer> metaFielsWidth = new HashMap<String, Integer>();
		
		int lIntMinEntero = 6;
		int lIntMaxEntero = 10;
		int lIntMinString = 10;
		int lIntTamMedioString = 20;
		int lIntMaxString = 50;
		int lIntTamMaxString = 35;
		int lIntFecha = 20;
		int lIntFactor = 6;
				
		try {			
			Class<?> object = Class.forName(className);
			for (String metaField : metaFields){
				Method method = object.getMethod("get" + StringUtils.capitalize(metaField));
				
				int returnMethodLength = 10;
				
				if (DynamicListUtil.getTypeBasic(method.getGenericReturnType()).equalsIgnoreCase(TagConstants.TYPE_BASIC_NUMBER)) {
					//type numeric
					if (returnMethodLength < lIntMinEntero) {
						metaFielsWidth.put(metaField, lIntMinEntero * lIntFactor);
					} else if (returnMethodLength > lIntMinEntero){
						metaFielsWidth.put(metaField, lIntMaxEntero * lIntFactor);
					} else {
						metaFielsWidth.put(metaField, returnMethodLength * lIntFactor);
					}
				}
				else if (DynamicListUtil.getTypeBasic(method.getGenericReturnType()).equalsIgnoreCase(TagConstants.TYPE_BASIC_DATE)) {
					//type date
					metaFielsWidth.put(metaField, lIntFecha * lIntFactor);
				}
				else {
					//type string
					if (returnMethodLength < lIntMinString) {
						metaFielsWidth.put(metaField, lIntMinString * lIntFactor);						
					} else if (returnMethodLength > lIntMaxString){
						metaFielsWidth.put(metaField, lIntTamMaxString * lIntFactor);						
					} else {
						metaFielsWidth.put(metaField, lIntTamMedioString * lIntFactor);						
					}
				}
			}
		} catch (ClassNotFoundException e) {				 
			 throw new DynamiclistException(1, "Error, problems on classObject define of DynamiclistConfig [" + className + "]",
					"DynamiclistServiceImpl", "searchMetaFieldsWidth");
		} catch (NoSuchMethodException e) {
			throw new DynamiclistException(1, "Error, problems on method of classObject define of DynamiclistConfig [" + className + "]",
					"DynamiclistServiceImpl", "searchMetaFieldsWidth");
		}		
		return metaFielsWidth;		
    }
	
	
	/**
	 * 
	 * @param filter
	 * @param config
	 * @return
	 * @throws DynamiclistException
	 */
	private String addFilterGlobalUserConfig (String filter, DynamiclistConfig config) throws DynamiclistException {
		
		String finalFilter = "";
		
		//Add GlobalConfig or UserConfig if exist
		if (config.getUserConfig() != null && StringUtils.isNotEmpty(config.getUserConfig().getWhereFilter())){
			//put whereFilter of UserConfig in the dynamic search
			if (StringUtils.isNotEmpty(filter)){
				finalFilter = "(" + config.getUserConfig().getWhereFilter() + ")" + " AND " + filter;
			} else {
				finalFilter = config.getUserConfig().getWhereFilter();
			}
		} else {
			//put whereFilterFix and orderBy of GlobalConfig in the dynamic search
			if (StringUtils.isNotEmpty(config.getGlobalConfig().getWhereFilterFix())){
				if (StringUtils.isNotEmpty(filter)){
					finalFilter = "(" + config.getGlobalConfig().getWhereFilterFix() + ")" + " AND " + filter;
				} else {
					finalFilter = config.getGlobalConfig().getWhereFilterFix();
				}
			} else {
				finalFilter = filter;
			}
		}
		
		return finalFilter;
	}
	
	
	/**
	 * 
	 * @param order
	 * @param config
	 * @return
	 * @throws DynamiclistException
	 */
	private String addOrderGlobalUserConfig (String order, DynamiclistConfig config) throws DynamiclistException {
		
		StringBuffer finalOrder = new StringBuffer();
		
		//Add GlobalConfig or UserConfig if exist
		if (config.getUserConfig() != null && StringUtils.isNotEmpty(config.getUserConfig().getOrderBy())){			
			//put orderBy of UserConfig in the dynamic search
				
				//If exist actualGroupBy, it´s 1º order and globalConfig.orderBy 2º
				//If exist actualGroupBy and actualOrderByColumn, 1º group, 2º the orderColumn and 3º userConfig.orderBy 			
				if (StringUtils.isNotEmpty(config.getActualGroupBy()) && 
						StringUtils.isNotEmpty(config.getActualOrderByColumn())) {
					String[] orders = order.split(",");
					if (orders.length < 2) {
						throw new DynamiclistException(1, "Error, problems on search, order [" + order + "] haven´t " +
							"actualGroupBy and actualOrderByColumn", "DynamiclistServiceImpl", "searchMetaFields");
					}
					for (int i = 0; i < orders.length; i++) {
						String auxOrder = orders[i];							
						if (i == 2) {
							finalOrder.append(config.getUserConfig().getOrderBy());
							if (i != orders.length - 1){
								finalOrder.append(", ");
							}
						}							
						finalOrder.append(auxOrder);
						if (i != (orders.length - 1)){
							finalOrder.append(", ");
						}							
					}										
				} else if (StringUtils.isNotEmpty(config.getActualGroupBy()) || StringUtils.isNotEmpty(config.getActualOrderByColumn())) {
					String[] orders = order.split(",");
					for (int i = 0; i < orders.length; i++) {
						String auxOrder = orders[i];							
						if (i == 1) {
							finalOrder.append(config.getUserConfig().getOrderBy());
							if (i != orders.length - 1){
								finalOrder.append(", ");
							}
						}							
						finalOrder.append(auxOrder);
						if (i != (orders.length - 1)){
							finalOrder.append(", ");
						}
						if (orders.length == 1){
							finalOrder.append(", ");
							finalOrder.append(config.getUserConfig().getOrderBy());
						}	
					}
				} else if (StringUtils.isEmpty(order)) {
					finalOrder.append(config.getUserConfig().getOrderBy());					
				} else {
					finalOrder.append(config.getUserConfig().getOrderBy());
					finalOrder.append(", ");
					finalOrder.append(order);
				}
			
		} else {			
			//put orderBy of GlobalConfig in the dynamic search
			if (StringUtils.isNotEmpty(config.getGlobalConfig().getOrderBy())){
				//if exist actualGroupBy, it´s 1º order and globalConfig.orderBy 2º
				//if exist actualGroupBy and actualOrderByColumn, 1º group, 2º the orderColumn and 3º globalConfig.orderBy							
				if (StringUtils.isNotEmpty(config.getActualGroupBy()) && 
						StringUtils.isNotEmpty(config.getActualOrderByColumn())) {					
					String[] orders = order.split(",");
					if (orders.length < 2) {
						throw new DynamiclistException(1, "Error, problems on search, order [" + order + "] haven´t " +
							"actualGroupBy and actualOrderByColumn", "DynamiclistServiceImpl", "searchMetaFields");
					}
					for (int i = 0; i < orders.length; i++) {
						String auxOrder = orders[i];							
						if (i == 2) {
							finalOrder.append(config.getGlobalConfig().getOrderBy());
							if (i != orders.length){
								finalOrder.append(", ");
							}
						}							
						finalOrder.append(auxOrder);
						if (i != orders.length){
							finalOrder.append(", ");
						}
					}					
				} else if (StringUtils.isNotEmpty(config.getActualGroupBy()) || StringUtils.isNotEmpty(config.getActualOrderByColumn())) {
					String[] orders = order.split(",");
					for (int i = 0; i < orders.length; i++) {
						String auxOrder = orders[i];							
						if (i == 1) {
							finalOrder.append(config.getGlobalConfig().getOrderBy());
							if (i != orders.length - 1){
								finalOrder.append(", ");
							}
						}							
						finalOrder.append(auxOrder);
						if (i != orders.length - 1){
							finalOrder.append(", ");
						}
						if (orders.length == 1){
							finalOrder.append(", ");
							finalOrder.append(config.getGlobalConfig().getOrderBy());
						}
					}
				} else if (StringUtils.isEmpty(order)) {
					finalOrder.append(config.getGlobalConfig().getOrderBy());					
				} else {
					finalOrder.append(config.getGlobalConfig().getOrderBy());
					finalOrder.append(", ");
					finalOrder.append(order);
				}				
			} else {
				if (StringUtils.isNotEmpty(order)){
					finalOrder.append(order);
				}
			}
		}
		
		return finalOrder.toString();
	}
	
	
	/**
	 * 
	 * @param classSimpleName
	 * @param request
	 * @return
	 * @throws DynamiclistException
	 */
	private DynamiclistConfig getDynamiclistConfig(Class<?> objectClass, HttpServletRequest request) throws DynamiclistException {
		
		DynamiclistConfig config = null;
		if (request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG) != null) {
    		config = (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);
    	} 
		
		if (config != null && config.getClassObjectName().equals(objectClass.getName())){			
			if (log.isInfoEnabled()){
				log.info("get DynamiclistConfig of session [" + config + "]");
			}			
		} else {
			
			config = new DynamiclistConfig();
			
			// --- GlobalConfig ---
			Collection<GlobalConfig> configs = globalConfigDao.findGlobalConfigByEntity(objectClass.getSimpleName());		
			if (configs.size() != 1){
				throw new DynamiclistException(1, "Error, not GlobalConfig define for " + objectClass.getSimpleName(),
						"DynamiclistServiceImpl", "getDynamiclistConfig");
			}		
			GlobalConfig globalConfig = configs.iterator().next();
			config.setGlobalConfig(globalConfig);
			
			// --- GlobalFilters			
			config.setGlobalFilters(globalFilterDao.findGlobalFilterByEntity(objectClass.getSimpleName()));
			
			
			//SPRING_SECURITY_LAST_USERNAME
			//Authentication.Principal.Username
			String loginUser = (String)request.getSession().getAttribute(TagConstants.AUTHENTICATION_USERNAME);
			if (StringUtils.isEmpty(loginUser)){
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				loginUser = ((User)(authentication.getPrincipal())).getUsername();
			}
			
			// --- UserConfig ---			
			Collection<UserConfig> userConfigs = userConfigDao.findUserConfigByEntityAndIdUser(objectClass.getSimpleName(), loginUser);		
			if (userConfigs.size() > 1){
				throw new DynamiclistException(1, "Error, found "+ userConfigs.size() +" UserConfig defines for " 
						+ objectClass.getSimpleName(), "DynamiclistServiceImpl", "getDynamiclistConfig");
			}	
			else if (userConfigs.size() > 0){
				UserConfig userConfig = userConfigs.iterator().next();
				config.setUserConfig(userConfig);
			}	
			
			// --- UserFilters
			config.setUserFilters(userFilterDao.findUserFilterByEntityAndIdUser(objectClass.getSimpleName(), loginUser));			
			config.setClassObjectName(objectClass.getName());
			config.setClassObjectSimpleName(objectClass.getSimpleName());
		}
				
		// set dynamiclistConfig in session
		request.getSession().setAttribute(TagConstants.DYNAMICLIST_CONFIG, config);
		
		return config;
	}
}

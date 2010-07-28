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
package org.gvnix.dynamiclist.web;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.gvnix.dynamiclist.dto.DynamiclistConfig;
import org.gvnix.dynamiclist.exception.DynamiclistException;
import org.gvnix.dynamiclist.jpa.bean.GlobalFilter;
import org.gvnix.dynamiclist.jpa.bean.UserConfig;
import org.gvnix.dynamiclist.jpa.bean.UserFilter;
import org.gvnix.dynamiclist.service.DynamiclistService;
import org.gvnix.dynamiclist.util.DynamicListUtil;
import org.gvnix.dynamiclist.util.Messages;
import org.gvnix.dynamiclist.util.TagConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Dynamiclist controller.
 * 
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 *
 */
@Controller
public class DynamiclistController {
	
	@Autowired
    protected DynamiclistService dynamiclistService = null;
	
	/**
     * <p>Group form request.</p>
     * 
     * <p>Expected HTTP GET and request '/dynamiclist/groupBy'.</p>
     */
    @RequestMapping(value="/dynamiclist/groupBy", method=RequestMethod.GET)
    public String group(@RequestParam(value = "urlBaseMapping", required = true) String urlBaseMapping, 
    		HttpServletRequest request, ModelMap modelmap) throws DynamiclistException {
    	
    	DynamiclistConfig config = (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);
    	
    	modelmap.addAttribute("mapColumns", DynamicListUtil.allAttributesObject(config));
    	modelmap.addAttribute("selectedColumn", config.getActualGroupBy());    	
    	modelmap.addAttribute("urlMapping", urlBaseMapping + TagConstants.URL_SEARCH);
    	    	
    	return "dynamiclist/groupBy";
    }
    
    
    /**
     * <p>Order form request.</p>
     * 
     * <p>Expected HTTP GET and request '/dynamiclist/orderBy'.</p>
     */
    @RequestMapping(value="/dynamiclist/orderBy", method=RequestMethod.GET)
    public String order(@RequestParam(value = "urlBaseMapping", required = true) String urlBaseMapping,
    		HttpServletRequest request, ModelMap modelmap) throws DynamiclistException {
    	
    	DynamiclistConfig config = (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);
    	
    	modelmap.addAttribute("mapColumns", DynamicListUtil.allAttributesObject(config));
    	modelmap.addAttribute("selectedColumn", config.getActualGroupBy());
    	
    	modelmap.addAttribute("orderByText", config.getActualOrderBy());
    	modelmap.addAttribute("humanOrderByText", DynamicListUtil.getHumanOrderBy(config.getActualOrderBy(), 
    			config.getClassObjectSimpleName(), request));    	
    	modelmap.addAttribute("urlMapping", urlBaseMapping + TagConstants.URL_SEARCH);    	
    	
    	return "dynamiclist/orderBy";
    }
    
    /**
     * <p>Export form request.</p>
     * 
     * <p>Expected HTTP GET and request '/dynamiclist/export'.</p>
     */
    @RequestMapping(value="/dynamiclist/export", method=RequestMethod.GET)
    public String export(@RequestParam(value = "urlBase", required = true) String urlBase,
    		HttpServletRequest request, ModelMap modelmap) throws DynamiclistException {
    	
    	DynamiclistConfig config= (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);    	
    	modelmap.addAttribute("simpleNameClass", config.getClassObjectSimpleName());
    	modelmap.addAttribute(TagConstants.URL_BASE, urlBase);    	
    	modelmap.addAttribute("labelFilter", config.getClassObjectSimpleName());
    	modelmap.addAttribute("humanTextFilter", config.getActualHumanWhereFilter());
    	
    	modelmap.addAttribute("columns", config.getActualFieldsNames());
    	modelmap.addAttribute("columnsWidth", 
    			dynamiclistService.searchMetaFieldsWidth(config.getActualFieldsNames(), config.getClassObjectName()));
    	
    	//DynamiclistDao dynamiclistDao = (DynamiclistDao)RequestContextUtils.getWebApplicationContext(request).getBean("dynamiclistDao");    	    	
    	Collection<?> dataList = dynamiclistService.searchExport(config.getClassObjectSimpleName(), config);
    	
    	modelmap.addAttribute("objectList", DynamicListUtil.getDataCollectionExport(dataList, config.getActualFieldsNames()));
    	
    	return "dynamiclist/exportList";
    }
    
    /**
     * <p>Columns form request.</p>
     * 
     * <p>Expected HTTP GET and request '/dynamiclist/selectProperties'.</p>
     */
    @RequestMapping(value="/dynamiclist/columns", method=RequestMethod.GET)
    public String columns(@RequestParam(value = "imagesPath", required = true) String imagesPath,
    		@RequestParam(value = "urlBaseMapping", required = true) String urlBaseMapping,
    		HttpServletRequest request, ModelMap modelmap) throws DynamiclistException {
    	
    	DynamiclistConfig config= (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);
    	
    	modelmap.addAttribute("mapColumns", DynamicListUtil.actualAttributesObject(config));    	    	
    	modelmap.addAttribute("mapColumnsDisplayNone", DynamicListUtil.actualAttributesNoVisibleObject(config));
    	modelmap.addAttribute("imagesPath", imagesPath);
    	
    	modelmap.addAttribute("urlMapping", request.getContextPath() + TagConstants.URL_UPDATE_COLUMNS +
    			"?urlBaseMapping=" + urlBaseMapping);
    	
    	return "dynamiclist/selectProperties";
    }
    
    
    /**
     * <p>Columns form request.</p>
     * 
     * <p>Expected HTTP GET and request '/dynamiclist/selectProperties'.</p>
     */
    @RequestMapping(value="/dynamiclist/updateColumns", method=RequestMethod.GET)
    public String updateColumns(@RequestParam(value = "columns", required = true) String columns,
    		@RequestParam(value = "urlBaseMapping", required = true) String urlBaseMapping,
    		HttpServletRequest request) throws DynamiclistException {
    	
    	DynamiclistConfig config= (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);
    	
    	Collection<String> collection = new ArrayList<String>();
    	String[] newColumns = columns.split(",");
    	for (int i = 0; i < newColumns.length; i++) {
			String col = newColumns[i];
			collection.add(col);
		}    	 
    	config.setActualFieldsNames(collection);
    	
    	request.getSession().setAttribute(TagConstants.DYNAMICLIST_CONFIG, config);    	 
    	return "redirect:/" + urlBaseMapping + TagConstants.URL_SEARCH;
    }
	
    
    // --- FILTERS ---
        
    @RequestMapping(value="/dynamiclist/filter", method=RequestMethod.GET)
    public String filter(@RequestParam(value = "urlBaseMapping", required = true) String urlBaseMapping, 
    		HttpServletRequest request, ModelMap modelmap) throws DynamiclistException {
    	
    	DynamiclistConfig config= (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);    	
    	modelmap.addAttribute("mapColumns", DynamicListUtil.allAttributesObjectForFilter(config));

    	modelmap.addAttribute("humanTextFilter", config.getActualHumanWhereFilter());
    	modelmap.addAttribute("textFilter", config.getActualWhereFilter());
    	
    	modelmap.addAttribute("urlMapping", request.getContextPath() + TagConstants.URL_SEARCH_FILTER +
    			"?urlBaseMapping=" + urlBaseMapping);
    	
    	return "dynamiclist/filter";
    }
    
    /**
     * 
     * @param urlBaseMapping
     * @param request
     * @return
     * @throws DynamiclistException
     */
    @RequestMapping(value="/dynamiclist/deleteActualfilter", method=RequestMethod.GET)
    public String deleteActualfilter(@RequestParam(value = "urlBaseMapping", required = true) String urlBaseMapping, 
    		HttpServletRequest request) throws DynamiclistException {
    	
    	DynamiclistConfig config= (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);    	
    	config.setActualHumanWhereFilter(null);
    	config.setActualWhereFilter(null);
    	config.setIdActualFilter(null);
    	config.setTypeActualFilter(null);
    	
    	return "redirect:/" + urlBaseMapping + TagConstants.URL_SEARCH;
    }
    
    /**
     * <p>Columns form request.</p>
     * 
     * <p>Expected HTTP GET and request '/dynamiclist/searchFilter'.</p>
     */
    @RequestMapping(value="/dynamiclist/searchFilter", method=RequestMethod.POST)
    public String searchFilter(@RequestParam(value = "humanTextFilter", required = true) String humanTextFilter,
    		@RequestParam(value = "textFilter", required = true) String textFilter,
    		@RequestParam(value = "urlBaseMapping", required = true) String urlBaseMapping,
    		HttpServletRequest request) throws DynamiclistException {
    	
    	//update filter actual values of the config 
    	DynamiclistConfig config= (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);
    	config.setActualHumanWhereFilter(humanTextFilter);
    	config.setActualWhereFilter(textFilter);
    	request.getSession().setAttribute(TagConstants.DYNAMICLIST_CONFIG, config);
    	
    	return "redirect:/" + urlBaseMapping + TagConstants.URL_SEARCH;
    }
    
    /**
     *    
     * @param request
     * @param modelmap
     * @return
     */
    @RequestMapping(value="/dynamiclist/filterInfo", method=RequestMethod.GET)
    public String filterInfo(HttpServletRequest request, ModelMap modelmap) {
    	
    	DynamiclistConfig config = (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);
    	
    	modelmap.addAttribute("simpleNameClass", config.getClassObjectSimpleName());
    	
    	String humanWhere = "";    	
    	if (StringUtils.isNotEmpty(config.getActualHumanWhereFilter())){
    		humanWhere = config.getActualHumanWhereFilter();
    	} else {
    		humanWhere = Messages.getMessage("filterInfo.undefined", request);
    	}    	
    	modelmap.addAttribute("humanTextFilter", humanWhere);
    	
    	String nameFilter = "";
    	if (config.getIdActualFilter() != null){
    		if (StringUtils.isNotEmpty(config.getTypeActualFilter()) &&  config.getTypeActualFilter().equals(TagConstants.TYPE_FILTER_USER)){
    			//filter scope User
    			Collection<UserFilter> userFilters = config.getUserFilters();
    			for (UserFilter userFilter : userFilters) {
					if(userFilter.getId().equals(config.getIdActualFilter())){
						nameFilter = Messages.getMessage(userFilter.getLabelFilter(), request);
					}
				}
    		} else {
    			//filter scope Global
    			Collection<GlobalFilter> globalFilters = config.getGlobalFilters();
    			for (GlobalFilter globalFilter : globalFilters) {
					if (globalFilter.getId().equals(config.getIdActualFilter())){
						nameFilter = Messages.getMessage(globalFilter.getLabelFilter(), request);
						break;
					}
				}
    		}
    	} else {
    		nameFilter = Messages.getMessage("filterInfo.undefined", request);
    	}
    	modelmap.addAttribute("nameFilter", nameFilter);    	
    	
    	return "dynamiclist/filterInfo";
    }
    
    /**
     * 
     * @param urlBaseMapping
     * @param request
     * @param modelmap
     * @return
     */
    @RequestMapping(value="/dynamiclist/saveFilter", method=RequestMethod.GET)
    public String saveFilter(@RequestParam(value = "urlBaseMapping", required = true) String urlBaseMapping,
    		HttpServletRequest request, ModelMap modelmap) {
    	
    	DynamiclistConfig config = (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);    	
    	String labelFilter = "";
    	if (config.getIdActualFilter() != null && StringUtils.isNotEmpty(config.getTypeActualFilter()) 
    			&&  config.getTypeActualFilter().equals(TagConstants.TYPE_FILTER_USER)){
    		Collection<UserFilter> userFilters = config.getUserFilters();
    		for (UserFilter userFilter : userFilters) {
				if (userFilter.getId().equals(config.getIdActualFilter())){
					labelFilter = userFilter.getLabelFilter();
					break;
				}
			}
    	}
    	
    	modelmap.addAttribute("labelFilter", labelFilter);
    	modelmap.addAttribute("humanTextFilter", config.getActualHumanWhereFilter());
    	modelmap.addAttribute("textFilter", config.getActualWhereFilter());
    	
    	modelmap.addAttribute("urlMapping", request.getContextPath() + TagConstants.URL_PERSISTENCE_SAVE_FILTER +
    			"?urlBaseMapping=" + urlBaseMapping);    	
    	
    	return "dynamiclist/saveFilter";
    }
    
    /**
     * 
     * @param nameFilter
     * @param urlBaseMapping
     * @param request
     * @param modelmap
     * @return
     * @throws DynamiclistException
     */
    @RequestMapping(value="/dynamiclist/persistenceSaveFilter", method=RequestMethod.GET)
    public String persistenceSaveFilter(@RequestParam(value = "nameFilter", required = true) String nameFilter,
    		@RequestParam(value = "urlBaseMapping", required = true) String urlBaseMapping,
    		HttpServletRequest request, ModelMap modelmap) throws DynamiclistException {
    	    	
    	UserFilter userFilter = new UserFilter();
    	DynamiclistConfig config = (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);
    	
    	//UPDATE/NEW userFilter    	    	
    	if (config.getIdActualFilter() != null && StringUtils.isNotEmpty(config.getTypeActualFilter()) && 
    			config.getTypeActualFilter().equals(TagConstants.TYPE_FILTER_USER)){
    		
    		for (UserFilter filter : config.getUserFilters()){
    			if (filter.getId().equals(config.getIdActualFilter())){
    				if (nameFilter.equals(filter.getLabelFilter())){
    					//the filter exist, is a update
    					userFilter.setId(filter.getId());
    				}
    				break;
    			}
    		}
    		
    	}
    	
    	//SPRING_SECURITY_LAST_USERNAME		
		String loginUser = (String)request.getSession().getAttribute(TagConstants.AUTHENTICATION_USERNAME);
		if (StringUtils.isEmpty(loginUser)){
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			loginUser = ((User)(authentication.getPrincipal())).getUsername();
		}    	    	
    	userFilter.setEntity(config.getClassObjectSimpleName());
    	userFilter.setIdUser(loginUser);
    	userFilter.setInfoFilter(config.getActualHumanWhereFilter());
    	userFilter.setWhereFilter(config.getActualWhereFilter());
    	userFilter.setLabelFilter(nameFilter);    	
    	
    	userFilter = dynamiclistService.saveUserFilter(userFilter);
    	
    	//update object config of the session
    	config.setUserFilters(dynamiclistService.searchUserFilters(config.getClassObjectSimpleName(), loginUser));
    	config.setIdActualFilter(userFilter.getId());
		config.setTypeActualFilter(TagConstants.TYPE_FILTER_USER);
    	
    	request.getSession().setAttribute(TagConstants.DYNAMICLIST_CONFIG, config);
    	
    	return "redirect:/" + urlBaseMapping + TagConstants.URL_SEARCH;
    }
    
    /**
     * 
     * @param request
     * @param modelmap
     * @return
     */
    @RequestMapping(value="/dynamiclist/deleteFilter", method=RequestMethod.GET)
    public String deleteFilter(HttpServletRequest request, ModelMap modelmap) {
    	
    	DynamiclistConfig config = (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);    	
    	if (config.getIdActualFilter() != null && config.getTypeActualFilter() == TagConstants.TYPE_FILTER_USER) {
    		modelmap.addAttribute("selectedColumn", config.getIdActualFilter());    	
    	}
    	modelmap.addAttribute("userFilters", config.getUserFilters());    	
    	modelmap.addAttribute("urlMapping", request.getContextPath() + TagConstants.URL_PERSISTENCE_DELETE_FILTER);
    	
    	return "dynamiclist/deleteFilter";
    }
    
    /**
     * 
     * @param id
     * @param request
     * @param modelmap
     * @return
     * @throws DynamiclistException
     */
    @RequestMapping(value="/dynamiclist/persistenceDeleteFilter", method=RequestMethod.GET)
    public String persistenceDeleteFilter(@RequestParam(value = "id", required = true) Integer id,    		
    		HttpServletRequest request, ModelMap modelmap) throws DynamiclistException {
    	
    	dynamiclistService.deleteUserFilter(id);
    	
    	//update the object config of the session
    	DynamiclistConfig config = (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);    	
    	if (config.getIdActualFilter() != null && config.getIdActualFilter().equals(id) && 
    			StringUtils.isNotEmpty(config.getTypeActualFilter()) && config.getTypeActualFilter().equals(TagConstants.TYPE_FILTER_USER)){
    		config.setIdActualFilter(null);
    		config.setTypeActualFilter(null);
    	}   	
    	config.setUserFilters(dynamiclistService.searchUserFilters(config.getClassObjectSimpleName(), 
    			(String)request.getSession().getAttribute(TagConstants.AUTHENTICATION_USERNAME)));    	    	
    	request.getSession().setAttribute(TagConstants.DYNAMICLIST_CONFIG, config);
    	
    	return "redirect:" + TagConstants.URL_DELETE_FILTER;
    }
    
    /**
     * 
     * @param id
     * @param typeFilter
     * @param urlBaseMapping
     * @param request
     * @return
     * @throws DynamiclistException
     */
    @RequestMapping(value="/dynamiclist/executeFilter", method=RequestMethod.GET)
    public String executeFilter(@RequestParam(value = "id", required = true) Integer id,
    		@RequestParam(value = "typeFilter", required = true) String typeFilter,    		
    		@RequestParam(value = "urlBaseMapping", required = true) String urlBaseMapping, 
    		HttpServletRequest request) throws DynamiclistException {
    	
    	DynamiclistConfig config = (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);
    	config.setIdActualFilter(id);    	
    	config.setTypeActualFilter(typeFilter);
    	
    	String whereFilter = null;
    	String humanWhereFilter = null;
    	if (typeFilter.equals(TagConstants.TYPE_FILTER_USER)){
    		for(UserFilter userFilter : config.getUserFilters()){
    			if(userFilter.getId().equals(id)){
    				whereFilter = userFilter.getWhereFilter();
    				humanWhereFilter = userFilter.getInfoFilter();
    				break;
    			} 
    		}
    	} else {
    		for(GlobalFilter globalFilter : config.getGlobalFilters()){
    			if (globalFilter.getId().equals(id)){
    				whereFilter = globalFilter.getWhereFilter();
    				humanWhereFilter = globalFilter.getInfoFilter();
    				break;
    			}
    		}
    	}
    	
    	if (StringUtils.isEmpty(whereFilter) || StringUtils.isEmpty(humanWhereFilter)){
    		throw new DynamiclistException(1, "Error, whereFilter not found of filter id [" + id + "] typeFilter ["+ typeFilter +"]",
					"DynamiclistController", "executeFilter");
    	}
    	
    	config.setActualWhereFilter(whereFilter);
    	config.setActualHumanWhereFilter(humanWhereFilter);
    	    	
    	request.getSession().setAttribute(TagConstants.DYNAMICLIST_CONFIG, config);    	
    	return "redirect:/" + urlBaseMapping + TagConstants.URL_SEARCH;
    }
    
    
    /**
     * <p>Delete config state form request.</p>
     * 
     * <p>Expected HTTP GET and request '/dynamiclist/deleteState'.</p>
     */
    @RequestMapping(value="/dynamiclist/deleteState", method=RequestMethod.GET)
    public String deleteState(@RequestParam(value = "urlBaseMapping", required = true) String urlBaseMapping, 
    		HttpServletRequest request) throws DynamiclistException {
    	
    	DynamiclistConfig config = (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);    	
    	if (config.getUserConfig() != null){
    		dynamiclistService.deleteUserConfig(config.getUserConfig());    		
    	}    	
    	request.getSession().setAttribute(TagConstants.DYNAMICLIST_CONFIG, null);
    	
    	return "redirect:/" + urlBaseMapping + TagConstants.URL_SEARCH;
    }
    
    /**
     * <p>Save config state form request.</p>
     * 
     * <p>Expected HTTP GET and request '/dynamiclist/saveState'.</p>
     */
    @RequestMapping(value="/dynamiclist/saveState", method=RequestMethod.GET)
    public String saveState(@RequestParam(value = "urlBaseMapping", required = true) String urlBaseMapping, 
    		HttpServletRequest request) throws DynamiclistException {
    	
    	DynamiclistConfig config = (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);
    	UserConfig userConfig;
    	
    	if (config.getUserConfig() != null){
    		userConfig = config.getUserConfig();    		
    	} else {
    		userConfig = new UserConfig();
    	}
    	    	
    	userConfig.setIdUser((String)request.getSession().getAttribute(TagConstants.AUTHENTICATION_USERNAME));
    	userConfig.setEntity(config.getClassObjectName());
    	userConfig.setEntityProperties(config.getActualFieldsNames().toString());
    	userConfig.setWhereFilter(config.getActualWhereFilter());
    	userConfig.setInfoFilter(config.getActualHumanWhereFilter());
    	userConfig.setGroupBy(config.getActualGroupBy());
    	userConfig.setOrderBy(config.getActualOrderBy());
    	    	
    	config.setUserConfig(dynamiclistService.saveUserConfig(userConfig));
    	request.getSession().setAttribute(TagConstants.DYNAMICLIST_CONFIG, config);    	 
    	return "redirect:/" + urlBaseMapping + TagConstants.URL_SEARCH;
    }
    
    
    
    // -------- guardarConfiguracion
    
    /*
    OperadorDelegate lObjUsApDel = new OperadorDelegate();
	if (pStrConfiguracion != null) {
		if (lObjUsApDel.existeConfiguracion(pStrUsuario, pStrIdAplicacion, pObjSesionSQL.getAStrTabla())) {
			lObjUsApDel.deleteConfiguracion(pStrUsuario, pStrIdAplicacion, pObjSesionSQL.getAStrTabla());
		}
		OperadorConfig lObjConfig = new OperadorConfig();
		lObjConfig.setIdinforme(pObjSesionSQL.getAStrTabla());
		lObjConfig.setIdoperador(pStrUsuario);
		lObjConfig.setOrdre(pObjSesionSQL.getAStrOrder());
		lObjConfig.setCamps(pObjSesionSQL.getAStrColumnas());
		lObjConfig.setGrup(pObjSesionSQL.getAStrAgrupacion());

		lObjUsApDel.insertarConfiguracion(lObjConfig);
	}*/
    
  
    // ---------- eliminarConfiguracion
    /*OperadorDelegate lObjUsApDel = new OperadorDelegate();
	if ((pStrIdAplicacion != null)
			&& (lObjUsApDel.existeConfiguracion(pStrUsuario, pStrIdAplicacion, pObjSesionSQL.getAStrTabla()))) {
		lObjUsApDel.deleteConfiguracion(pStrUsuario, pStrIdAplicacion, pObjSesionSQL.getAStrTabla());
	}*/
}

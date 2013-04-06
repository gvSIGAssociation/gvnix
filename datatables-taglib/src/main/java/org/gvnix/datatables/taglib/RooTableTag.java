/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana Copyright (C)
 * 2013 Generalitat Valenciana
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
 * this program. If not, see &lt;http://www.gnu.org/copyleft/gpl.html&gt;.
 */
package org.gvnix.datatables.taglib;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.datatables.core.constants.ExportConstants;
import com.github.dandelion.datatables.core.export.ExportConf;
import com.github.dandelion.datatables.core.export.ExportType;
import com.github.dandelion.datatables.jsp.tag.AbstractColumnTag;
import com.github.dandelion.datatables.jsp.tag.TableTag;

/**
 * Extends the {@link TableTag} to make it compatible with the usual roo tag
 * attributes. This way it may replace the roo table tag just by changing the
 * xmlns:table reference in the jspx header to the URL of this tag library.
 * 
 * @author gvNIX Team
 */
public class RooTableTag extends TableTag {

    // Logger
    private static Logger LOGGER = LoggerFactory.getLogger(RooTableTag.class);

    /**
	 * 
	 */
    private static final long serialVersionUID = 8646911296425084063L;

    public static final String TABLE_TAG_VARIABLE = "__datatables_table_tag_instance__";

    /** The identifier field name for the type (defaults to 'id'). */
    private String typeIdFieldName = "id";

    /** Include 'create' link into table (default true). */
    private Boolean create = Boolean.TRUE;

    /** Include 'update' link into table (default true). */
    private Boolean update = Boolean.TRUE;

    /** Include 'delete' link into table (default true). */
    private Boolean delete = Boolean.TRUE;

    /**
     * Indicate if the contents of this tag and all enclosed tags should be
     * rendered (default 'true')
     */
    private Boolean render = Boolean.TRUE;

    /**
     * Used for checking if element has been modified (to recalculate simply
     * provide empty string value)
     */
    private String z;

    private String path;

    private void configureExport(ExportType exportType) {

        // Export URL build
        String url = getTable().getCurrentUrl() + "?"
                + ExportConstants.DDL_DT_REQUESTPARAM_EXPORT_TYPE + "="
                + exportType.getUrlParameter() + "&"
                + ExportConstants.DDL_DT_REQUESTPARAM_EXPORT_ID + "="
                + getTable().getId();

        ExportConf conf = new ExportConf(exportType, url);

        // Other fields
        conf.setFileName("export");
        StringBuffer cssClass = new StringBuffer("icon export_")
                .append(exportType.toString().toLowerCase());
        conf.setCssClass(cssClass);
        conf.setAutoSize(true);

        getTable().getExportConfMap().put(exportType, conf);
    }

    private boolean doRender() {
        return Boolean.TRUE.equals(render) || render == null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public int doStartTag() throws JspException {
        if (!doRender()) {
            return SKIP_BODY;
        }

        // Add reference to this tag as request attributes
        // to assure it's available for columns tags
        pageContext.setAttribute(TABLE_TAG_VARIABLE, this,
                PageContext.REQUEST_SCOPE);

        // Check url value
        if (serverSide != null && serverSide) {
            if (StringUtils.isBlank(url) && StringUtils.isNotBlank(path)) {
                // generate url based on path
                setUrl(path.concat("/datatables/ajax"));
            }
            else {
                setUrl(url);
            }
        }
        else {
            // Check dom data
            if (data != null) {
                setData((Collection) data);
            }
        }

        int value = super.doStartTag();

        // Configure export
        if (StringUtils.isNotBlank(getExport())) {
            List<String> selectedExport = Arrays.asList(StringUtils.split(
                    getExport(), ','));
            ExportType exportType;
            for (String type : selectedExport) {
                try {
                    exportType = ExportType.valueOf(type);
                }
                catch (Exception e) {
                    LOGGER.debug("Unknow export type '".concat(type).concat(
                            "'."));
                    continue;
                }
                configureExport(exportType);
            }
        }

        return value;
    }

    @Override
    public int doEndTag() throws JspException {
        if (!doRender()) {
            return SKIP_PAGE;
        }

        int result = super.doEndTag();

        // Remove this tag reference as tag is finished
        pageContext.removeAttribute(TABLE_TAG_VARIABLE);

        return result;
    }

    @Override
    public int doAfterBody() throws JspException {
        if (!doRender()) {
            return SKIP_BODY;
        }
        return super.doAfterBody();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTypeIdFieldName() {
        return typeIdFieldName;
    }

    public void setTypeIdFieldName(String typeIdFieldName) {
        this.typeIdFieldName = typeIdFieldName;
    }

    public Boolean getCreate() {
        return create;
    }

    public void setCreate(Boolean create) {
        this.create = create;
    }

    public Boolean getUpdate() {
        return update;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }

    public Boolean getRender() {
        return render;
    }

    public void setRender(Boolean render) {
        this.render = render;
    }

    public String getZ() {
        return z;
    }

    public void setZ(String z) {
        this.z = z;
    }

}

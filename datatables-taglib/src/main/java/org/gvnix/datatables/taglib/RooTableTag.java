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

import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import com.github.dandelion.datatables.core.constants.ExportConstants;
import com.github.dandelion.datatables.core.export.ExportConf;
import com.github.dandelion.datatables.core.export.ExportType;
import com.github.dandelion.datatables.jsp.tag.TableTag;

/**
 * Extends the {@link TableTag} to make it compatible with the usual roo tag
 * attributes. This way it may replace the roo table tag just by changing the
 * xmlns:table reference in the jspx header to the URL of this tag library.
 * 
 * @author gvNIX Team
 */
public class RooTableTag extends TableTag {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8646911296425084063L;

    public static final String TABLE_TAG = "TableTag";

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

    private SpringContextHelper helper = new SpringContextHelper();

    public RooTableTag() {
        // TODO: extract the default values to an external configuration file,
        // or to the Spring app context.
        /* Override default values. */
        setAutoWidth(Boolean.FALSE);
        setPaginationType("full_numbers");
        setProcessing(Boolean.TRUE);
        // TODO: ajax
        // setServerSide(Boolean.TRUE);
        setStateSave(Boolean.TRUE);
        setAppear("fadein,0");
        setRow("item");
        // Seems to have some bugs, disable just in case
        // setColReorder(Boolean.TRUE);
        setPaginationType("full_numbers");
        setExport("CSV,XLS,PDF");
        setExportLinks("top_middle");
    }

    @Override
    public void setPageContext(PageContext pageContext) {
        super.setPageContext(pageContext);
        Locale locale = helper.getRequestLocale(pageContext);
        setLabels("/resources/datatables/i18n/labels_" + locale.getLanguage()
                + ".txt");
    }

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

    @Override
    public int doStartTag() throws JspException {
        if (!doRender()) {
            return SKIP_BODY;
        }

        int value = super.doStartTag();

        configureExport(ExportType.CSV);
        configureExport(ExportType.XLS);
        configureExport(ExportType.PDF);

        return value;
    }

    @Override
    public int doEndTag() throws JspException {
        if (!doRender()) {
            return SKIP_PAGE;
        }
        return super.doEndTag();
    }

    @Override
    public int doAfterBody() throws JspException {
        if (!doRender()) {
            return SKIP_BODY;
        }
        return super.doAfterBody();
    }

    public String getPath() {
        return getUrl();
    }

    public void setPath(String path) {
        setUrl(path);
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

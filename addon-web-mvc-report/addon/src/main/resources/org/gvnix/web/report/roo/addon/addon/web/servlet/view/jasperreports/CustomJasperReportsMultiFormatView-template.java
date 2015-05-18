/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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

package ${PACKAGE};

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JasperPrint;

import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;

/**
 * This class is installed by <b>web report setup</b> from addon-web-report
 * Add-on from gvNix project
 *
 * It only overrides
 * {@link org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView}
 * .renderReport(JasperPrint populatedReport, Map<String, Object> model,
 * HttpServletResponse response) The main purpose of the class is to modify the
 * Content-disposition HTTP header setting a name for the report file generated
 * in the request.
 *
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
public class CustomJasperReportsMultiFormatView extends
        JasperReportsMultiFormatView {
    public static final String DEFAULT_FORMAT_KEY = "format";
    public static final String DEFAULT_FILENAME_KEY = "title";

    private String formatKey = DEFAULT_FORMAT_KEY;
    private String fileNameKey = DEFAULT_FILENAME_KEY;

    /**
     * Set a file name for the generated report and set it to the
     * Content-disposition header. Delegates the render of the report to the
     * {@link org.springframework.web.servlet.view.jasperreports.
     * JasperReportsMultiFormatView.renderReport(JasperPrint, Map<String,
     * Object>, HttpServletResponse)}
     *
     * @param populatedReport
     * @param model
     * @param response
     * @throws Exception
     */
    protected void renderReport(JasperPrint populatedReport,
            Map<String, Object> model, HttpServletResponse response)
            throws Exception {

        String format = (String) model.get(this.formatKey);
        if (format == null) {
            throw new IllegalArgumentException(
                    "No format format found in model");
        }

        // Prepare response and render report.

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String todayYyyyMMdd = sdf.format(Calendar.getInstance().getTime());

        String fileName = (String) model.get(this.fileNameKey);
        if (fileName == null) {
            fileName = "REPORT_";
        }
        fileName = fileName.toUpperCase().replaceAll(" ", "_").concat("-")
                .concat(todayYyyyMMdd);

        StringBuilder contentDisposition = new StringBuilder("attachement");
        contentDisposition.append("; filename=");
        contentDisposition.append(fileName).append(".")
                .append(format.toLowerCase());
        response.setHeader(HEADER_CONTENT_DISPOSITION,
                contentDisposition.toString());

        super.renderReport(populatedReport, model, response);
    }

	/**
	 * Gets format model key-name
	 *
	 * @return the formatKey
	 */
	public String getFormatKey() {
		return formatKey;
	}

	/**
	 * Gets format model key-name
	 *
	 * @param formatKey the formatKey to set
	 */
	public void setFormatKey(String formatKey) {
		this.formatKey = formatKey;
	}

	/**
	 * Gets file-name model key-name
	 *
	 * @return the fileNameKey
	 */
	public String getFileNameKey() {
		return fileNameKey;
	}

	/**
	 * Gets file-name model key-name
	 *
	 * @param fileNameKey the fileNameKey to set
	 */
	public void setFileNameKey(String fileNameKey) {
		this.fileNameKey = fileNameKey;
	}
}

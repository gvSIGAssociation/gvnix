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
 */
public class CustomJasperReportsMultiFormatView extends
        JasperReportsMultiFormatView {
    private static final String DEFAULT_FORMAT_KEY = "format";
    private static final String DEFAULT_FILENAME_KEY = "title";

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
}

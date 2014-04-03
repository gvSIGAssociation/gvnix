package org.gvnix.addon.bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.gvnix.support.WebProjectUtils;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;

public class BootstrapUtils {

    /**
     * This method update an existing file in a directory
     * 
     * @param fileManager
     * @param loadingClass
     * @param filePath
     * @param fileName
     * @param directory
     */
    public static void updateFilesInLocationIfExists(FileManager fileManager,
            Class loadingClass, String filePath, String fileName,
            String directory) {
        if (fileManager.exists(filePath)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(loadingClass,
                        directory.concat(fileName));
                outputStream = fileManager.updateFile(filePath)
                        .getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }

    }

    /**
     * This method copy a new file in a directory if the file not exists in the
     * system
     * 
     * @param fileManager
     * @param loadingClass
     * @param filePath
     * @param fileName
     * @param directory
     */
    public static void createFilesInLocationIfNotExists(
            FileManager fileManager, Class loadingClass, String filePath,
            String fileName, String directory) {
        if (!fileManager.exists(filePath)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(loadingClass,
                        directory.concat(fileName));
                outputStream = fileManager.createFile(filePath)
                        .getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }

    }

    /**
     * This method copy a new file in a directory if the file not exists and
     * update the file if exists
     * 
     * @param fileManager
     * @param loadingClass
     * @param filePath
     * @param fileName
     * @param directory
     */
    public static void createFilesInLocationIfNotExistsUpdateIfExists(
            FileManager fileManager, Class loadingClass, String filePath,
            String fileName, String directory) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils.getInputStream(loadingClass,
                    directory.concat(fileName));
            if (!fileManager.exists(filePath)) {
                outputStream = fileManager.createFile(filePath)
                        .getOutputStream();
            }
            else {
                outputStream = fileManager.updateFile(filePath)
                        .getOutputStream();
            }
            IOUtils.copy(inputStream, outputStream);
        }
        catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

    }

    /**
     * Updates all JSP pages of target controller to use JQuery
     * 
     * @param pathResolver
     * @param webappPath
     * @param projectOperations
     * @param fileManager
     */
    public static void updateJSPViewsToUseJQuery(PathResolver pathResolver,
            LogicalPath webappPath, ProjectOperations projectOperations,
            FileManager fileManager) {

        String path = "";
        // Getting all views of the application
        String viewsPath = pathResolver.getIdentifier(webappPath,
                "WEB-INF/views/");
        File directory = new File(viewsPath);
        File[] folders = directory.listFiles();

        for (File folder : folders) {
            if (folder.isDirectory()) {
                path = folder.getName().concat("/");

                // List of pages to update
                // List of pages to update
                List<String> pageList = new ArrayList<String>();

                // Getting all jspx files inside the folder
                File[] files = folder.listFiles();
                for (File file : files) {
                    String fileName = file.getName();
                    if (file.isFile()
                            && fileName.contains("jspx")
                            && (fileName.contains("create")
                                    || fileName.contains("update")
                                    || fileName.contains("show")
                                    || fileName.contains("list") || fileName
                                        .contains("find"))) {
                        pageList.add(file.getName());
                    }
                }

                // 3rd party add-ons could customize default Roo tags as gvNIX
                // does,
                // to avoid to overwrite them with jQuery namespaces we will
                // update
                // default Roo namespaces only
                Map<String, String> rooUriMap = new HashMap<String, String>();
                rooUriMap.put("xmlns:field",
                        "urn:jsptagdir:/WEB-INF/tags/form/fields");
                rooUriMap.put("xmlns:form", "urn:jsptagdir:/WEB-INF/tags/form");
                rooUriMap.put("xmlns:table",
                        "urn:jsptagdir:/WEB-INF/tags/form/fields");
                rooUriMap.put("xmlns:page", "urn:jsptagdir:/WEB-INF/tags/form");
                rooUriMap.put("xmlns:util", "urn:jsptagdir:/WEB-INF/tags/util");

                // new jQuery namespaces
                Map<String, String> uriMap = new HashMap<String, String>();
                uriMap.put("xmlns:field",
                        "urn:jsptagdir:/WEB-INF/tags/jquery/form/fields");
                uriMap.put("xmlns:form",
                        "urn:jsptagdir:/WEB-INF/tags/jquery/form");
                uriMap.put("xmlns:table",
                        "urn:jsptagdir:/WEB-INF/tags/jquery/form/fields");
                uriMap.put("xmlns:page",
                        "urn:jsptagdir:/WEB-INF/tags/jquery/form");
                uriMap.put("xmlns:util",
                        "urn:jsptagdir:/WEB-INF/tags/jquery/util");

                // do the update
                for (String jspxName : pageList) {
                    String tagxFile = "WEB-INF/views/".concat(path).concat(
                            jspxName);
                    WebProjectUtils.updateTagxUriInJspx(tagxFile, rooUriMap,
                            uriMap, projectOperations, fileManager);
                }
            }
        }
    }

}

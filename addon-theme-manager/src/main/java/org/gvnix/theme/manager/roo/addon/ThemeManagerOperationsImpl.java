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
package org.gvnix.theme.manager.roo.addon;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.*;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.*;
import org.springframework.roo.support.util.*;
import org.w3c.dom.*;

/**
 * Implementation of commands that are available via the Roo shell.
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
@Component
@Service
public class ThemeManagerOperationsImpl implements ThemeManagerOperations {

    private static Logger logger = Logger
	    .getLogger(ThemeManagerOperationsImpl.class.getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private NotifiableFileMonitorService fileMonitorService;

    final static String APP_THEME_LOCATION = "/themes/";

    final static String APP_WEBINF_DIR = "/WEB-INF/";
    final static String APP_VIEWS_DIR = "views";

    final static String THEME_XML = "theme.xml";

    private List<Theme> gvNixDistributionThemes;
    private List<Theme> installedThemes;
    private Theme activeTheme;

    private ComponentContext context;

    protected void activate(ComponentContext context) {
	this.context = context;

	String themesPath = getThemesPath();

	if ((new File(themesPath)).exists()) {
	    DirectoryMonitoringRequest directoryGvNixThemesMonitoringRequest = new DirectoryMonitoringRequest(
		    new File(themesPath), true, (MonitoringRequest
			    .getInitialMonitoringRequest(themesPath))
			    .getNotifyOn());

	    fileMonitorService.add(directoryGvNixThemesMonitoringRequest);

	}

	// Add themes folder to the listener if it exists.
	String themesDirectory = pathResolver.getIdentifier(Path.ROOT,
		APP_THEME_LOCATION);

	if (fileManager.exists(themesDirectory)) {

	    DirectoryMonitoringRequest directoryAppThemesMonitoringRequest = new DirectoryMonitoringRequest(
		    new File(themesDirectory), true, (MonitoringRequest
			    .getInitialMonitoringRequest(themesDirectory))
			    .getNotifyOn());

	    fileMonitorService.add(directoryAppThemesMonitoringRequest);
	}

	fileMonitorService.scanAll();

    }

    /**
     * {@inheritDoc}
     * 
     * Returns the path for gvNIX installation themes. Search order:
     * <ul>
     * <li>
     * System property 'roo.themes' (java -Droo.themes).</li>
     * <li>
     * Environment variable 'ROO_THEMES'.</li>
     * <li>
     * System property 'roo.home' (java -Droo.home).</li>
     * <li>
     * Environment variable 'ROO_HOME'.</li>
     * </ul>
     */
    public String getThemesPath() {

	String themesPath;

	// roo.themes
	themesPath = System.getProperty("roo.themes");

	if (themesPath != null) {
	    if (!themesPath.endsWith("/")) {
		themesPath += "/";
	    }
	    return themesPath;
	}

	// ROO_THEMES
	themesPath = System.getenv("ROO_THEMES");

	if (themesPath != null) {
	    if (!themesPath.endsWith("/")) {
		themesPath += "/";
	    }
	    return themesPath;
	}

	// roo.home
	themesPath = System.getProperty("roo.home");

	if (themesPath != null) {
	    if (!themesPath.endsWith("/")) {
		themesPath += "/";
	    }
	    return themesPath.concat("themes");
	}

	// ROO_HOME
	themesPath = System.getenv("ROO_HOME");

	if (themesPath != null) {
	    if (!themesPath.endsWith("/")) {
		themesPath += "/";
	    }
	    return themesPath.concat("themes");
	}

	return themesPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.theme.manager.roo.addon.ThemeManagerOperations#installTheme
     * (java.lang.String)
     */
    public void installTheme(String themeName) {
	String themesPath = getThemesPath();

	Assert.isTrue(themesPath != null,
		"The variable ROO_THEMES or ROO_HOME has to be set.");

	String themesDirectory = pathResolver.getIdentifier(Path.ROOT,
		APP_THEME_LOCATION);

	if (!fileManager.exists(themesDirectory)) {
	    fileManager.createDirectory(themesDirectory);
	}

	DirectoryMonitoringRequest directoryMonitoringRequest = new DirectoryMonitoringRequest(
		new File(themesDirectory), true, (MonitoringRequest
			.getInitialMonitoringRequest(themesDirectory))
			.getNotifyOn());

	fileMonitorService.add(directoryMonitoringRequest);
	fileMonitorService.scanAll();

	if (!themesPath.endsWith("/")) {
	    themesPath += "/";
	}

	String sourcePath = themesPath.concat(themeName);

	File file = new File(sourcePath);

	Assert.isTrue(file.exists() && file.isDirectory()
		&& file.listFiles().length != 0, "Theme " + themeName
		+ " not exists in gvNIX themes directory.");

	String targetDirectory = pathResolver.getIdentifier(Path.ROOT,
		APP_THEME_LOCATION.concat(themeName));

	File destinationFile = new File(targetDirectory);

	Assert.isTrue(copyRecursively(file, destinationFile, false),
		"Error copying files.");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.theme.manager.roo.addon.ThemeManagerOperations#setThemeActive
     * (java.lang.String)
     */
    public void setThemeActive(String themeName) {

	String installedthemeDirectory = pathResolver.getIdentifier(Path.ROOT,
		APP_THEME_LOCATION.concat(themeName));

	File file = new File(installedthemeDirectory);

	Assert.isTrue(file.exists() && file.isDirectory()
		&& file.listFiles().length != 0, "Theme " + themeName
		+ " not exists in themes project directory.");

	String targetDirectory = pathResolver.getIdentifier(
		Path.SRC_MAIN_WEBAPP, "");

	File destinationFile = new File(targetDirectory);

	Assert.isTrue(copyRecursively(file, destinationFile, false),
		"Error copying files into webapp directory.");

	// Update theme.xml file to set theme active.
	updateThemeXml(themeName);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.theme.manager.roo.addon.ThemeManagerOperations#updateLayoutsXml
     * (java.lang.String, java.lang.String)
     */
    public void updateThemeXml(String themeName) {

	String themeFile = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		APP_WEBINF_DIR.concat(THEME_XML));

	Assert
		.isTrue(
			fileManager.exists(themeFile),
			"Must define a "
				+ THEME_XML
				+ " in src/main/webapp/WEB-INF directory file to activate theme: "
				+ themeName);

	// theme.xml
	MutableFile webXmlMutableFileTemplate = null;
	Document themeXml;

	try {
	    webXmlMutableFileTemplate = fileManager.updateFile(themeFile);
	    themeXml = XmlUtils.getDocumentBuilder().parse(
		    webXmlMutableFileTemplate.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
	Element rootTemplate = themeXml.getDocumentElement();

	Element themeElement;
	Element themeId;

	// Theme element
	themeElement = XmlUtils.findFirstElement("/theme", rootTemplate);

	Assert.notNull(themeElement,
		"There is no theme element defined in 'theme.xml' file.");

	Element themeDescription = XmlUtils.findFirstElementByName(
		"description", themeElement);

	Assert.notNull(themeDescription,
		"There is no description element defined in installed theme: "
			+ themeName + " '/WEB-INF/theme.xml' file.");

	themeId = themeXml.createElement("id");
	themeId.setTextContent(themeName);

	themeElement.appendChild(themeId);

	XmlUtils
		.writeXml(webXmlMutableFileTemplate.getOutputStream(), themeXml);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.theme.manager.roo.addon.ThemeManagerOperations#showThemesList()
     */
    public String showThemesList() {

	String distributionThemesPath = getThemesPath();

	Assert.isTrue(distributionThemesPath != null,
		"The variables ROO_THEMES or ROO_HOME have to be set.");

	File file = new File(distributionThemesPath);

	Assert.isTrue(file.exists() && file.isDirectory(),
		"Themes directory not exists in gvNIX directory.");

	// 1 gvNIX distribution themes.
	this.gvNixDistributionThemes = getThemesData(ThemeType.DISTRIBUTION,
		distributionThemesPath);

	List<Theme> tmpGvNixDistributionThemes;
	tmpGvNixDistributionThemes = new ArrayList<Theme>();
	tmpGvNixDistributionThemes.addAll(gvNixDistributionThemes);

	// 2 Themes project folder.
	String installedThemesPath = pathResolver.getIdentifier(Path.ROOT,
		APP_THEME_LOCATION);

	List<Theme> tmpInstalledThemes;

	if (!fileManager.exists(installedThemesPath)) {

	    tmpInstalledThemes = new ArrayList<Theme>();

	} else {

	    this.installedThemes = getThemesData(ThemeType.INSTALLED,
		    installedThemesPath);

	    tmpInstalledThemes = new ArrayList<Theme>();
	    tmpInstalledThemes.addAll(installedThemes);
	}

	// 3 Active theme.
	String activeThemeFile = pathResolver.getIdentifier(
		Path.SRC_MAIN_WEBAPP, APP_WEBINF_DIR.concat(THEME_XML));

	this.activeTheme = getActiveThemeXmlData(ThemeType.ACTIVE,
		activeThemeFile);

	ActiveTheme tmpActiveTheme;

	if (activeTheme != null) {
	    tmpActiveTheme = new ActiveTheme();
	    tmpActiveTheme.setName(activeTheme.getName());
	    tmpActiveTheme.setDescription(activeTheme.getDescription());
	} else {
	    tmpActiveTheme = null;
	}

	StringBuilder stringBuilder;

	stringBuilder = getMessageList(tmpGvNixDistributionThemes, tmpInstalledThemes,
		tmpActiveTheme);

	return stringBuilder.toString();
    }

    public List<Theme> getThemesData(ThemeType themeType, String themesPath) {

	Assert.isTrue(StringUtils.hasText(themesPath),
		"Theme's path must be defined.");

	List<Theme> themeList = new ArrayList<Theme>();

	File themePathfile = new File(themesPath);

	Assert.isTrue(themePathfile.isDirectory(), "Source directory '"
		+ themesPath + "' must be a directory");

	Theme theme;

	for (File themeDir : themePathfile.listFiles()) {

	    if (themeDir.isDirectory() && (!themeDir.getName().startsWith("."))) {

		theme = getThemeXmlData(themeType, themeDir.getAbsolutePath()
			.concat(APP_WEBINF_DIR).concat(THEME_XML));
		if (theme != null) {
		    theme.setName(themeDir.getName());
		    themeList.add(theme);
		}
	    }
	}

	return Collections.unmodifiableList(themeList);
    }

    /**
     * Method to retrieve Active Theme object.
     * 
     * @param themeType
     *            to get specific properties from xml.
     * @param themesPath
     *            Path of theme.xml file.
     * @return {@link Theme} Theme entity with the xml attributes. Returns null
     *         if there is no theme.xml file.
     */
    public Theme getThemeXmlData(ThemeType themeType, String themeXmlPath) {

	// Origin layouts.xml
	InputStream xmlThemeInputStream = null;
	Document themeXml;

	try {
	    xmlThemeInputStream = fileManager.getInputStream(themeXmlPath);
	    themeXml = XmlUtils.getDocumentBuilder().parse(xmlThemeInputStream);
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
	Element root = themeXml.getDocumentElement();

	Element themeElement;

	// Theme element
	themeElement = XmlUtils.findFirstElement("/theme", root);

	Assert.notNull(themeElement,
		"There is no 'theme' element defined in 'theme.xml' file in:\t"
			+ themeXmlPath);

	Element themeDescription = XmlUtils.findFirstElementByName(
		"description", themeElement);

	Assert.notNull(themeDescription,
		"There is no 'description' element defined in "
			+ themeType.toString() + " theme " + themeXmlPath
			+ " file.");

	if (themeType.equals(ThemeType.ACTIVE)) {

	    Element themeId = XmlUtils.findFirstElementByName("id",
		    themeElement);

	    Assert.notNull(themeId, "There is no 'id' element defined in "
		    + themeType.toString() + " theme " + themeXmlPath
		    + " file.");

	    ActiveTheme activeTheme = new ActiveTheme(themeElement);
	    return activeTheme;

	} else if (themeType.equals(ThemeType.INSTALLED)) {

	    InstalledTheme installedTheme = new InstalledTheme(themeElement);
	    return installedTheme;
	} else if (themeType.equals(ThemeType.DISTRIBUTION)) {

	    DistributionTheme distributionTheme = new DistributionTheme(
		    themeElement);
	    return distributionTheme;
	}

	return null;
    }

    private Theme getActiveThemeXmlData(ThemeType themeType, String themeXmlPath) {

	// Origin layouts.xml
	InputStream xmlThemeInputStream = null;
	Document themeXml;

	if (!fileManager.exists(themeXmlPath)) {
	    return null;
	}
	try {
	    xmlThemeInputStream = fileManager.getInputStream(themeXmlPath);
	    themeXml = XmlUtils.getDocumentBuilder().parse(xmlThemeInputStream);
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
	Element root = themeXml.getDocumentElement();

	Element themeElement;

	// Theme element
	themeElement = XmlUtils.findFirstElement("/theme", root);

	Assert.notNull(themeElement,
		"There is no 'theme' element defined in 'theme.xml' file in:\t"
			+ themeXmlPath);

	Element themeDescription = XmlUtils.findFirstElementByName(
		"description", themeElement);

	Assert.notNull(themeDescription,
		"There is no 'description' element defined in "
			+ themeType.toString() + " theme " + themeXmlPath
			+ " file.");

	Element themeId = XmlUtils.findFirstElementByName("id", themeElement);

	Assert.notNull(themeId, "There is no 'id' element defined in "
		+ themeType.toString() + " theme " + themeXmlPath + " file.");

	ActiveTheme activeTheme = new ActiveTheme(themeElement);

	return activeTheme;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.theme.manager.roo.addon.ThemeManagerOperations#isProjectAvailable
     * ()
     */
    public boolean isProjectAvailable() {
	if (getPathResolver() == null) {
	    return false;
	}

	String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/spring/webmvc-config.xml");

	if (!fileManager.exists(webXmlPath)) {
	    return false;
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.theme.manager.roo.addon.ThemeManagerOperations#getPathResolver
     * ()
     */
    public PathResolver getPathResolver() {
	ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
		.get(ProjectMetadata.getProjectIdentifier());
	if (projectMetadata == null) {
	    return null;
	}
	return projectMetadata.getPathResolver();
    }

    /**
     * Copies the specified source directory to the destination.
     * 
     * @param source
     *            the already-existing source directory (required)
     * @param destination
     *            the destination directory (required)
     * @param deleteDestinationOnExit
     *            indicates whether to mark any created destinations for
     *            deletion on exit
     * @return true if the copy was successful
     */
    private boolean copyRecursively(File source, File destination,
	    boolean deleteDestinationOnExit) {
	Assert.notNull(source, "Source directory required");
	Assert.notNull(destination, "Destination directory required");
	Assert.isTrue(source.exists(), "Source directory '" + source
		+ "' must exist");
	Assert.isTrue(source.isDirectory(), "Source directory '" + source
		+ "' must be a directory");

	if (destination.exists()) {
	    Assert.isTrue(destination.isDirectory(), "Destination directory '"
		    + destination + "' must be a directory");
	} else {
	    destination.mkdirs();
	    if (deleteDestinationOnExit) {
		destination.deleteOnExit();
	    }
	}
	for (File s : source.listFiles()) {
	    File d = new File(destination, s.getName());
	    if (deleteDestinationOnExit) {
		d.deleteOnExit();
	    }
	    if (s.isFile()) {
		fileCopyToProject(s, d);
	    } else {
		// It is not a .svn sub-directory, so copy it
		if (!d.getName().startsWith(".")) {
		    d.mkdir();
		    if (!copyRecursively(s, d, deleteDestinationOnExit)) {
			return false;
		    }
		}
	    }
	}
	return true;
    }

    /**
     * Copy file to file
     * 
     * @param source
     *            the already-existing source file (required)
     * @param destination
     *            the destination file (required)
     */
    private void fileCopyToProject(File source, File destination) {

	MutableFile mutableFile;
	byte[] template;

	InputStream templateInputStream;
	InputStreamReader readerFile;

	try {

	    templateInputStream = new FileInputStream(source);

	    readerFile = new InputStreamReader(templateInputStream);

	    template = FileCopyUtils.copyToByteArray(templateInputStream);

	} catch (IOException ioe) {
	    throw new IllegalStateException("Unable load ITD css template", ioe);
	}

	if (!fileManager.exists(destination.getAbsolutePath())) {
	    mutableFile = fileManager.createFile(destination.getAbsolutePath());
	    Assert.notNull(mutableFile, "Could not create ITD file '"
		    + destination.getAbsolutePath() + "'");
	} else {
	    mutableFile = fileManager.updateFile(destination.getAbsolutePath());
	}

	try {

	    FileCopyUtils.copy(template, mutableFile.getOutputStream());

	} catch (IOException ioe) {
	    throw new IllegalStateException("Could not output '"
		    + mutableFile.getCanonicalPath() + "'", ioe);
	}
    }

    /*
     * Creates a message with the available themes to manage.
     */
    private StringBuilder getMessageList(List<Theme> gvNixDistributionThemes,
	    List<Theme> installedThemes, Theme activeTheme) {

	// Message builder.
	StringBuilder stringBuilder = new StringBuilder();

	stringBuilder
		.append("\t\tgvNIX\t\tInstalled\t\tActive\t\tName\t\t\tDescription\n");
	stringBuilder
		.append("-------------- ---------------- -------------- ----------- -------------------------------\n");

	Iterator<Theme> iterator;
	for (Theme theme : gvNixDistributionThemes) {

	    stringBuilder.append("\tX\t\t\t\t");

	    iterator = installedThemes.iterator();

	    if (installedThemes.size() == 0) {
		stringBuilder.append("-\t\t\t\t");
	    }
	    while (iterator.hasNext()) {

		Theme installedTheme = iterator.next();

		if (installedTheme.getName().equals(theme.getName())) {
		    stringBuilder.append("X\t\t\t\t");
		    iterator.remove();
		    break;
		}
		stringBuilder.append("-\t\t\t\t");
	    }

	    if ((activeTheme != null)
		    && (activeTheme.getName().compareTo(theme.getName()) == 0)) {
		activeTheme = null;
		stringBuilder.append("X\t\t");
	    } else {
		stringBuilder.append("-\t\t");
	    }

	    stringBuilder.append(theme.getName() + "\t\t\t");
	    stringBuilder.append(theme.getDescription() + "\n");

	}

	for (Theme theme : installedThemes) {

	    stringBuilder.append("\tX\t\t\t\t");

	    iterator = gvNixDistributionThemes.iterator();

	    if (gvNixDistributionThemes.size() == 0) {
		stringBuilder.append("-\t\t\t\t");
	    }

	    while (iterator.hasNext()) {

		Theme distributionTheme = iterator.next();

		if (distributionTheme.getName().equals(theme.getName())) {
		    stringBuilder.append("X\t\t\t\t");
		    iterator.remove();
		    break;
		}
		stringBuilder.append("-\t\t\t\t");
	    }

	    if ((activeTheme != null)
		    && (activeTheme.getName().compareTo(theme.getName()) == 0)) {
		activeTheme = null;
		stringBuilder.append("X\t\t");
	    } else {
		stringBuilder.append("-\t\t");
	    }

	    stringBuilder.append(theme.getName() + "\t\t\t");
	    stringBuilder.append(theme.getDescription() + "\n");
	}

	// Active theme.
	if (activeTheme != null) {

	    stringBuilder.append("\t\t-\t\t\t\t");
	    stringBuilder.append("-\t\t\t\t");
	    stringBuilder.append("X\t\t");

	    stringBuilder.append(activeTheme.getName() + "\t\t\t");
	    stringBuilder.append(activeTheme.getDescription() + "\n");
	}

	return stringBuilder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.theme.manager.roo.addon.ThemeManagerOperations#reloadThemeList
     * (org.gvnix.theme.manager.roo.addon.ThemeType, java.lang.String)
     */
    public void reloadThemeList(ThemeType themeType, String themePath) {

	if ((themeType.toString().compareTo(ThemeType.DISTRIBUTION.toString())) == 0) {
	    this.gvNixDistributionThemes = getThemesData(themeType, themePath);

	} else if ((themeType.toString().compareTo(ThemeType.INSTALLED
		.toString())) == 0) {
	    this.installedThemes = getThemesData(themeType, themePath);
	}
    }

    public List<Theme> getGvNixDistributionThemes() {
	return gvNixDistributionThemes;
    }

    public List<Theme> getInstalledThemes() {
	return installedThemes;
    }

    public Theme getActiveTheme() {
	return activeTheme;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.gvnix.theme.manager.roo.addon.ThemeManagerOperations#
     * resetGvNixDistributionThemes()
     */
    public void resetGvNixDistributionThemes() {
	this.gvNixDistributionThemes = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.theme.manager.roo.addon.ThemeManagerOperations#resetInstalledThemes
     * ()
     */
    public void resetInstalledThemes() {
	this.installedThemes = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvnix.theme.manager.roo.addon.ThemeManagerOperations#resetActiveTheme
     * ()
     */
    public void resetActiveTheme() {
	this.activeTheme = null;
    }
}

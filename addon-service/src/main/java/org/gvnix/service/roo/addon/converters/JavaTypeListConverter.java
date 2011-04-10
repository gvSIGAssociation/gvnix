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
package org.gvnix.service.roo.addon.converters;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.roo.addon.converters.JavaTypeList;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.*;
import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;
import org.springframework.roo.support.util.StringUtils;

/**
 * @author Ricardo García Fernández at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class JavaTypeListConverter implements Converter {

  @Reference private MetadataService metadataService;
  @Reference private FileManager fileManager;

  /*
   * (non-Javadoc)
   * @see
   * org.springframework.roo.shell.Converter#convertFromText(java.lang.String,
   * java.lang.Class, java.lang.String)
   */
  public Object convertFromText(String value, Class<?> requiredType,
                                String optionContext) {

    JavaTypeList javaTypeList = new JavaTypeList();
    List<JavaType> javaTypes = new ArrayList<JavaType>();

    String[] javaTypeStringList;

    javaTypeStringList = StringUtils.commaDelimitedListToStringArray(value);

    JavaType javaType;
    for (int i = 0; i <= javaTypeStringList.length - 1; i++) {
      javaType = new JavaType(javaTypeStringList[i]);
      javaTypes.add(javaType);
    }

    javaTypeList.setJavaTypes(javaTypes);

    return javaTypeList;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.roo.shell.Converter#getAllPossibleValues(java.util
   * .List, java.lang.Class, java.lang.String, java.lang.String,
   * org.springframework.roo.shell.MethodTarget)
   */
  public boolean getAllPossibleValues(List<String> completions,
                                      Class<?> requiredType,
                                      String existingData,
                                      String optionContext, MethodTarget target) {

    if (existingData == null) {
      existingData = "";
    }

    // Prepare the strings to compare with JavaTypes.
    String tmpExistingData = existingDataToComplete(existingData);
    String completeExistingDataList = convertInputIntoPrefixCompletion(existingData);

    if (optionContext != null && optionContext.contains("java")) {

      // Compare Java Basic Types.
      completeJavaSpecificPaths(completions, existingData, optionContext,
          completeExistingDataList, tmpExistingData);

      // Compare Project Java Types.
      completeProjectSpecificPaths(completions, existingData,
          completeExistingDataList, tmpExistingData);

    }
    else if (optionContext != null && optionContext.contains("exceptions")) {

      // TODO: Nothing to do.
    }

    return false;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.roo.shell.Converter#supports(java.lang.Class,
   * java.lang.String)
   */
  public boolean supports(Class<?> requiredType, String optionContext) {
    return JavaTypeList.class.isAssignableFrom(requiredType);
  }

  /**
   * Adds common "java." types to the completions. For now we just provide them
   * statically.
   * 
   * @param completions Completions to show in console for the Input String.
   * @param existingData String value from the command attribute.
   * @param optionContext Option context to evaluate.
   * @param completeExistingDataList String separated comma list of JavaType.
   * @param tmpExistingData The last JavaType from console Input to auto
   *          complete.
   */
  private void completeJavaSpecificPaths(List<String> completions,
                                         String existingData,
                                         String optionContext,
                                         String completeExistingDataList,
                                         String tmpExistingData) {

    List<String> types = new ArrayList<String>();

    types.add(Boolean.class.getName());
    types.add(String.class.getName());
    // lang - numeric
    types.add(Number.class.getName());
    types.add(Short.class.getName());
    types.add(Byte.class.getName());
    types.add(Integer.class.getName());
    types.add(Long.class.getName());
    types.add(Float.class.getName());
    types.add(Double.class.getName());
    // misc
    types.add(BigDecimal.class.getName());
    types.add(BigInteger.class.getName());
    // util
    types.add(List.class.getName());
    // util
    types.add(Date.class.getName());
    types.add(Calendar.class.getName());

    for (String type : types) {
      if (type.startsWith(tmpExistingData) || tmpExistingData.startsWith(type)) {
        completions.add(completeExistingDataList.concat(type));
      }
    }
  }

  /**
   * Converts the input String from the shell to a Prefix to the completions.
   * 
   * @param existingData String value from the command attribute.
   * @return {@link String} completePrefix to add for completions.
   */
  private String convertInputIntoPrefixCompletion(String existingData) {

    String[] existingDataList;
    String completeExistingDataList = "";

    existingDataList = StringUtils
        .commaDelimitedListToStringArray(existingData);

    for (int i = 0; i < existingDataList.length - 1; i++) {
      if (completeExistingDataList.compareTo("") == 0) {
        completeExistingDataList = completeExistingDataList
            .concat(existingDataList[i]);
      }
      else {
        completeExistingDataList = completeExistingDataList.concat(",").concat(
            existingDataList[i]);
      }
    }

    if (existingDataList.length > 1) {
      completeExistingDataList = completeExistingDataList.concat(",");
    }

    return completeExistingDataList;
  }

  /**
   * Return the last String member for auto completion.
   * <p>
   * The Input String is separated between commas.
   * </p>
   * 
   * @param existingData Console Input String to compare.
   * @return {@link String} last member of the String to compare.
   */
  private String existingDataToComplete(String existingData) {

    String[] existingDataList;
    String tmpExistingData;

    existingDataList = StringUtils
        .commaDelimitedListToStringArray(existingData);

    if (existingDataList.length > 0) {
      tmpExistingData = StringUtils
          .trimAllWhitespace(existingDataList[existingDataList.length - 1]);
    }
    else {
      tmpExistingData = existingData;
    }

    return tmpExistingData;
  }

  /**
   * Adds project "java." types to the completions. For now we just provide them
   * statically.
   * 
   * @param completions
   * @param existingData
   * @param completeExistingDataList
   * @param tmpExistingData
   */
  private void completeProjectSpecificPaths(List<String> completions,
                                            String existingData,
                                            String completeExistingDataList,
                                            String tmpExistingData) {

    String topLevelPath = "";
    ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
        .get(ProjectMetadata.getProjectIdentifier());

    if (projectMetadata == null) {
      return;
    }

    topLevelPath = projectMetadata.getTopLevelPackage()
        .getFullyQualifiedPackageName();

    String newValue = tmpExistingData;
    if (tmpExistingData.startsWith("~")) {
      if (tmpExistingData.length() > 1) {
        if (tmpExistingData.charAt(1) == '.') {
          newValue = topLevelPath + tmpExistingData.substring(1);
        }
        else {
          newValue = topLevelPath + "." + tmpExistingData.substring(1);
        }
      }
      else {
        newValue = topLevelPath + File.separator;
      }
    }

    PathResolver pathResolver = projectMetadata.getPathResolver();
    String antPath = pathResolver.getRoot(Path.SRC_MAIN_JAVA)
        + File.separatorChar + newValue.replace(".", File.separator) + "*";
    SortedSet<FileDetails> entries = fileManager.findMatchingAntPath(antPath);

    for (FileDetails fileIdentifier : entries) {
      String candidate = pathResolver.getRelativeSegment(
          fileIdentifier.getCanonicalPath()).substring(1); // drop the
      // leading
      // "/"
      boolean include = false;
      boolean directory = false;
      if (fileIdentifier.getFile().isDirectory()) {
        // Do not include directories that start with ., as this is used
        // for purposes like SVN (see ROO-125)
        if (!fileIdentifier.getFile().getName().startsWith(".")) {
          include = true;
          directory = true;
        }
      }
      else {
        // a file
        if (candidate.endsWith(".java")) {
          candidate = candidate.substring(0, candidate.length() - 5); // drop
          // .java
          include = true;
        }
      }

      if (include) {
        // Convert this path back into something the user would type
        if (tmpExistingData.startsWith("~")) {
          if (tmpExistingData.length() > 1) {
            if (tmpExistingData.charAt(1) == '.') {
              candidate = "~." + candidate.substring(topLevelPath.length() + 1);
            }
            else {
              candidate = "~" + candidate.substring(topLevelPath.length() + 1);
            }
          }
          else {
            candidate = "~" + candidate.substring(topLevelPath.length() + 1);
          }
        }
        candidate = candidate.replace(File.separator, ".");
        if (directory) {
          candidate = candidate + ".";
        }
        completions.add(completeExistingDataList.concat(candidate));
      }
    }
  }
}

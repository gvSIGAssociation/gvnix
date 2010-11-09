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
package org.gvnix.service.layer.roo.addon;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.*;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.ServiceLayerWsConfigService.GvNIXAnnotationType;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.event.*;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.Assert;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class ServiceLayerWSExportWSDLListener implements FileEventListener {

    private String generateSourcesDirectory;

    static final String webService = "javax.jws.WebService";
    static final String webServiceInterface = "WebService";
    static final String xmlRootElement = "XmlRootElement";
    static final String xmlAccessorType = "XmlAccessorType";
    static final String xmlType = "XmlType";
    static final String webFault = "WebFault";

    @Reference
    private PathResolver pathResolver;
    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;

    private static final String GENERATED_CXF_SOURCES_DIR = "target/generated-sources/cxf/server/";

    protected static Logger logger = Logger
            .getLogger(ServiceLayerWSExportWSDLListener.class.getName());

    protected void activate(ComponentContext context) {

        this.generateSourcesDirectory = pathResolver.getIdentifier(Path.ROOT,
                GENERATED_CXF_SOURCES_DIR);

        // Reset generated file lists.
        serviceLayerWsConfigService.resetGeneratedFilesList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.file.monitor.event.FileEventListener#onFileEvent
     * (org.springframework.roo.file.monitor.event.FileEvent)
     */
    public void onFileEvent(FileEvent fileEvent) {

        File file = fileEvent.getFileDetails().getFile();

        if (fileEvent.getOperation().compareTo(FileOperation.MONITORING_START) == 0) {

            if (file.getAbsolutePath().contains(this.generateSourcesDirectory)
                    && !file.isDirectory()) {

                // Parse Java file.
                CompilationUnit unit;
                try {
                    unit = JavaParser.parse(file);

                    // Get the first class or interface Java type
                    List<TypeDeclaration> types = unit.getTypes();
                    if (types != null) {
                        TypeDeclaration type = types.get(0);
                        if (type instanceof ClassOrInterfaceDeclaration) {

                            logger.fine(fileEvent.getFileDetails().getFile()
                                    .getAbsolutePath());

                            // Get all annotations.
                            List<AnnotationExpr> annotations = type
                                    .getAnnotations();

                            // Check annotation types.
                            for (AnnotationExpr annotationExpr : annotations) {

                                if (analyzeAnnotations(file, annotationExpr,
                                        type)) {
                                    break;
                                }

                            }
                        }
                    }

                } catch (ParseException e) {
                    Assert.state(false,
                            "Generated web service java file has errors:\n"
                                    + e.getMessage());

                } catch (IOException e) {
                    e.printStackTrace();
                    Assert.state(false,
                            "Generated web service java file has errors:\n"
                                    + e.getMessage());

                }

            }
        }
    }

    /**
     * Check annotations from java generated classes to set file in priority
     * lists.
     * 
     * @param annotationExpr
     *            to check.
     * @param file
     *            file to Add to priority list.
     * @param type 
     *            to check if Web Service class is an interface.
     * 
     * @return true if has found selected annotation.
     * 
     */
    public boolean analyzeAnnotations(File file, AnnotationExpr annotationExpr, TypeDeclaration type) {

        ClassOrInterfaceDeclaration classOrInterfaceDeclaration;

        if (annotationExpr instanceof NormalAnnotationExpr) {

            NormalAnnotationExpr normalAnnotationExpr = (NormalAnnotationExpr) annotationExpr;

            if (normalAnnotationExpr.getName().getName().contains(
                    xmlRootElement)
                    || normalAnnotationExpr.getName().getName().contains(
                            xmlAccessorType)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.XML_ELEMENT);
                return true;
            } else if (normalAnnotationExpr.getName().getName().contains(
                    webFault)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.WEB_FAULT);
                return true;
            } else if (normalAnnotationExpr.getName().toString().contentEquals(
                    webService) || normalAnnotationExpr.getName().toString().contentEquals(
                            webServiceInterface)) {

                classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) type;
                
                if (!classOrInterfaceDeclaration.isInterface()) {

                    serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                            GvNIXAnnotationType.WEB_SERVICE);
                }
                else {
                    serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                            GvNIXAnnotationType.WEB_SERVICE_INTERFACE);
                }

                return true;
            }

        } else if (annotationExpr instanceof MarkerAnnotationExpr) {

            MarkerAnnotationExpr markerAnnotationExpr = (MarkerAnnotationExpr) annotationExpr;

            if (markerAnnotationExpr.getName().getName().contains(
                    xmlRootElement)
                    || markerAnnotationExpr.getName().getName().contains(
                            xmlAccessorType)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.XML_ELEMENT);
                return true;
            } else if (markerAnnotationExpr.getName().getName().contains(
                    webFault)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.WEB_FAULT);
                return true;
            } else if (markerAnnotationExpr.getName().toString().contains(
                    webService) || markerAnnotationExpr.getName().toString().contentEquals(
                            webServiceInterface)) {

                classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) type;

                if (!classOrInterfaceDeclaration.isInterface()) {

                    serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                            GvNIXAnnotationType.WEB_SERVICE);
                }
                else {
                    serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                            GvNIXAnnotationType.WEB_SERVICE_INTERFACE);
                }

                return true;
            }

        } else if (annotationExpr instanceof SingleMemberAnnotationExpr) {

            SingleMemberAnnotationExpr singleMemberAnnotationExpr = (SingleMemberAnnotationExpr) annotationExpr;

            if (singleMemberAnnotationExpr.getName().getName().contains(
                    xmlRootElement)
                    || singleMemberAnnotationExpr.getName().getName().contains(
                            xmlAccessorType)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.XML_ELEMENT);
                return true;
            } else if (singleMemberAnnotationExpr.getName().getName().contains(
                    webFault)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.WEB_FAULT);
                return true;
            } else if (singleMemberAnnotationExpr.getName().getName().contains(
                    webService) || singleMemberAnnotationExpr.getName().getName().contains(
                            webServiceInterface)) {

                classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) type;

                if (!classOrInterfaceDeclaration.isInterface()) {

                    serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                            GvNIXAnnotationType.WEB_SERVICE);
                }
                else {
                    serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                            GvNIXAnnotationType.WEB_SERVICE_INTERFACE);
                }

                return true;
            }
        }

        return false;
    }

}

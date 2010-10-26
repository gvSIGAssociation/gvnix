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
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.gvnix.service.layer.roo.addon.ServiceLayerWsConfigService.GvNIXAnnotationType;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

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

    static final String webService = "WebService";
    static final String xmlRootElement = "XmlRootElement";
    static final String xmlType = "XmlType";
    static final String webFault = "WebFault";

    @Reference
    private PathResolver pathResolver;
    @Reference
    private ServiceLayerWsConfigService serviceLayerWsConfigService;

    protected static Logger logger = Logger
            .getLogger(ServiceLayerWSExportWSDLListener.class.getName());

    protected void activate(ComponentContext context) {

        this.generateSourcesDirectory = pathResolver.getIdentifier(Path.ROOT,
                "target/generated-sources/cxf/");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.file.monitor.event.FileEventListener#onFileEvent
     * (org.springframework.roo.file.monitor.event.FileEvent)
     */
    public void onFileEvent(FileEvent fileEvent) {
        // TODO Auto-generated method stub
        File file = fileEvent.getFileDetails().getFile();

        if (file.getAbsolutePath().contains(this.generateSourcesDirectory)
                && !file.isDirectory()) {

            // Parse the port type Java file
            CompilationUnit unit;
            try {
                unit = JavaParser.parse(file);

                // Get the first class or interface Java type
                List<TypeDeclaration> types = unit.getTypes();
                if (types != null) {
                    TypeDeclaration type = types.get(0);
                    if (type instanceof ClassOrInterfaceDeclaration) {

                        logger.info(fileEvent.getFileDetails().getFile()
                                .getAbsolutePath());

                        // Get all annotations.
                        List<AnnotationExpr> annotations = type
                                .getAnnotations();

                        // Check annotation types.
                        for (AnnotationExpr annotationExpr : annotations) {

                            if (analizeAnnotations(file, annotationExpr)) {
                                break;
                            }

                        }
                    }
                }

            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
     * 
     * @return true if has found selected annotation.
     * 
     */
    public boolean analizeAnnotations(File file, AnnotationExpr annotationExpr) {

        if (annotationExpr instanceof NormalAnnotationExpr) {

            NormalAnnotationExpr normalAnnotationExpr = (NormalAnnotationExpr) annotationExpr;

            if (normalAnnotationExpr.getName().getName().contains(xmlType)
                    || normalAnnotationExpr.getName().getName().contains(
                            xmlRootElement)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.XML_ELEMENT);
                return true;
            } else if (normalAnnotationExpr.getName().getName().contains(
                    webFault)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.WEB_FAULT);
                return true;
            } else if (normalAnnotationExpr.getName().getName().contains(
                    webService)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.WEB_SERVICE);
                return true;
            }

        } else if (annotationExpr instanceof MarkerAnnotationExpr) {

            MarkerAnnotationExpr markerAnnotationExpr = (MarkerAnnotationExpr) annotationExpr;

            if (markerAnnotationExpr.getName().getName().contains(xmlType)
                    || markerAnnotationExpr.getName().getName().contains(
                            xmlRootElement)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.XML_ELEMENT);
                return true;
            } else if (markerAnnotationExpr.getName().getName().contains(
                    webFault)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.WEB_FAULT);
                return true;
            } else if (markerAnnotationExpr.getName().getName().contains(
                    webService)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.WEB_SERVICE);
                return true;
            }

        } else if (annotationExpr instanceof SingleMemberAnnotationExpr) {

            SingleMemberAnnotationExpr singleMemberAnnotationExpr = (SingleMemberAnnotationExpr) annotationExpr;

            if (singleMemberAnnotationExpr.getName().getName()
                    .contains(xmlType)
                    || singleMemberAnnotationExpr.getName().getName().contains(
                            xmlRootElement)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.XML_ELEMENT);
                return true;
            } else if (singleMemberAnnotationExpr.getName().getName().contains(
                    webFault)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.WEB_FAULT);
                return true;
            } else if (singleMemberAnnotationExpr.getName().getName().contains(
                    webService)) {

                serviceLayerWsConfigService.addFileToUpdateAnnotation(file,
                        GvNIXAnnotationType.WEB_SERVICE);
                return true;
            }
        }

        return false;
    }

}

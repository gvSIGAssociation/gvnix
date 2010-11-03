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

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.TypeDeclaration;

import java.util.List;

import org.springframework.roo.classpath.javaparser.CompilationUnitServices;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;

/**
 * @author Ricardo García Fernández ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class ServiceLayerWSCompilationUnit implements CompilationUnitServices {

    private JavaPackage compilationUnitPackage;
    private JavaType enclosingTypeName;
    private List<ImportDeclaration> imports;
    private List<TypeDeclaration> innerTypes;
    
    /**
     * 
     * @param compilationUnitPackage
     * @param enclosingTypeName
     * @param imports
     * @param innerTypes
     */
    public ServiceLayerWSCompilationUnit(JavaPackage compilationUnitPackage,
            JavaType enclosingTypeName, List<ImportDeclaration> imports,
            List<TypeDeclaration> innerTypes) {
        super();
        this.compilationUnitPackage = compilationUnitPackage;
        this.enclosingTypeName = enclosingTypeName;
        this.imports = imports;
        this.innerTypes = innerTypes;
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.javaparser.CompilationUnitServices#flush()
     */
    public void flush() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.javaparser.CompilationUnitServices#getCompilationUnitPackage()
     */
    public JavaPackage getCompilationUnitPackage() {
        // TODO Auto-generated method stub
        return compilationUnitPackage;
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.javaparser.CompilationUnitServices#getEnclosingTypeName()
     */
    public JavaType getEnclosingTypeName() {
        // TODO Auto-generated method stub
        return enclosingTypeName;
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.javaparser.CompilationUnitServices#getImports()
     */
    public List<ImportDeclaration> getImports() {
        // TODO Auto-generated method stub
        return imports;
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.classpath.javaparser.CompilationUnitServices#getInnerTypes()
     */
    public List<TypeDeclaration> getInnerTypes() {
        // TODO Auto-generated method stub
        return innerTypes;
    }

}

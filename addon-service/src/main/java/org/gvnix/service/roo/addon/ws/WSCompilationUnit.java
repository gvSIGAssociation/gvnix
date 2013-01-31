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
package org.gvnix.service.roo.addon.ws;

import com.github.antlrjavaparser.api.ImportDeclaration;
import com.github.antlrjavaparser.api.body.TypeDeclaration;

import java.util.List;

import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.antlrjavaparser.CompilationUnitServices;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;

/**
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class WSCompilationUnit implements CompilationUnitServices {

    private final JavaPackage compilationUnitPackage;
    private final JavaType enclosingTypeName;
    private final List<ImportDeclaration> imports;
    private final List<TypeDeclaration> innerTypes;

    // Added property and initialited on constructor
    private final PhysicalTypeCategory physicalTypeCategory;

    /**
     * 
     * @param compilationUnitPackage
     * @param enclosingTypeName
     * @param imports
     * @param innerTypes
     */
    public WSCompilationUnit(JavaPackage compilationUnitPackage,
            JavaType enclosingTypeName, List<ImportDeclaration> imports,
            List<TypeDeclaration> innerTypes,
            PhysicalTypeCategory physicalTypeCategory) {
        super();
        this.compilationUnitPackage = compilationUnitPackage;
        this.enclosingTypeName = enclosingTypeName;
        this.imports = imports;
        this.innerTypes = innerTypes;
        this.physicalTypeCategory = physicalTypeCategory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.classpath.antlrjavaparser.CompilationUnitServices#
     * flush()
     */
    public void flush() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.classpath.antlrjavaparser.CompilationUnitServices#
     * getCompilationUnitPackage()
     */
    public JavaPackage getCompilationUnitPackage() {
        return compilationUnitPackage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.classpath.antlrjavaparser.CompilationUnitServices#
     * getEnclosingTypeName()
     */
    public JavaType getEnclosingTypeName() {
        return enclosingTypeName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.classpath.antlrjavaparser.CompilationUnitServices#
     * getImports()
     */
    public List<ImportDeclaration> getImports() {
        return imports;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.classpath.antlrjavaparser.CompilationUnitServices#
     * getInnerTypes()
     */
    public List<TypeDeclaration> getInnerTypes() {
        return innerTypes;
    }

    public PhysicalTypeCategory getPhysicalTypeCategory() {
        return physicalTypeCategory;
    }

}

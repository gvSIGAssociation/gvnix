/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.addon.datatables.addon;

import java.util.Collections;
import java.util.List;

import org.springframework.roo.addon.finder.QueryHolder;
import org.springframework.roo.addon.finder.Token;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Class which replaces {@link QueryHolder} to make accessible
 * {@link #getTokens()} method (needed to generate finders in ajax mode)
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @see DatatablesMetadataProvider
 */
public class QueryHolderTokens {

    private final String jpaQuery;
    private final List<JavaType> parameterTypes;
    private final List<JavaSymbolName> parameterNames;
    private final List<Token> tokens;

    public QueryHolderTokens(final String jpaQuery,
            final List<JavaType> parameterTypes,
            final List<JavaSymbolName> parameterNames, final List<Token> tokens) {
        this.jpaQuery = jpaQuery;
        this.parameterTypes = Collections.unmodifiableList(parameterTypes);
        this.parameterNames = Collections.unmodifiableList(parameterNames);
        this.tokens = Collections.unmodifiableList(tokens);
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<JavaType> getParameterTypes() {
        return parameterTypes;
    }

    public List<JavaSymbolName> getParameterNames() {
        return parameterNames;
    }
}

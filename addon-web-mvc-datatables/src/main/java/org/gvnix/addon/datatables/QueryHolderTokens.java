package org.gvnix.addon.datatables;

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
 * @author gvNIX Team
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

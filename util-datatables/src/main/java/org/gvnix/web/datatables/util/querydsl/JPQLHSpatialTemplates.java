package org.gvnix.web.datatables.util.querydsl;

import java.util.Map;
import java.util.Map.Entry;

import com.mysema.query.jpa.DefaultQueryHandler;
import com.mysema.query.jpa.JPQLTemplates;
import com.mysema.query.jpa.QueryHandler;
import com.mysema.query.types.Operator;

public class JPQLHSpatialTemplates extends JPQLTemplates {

    public static final JPQLHSpatialTemplates DEFAULT = new JPQLHSpatialTemplates();

    public JPQLHSpatialTemplates() {
        this(DEFAULT_ESCAPE, DefaultQueryHandler.DEFAULT);
    }

    public JPQLHSpatialTemplates(char escape, QueryHandler queryHandler) {
        super(escape, queryHandler);
        Map<Operator<?>, String> ops = HibernateSpatialSupport.getSpatialOps(
                "", false);
        for (Entry<Operator<?>, String> entry : ops.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public JPQLHSpatialTemplates(char escape) {
        this(escape, DefaultQueryHandler.DEFAULT);
    }

}

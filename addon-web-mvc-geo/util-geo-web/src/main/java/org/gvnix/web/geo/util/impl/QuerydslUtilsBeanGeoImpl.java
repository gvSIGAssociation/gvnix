/*
 * Copyright 2015 DiSiD Technologies S.L.L. All rights reserved.
 * 
 * Project  : DiSiD org.gvnix.web.geo 
 * SVN Id   : $Id$
 */
package org.gvnix.web.geo.util.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.gvnix.web.datatables.util.impl.QuerydslUtilsBeanImpl;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.spatial.jts.path.JTSPolygonPath;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.PathBuilder;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * @author gvNIX Team
 * 
 */
public class QuerydslUtilsBeanGeoImpl extends QuerydslUtilsBeanImpl {

    @Override
    public <T> BooleanBuilder createPredicateByAnd(PathBuilder<T> entity,
            Map<String, Object> searchArgs) {
        BooleanBuilder predicate = super.createPredicateByAnd(entity,
                searchArgs);
        if (searchArgs == null || searchArgs.isEmpty()) {
            return predicate;
        }

        // Build the predicate
        for (Entry<String, Object> entry : searchArgs.entrySet()) {
            String key = entry.getKey();

            // searchArgs can contain dtt_bbox attribute
            if (key.equals(DatatablesUtilsBeanGeoImpl.BOUNDING_BOX_PARAM)) {
                // Getting bbox to Search
                String bBoxToSearch = ((String[]) entry.getValue())[0];
                Geometry bBoxGeometry = null;
                try {
                    bBoxGeometry = getConversionService().convert(bBoxToSearch,
                            Geometry.class);
                }
                catch (Exception e) {
                    try {
                        // Legacy bbox parameter support (no WKT string)
                        bBoxGeometry = getConversionService().convert(
                                String.format("POLYGON((%s))", bBoxToSearch),
                                Geometry.class);
                    }
                    catch (Exception e1) {
                        throw new RuntimeException(
                                String.format(
                                        "Error getting map Bounding Box on QuerydslUtils from string: '%s'",
                                        bBoxToSearch), e);
                    }
                }
                // Getting fields to filter using bbox
                if (searchArgs
                        .get(DatatablesUtilsBeanGeoImpl.BOUNDING_BOX_FIELDS_PARAM) != null
                        && bBoxGeometry != null) {
                    String bBoxFields = ((String[]) searchArgs
                            .get(DatatablesUtilsBeanGeoImpl.BOUNDING_BOX_FIELDS_PARAM))[0];
                    String[] separatedFields = StringUtils.split(bBoxFields,
                            ",");
                    for (String field : separatedFields) {
                        predicate.or(createIntersectsExpression(entity, field,
                                bBoxGeometry));
                    }
                }
            }
        }
        return predicate;
    }

    /**
     * Method to create Bounding box intersects expression
     * 
     * @param entityPath
     * @param boundingBox
     * @param fieldName
     * @return
     */
    public static <T> Predicate createIntersectsExpression(
            PathBuilder<T> entityPath, String fieldName, Geometry boundingBox) {
        JTSPolygonPath<Polygon> polygonPath = new JTSPolygonPath<Polygon>(
                entityPath, fieldName);
        BooleanExpression intersectsExpression = polygonPath
                .intersects(boundingBox);
        return intersectsExpression;
    }
}

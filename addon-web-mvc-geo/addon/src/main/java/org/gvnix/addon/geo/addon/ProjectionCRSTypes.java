package org.gvnix.addon.geo.addon;

/**
 * Example of an enum used for tab-completion of properties.
 * 
 * @since 1.4.0
 */
public enum ProjectionCRSTypes {

    EPSG3857("EPSG3857"), EPSG4326("EPSG4326"), EPSG3395("EPSG3395"), Simple(
            "Simple");

    public final String descripcion;

    ProjectionCRSTypes(String descripcion) {
        this.descripcion = descripcion;
    }

    public static ProjectionCRSTypes getCRSTypes(String crsTypes) {
        if (crsTypes != null && !"null".equals(crsTypes)) {
            try {
                return ProjectionCRSTypes.valueOf(crsTypes);
            }
            catch (java.lang.IllegalArgumentException ex) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
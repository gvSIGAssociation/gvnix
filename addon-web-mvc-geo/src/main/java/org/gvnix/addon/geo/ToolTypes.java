package org.gvnix.addon.geo;

/**
 * Example of an enum used for tab-completion of properties.
 * 
 * @since 1.4.0
 */
public enum ToolTypes {

    GENERIC("generic"), MEASURE_TOOL("measure");

    public final String descripcion;

    ToolTypes(String descripcion) {
        this.descripcion = descripcion;
    }

    public static ToolTypes getCRSTypes(String toolTypes) {
        if (toolTypes != null && !"null".equals(toolTypes)) {
            try {
                return ToolTypes.valueOf(toolTypes);
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
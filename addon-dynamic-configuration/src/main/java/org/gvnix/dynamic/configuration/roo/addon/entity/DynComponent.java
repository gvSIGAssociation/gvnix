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
package org.gvnix.dynamic.configuration.roo.addon.entity;

/**
 * Dynamic configuration component entity.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class DynComponent {

    private String id;

    private String name;

    private DynPropertyList properties;

    public DynComponent() {
        super();
    }

    public DynComponent(String id, String name, DynPropertyList properties) {
        super();
        this.id = id;
        this.name = name;
        this.properties = properties;
    }

    public DynComponent(String name) {
        super();
        this.id = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {

        if (name != null && name.length() > 0) {

            return name;
        }

        return id.substring(id.lastIndexOf(".") + 1, id.length());
    }

    public void setName(String name) {
        this.name = name;
    }

    public DynPropertyList getProperties() {
        return properties;
    }

    public void addProperty(DynProperty property) {
        properties.add(property);
    }

    /**
     * {@inheritDoc}
     * 
     * Name and property/value set, each on new line.
     */
    @Override
    public String toString() {

        // Show the component name
        StringBuffer buffer = new StringBuffer();
        buffer.append(" * " + getName());

        // Show properties
        for (DynProperty prop : properties) {

            // Show the property and value if exists with format
            buffer.append("\n");
            buffer.append("   - " + prop.getKey());
            if (prop.getValue() == null) {

                buffer.append(" = (UNDEFINED)");
            } else {

                buffer.append(" = \"" + prop.getValue() + "\"");
            }
        }

        return buffer.toString();
    }

    /**
     * Two components are equal if their properties are equal.
     * 
     * @param obj
     *            Component to compare to
     * @return Component equals
     */
    public boolean equals(DynComponent obj) {

        for (DynProperty component : properties) {

            boolean exist = false;
            for (DynProperty component2 : obj.getProperties()) {
                if (component.equals(component2)) {

                    exist = true;
                    break;
                }
            }

            if (!exist) {
                return false;
            }
        }

        return true;
    }

}

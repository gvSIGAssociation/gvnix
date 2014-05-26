/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
 * Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.dynamic.configuration.roo.addon.entity;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Dynamic configuration entity.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class DynConfiguration {

    private String name;

    private Boolean active;

    private final DynComponentList components;

    public DynConfiguration() {
        super();
        this.components = new DynComponentList();
        active = Boolean.FALSE;
    }

    public DynConfiguration(String name) {
        super();
        this.components = new DynComponentList();
        active = Boolean.FALSE;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public DynComponentList getComponents() {
        return components;
    }

    public void addComponent(DynComponent component) {
        components.add(component);
    }

    /**
     * {@inheritDoc} Name and active message if configuration active.
     */
    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();

        if (active) {
            buffer.append("      (Active)      ");
        }
        else {
            buffer.append("                    ");
        }
        buffer.append(getName());
        buffer.append("\n----------------------------------------");

        return buffer.toString();
    }

    /**
     * Two configurations are equal if their components are equal.
     * 
     * @param obj Configuration to compare to
     * @return Configuration equals
     */
    @Override
    public boolean equals(Object obj) {
        if (ObjectUtils.equals(this, obj)) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DynConfiguration)) {
            return false;
        }
        DynConfiguration other = (DynConfiguration) obj;
        for (DynComponent component : components) {

            boolean exist = false;
            for (DynComponent component2 : other.getComponents()) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (DynComponent component : components) {
            result = prime * result
                    + ((component == null) ? 0 : component.hashCode());
        }
        return result;
    }
}

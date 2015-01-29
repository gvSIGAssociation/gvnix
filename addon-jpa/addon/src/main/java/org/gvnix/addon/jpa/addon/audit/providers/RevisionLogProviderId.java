/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.jpa.addon.audit.providers;

/**
 * Immutable representation of a {@link RevisionLogProvider}
 * 
 * @author gvNIX Team
 * @since 1.3.0
 * 
 */
public class RevisionLogProviderId {

    private String name;
    private String description;
    private String className;

    public RevisionLogProviderId(RevisionLogProvider provider) {
        this.name = provider.getName();
        this.description = provider.getDescription();
        this.className = provider.getClass().getCanonicalName();
    }

    /**
     * @return provider identifier
     */
    public String getId() {
        return this.name;
    }

    /**
     * @return provider description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param provider
     * @return if provider is current provider
     */
    public boolean is(RevisionLogProvider provider) {
        return name.equals(provider.getName())
                && className.equals(provider.getClass().getCanonicalName());
    }

}

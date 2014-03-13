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
package org.gvnix.addon.jpa.audit.providers;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.audit.JpaAuditCommands;
import org.gvnix.addon.jpa.audit.JpaAuditOperations;
import org.springframework.roo.shell.Completion;
import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;

/**
 * Roo Shell converter for {@link RevisionLogProvider} of
 * {@link JpaAuditCommands}
 * 
 * @author gvNIX Team
 * @since 1.3.0
 */
@Component
@Service
public class RevisionLogIdConverter implements Converter<RevisionLogProviderId> {

    @Reference
    private JpaAuditOperations operations;

    protected void bindOperations(JpaAuditOperations operations) {
        this.operations = operations;
    }

    protected void unbindOperations(JpaAuditOperations operations) {
        this.operations = null;
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.shell.Converter#convertFromText(java.lang.String, java.lang.Class, java.lang.String)
     */
    @Override
    public RevisionLogProviderId convertFromText(String value,
            Class<?> targetType, String optionContext) {
        return operations.getProviderIdByName(value);
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.shell.Converter#getAllPossibleValues(java.util.List, java.lang.Class, java.lang.String, java.lang.String, org.springframework.roo.shell.MethodTarget)
     */
    @Override
    public boolean getAllPossibleValues(List<Completion> completions,
            Class<?> targetType, String existingData, String optionContext,
            MethodTarget target) {
        for (final RevisionLogProviderId id : operations.getProvidersId()) {
            if (existingData.isEmpty() || id.getId().equals(existingData)
                    || id.getId().startsWith(existingData)) {
                completions.add(new Completion(id.getId()));
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.roo.shell.Converter#supports(java.lang.Class, java.lang.String)
     */
    @Override
    public boolean supports(Class<?> type, String optionContext) {
        return RevisionLogProviderId.class.isAssignableFrom(type);
    }

}

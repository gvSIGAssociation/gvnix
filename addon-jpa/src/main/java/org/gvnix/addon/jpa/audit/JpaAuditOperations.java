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
package org.gvnix.addon.jpa.audit;

import java.util.List;

import org.gvnix.addon.jpa.audit.providers.RevisionLogProvider;
import org.gvnix.addon.jpa.audit.providers.RevisionLogProviderId;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;

/**
 * <code>jpa audit</code> operations. Manages basic audition information in
 * entities (creation/last-update date and user).
 * <p/>
 * Audition data is managed using <em>JPA Entity Listeners</em>
 * 
 * @author gvNIX Team
 * @since 1.3.0
 */
public interface JpaAuditOperations {

    /**
     * Indicate setup commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isSetupCommandAvailable();

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isCommandAvailable();

    /**
     * Indicates if there are providers available
     * 
     * @return true if there is providers available
     */
    boolean isProvidersAvailable();

    /**
     * Enables audition on selected entity
     * <p/>
     * If <code>target</code> is not defined, the new class will be create from
     * <code>entity</code> name adding <code>AuditListener</code> suffix, in the
     * same package of <code>entity</code>.
     * <p/>
     * If <code>entity</code> already is configure throws an exception.
     * 
     * @param target (optional) class to create
     * @param entity JPA Active-Record entity to manage
     */
    void create(JavaType entity, JavaType target);

    /**
     * Enables audition on every JPA Active-Record entity in current
     * application. <br>
     * The new classes will be create from <code>entity</code> name adding
     * <code>AuditListener</code> suffix. If <code>targetPackage</code> is not
     * defined, generated classes in the same package of <code>entity</code>.
     * 
     * @param targetPackage (optional)
     */
    void createAll(JavaPackage targetPackage);

    /**
     * Enables audition on selected entity
     * <p/>
     * If <code>target</code> is not defined, the new class will be create from
     * <code>entity</code> name adding <code>AuditListener</code> suffix, in the
     * same package of <code>entity</code>.
     * 
     * If <code>entity</code> already is configure and
     * <code>failIfAlreadySet</code> throws an exception.
     * 
     * @param entity
     * @param target
     * @param failIfAlreadySet or not
     */
    void create(JavaType entity, JavaType target, boolean failIfAlreadySet);

    /**
     * @return the revision log providers available for current project
     */
    List<RevisionLogProvider> getAvailableRevisionLogProviders();

    /**
     * @return the active revision log provider or null if none is activated
     * @throws IllegalStateException if more that one provider informs it's
     *         activated
     */
    RevisionLogProvider getActiveRevisionLogProvider();

    /**
     * @return the revision log providers id by name
     */
    RevisionLogProviderId getProviderIdByName(String value);

    /**
     * @return the revision log providers id available for current project
     */
    List<RevisionLogProviderId> getProvidersId();

    /**
     * Activate revision log functionality
     * 
     * @param provider
     */
    void activeRevisionLog(RevisionLogProviderId provider);

    /**
     * Creates the class which provides the object User for auditory
     * <p/>
     * This method shows a warning to developer that must provide the code to
     * identify user data when:
     * <ul>
     * <li>Spring Security is not configure</li>
     * <li>userType is not {@link String} nor implements Spring Security
     * UserDetails interface</li>
     * </ul>
     * <p/>
     * If userType is a JPA entity user properties will be a relation (annotated
     * it with ManyToOne annotation)
     * 
     * @param serviceClass
     * @param userType
     */
    void setup(JavaType serviceClass, JavaType userType);

}
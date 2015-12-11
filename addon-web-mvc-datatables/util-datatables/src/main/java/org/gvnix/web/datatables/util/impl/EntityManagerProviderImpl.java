/*
 * Copyright 2015 DiSiD Technologies S.L.L. All rights reserved.
 *
 * Project  : DiSiD petclinic
 * SVN Id   : $Id$
 */
package org.gvnix.web.datatables.util.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.persistence.EntityManager;

import org.gvnix.web.datatables.util.EntityManagerProvider;

/**
 * Service which provides EntityManager instance for a JPA Entity class from a
 * ActiveRecord Spring-Roo entity
 *
 * @author gvNIX Team
 * @since 1.4.1
 */
public class EntityManagerProviderImpl implements EntityManagerProvider {

    /**
     * Gets EntityManger instance for a JPA Entity class <br/>
     * Use Introspect API to locate and invoke
     * <code>EntityManager klass.entityManager()</code> static method to get
     * EntityManager instance.
     *
     * @param klass ActiveRecord JPA Entity class
     * @return applicable JPA EntityManager for <code>klass</code>
     * @throws IllegalStateException if <code>klass</code> has no matching
     *         method or can't be invoke
     */
    public EntityManager getEntityManager(Class klass) {

        try {
            Method[] methods = klass.getMethods();

            for (Method method : methods) {
                if ((method.getModifiers() & Modifier.STATIC) != 0) {
                    if (method.getReturnType() == EntityManager.class) {
                        method.setAccessible(true);
                        return (EntityManager) method.invoke(null, null);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new IllegalStateException(
                    "Error getting entity manager for domain class: ".concat(klass
                            .getName()), e);
        }
        throw new IllegalStateException(
                "Cannot get entity manager for domain class: ".concat(klass
                        .getName()));
    }

}

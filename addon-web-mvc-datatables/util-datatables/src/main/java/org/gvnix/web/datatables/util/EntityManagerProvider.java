/*
 * Copyright 2015 DISID Corporation S.L. All rights reserved.
 *
 * Project  : [PROJECT NAME]
 * SVN Id   : $Id$
 */
package org.gvnix.web.datatables.util;

import javax.persistence.EntityManager;

public interface EntityManagerProvider {

    /**
     * Gets EntityManger instance for a JPA Entity class
     * 
     * @param klass JPA Entity class
     * @return applicable JPA EntityManager for <code>klass</code>
     * @throws IllegalStateException if <code>klass</code> has no matching
     *         method or can't be invoke
     */
    public abstract EntityManager getEntityManager(Class klass);

}

/*
 * Copyright 2015 DISID Corporation S.L. All rights reserved.
 *
 * Project  : [PROJECT NAME]
 * SVN Id   : $Id$
 */
package org.gvnix.web.datatables.util;

import javax.persistence.EntityManager;

public interface EntityManagerProvider {

    public abstract EntityManager getEntityManager(Class klass);

}

/*
 * Copyright 2015 DiSiD Technologies S.L.L. All rights reserved.
 *
 * Project  : DiSiD petclinic
 * SVN Id   : $Id$
 */
package org.gvnix.web.datatables.util;

import javax.persistence.EntityManager;

public interface EntityManagerProvider {

    public abstract EntityManager getEntityManager(Class klass);

}

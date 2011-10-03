package org.hibernate.ejb.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.persistence.spi.PersistenceProvider;

import org.hibernate.ejb.HibernatePersistence;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

  private static boolean debug = false;

  private BundleContext bundleContext;

  private static final String JAVAX_PERSISTENCE_PROVIDER_PROP ="javax.persistence.provider";
  private ServiceRegistration serviceRegistration;

    static {
        try {
            String prop = System.getProperty("org.gvnix.osgi.debug");
            debug = prop != null && !"false".equals(prop);
        } catch (Throwable t) { }
    }

  @Override
  public void start(BundleContext context) throws Exception {
    this.bundleContext = bundleContext;
    debugPrintln("Activating ...");

    HibernatePersistence persistenceProvider = new HibernatePersistence();

    Dictionary<String, String> props = new Hashtable<String, String>();
    props.put(JAVAX_PERSISTENCE_PROVIDER_PROP, 
              persistenceProvider.getClass().getName());

    serviceRegistration = context.registerService(PersistenceProvider.class.getName(), persistenceProvider, props);

    debugPrintln("Service: ".concat(PersistenceProvider.class.getName()).concat(" registered. Bundle activated."));
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    serviceRegistration.unregister();
  }

    /**
     * <p>Output debugging messages.</p>
     *
     * @param msg <code>String</code> to print to <code>stderr</code>.
     */
    protected void debugPrintln(String msg) {
        if (debug) {
            System.err.println("gvNIX OSGi (".concat(Long.toString(bundleContext.getBundle().getBundleId())).concat("): ").concat(msg));
        }
    }

}


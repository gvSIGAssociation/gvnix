/**
 * 
 */
package org.gvnix.dynamic.configuration.roo.addon;


/**
 * @author mmartinez
 *
 */
@DynamicConfiguration(file="database.properties")
public class DatabaseDynamicConfiguration implements
    PropertyDynamicConfiguration {

  /* (non-Javadoc)
   * @see org.gvnix.dynamic.configuration.roo.addon.PropertyDynamicConfiguration#read()
   */
  public Object read() {
    
    System.out.println("read database.properties");
    
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.gvnix.dynamic.configuration.roo.addon.PropertyDynamicConfiguration#write(java.lang.Object)
   */
  public void write(Object file) {
    
    System.out.println("write database.properties");
    
    // TODO Auto-generated method stub
  }

}

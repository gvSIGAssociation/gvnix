package __TOP_LEVEL_PACKAGE__.web.menu;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Menu model loader and publisher.
 * <p>
 * This class automatically is loaded by Spring ({@link Component} and
 * {@link Configurable}).
 * <p>
 * In application initialization ({@link WebApplicationObjectSupport}) loads
 * the menu structure and put it in the application context ready for 
 * rendering. Then {@link ContextMenuStrategy} and security will decide which
 * section and items have to be shown.
 */
@Component
@Configurable
public class MenuLoader extends WebApplicationObjectSupport {

  /** Configuration file for menus */
  public static final String MENU_CONFIG_FILE = "/WEB-INF/views/menu.xml";

  /** Key to store Menu in application context */
  public static final String MENU_SERVLET_CONTEXT_KEY = "gvnixMenu";

  public static final String MENU_ITEM_ELEMENT = "menu-item";

  /**
   * Calls to {@link #loadMenu()} and set the menu to the {@link ServletContext}
   */
  protected void initApplicationContext() throws ApplicationContextException {
    if (!(getApplicationContext() instanceof WebApplicationContext)) {
      return;
    }
    Menu menu;
    try {
      menu = loadMenu();
    }
    catch (Exception e) {
      throw new ApplicationContextException("Error loading gvNIX web menu", e);
    }
    getServletContext().setAttribute(MENU_SERVLET_CONTEXT_KEY, menu);
  }

  /**
   * Loads menu model from XML file {@link #MENU_CONFIG_FILE}. This method loads
   * menu using recursivity. Override this
   * method to change menu's source. Remember to set to <code>null</code>
   * {@link #MENU_CONFIG_FILE}.
   * 
   * @return
   * @throws Exception
   */
  protected Menu loadMenu() throws Exception {

    // load and parse menu.xml
    DocumentBuilder db = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder();
    Document xml = null;

    InputStream input = getServletContext().getResourceAsStream(MENU_CONFIG_FILE);
    Assert.notNull(input, "gvNIX menu configuration not found '"
        .concat(MENU_CONFIG_FILE).concat("'"));
    try {
      xml = db.parse(input);
    }
    catch (Exception e) {
      throw e;
    }
    finally {
      input.close();
    }

    // create menu root
    Element root = xml.getDocumentElement();
    String menuId = root.getAttribute("id");
    Menu menu = new Menu(menuId);

    // parse children
    NodeList childNodes = root.getChildNodes();

    // return empty menu if there are no children 
    if(childNodes.getLength() == 0) {
      return menu;
    }

    // load root menu items and their children
    List<MenuItem> childItems = loadMenuItems(childNodes);
    menu.setChildren(childItems);

    return menu;
  }

  /**
   * Transform a list of {@code <menu-item>} elements into {@code MenuItem} 
   * objects.
   * <p>
   * This method loads the children recursively.
   * 
   * @param nodes List of XML elements
   * @return List of MenuItem, note that MenuItem is complete, that is
   * each MenuItem will contain their children List
   */
  public List<MenuItem> loadMenuItems(NodeList nodes) {
    List<MenuItem> items = new ArrayList<MenuItem>();

    for(int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE
          || !MENU_ITEM_ELEMENT.equals(node.getNodeName())) {
        continue;
      }

      // create the MenuItem object 
      MenuItem item = new MenuItem((Element) node);

      // recursively load children
      NodeList childNodes = node.getChildNodes();

      if(childNodes.getLength() > 0) {
        List<MenuItem> childItems = loadMenuItems(childNodes);
        item.setChildren(childItems);
      }

      // add MenuItem to result list 
      items.add(item);
    }
    return items;
  }
}

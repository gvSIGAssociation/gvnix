package org.gvnix.web.screen.roo.addon;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

public interface SeleniumServices {
	
	public void generateTest(JavaType controller, WebPatternType type, WebPatternHierarchy hierarchy, JavaSymbolName field, String name, String serverURL);

}
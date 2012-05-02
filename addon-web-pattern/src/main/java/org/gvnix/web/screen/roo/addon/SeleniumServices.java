package org.gvnix.web.screen.roo.addon;

import org.springframework.roo.model.JavaType;

public interface SeleniumServices {
	
	public void generateTest(JavaType controller, WebPattern type, String name, String serverURL);

}
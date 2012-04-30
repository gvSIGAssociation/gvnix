package org.gvnix.web.screen.roo.addon;

import org.springframework.roo.model.JavaType;

public interface SeleniumServices {
	
	public void generateTest(JavaType controller, String name, String serverURL);

}
package org.gvnix.web.screen.roo.addon;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

public interface SeleniumServices {

	public void generateTestMasterRegister(JavaType controller, String name, String serverURL);

	public void generateTestMasterTabular(JavaType controller, String name, String serverURL);

	public void generateTestMasterTabularEditRegister(JavaType controller, String name, String serverURL);

	public void generateTestDetailTabular(JavaType controller, WebPatternType type, JavaSymbolName field, String name, String serverURL);
}
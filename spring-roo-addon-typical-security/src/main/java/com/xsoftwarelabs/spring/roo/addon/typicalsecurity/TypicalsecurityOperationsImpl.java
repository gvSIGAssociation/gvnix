package com.xsoftwarelabs.spring.roo.addon.typicalsecurity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.Shell;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.xsoftwarelabs.spring.roo.addon.typicalsecurity.utils.TokenReplacementFileCopyUtils;

/**
 * Implementation of commands that are available via the Roo shell.
 * 
 * @since 1.1
 */
@Component
@Service
public class TypicalsecurityOperationsImpl implements TypicalsecurityOperations {
	private static Logger logger = Logger
			.getLogger(TypicalsecurityOperations.class.getName());
	@Reference
	private MetadataService metadataService;
	@Reference
	private FileManager fileManager;
	@Reference
	private PathResolver pathResolver;
	@Reference
	private ProjectOperations projectOperations;
	@Reference
	private Shell shell;
	
	/*	Not available since Roo 1.1.2 (See ROO-2066)
	 *	@Reference
	 *	private ClasspathOperations classpathOperations;
	 */
	
	private static char separator = File.separatorChar;

	public boolean isCommandAvailable() {
		/*	Not available since Roo 1.1.2 (See ROO-2066)
		 *		return getPathResolver() != null
		 *				&& classpathOperations.isPersistentClassAvailable();
		 */
		return projectOperations.isProjectAvailable();
	}

	public String setup(String entityPackage, String controllerPackage) {

		injectCaptchaDependency();
		createUserRoleEntities(entityPackage);
		createControllers(entityPackage, controllerPackage);
		injectDatabasebasedSecurity(entityPackage, controllerPackage);

		return "Done";
	}

	private void injectCaptchaDependency() {
		// <dependency>
		// <groupId>net.tanesha.recaptcha4j</groupId>
		// <artifactId>recaptcha4j</artifactId>
		// <version>0.0.7</version>
		// </dependency>
		projectOperations.addDependency(new Dependency(
				"net.tanesha.recaptcha4j", "recaptcha4j", "0.0.7"));

	}

	/**
	 * Create All the entities required for User, Role and User Role
	 * 
	 * @param entityPackage
	 */
	private void createUserRoleEntities(String entityPackage) {

		// -----------------------------------------------------------------------------------
		// Create User entity
		// -----------------------------------------------------------------------------------
		shell.executeCommand("entity --class " + entityPackage
				+ ".User --testAutomatically --permitReservedWords");
		shell.executeCommand("field string --fieldName firstName --sizeMin 1 --notNull");
		shell.executeCommand("field string --fieldName lastName --sizeMin 1 --notNull");
		shell.executeCommand("field string --fieldName emailAddress --sizeMin 1 --notNull --unique");
		shell.executeCommand("field string --fieldName password --sizeMin 1 --notNull");
		shell.executeCommand("field date --fieldName activationDate --type java.util.Date ");
		shell.executeCommand("field string --fieldName activationKey ");
		shell.executeCommand("field boolean --fieldName enabled ");
		shell.executeCommand("field boolean --fieldName locked ");

		// -----------------------------------------------------------------------------------
		// Create Role entity
		// -----------------------------------------------------------------------------------
		shell.executeCommand("entity --class " + entityPackage
				+ ".Role --testAutomatically --permitReservedWords");
		shell.executeCommand("field string --fieldName roleName --sizeMin 1 --notNull --unique");
		shell.executeCommand("field string --fieldName roleDescription --sizeMin --sizeMax 200 --notNull");

		// -----------------------------------------------------------------------------------
		// Create User Role Mapping
		// -----------------------------------------------------------------------------------
		shell.executeCommand("entity --class " + entityPackage
				+ ".UserRole --testAutomatically");
		shell.executeCommand("field reference --fieldName userEntry --type "
				+ entityPackage + ".User --notNull");
		shell.executeCommand("field reference --fieldName roleEntry --type "
				+ entityPackage + ".Role --notNull");

		// -----------------------------------------------------------------------------------
		// Create Finders for find user by email address and find user role by
		// user
		// -----------------------------------------------------------------------------------
		shell.executeCommand("finder add findUsersByEmailAddress --class " + entityPackage
				+ ".User");
		shell.executeCommand("finder add findUsersByActivationKeyAndEmailAddress --class " + entityPackage
				+ ".User");
		shell.executeCommand("finder add findUserRolesByUserEntry --class " + entityPackage
				+ ".UserRole");

	}

	/**
	 * Create an Controller for User, Role and UserRole
	 * 
	 * @param entityPackage
	 * @param controllerPackage
	 */
	private void createControllers(String entityPackage,
			String controllerPackage) {

		// -----------------------------------------------------------------------------------
		// Controller for User
		// -----------------------------------------------------------------------------------
		shell.executeCommand("controller scaffold --class " + controllerPackage
				+ ".UserController --entity " + entityPackage
				+ ".User");

		// -----------------------------------------------------------------------------------
		// Controller for Role
		// -----------------------------------------------------------------------------------
		shell.executeCommand("controller scaffold --class " + controllerPackage
				+ ".RoleController --entity " + entityPackage
				+ ".Role");

		// -----------------------------------------------------------------------------------
		// Controller for User Role
		// -----------------------------------------------------------------------------------
		shell.executeCommand("controller scaffold --class " + controllerPackage
				+ ".UserRoleController --entity " + entityPackage
				+ ".UserRole");

	}

	/**
	 * Inject database based authentication provider in Spring Security
	 * 
	 * @param entityPackage
	 */
	private void injectDatabasebasedSecurity(String entityPackage,
			String controllerPackage) {

		// ----------------------------------------------------------------------
		// Run Security Setup Addon
		// ----------------------------------------------------------------------
		shell.executeCommand("security setup");

		// ----------------------------------------------------------------------
		// Copy DatabaseAuthenticationProvider from template
		// ----------------------------------------------------------------------
		createAuthenticationProvider(entityPackage, controllerPackage);

		// ----------------------------------------------------------------------
		// Inject database based authentication provider into
		// applicationContext-security.xml
		// ----------------------------------------------------------------------
		injectDatabasebasedAuthProviderInXml(entityPackage);
		
		// ----------------------------------------------------------------------
		// Autowire MessageDigestPasswordEncoder in applicationContext.xml
		// ----------------------------------------------------------------------
		autowireMessageDigestPasswordEncoder(entityPackage);

		createMailSender();
		addForgotPasswordRegisterUserToLoginPage();
		addChangePasswordToFooter();
	}

	private void createMailSender() {
		shell.executeCommand("email sender setup --hostServer smtp.gmail.com --port 587 --protocol SMTP --username rohitsghatoltest@gmail.com --password password4me");
		shell.executeCommand("email template setup --from rohitsghatoltest@gmail.com --subject Password Recovery");

	}

	/**
	 * Inject database based authentication provider into
	 * applicationContext-security.xml
	 * 
	 * @param entityPackage
	 */
	private void injectDatabasebasedAuthProviderInXml(String entityPackage) {
		String springSecurity = pathResolver.getIdentifier(
				Path.SRC_MAIN_RESOURCES,
				"META-INF/spring/applicationContext-security.xml");

		MutableFile mutableConfigXml = null;
		Document webConfigDoc;
		try {
			if (fileManager.exists(springSecurity)) {
				mutableConfigXml = fileManager.updateFile(springSecurity);
				webConfigDoc = XmlUtils.getDocumentBuilder().parse(
						mutableConfigXml.getInputStream());
			} else {
				throw new IllegalStateException("Could not acquire "
						+ springSecurity);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		Element firstInterceptUrl = XmlUtils.findFirstElementByName(
				"intercept-url", webConfigDoc.getDocumentElement());
		Assert.notNull(firstInterceptUrl, "Could not find intercept-url in "
				+ springSecurity);

		firstInterceptUrl.getParentNode().insertBefore(
				new XmlElementBuilder("intercept-url", webConfigDoc)
						.addAttribute("pattern", "/")
						.addAttribute("access", "isAuthenticated()").build(),
				firstInterceptUrl);

		JavaPackage topLevelPackage = getProjectMetadata().getTopLevelPackage();

		String authenticationProviderClass = topLevelPackage
				.getFullyQualifiedPackageName()
				+ ".provider.DatabaseAuthenticationProvider";

		Element databaseAuthenticationProviderBean = new XmlElementBuilder(
				"beans:bean", webConfigDoc)
				.addAttribute("id", "databaseAuthenticationProvider")
				.addAttribute("class", authenticationProviderClass)
				.addChild(
						new XmlElementBuilder("beans:property", webConfigDoc)
								.addAttribute("name", "adminUser")
								.addAttribute("value", "admin").build())
				.addChild(
						new XmlElementBuilder("beans:property", webConfigDoc)
								.addAttribute("name", "adminPassword")
								.addAttribute("value", "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918").build())
				.build();

		Element authenticationManager = XmlUtils.findFirstElementByName(
				"authentication-manager", webConfigDoc.getDocumentElement());

		authenticationManager.getParentNode().insertBefore(
				databaseAuthenticationProviderBean, authenticationManager);

		Element oldAuthProvider = XmlUtils.findFirstElementByName(
				"authentication-provider", webConfigDoc.getDocumentElement());

		// <authentication-provider ref="databaseAuthenticationProvider" />

		Element newAuthProvider = new XmlElementBuilder(
				"authentication-provider", webConfigDoc).addAttribute("ref",
				"databaseAuthenticationProvider").build();
		
		authenticationManager.replaceChild(newAuthProvider, oldAuthProvider);

		newAuthProvider.appendChild(
				new XmlElementBuilder("password-encoder", webConfigDoc)
					.addAttribute("hash", "sha-256").build());

		XmlUtils.writeXml(mutableConfigXml.getOutputStream(), webConfigDoc);

	}

	/**
	 * Inject MessageDigestPasswordEncoder bean in applicationContext.xml
	 * 
	 * @param entityPackage
	 */
	private void autowireMessageDigestPasswordEncoder(String entityPackage) {
		String applicationContext = pathResolver.getIdentifier(
				Path.SRC_MAIN_RESOURCES,
				"META-INF/spring/applicationContext.xml");

		MutableFile mutableConfigXml = null;
		Document webConfigDoc;
		try {
			if (fileManager.exists(applicationContext)) {
				mutableConfigXml = fileManager.updateFile(applicationContext);
				webConfigDoc = XmlUtils.getDocumentBuilder().parse(
						mutableConfigXml.getInputStream());
			} else {
				throw new IllegalStateException("Could not acquire "
						+ applicationContext);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		Element messageDigestPasswordEncoder = new XmlElementBuilder("bean", webConfigDoc)
			.addAttribute("id", "messageDigestPasswordEncoder")
			.addAttribute("class", "org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder")
			.addChild(
				new XmlElementBuilder("constructor-arg", webConfigDoc)
						.addAttribute("value", "sha-256").build()).build();
		
		webConfigDoc.getDocumentElement().appendChild(messageDigestPasswordEncoder);
		
		XmlUtils.writeXml(mutableConfigXml.getOutputStream(), webConfigDoc);

	}

	/**
	 * Copy DatabaseAuthenticationProvider from template
	 * 
	 * @param entityPackage
	 */
	private void createAuthenticationProvider(String entityPackage,
			String controllerPackage) {

		JavaPackage topLevelPackage = getProjectMetadata().getTopLevelPackage();

		String packagePath = topLevelPackage.getFullyQualifiedPackageName()
				.replace('.', separator);

		String finalEntityPackage = entityPackage.replace("~",
				topLevelPackage.getFullyQualifiedPackageName());

		String finalControllerPackage = controllerPackage.replace("~",
				topLevelPackage.getFullyQualifiedPackageName());

		Properties properties = new Properties();
		properties.put("__TOP_LEVEL_PACKAGE__",
				topLevelPackage.getFullyQualifiedPackageName());
		properties.put("__ENTITY_LEVEL_PACKAGE__", finalEntityPackage);
		properties.put("__CONTROLLER_PACKAGE__", finalControllerPackage);

		Map<String, String> map = new HashMap<String, String>();

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
				finalControllerPackage.replace('.', separator) + separator
						+ "ChangePasswordController.java"),
				"ChangePasswordController.java-template");

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
				finalControllerPackage.replace('.', separator) + separator
						+ "ChangePasswordForm.java"),
				"ChangePasswordForm.java-template");

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
				finalControllerPackage.replace('.', separator) + separator
						+ "ChangePasswordValidator.java"),
				"ChangePasswordValidator.java-template");

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
				finalControllerPackage.replace('.', separator) + separator
						+ "SignUpController.java"),
				"SignUpController.java-template");

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
				finalControllerPackage.replace('.', separator) + separator
						+ "UserRegistrationForm.java"),
				"UserRegistrationForm.java-template");

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
				finalControllerPackage.replace('.', separator) + separator
						+ "SignUpValidator.java"),
				"SignUpValidator.java-template");

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
				finalControllerPackage.replace('.', separator) + separator
						+ "ForgotPasswordController.java"),
				"ForgotPasswordController.java-template");

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
				finalControllerPackage.replace('.', separator) + separator
						+ "ForgotPasswordForm.java"),
				"ForgotPasswordForm.java-template");

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA, packagePath
				+ separator + "provider" + separator
				+ "DatabaseAuthenticationProvider.java"),
				"DatabaseAuthenticationProvider.java-template");

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
				finalControllerPackage.replace('.', separator) + separator
						+ "UserController.java"),
				"UserController.java-template");

		String prefix = separator + "WEB-INF/views";

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, prefix
				+ separator + "signup" + separator + "index.jspx"),
				"signup/index.jspx");
		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, prefix
				+ separator + "signup" + separator + "thanks.jspx"),
				"signup/thanks.jspx");
		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, prefix
				+ separator + "signup" + separator + "error.jspx"),
				"signup/error.jspx");
		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, prefix
				+ separator + "signup" + separator + "views.xml"),
				"signup/views.xml");

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, prefix
				+ separator + "forgotpassword" + separator + "index.jspx"),
				"forgotpassword/index.jspx");
		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, prefix
				+ separator + "forgotpassword" + separator + "thanks.jspx"),
				"forgotpassword/thanks.jspx");
		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, prefix
				+ separator + "forgotpassword" + separator + "views.xml"),
				"forgotpassword/views.xml");

		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, prefix
				+ separator + "changepassword" + separator + "index.jspx"),
				"changepassword/index.jspx");
		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, prefix
				+ separator + "changepassword" + separator + "thanks.jspx"),
				"changepassword/thanks.jspx");
		map.put(pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP, prefix
				+ separator + "changepassword" + separator + "views.xml"),
				"changepassword/views.xml");

		for (Entry<String, String> entry : map.entrySet()) {

			MutableFile mutableFile = null;

			String path = entry.getKey();
			String file = entry.getValue();
			try {

				if (fileManager.exists(path))
					mutableFile = fileManager.updateFile(path);
				else
					mutableFile = fileManager.createFile(path);

				TokenReplacementFileCopyUtils.replaceAndCopy(
						TemplateUtils.getTemplate(getClass(), file),
						mutableFile.getOutputStream(), properties);
				
				insertI18nMessages();

			} catch (IOException ioe) {
				throw new IllegalStateException(ioe);
			}
		}

	}

	private void addChangePasswordToFooter() {
		// Look for following in footer.jspx
		// <a href="${logout}">
		// <spring:message code="security_logout"/>
		// </a>
		// and append
		// <a href="changePassword/index">Change Password</a>

		String footerJspx = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
				"WEB-INF/views/footer.jspx");

		MutableFile mutableFooterJspx = null;
		Document footerJspxDoc;
		try {
			if (fileManager.exists(footerJspx)) {
				mutableFooterJspx = fileManager.updateFile(footerJspx);
				footerJspxDoc = XmlUtils.getDocumentBuilder().parse(
						mutableFooterJspx.getInputStream());
				Element logout = XmlUtils.findFirstElement(
						"//a[@href=\"${logout}\"]",
						footerJspxDoc.getDocumentElement());
				Assert.notNull(logout,
						"Could not find <a href=\"${logout}\"> in "
								+ footerJspx);

				logout.getParentNode().appendChild(
						new XmlElementBuilder("div", footerJspxDoc).addChild(
								footerJspxDoc.createTextNode("|")).build());
				String contextPath = getProjectMetadata().getProjectName();
				logout.getParentNode().appendChild(
						new XmlElementBuilder("a", footerJspxDoc)
								.addAttribute(
										"href",
										"/" + contextPath
												+ "/changepassword/index")
								.addChild(
										footerJspxDoc
												.createTextNode("password"))
								.build());
				XmlUtils.writeXml(mutableFooterJspx.getOutputStream(),
						footerJspxDoc);

			} else {
				throw new IllegalStateException("Could not acquire "
						+ footerJspx);
			}
		} catch (Exception e) {
			System.out.println("---> " + e.getMessage());
			throw new IllegalStateException(e);
		}

	}

	private void addForgotPasswordRegisterUserToLoginPage() {
		// <div>
		// <a href ="/TypicalSecurity/forgotpassword/index">Forgot Password</a>
		// | Not a User Yet? <a href ="/TypicalSecurity/signup?form">Sign In</a>
		// </div>

		String loginJspx = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
				"WEB-INF/views/login.jspx");

		MutableFile mutableLoginJspx = null;
		Document loginJspxDoc;
		try {
			if (fileManager.exists(loginJspx)) {
				mutableLoginJspx = fileManager.updateFile(loginJspx);
				loginJspxDoc = XmlUtils.getDocumentBuilder().parse(
						mutableLoginJspx.getInputStream());
				Element form = XmlUtils.findFirstElementByName("form",
						loginJspxDoc.getDocumentElement());
				Assert.notNull(form, "Could not find form in " + loginJspx);

				String contextPath = getProjectMetadata().getProjectName();
				form.appendChild(new XmlElementBuilder("div", loginJspxDoc)
						.addChild(
								loginJspxDoc
										.createTextNode("<br/><a href =\"/"
												+ contextPath
												+ "/forgotpassword/index\">Forgot Password</a> | Not a User Yet? <a href =\"/"
												+ contextPath
												+ "/signup?form\">Sign Up</a>"))
						.build());
				XmlUtils.writeXml(mutableLoginJspx.getOutputStream(),
						loginJspxDoc);

			} else {
				throw new IllegalStateException("Could not acquire "
						+ loginJspx);
			}
		} catch (Exception e) {
			System.out.println("---> " + e.getMessage());
			throw new IllegalStateException(e);
		}

	}

	private void insertI18nMessages() {
		String applicationProperties = pathResolver.getIdentifier(
				Path.SRC_MAIN_WEBAPP, "WEB-INF/i18n/application.properties");

		MutableFile mutableApplicationProperties = null;

		try {
			if (fileManager.exists(applicationProperties)) {
				mutableApplicationProperties = fileManager
						.updateFile(applicationProperties);
				String originalData = convertStreamToString(mutableApplicationProperties
						.getInputStream());

				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						mutableApplicationProperties.getOutputStream()));

				out.write(originalData);
				out.write("label_com_training_spring_roo_model_user_id=Id\n");
				out.write("label_com_training_spring_roo_model_user_lastname=Last Name\n");
				out.write("label_com_training_spring_roo_model_user_failedloginattempts=Failed\n");
				out.write("label_com_training_spring_roo_model_user_password=Password\n");
				out.write("label_com_training_spring_roo_model_userstatusmodel_failedloginattempts=Failed Login Attempts\n");
				out.write("label_com_training_spring_roo_model_user_repeat_password=Repeat Password\n");
				out.write("label_com_training_spring_roo_model_user_version=Version\n");
				out.write("label_com_training_spring_roo_model_user_firstname=First Name\n");
				out.write("label_com_training_spring_roo_model_user_plural=Users\n");
				out.write("label_com_training_spring_roo_model_user=User\n");
				out.write("label_com_training_spring_roo_model_user_enabled=Enabled\n");
				out.write("label_com_training_spring_roo_model_user_repeatpassword=Repeat Password\n");
				out.write("label_com_training_spring_roo_model_user_locked=Locked\n");
				out.write("label_com_training_spring_roo_model_user_activationkey=Activation Key\n");
				out.write("label_com_training_spring_roo_model_user_emailaddress=Email Address\n");
				out.write("label_com_training_spring_roo_model_user_activationdate=Activation Date\n");

				out.close();

			} else {
				throw new IllegalStateException("Could not acquire "
						+ applicationProperties);
			}
		} catch (Exception e) {
			System.out.println("---> " + e.getMessage());
			throw new IllegalStateException(e);
		}

	}

	private String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	/**
	 * @return the path resolver or null if there is no user project
	 */
	private PathResolver getPathResolver() {
		ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
				.get(ProjectMetadata.getProjectIdentifier());
		if (projectMetadata == null) {
			return null;
		}
		return projectMetadata.getPathResolver();
	}

	/**
	 * 
	 * @return the project metadata
	 */
	private ProjectMetadata getProjectMetadata() {
		return (ProjectMetadata) metadataService.get(ProjectMetadata
				.getProjectIdentifier());
	}

}
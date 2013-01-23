
## Test run from hudson integration system.
## Run example:
##  ${WORKSPACE}/code/src/test/resources/integration.sh ${BUILD_NUMBER} ${WORKSPACE} 

##
## Initial
##
	
	## Start graphic environment emulator to start firefox for selenium integration tests
	startx -- `which Xvfb` :1 -screen 0 1024x768x24 2>&1 >/dev/null &
	export DISPLAY=:1
	
	## Remove old potential temporal folder of previous test, create new one and go into
	rm -rf /tmp/gvnixtest*
	mkdir /tmp/gvnixtest$1
	cd /tmp/gvnixtest$1

##
## Roo
##
	
	## bikeshop
	echo bikeshop
	mkdir bikeshop
	cd bikeshop
	gvnix.sh script --file bikeshop.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true
	cd .. 
	
	## clinic
	echo clinic
	mkdir clinic
	cd clinic
	gvnix.sh script --file clinic.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## embedding
	echo embedding
	mkdir embedding
	cd embedding
	gvnix.sh script --file embedding.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## expenses
	echo expenses
	mkdir expenses
	cd expenses
	gvnix.sh script --file expenses.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## gae-expenses
	echo gae-expenses
	mkdir gae-expenses
	cd gae-expenses
	gvnix.sh script --file gae-expenses.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## multimodule
	echo multimodule
	mkdir multimodule
	cd multimodule
	gvnix.sh script --file multimodule.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## pizzashop
	echo pizzashop
	mkdir pizzashop
	cd pizzashop
	gvnix.sh script --file pizzashop.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## vote
	echo vote
	mkdir vote
	cd vote
	gvnix.sh script --file vote.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## wedding
	echo wedding
	mkdir wedding
	cd wedding
	gvnix.sh script --file wedding.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX binding add-on
##
	
	## binding
	echo binding
	mkdir binding
	cd binding
	gvnix.sh script --file $2/code/addon-web-mvc-binding/src/main/resources/binding.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX configuration add-on
##
	
	## configuration
	echo configuration
	mkdir configuration
	cd configuration
	gvnix.sh script --file $2/code/addon-dynamic-configuration/src/main/resources/configuration.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX dialog add-on
##
	
	## dialog
	echo dialog
	mkdir dialog
	cd dialog
	gvnix.sh script --file $2/code/addon-web-dialog/src/main/resources/dialog.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX flex add-on
##
	
	## basic-flex-scaffold-test
	echo basic-flex-scaffold-test
	mkdir basic-flex-scaffold-test
	cd basic-flex-scaffold-test
	gvnix.sh script --file $2/code/spring-flex-roo/org.springframework.flex.roo.addon/src/test/resources/basic-flex-scaffold-test.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## remoting-scaffold-all-test
	echo remoting-scaffold-all-test
	mkdir remoting-scaffold-all-test
	cd remoting-scaffold-all-test
	gvnix.sh script --file $2/code/spring-flex-roo/org.springframework.flex.roo.addon/src/test/resources/remoting-scaffold-all-test.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## flex
	echo flex
	mkdir flex
	cd flex
	gvnix.sh script --file $2/code/spring-flex-roo/org.springframework.flex.roo.addon/src/main/resources/flex.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## rootunes
	echo rootunes
	mkdir rootunes
	cd rootunes
	gvnix.sh script --file $2/code/spring-flex-roo/rootunes.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX i18n add-on
##
	
	## es-i18n
	echo es-i18n
	mkdir es-i18n
	cd es-i18n
	gvnix.sh script --file $2/code/addon-web-i18n/src/main/resources/es-i18n.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX menu add-on
##
	
	## menu
	echo menu
	mkdir menu
	cd menu
	gvnix.sh script --file $2/code/addon-web-menu/src/main/resources/menu.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## base
	echo base
	mkdir base
	cd base
	gvnix.sh script --file $2/code/addon-web-menu/src/test/resources/base.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX occ add-on
##
	
	## occ
	echo occ
	mkdir occ
	cd occ
	gvnix.sh script --file $2/code/addon-occ/src/main/resources/occ.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX pattern add-on
##
	
	## pattern
	echo pattern
	mkdir pattern
	cd pattern
	gvnix.sh script --file $2/code/addon-web-pattern/src/main/resources/pattern.roo
	gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## test-script-pkc-roo
	echo test-script-pkc-roo
	mkdir test-script-pkc-roo
	cd test-script-pkc-roo
	gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc-roo
	gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## test-script-pkc2-roo
	echo test-script-pkc2-roo
	mkdir test-script-pkc2-roo
	cd test-script-pkc2-roo
	gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc2-roo
	gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## test-script-pkc3-roo
	echo test-script-pkc3-roo
	mkdir test-script-pkc3-roo
	cd test-script-pkc3-roo
	gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc3-roo
	gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## test-script-manytomany-roo
	echo test-script-manytomany-roo
	mkdir test-script-manytomany-roo
	cd test-script-manytomany-roo
	gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-manytomany-roo
	gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX report add-on
##
	
	## report
	echo report
	mkdir report
	cd report
	gvnix.sh script --file $2/code/addon-web-report/src/main/resources/report.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## gvnix-test
	echo gvnix-test
	mkdir gvnix-test
	cd gvnix-test
	gvnix.sh script --file $2/code/addon-web-report/src/test/resources/gvnix-test.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX service add-on
##
	
	## bing
	echo bing
	mkdir bing
	cd bing
	gvnix.sh script --file $2/code/addon-service/src/main/resources/bing.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## service
	echo service
	mkdir service
	cd service
	gvnix.sh script --file $2/code/addon-service/src/main/resources/service.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## gvnix-test-no-web
	echo gvnix-test-no-web
	mkdir gvnix-test-no-web
	cd gvnix-test-no-web
	gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-no-web.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## gvnix-test
	echo gvnix-test
	mkdir gvnix-test
	cd gvnix-test
	gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX theme add-on
##
	
	## theme
	echo theme
	mkdir theme
	cd theme
	gvnix.sh script --file $2/code/addon-web-theme/src/main/resources/theme.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX add-ons
##
	
	## gvnix-sample
	echo gvnix-sample
	mkdir gvnix-sample
	cd gvnix-sample
	gvnix.sh script --file $2/code/src/main/assembly/samples/gvnix-sample.roo
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## script
	echo script
	mkdir script
	cd script
	gvnix.sh script --file $2/code/src/test/resources/script.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## tiendavirtual
	echo tiendavirtual
	mkdir tiendavirtual
	cd tiendavirtual
	gvnix.sh script --file $2/code/src/test/resources/tiendavirtual.roo
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..


## Run all integration tests from console and form jenkins integration system daily.
## Run example:
##  ${WORKSPACE}/code/src/test/resources/integration.sh ${BINARY_PATH} ${SOURCE_PATH}
## Where input parameters are:
##  * BINARY_PATH: Path to gvNIX distribution bin folder with which execute the tests 
##  * SOURCE_PATH: Path to gvNIX root source folder for access the test scripts
## Output execution tests will be available at /tmp/gvnixtest folder

##
## Initial
##
	
	## Start graphic environment emulator to start firefox for selenium integration tests
	startx -- `which Xvfb` :1 -screen 0 1024x768x24 2>&1 >/dev/null &
	export DISPLAY=:1
	
	## Remove old potential temporal folder of previous test, create new one and go into
	rm -rf /tmp/gvnixtest
	mkdir /tmp/gvnixtest
	cd /tmp/gvnixtest

##
## Roo
##
	
	## bikeshop
	echo bikeshop
	mkdir bikeshop
	cd bikeshop
	$1/gvnix.sh script --file bikeshop.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true
	cd .. 
	
	## clinic
	echo clinic
	mkdir clinic
	cd clinic
	$1/gvnix.sh script --file clinic.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## embedding
	echo embedding
	mkdir embedding
	cd embedding
	$1/gvnix.sh script --file embedding.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## expenses
	echo expenses
	mkdir expenses
	cd expenses
	$1/gvnix.sh script --file expenses.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## gae-expenses
	echo gae-expenses
	mkdir gae-expenses
	cd gae-expenses
	$1/gvnix.sh script --file gae-expenses.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## multimodule
	echo multimodule
	mkdir multimodule
	cd multimodule
	$1/gvnix.sh script --file multimodule.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## pizzashop
	echo pizzashop
	mkdir pizzashop
	cd pizzashop
	$1/gvnix.sh script --file pizzashop.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## vote
	echo vote
	mkdir vote
	cd vote
	$1/gvnix.sh script --file vote.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## wedding
	echo wedding
	mkdir wedding
	cd wedding
	$1/gvnix.sh script --file wedding.roo --lineNumbers true
##  Error pending on selenium tests
##	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	mvn test tomcat:run -Dmaven.tomcat.fork=true
	cd ..

##
## gvNIX binding add-on
##
	
	## binding
	echo binding
	mkdir binding
	cd binding
	$1/gvnix.sh script --file $2/code/addon-web-mvc-binding/src/main/resources/binding.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX configuration add-on
##
	
	## configuration
	echo configuration
	mkdir configuration
	cd configuration
	$1/gvnix.sh script --file $2/code/addon-dynamic-configuration/src/main/resources/configuration.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX dialog add-on
##
	
	## dialog
	echo dialog
	mkdir dialog
	cd dialog
	$1/gvnix.sh script --file $2/code/addon-web-dialog/src/main/resources/dialog.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX flex add-on
##
	
	## basic-flex-scaffold-test
	echo basic-flex-scaffold-test
	mkdir basic-flex-scaffold-test
	cd basic-flex-scaffold-test
	$1/gvnix.sh script --file $2/code/addon-flex/src/test/resources/basic-flex-scaffold-test.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## remoting-scaffold-all-test
	echo remoting-scaffold-all-test
	mkdir remoting-scaffold-all-test
	cd remoting-scaffold-all-test
	$1/gvnix.sh script --file $2/code/addon-flex/src/test/resources/remoting-scaffold-all-test.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## flex
	echo flex
	mkdir flex
	cd flex
	$1/gvnix.sh script --file $2/code/addon-flex/src/main/resources/flex.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## rootunes
	echo rootunes
	mkdir rootunes
	cd rootunes
	$1/gvnix.sh script --file $2/code/addon-flex/src/main/resources/rootunes.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX i18n add-on
##
	
	## es-i18n
	echo es-i18n
	mkdir es-i18n
	cd es-i18n
	$1/gvnix.sh script --file $2/code/addon-web-i18n/src/main/resources/es-i18n.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX menu add-on
##
	
	## menu
	echo menu
	mkdir menu
	cd menu
	$1/gvnix.sh script --file $2/code/addon-web-menu/src/main/resources/menu.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## base
	echo base
	mkdir base
	cd base
	$1/gvnix.sh script --file $2/code/addon-web-menu/src/test/resources/base.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX occ add-on
##
	
	## occ
	echo occ
	mkdir occ
	cd occ
	$1/gvnix.sh script --file $2/code/addon-occ/src/main/resources/occ.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX pattern add-on
##
	
	## pattern
	echo pattern
	mkdir pattern
	cd pattern
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/main/resources/pattern.roo --lineNumbers true
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## test-script-pkc
	echo test-script-pkc
	mkdir test-script-pkc
	cd test-script-pkc
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc.roo --lineNumbers true
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## test-script-pkc2
	echo test-script-pkc2
	mkdir test-script-pkc2
	cd test-script-pkc2
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc2.roo --lineNumbers true
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## test-script-pkc3
	echo test-script-pkc3
	mkdir test-script-pkc3
	cd test-script-pkc3
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc3.roo --lineNumbers true
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## test-script-manytomany
	echo test-script-manytomany
	mkdir test-script-manytomany
	cd test-script-manytomany
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-manytomany.roo --lineNumbers true
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX report add-on
##
	
	## report
	echo report
	mkdir report
	cd report
	$1/gvnix.sh script --file $2/code/addon-web-report/src/main/resources/report.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## gvnix-test-report
	echo gvnix-test-report
	mkdir gvnix-test-report
	cd gvnix-test-report
	$1/gvnix.sh script --file $2/code/addon-web-report/src/test/resources/gvnix-test-report.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX service add-on
##
	
	## bing
	echo bing
	mkdir bing
	cd bing
	$1/gvnix.sh script --file $2/code/addon-service/src/main/resources/bing.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## service
	echo service
	mkdir service
	cd service
	$1/gvnix.sh script --file $2/code/addon-service/src/main/resources/service.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## gvnix-test-no-web
	echo gvnix-test-no-web
	mkdir gvnix-test-no-web
	cd gvnix-test-no-web
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-no-web.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## gvnix-test
	echo gvnix-test
	mkdir gvnix-test
	cd gvnix-test
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX typicalsecurity add-on
##
	
	## typicalsecurity
	echo typicalsecurity
	mkdir typicalsecurity
	cd typicalsecurity
	$1/gvnix.sh script --file $2/code/addon-web-mvc-typicalsecurity/src/main/resources/typicalsecurity.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX theme add-on
##
	
	## theme
	echo theme
	mkdir theme
	cd theme
	$1/gvnix.sh script --file $2/code/addon-web-theme/src/main/resources/theme.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

##
## gvNIX add-ons
##
	
	## gvnix-sample
	echo gvnix-sample
	mkdir gvnix-sample
	cd gvnix-sample
	$1/gvnix.sh script --file $2/code/src/main/resources/gvnix-sample.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	
	## script
	echo script
	mkdir script
	cd script
	$1/gvnix.sh script --file $2/code/src/main/resources/script.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	
	## tiendavirtual
	echo tiendavirtual
	mkdir tiendavirtual
	cd tiendavirtual
	$1/gvnix.sh script --file $2/code/src/main/resources/tiendavirtual.roo --lineNumbers true
##  The operator > is undefined for the argument type(s) java.lang.String
##  Inter-type declaration conflicts with existing member
##	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..

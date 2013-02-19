
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
	echo bikeshop start
	mkdir bikeshop
	cd bikeshop
	$1/gvnix.sh script --file bikeshop.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true
	cd .. 
	echo bikeshop end
	
	## clinic
	echo clinic start
	mkdir clinic
	cd clinic
	$1/gvnix.sh script --file clinic.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo clinic end

	## embedding
	echo embedding start
	mkdir embedding
	cd embedding
	$1/gvnix.sh script --file embedding.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo embedding end

	## expenses
	echo expenses start
	mkdir expenses
	cd expenses
	$1/gvnix.sh script --file expenses.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo expenses end

	## gae-expenses
	echo gae-expenses start
	mkdir gae-expenses
	cd gae-expenses
	$1/gvnix.sh script --file gae-expenses.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo gae-expenses end

	## multimodule
	echo multimodule start
	mkdir multimodule
	cd multimodule
	$1/gvnix.sh script --file multimodule.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo multimodule end

	## pizzashop
	echo pizzashop start
	mkdir pizzashop
	cd pizzashop
	$1/gvnix.sh script --file pizzashop.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo pizzashop end

	## vote
	echo vote start
	mkdir vote
	cd vote
	$1/gvnix.sh script --file vote.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo vote end

	## wedding
	echo wedding start
	mkdir wedding
	cd wedding
	$1/gvnix.sh script --file wedding.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	mvn test tomcat:run -Dmaven.tomcat.fork=true
	cd ..
	echo wedding end

##
## gvNIX binding add-on
##
	
	## binding
	echo binding start
	mkdir binding
	cd binding
	$1/gvnix.sh script --file $2/code/addon-web-mvc-binding/src/main/resources/binding.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo binding end

##
## gvNIX configuration add-on
##
	
	## configuration
	echo configuration start
	mkdir configuration
	cd configuration
	$1/gvnix.sh script --file $2/code/addon-dynamic-configuration/src/main/resources/configuration.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo configuration end

##
## gvNIX dialog add-on
##
	
	## dialog
	echo dialog start
	mkdir dialog
	cd dialog
	$1/gvnix.sh script --file $2/code/addon-web-dialog/src/main/resources/dialog.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo dialog end

##
## gvNIX flex add-on
##
	
	## basic-flex-scaffold-test
	echo basic-flex-scaffold-test start
	mkdir basic-flex-scaffold-test
	cd basic-flex-scaffold-test
	$1/gvnix.sh script --file $2/code/addon-flex/src/test/resources/basic-flex-scaffold-test.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo basic-flex-scaffold-test end

	## remoting-scaffold-all-test
	echo remoting-scaffold-all-test start
	mkdir remoting-scaffold-all-test
	cd remoting-scaffold-all-test
	$1/gvnix.sh script --file $2/code/addon-flex/src/test/resources/remoting-scaffold-all-test.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo remoting-scaffold-all-test end

	## flex
	echo flex start
	mkdir flex
	cd flex
	$1/gvnix.sh script --file $2/code/addon-flex/src/main/resources/flex.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo flex end

	## rootunes
	echo rootunes start
	mkdir rootunes
	cd rootunes
	$1/gvnix.sh script --file $2/code/addon-flex/src/main/resources/rootunes.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo rootunes end

##
## gvNIX i18n add-on
##
	
	## es-i18n
	echo es-i18n start
	mkdir es-i18n
	cd es-i18n
	$1/gvnix.sh script --file $2/code/addon-web-i18n/src/main/resources/es-i18n.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo es-i18n end

##
## gvNIX menu add-on
##
	
	## menu
	echo menu start
	mkdir menu
	cd menu
	$1/gvnix.sh script --file $2/code/addon-web-menu/src/main/resources/menu.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo menu end
	
	## base
	echo base start
	mkdir base
	cd base
	$1/gvnix.sh script --file $2/code/addon-web-menu/src/test/resources/base.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo base end

##
## gvNIX occ add-on
##
	
	## occ
	echo occ start
	mkdir occ
	cd occ
	$1/gvnix.sh script --file $2/code/addon-occ/src/main/resources/occ.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo occ end

##
## gvNIX pattern add-on
##
	
	## pattern
	echo pattern start
	mkdir pattern
	cd pattern
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/main/resources/pattern.roo --lineNumbers true
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo pattern end
	
	## test-script-pkc
	echo test-script-pkc start
	mkdir test-script-pkc
	cd test-script-pkc
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc.roo --lineNumbers true
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo test-script-pkc end
	
	## test-script-pkc2
	echo test-script-pkc2 start
	mkdir test-script-pkc2
	cd test-script-pkc2
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc2.roo --lineNumbers true
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo test-script-pkc2 end

	## test-script-pkc3
	echo test-script-pkc3 start
	mkdir test-script-pkc3
	cd test-script-pkc3
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc3.roo --lineNumbers true
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo test-script-pkc3 end

	## test-script-manytomany
	echo test-script-manytomany start
	mkdir test-script-manytomany
	cd test-script-manytomany
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-manytomany.roo --lineNumbers true
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo test-script-manytomany end

##
## gvNIX report add-on
##
	
	## report
	echo report start
	mkdir report
	cd report
	$1/gvnix.sh script --file $2/code/addon-web-report/src/main/resources/report.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo report end
	
	## gvnix-test-report
	echo gvnix-test-report start
	mkdir gvnix-test-report
	cd gvnix-test-report
	$1/gvnix.sh script --file $2/code/addon-web-report/src/test/resources/gvnix-test-report.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo gvnix-test-report end

##
## gvNIX service add-on
##
	
	## bing
	echo bing start
	mkdir bing
	cd bing
	$1/gvnix.sh script --file $2/code/addon-service/src/main/resources/bing.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo bing end

	## service
	echo service start
	mkdir service
	cd service
	$1/gvnix.sh script --file $2/code/addon-service/src/main/resources/service.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo service end

	## gvnix-test-no-jpa-no-web
	echo gvnix-test-no-jpa-no-web start
	mkdir gvnix-test-no-jpa-no-web
	cd gvnix-test-no-jpa-no-web
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-no-jpa-no-web.roo --lineNumbers true
	mvn test package -Dmaven.tomcat.fork=true 
	cd ..
	echo gvnix-test-no-jpa-no-web end

	## gvnix-test-no-jpa
	echo gvnix-test-no-jpa start
	mkdir gvnix-test-no-jpa
	cd gvnix-test-no-jpa
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-no-jpa.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo gvnix-test-no-jpa end

	## gvnix-test-no-web
	echo gvnix-test-no-web start
	mkdir gvnix-test-no-web
	cd gvnix-test-no-web
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-no-web.roo --lineNumbers true
	mvn test package -Dmaven.tomcat.fork=true 
	cd ..
	echo gvnix-test-no-web end
	
	## gvnix-test
	echo gvnix-test start
	mkdir gvnix-test
	cd gvnix-test
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo gvnix-test end

	## gvnix-test-security
	echo gvnix-test-security start
	mkdir gvnix-test-security
	cd gvnix-test-security
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-security.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo gvnix-test-security end

	## gvnix-test-entity
	echo gvnix-test-entity start
	mkdir gvnix-test-entity
	cd gvnix-test-entity
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-entity.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo gvnix-test-entity end

##
## gvNIX typicalsecurity add-on
##
	
	## typicalsecurity
	echo typicalsecurity start
	mkdir typicalsecurity
	cd typicalsecurity
	$1/gvnix.sh script --file $2/code/addon-web-mvc-typicalsecurity/src/main/resources/typicalsecurity.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo typicalsecurity end

##
## gvNIX theme add-on
##
	
	## theme
	echo theme start
	mkdir theme
	cd theme
	$1/gvnix.sh script --file $2/code/addon-web-theme/src/main/resources/theme.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo theme end

##
## gvNIX add-ons
##
	
	## gvnix-sample
	echo gvnix-sample start
	mkdir gvnix-sample
	cd gvnix-sample
	$1/gvnix.sh script --file $2/code/src/main/resources/gvnix-sample.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo gvnix-sample end

	## script
	echo script start
	mkdir script
	cd script
	$1/gvnix.sh script --file $2/code/src/main/resources/script.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo script end
	
	## tiendavirtual
	echo tiendavirtual start
	mkdir tiendavirtual
	cd tiendavirtual
	$1/gvnix.sh script --file $2/code/src/main/resources/tiendavirtual.roo --lineNumbers true
##  Inter-type declaration conflicts with existing member, avoid it temporally
    mvn test-compile > /dev/null
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo tiendavirtual end

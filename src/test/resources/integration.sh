
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
## gvNIX cit security add-on
##

	## cit-security
	echo cit-security start
	mkdir cit-security
	cd cit-security
	$1/gvnix.sh script --file $2/code/addon-cit-security/src/test/resources/cit-security.roo --lineNumbers true
	mvn test tomcat:run &
	mkdir target
	# Get login page when accessing a not allowed page and login in es and en languages
	wget --retry-connrefused -O target/loginredirect.html http://localhost:8080/petclinic/pets
	wget --retry-connrefused -O target/logines.html http://localhost:8080/petclinic/login/?lang=es
	wget --retry-connrefused -O target/loginen.html http://localhost:8080/petclinic/login/?lang=en
	# Get logout URL
	wget --retry-connrefused -O target/logout.html http://localhost:8080/petclinic/static/j_spring_security_logout
    MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
	cd ..
	echo cit-security end

##
## gvNIX configuration add-on
##
	
	## configuration
	echo configuration start
	mkdir configuration
	cd configuration
	$1/gvnix.sh script --file $2/code/addon-dynamic-configuration/src/main/resources/configuration.roo --lineNumbers true
	# Start application with both configurations
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true -Pdev
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true -Ppro -Ddatabase.password=
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
	mkdir target
    # Get no entities dialog message in es and en languages
	wget --retry-connrefused -O target/petses.html http://localhost:8080/petclinic/pets?page=1&size=10&lang=es &
	wget --retry-connrefused -O target/petsen.html http://localhost:8080/petclinic/pets?page=1&size=10&lang=en &	
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
##  java.lang.ClassNotFoundException: org.hibernate.collection.PersistentCollection
##	mvn test tomcat:run -Dmaven.tomcat.fork=true
    mvn test 
	cd ..
	echo basic-flex-scaffold-test end

	## remoting-scaffold-all-test
	echo remoting-scaffold-all-test start
	mkdir remoting-scaffold-all-test
	cd remoting-scaffold-all-test
	$1/gvnix.sh script --file $2/code/addon-flex/src/test/resources/remoting-scaffold-all-test.roo --lineNumbers true
##  java.lang.ClassNotFoundException: org.hibernate.collection.PersistentCollection
##	mvn test tomcat:run -Dmaven.tomcat.fork=true
    mvn test 
	cd ..
	echo remoting-scaffold-all-test end

	## flex
	echo flex start
	mkdir flex
	cd flex
	$1/gvnix.sh script --file $2/code/addon-flex/src/main/resources/flex.roo --lineNumbers true
##  java.lang.ClassNotFoundException: org.hibernate.collection.PersistentCollection
##	mvn test tomcat:run -Dmaven.tomcat.fork=true
    mvn test 
	cd ..
	echo flex end

	## rootunes
	echo rootunes start
	mkdir rootunes
	cd rootunes
	$1/gvnix.sh script --file $2/code/addon-flex/src/main/resources/rootunes.roo --lineNumbers true
##  java.lang.ClassNotFoundException: org.hibernate.collection.PersistentCollection
##	mvn test tomcat:run -Dmaven.tomcat.fork=true
    mvn test 
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
	mkdir target
	# Request the main pages in ca and es languages (home, create, list and find)
	wget --retry-connrefused -O target/langca.html http://localhost:8080/petclinic/?lang=ca &
	wget --retry-connrefused -O target/petsformca.html http://localhost:8080/petclinic/pets?form&lang=ca &
	wget --retry-connrefused -O target/ownersca.html http://localhost:8080/petclinic/owners?page=1&size=10&lang=ca &
	wget --retry-connrefused -O target/visitsfindca.html http://localhost:8080/petclinic/visits?find=ByDescriptionAndVisitDate&form&lang=ca &
	wget --retry-connrefused -O target/langes.html http://localhost:8080/petclinic/?lang=es &
	wget --retry-connrefused -O target/petsformes.html http://localhost:8080/petclinic/pets?form&lang=es &
	wget --retry-connrefused -O target/ownerses.html http://localhost:8080/petclinic/owners?page=1&size=10&lang=es &
	wget --retry-connrefused -O target/visitsfindes.html http://localhost:8080/petclinic/visits?find=ByDescriptionAndVisitDate&form&lang=es &
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
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
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
##  Reopen shell to generate pending pattern resources
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo pattern end
	
	## test-script-pkc
	echo test-script-pkc start
	mkdir test-script-pkc
	cd test-script-pkc
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc.roo --lineNumbers true
##  Reopen shell to generate pending pattern resources
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo test-script-pkc end
	
	## test-script-pkc2
	echo test-script-pkc2 start
	mkdir test-script-pkc2
	cd test-script-pkc2
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc2.roo --lineNumbers true
##  Reopen shell to generate pending pattern resources
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo test-script-pkc2 end

	## test-script-pkc3
	echo test-script-pkc3 start
	mkdir test-script-pkc3
	cd test-script-pkc3
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc3.roo --lineNumbers true
##  Reopen shell to generate pending pattern resources
	$1/gvnix.sh hint
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo test-script-pkc3 end

	## test-script-manytomany
	echo test-script-manytomany start
	mkdir test-script-manytomany
	cd test-script-manytomany
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-manytomany.roo --lineNumbers true
##  Reopen shell to generate pending pattern resources
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
	mkdir target
	# Request report form and report generation URLs for en and es languages
	wget --retry-connrefused -O target/reportformen.html http://localhost:8080/petclinic/pets/reports/petlist?form&lang=en &
	wget --retry-connrefused -O target/reportpdfen.html http://localhost:8080/petclinic/pets/reports/petlist?format=pdf&lang=en &
	wget --retry-connrefused -O target/reportxlsen.html http://localhost:8080/petclinic/pets/reports/petlist?format=xls&lang=en &
	wget --retry-connrefused -O target/reporthtmlen.html http://localhost:8080/petclinic/pets/reports/petlist?format=html&lang=en &
	wget --retry-connrefused -O target/reportcsven.html http://localhost:8080/petclinic/pets/reports/petlist?format=csv&lang=en &
	wget --retry-connrefused -O target/reportformes.html http://localhost:8080/petclinic/pets/reports/petlist?form&lang=es &
	wget --retry-connrefused -O target/reportpdfes.html http://localhost:8080/petclinic/pets/reports/petlist?format=pdf&lang=es &
	wget --retry-connrefused -O target/reportxlses.html http://localhost:8080/petclinic/pets/reports/petlist?format=xls&lang=es &
	wget --retry-connrefused -O target/reporthtmles.html http://localhost:8080/petclinic/pets/reports/petlist?format=html&lang=es &
	wget --retry-connrefused -O target/reportcsves.html http://localhost:8080/petclinic/pets/reports/petlist?format=csv&lang=es &
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo report end
	
	## gvnix-test-report
	echo gvnix-test-report start
	mkdir gvnix-test-report
	cd gvnix-test-report
	$1/gvnix.sh script --file $2/code/addon-web-report/src/test/resources/gvnix-test-report.roo --lineNumbers true
	mkdir target
	# Request report form and report generation URLs
	wget --retry-connrefused -O target/reportform.html http://localhost:8080/webreport-test/people/reports/personlist?form &
	wget --retry-connrefused -O target/report.html http://localhost:8080/webreport-test/people/reports/personlist?format=pdf &
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
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
	# Get services summary page and available WSDLs
	wget --retry-connrefused -O target/services.html http://localhost:8080/petclinic/services/ &
	wget --retry-connrefused -O target/petservice.wsdl http://localhost:8080/petclinic/services/PetService?wsdl &
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo service end

	## gvnix-test-no-jpa-no-web
	echo gvnix-test-no-jpa-no-web start
	mkdir gvnix-test-no-jpa-no-web
	cd gvnix-test-no-jpa-no-web
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-no-jpa-no-web.roo --lineNumbers true
	mvn test package
	cd ..
	echo gvnix-test-no-jpa-no-web end

	## gvnix-test-no-jpa
	echo gvnix-test-no-jpa start
	mkdir gvnix-test-no-jpa
	cd gvnix-test-no-jpa
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-no-jpa.roo --lineNumbers true
	# Error #5832 pending from ROO-770 and SPR-6819 resolution
#	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo gvnix-test-no-jpa end

	## gvnix-test-no-web
	echo gvnix-test-no-web start
	mkdir gvnix-test-no-web
	cd gvnix-test-no-web
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-no-web.roo --lineNumbers true
	mvn test package
	cd ..
	echo gvnix-test-no-web end
	
	## gvnix-test
	echo gvnix-test start
	mkdir gvnix-test
	cd gvnix-test
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test.roo --lineNumbers true
	# Get services summary page and available WSDLs
	wget --retry-connrefused -O target/services.html http://localhost:8080/service-layer-test/services/ &
	wget --retry-connrefused -O target/clase.wsdl http://localhost:8080/service-layer-test/services/Clase?wsdl &
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo gvnix-test end

	## gvnix-test-security
	echo gvnix-test-security start
	mkdir gvnix-test-security
	cd gvnix-test-security
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-security.roo --lineNumbers true
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo gvnix-test-security end

	## gvnix-test-entity
	echo gvnix-test-entity start
	mkdir gvnix-test-entity
	cd gvnix-test-entity
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-entity.roo --lineNumbers true
	mkdir target
	# Get services summary page and available WSDLs
	wget --retry-connrefused -O target/services.html http://localhost:8080/petclinic/services/ &
	wget --retry-connrefused -O target/pet.wsdl http://localhost:8080/petclinic/services/Pet?wsdl &
	wget --retry-connrefused -O target/visit.wsdl http://localhost:8080/petclinic/services/Visit?wsdl &
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
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
	mvn test tomcat:run &
	mkdir target
	# Get login page when accessing a not allowed page and login in es and en languages
	wget --retry-connrefused -O target/loginredirect.html http://localhost:8080/petclinic/pets
	wget --retry-connrefused -O target/logines.html http://localhost:8080/petclinic/login/?lang=es
	wget --retry-connrefused -O target/loginen.html http://localhost:8080/petclinic/login/?lang=en
	# Get forgotpassword and signup pages in es and en languages
	wget --retry-connrefused -O target/forgotpasswordes.html http://localhost:8080/petclinic/forgotpassword/index?lang=es
	wget --retry-connrefused -O target/signupes.html http://localhost:8080/petclinic/signup?form&lang=es
	wget --retry-connrefused -O target/forgotpassworden.html http://localhost:8080/petclinic/forgotpassword/index?lang=en
	wget --retry-connrefused -O target/signupen.html http://localhost:8080/petclinic/signup?form&lang=en
	# Get logout URL
	wget --retry-connrefused -O target/logout.html http://localhost:8080/petclinic/static/j_spring_security_logout
    MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
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
	mkdir target
	# Get home a non home pages in es and en languages
	wget --retry-connrefused -O target/homees.html http://localhost:8080/petclinic/?lang=es &
	wget --retry-connrefused -O target/petses.html http://localhost:8080/petclinic/pets?lang=es &
	wget --retry-connrefused -O target/homeen.html http://localhost:8080/petclinic/?lang=en &
	wget --retry-connrefused -O target/petsen.html http://localhost:8080/petclinic/pets?lang=en &
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo theme end
	
	## theme-gvnix
	echo theme-gvnix start
	mkdir theme-gvnix
	cd theme-gvnix
	$1/gvnix.sh script --file $2/code/addon-web-theme/src/test/resources/theme-gvnix.roo --lineNumbers true
	mkdir target
	# Get home pages in es and en languages
	wget --retry-connrefused -O target/homees.html http://localhost:8080/petclinic/?lang=es &
	wget --retry-connrefused -O target/homeen.html http://localhost:8080/petclinic/?lang=en &
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo theme-gvnix end

##
## gvNIX add-ons
##
	
	## gvnix-sample
	echo gvnix-sample start
	mkdir gvnix-sample
	cd gvnix-sample
	$1/gvnix.sh script --file $2/code/src/main/resources/gvnix-sample.roo --lineNumbers true
##  Inter-type declaration conflicts with existing member, avoid it temporally
    mvn test-compile > /dev/null
	# Get services summary page and available WSDLs
	wget --retry-connrefused -O target/services.html http://localhost:8080/sample/services/ &
	wget --retry-connrefused -O target/claseservicio.wsdl http://localhost:8080/sample/services/ClaseServicio?wsdl &
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo gvnix-sample end

	## script
	echo script start
	mkdir script
	cd script
	$1/gvnix.sh script --file $2/code/src/main/resources/script.roo --lineNumbers true
	# Request the home URL
	wget --retry-connrefused -O target/home.html http://localhost:8080/petclinic/ &
	# Get home a non home pages in ca language
	wget --retry-connrefused -O target/homeca.html http://localhost:8080/petclinic/?lang=ca &
	wget --retry-connrefused -O target/petsca.html http://localhost:8080/petclinic/pets?lang=ca &
	# Get services summary page and available WSDLs
	wget --retry-connrefused -O target/services.html http://localhost:8080/petclinic/services/ &
	wget --retry-connrefused -O target/clase.wsdl http://localhost:8080/petclinic/services/Clase?wsdl &
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo script end
	
	## tiendavirtual
	echo tiendavirtual start
	mkdir tiendavirtual
	cd tiendavirtual
	$1/gvnix.sh script --file $2/code/src/main/resources/tiendavirtual.roo --lineNumbers true
##  Reopen shell to generate pending pattern resources
	$1/gvnix.sh hint
##  Inter-type declaration conflicts with existing member, avoid it temporally
    mvn test-compile > /dev/null
	# Request the home URL
	wget --retry-connrefused -O target/home.html http://localhost:8080/tiendavirtual/ &	
    # Get no entities dialog message
	wget --retry-connrefused -O target/dialog.html http://localhost:8080/tiendavirtual/pedidoes?gvnixform&gvnixpattern=pedido&index=1 &
	# Request report form and report generation URLs in ca language
	wget --retry-connrefused -O target/reportformca.html http://localhost:8080/tiendavirtual/pedidoes/reports/informepedidos?form&lang=ca &
	wget --retry-connrefused -O target/reportpdfca.html http://localhost:8080/tiendavirtual/pedidoes/reports/informepedidos?format=pdf&lang=ca &
	wget --retry-connrefused -O target/reportxlsca.html http://localhost:8080/tiendavirtual/pedidoes/reports/informepedidos?format=xls&lang=ca &
	wget --retry-connrefused -O target/reporthtmlca.html http://localhost:8080/tiendavirtual/pedidoes/reports/informepedidos?format=html&lang=ca &
	wget --retry-connrefused -O target/reportcsvca.html http://localhost:8080/tiendavirtual/pedidoes/reports/informepedidos?format=csv&lang=ca &
	# Get pattern urls of 3 patterns (one of each type) in ca and en languages
	wget --retry-connrefused -O target/patternregistroca.html http://localhost:8080/tiendavirtual/productoes?gvnixform&gvnixpattern=ficha&index=1&lang=ca &
	wget --retry-connrefused -O target/patterntabularca.html http://localhost:8080/tiendavirtual/clientes?gvnixpattern=clientes&lang=ca &
	wget --retry-connrefused -O target/patternregistrotabularca.html http://localhost:8080/tiendavirtual/pedidoes?gvnixform&gvnixpattern=pedido&index=1&lang=ca &
	wget --retry-connrefused -O target/patternregistroen.html http://localhost:8080/tiendavirtual/productoes?gvnixform&gvnixpattern=ficha&index=1&lang=en &
	wget --retry-connrefused -O target/patterntabularen.html http://localhost:8080/tiendavirtual/clientes?gvnixpattern=clientes&lang=en &
	wget --retry-connrefused -O target/patternregistrotabularen.html http://localhost:8080/tiendavirtual/pedidoes?gvnixform&gvnixpattern=pedido&index=1&lang=en &
	mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
	cd ..
	echo tiendavirtual end

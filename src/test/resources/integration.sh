
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
	## startx -- `which Xvfb` :1 -screen 0 1024x768x24 2>&1 >/dev/null &
	export DISPLAY=:1
	
	## Remove old potential temporal folder of previous test, create new one and go into
	rm -rf /tmp/gvnixtest
	mkdir /tmp/gvnixtest
	cd /tmp/gvnixtest

##
## Roo
##
	
	
	## clinic
    echo "======================================================"
    echo "------------------------------------------------------"
	echo clinic start
	mkdir clinic
	cd clinic
	$1/gvnix.sh script --file clinic.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo clinic end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## embedding
    echo "======================================================"
    echo "------------------------------------------------------"
	echo embedding start
	mkdir embedding
	cd embedding
	$1/gvnix.sh script --file embedding.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo embedding end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .


	## multimodule
    echo "======================================================"
    echo "------------------------------------------------------"
	echo multimodule start
	mkdir multimodule
	cd multimodule
	$1/gvnix.sh script --file multimodule.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo multimodule end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## pizzashop
    echo "======================================================"
    echo "------------------------------------------------------"
	echo pizzashop start
	mkdir pizzashop
	cd pizzashop
	$1/gvnix.sh script --file pizzashop.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo pizzashop end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## vote
    echo "======================================================"
    echo "------------------------------------------------------"
	echo vote start
	mkdir vote
	cd vote
	$1/gvnix.sh script --file vote.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo vote end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## wedding
    echo "======================================================"
    echo "------------------------------------------------------"
	echo wedding start
	mkdir wedding
	cd wedding
	$1/gvnix.sh script --file wedding.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo wedding end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

##
## gvNIX binding add-on
##
	
	## binding
    echo "======================================================"
    echo "------------------------------------------------------"
	echo binding start
	mkdir binding
	cd binding
	$1/gvnix.sh script --file $2/code/addon-web-mvc-binding/src/main/resources/binding.roo --lineNumbers true
	mkdir target
	mvn clean compile
	cd ..
	echo binding end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	
	## configuration
    echo "======================================================"
    echo "------------------------------------------------------"
	echo configuration start
	mkdir configuration
	cd configuration
	$1/gvnix.sh script --file $2/code/addon-dynamic-configuration/src/main/resources/configuration.roo --lineNumbers true
	mkdir target
	mvn clean compile
	cd ..
	echo configuration end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .


##
## gvNIX Web MVC Addon
## 

	## batch
	echo "======================================================"
   	 echo "------------------------------------------------------"
	echo batch start
	mkdir batch
	cd batch
	$1/gvnix.sh script --file $2/code/addon-web-mvc/addon/src/test/batch.roo --lineNumbers true
	mkdir target
	mvn test tomcat:run & 
	wget --retry-connrefused -O target/main.html http://localhost:8080/petclinic
	kill -9 $MVN_TOMCAT_PID
	cd ..
	echo batch end
   	 echo "------------------------------------------------------"
    	echo "======================================================"
    	echo .

	

	## jquery
	echo "======================================================"
   	 echo "------------------------------------------------------"
	echo jquery start
	mkdir jquery
	cd jquery
	$1/gvnix.sh script --file $2/code/addon-web-mvc/addon/src/test/jquery.roo --lineNumbers true
	mkdir target
	mvn test tomcat:run &
	wget --retry-connrefused -O target/owners.html http://localhost:8080/petclinic/owners
	kill -9 $MVN_TOMCAT_PID
	cd ..
	echo jquery end
   	 echo "------------------------------------------------------"
    	echo "======================================================"
    	echo .

##
## gvNIX bootstrap add-on
##
	echo "======================================================"
   	 echo "------------------------------------------------------"
	echo bootstrap start
	mkdir bootstrap
	cd bootstrap
	$1/gvnix.sh script --file $2/code/addon-web-mvc-bootstrap/src/test/resources/bootstrap.roo--lineNumbers true
	mkdir target
	mvn test tomcat:run &
	wget --retry-connrefused -O target/main.html http://localhost:8080/petclinic 
	kill -9 $MVN_TOMCAT_PID	
	cd ..
	echo bootstrap end
   	 echo "------------------------------------------------------"
    	echo "======================================================"
    	echo .



##
## gvNIX datatables add-on
##
	
	## datatables
    echo "======================================================"
    echo "------------------------------------------------------"
	echo datatables start
	mkdir datatables
	cd datatables
	$1/gvnix.sh script --file $2/code/addon-web-mvc-datatables/src/main/resources/datatables.roo --lineNumbers true
	mkdir target
	mvn clean compile
    	cd ..
	echo datatables end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## datatables-multimodule
    echo "======================================================"
    echo "------------------------------------------------------"
	echo datatables-multimodule start
	mkdir datatables-multimodule
	cd datatables-multimodule
	$1/gvnix.sh script --file $2/code/addon-web-mvc-datatables/src/test/resources/datatables-multimodule.roo --lineNumbers true
	mvn test tomcat:run &
	mkdir target
      MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
	cd ..
	echo datatables-multimodule end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## datatables-test
    echo "======================================================"
    echo "------------------------------------------------------"
	echo datatables-test start
	mkdir datatables-test
	cd datatables-test
	$1/gvnix.sh script --file $2/code/addon-web-mvc-datatables/src/test/resources/datatables-test.roo --lineNumbers true
	mkdir target
	mvn clean compile
   	cd ..
	echo datatables-test end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## datatables-pkc
    echo "======================================================"
    echo "------------------------------------------------------"
	echo datatables-pkc start
	mkdir datatables-pkc
	cd datatables-pkc
	$1/gvnix.sh script --file $2/code/addon-web-mvc-datatables/src/test/resources/datatables-pkc.roo --lineNumbers true
	mvn test tomcat:run &
	mkdir target
    MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
	cd ..
	echo datatables-pkc end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

##
## gvNIX loupe add-on
##
	
	## loupefield
    echo "======================================================"
    echo "------------------------------------------------------"
	echo loupefield start
	mkdir loupefield
	cd loupefield
	$1/gvnix.sh script --file $2/code/addon-web-mvc-loupefield/src/main/resources/loupe.roo --lineNumbers true
	mkdir target
    	mvn clean compile
	cd ..
	echo loupefield end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

##
## gvNIX dialog add-on
##
	
	## dialog
    echo "======================================================"
    echo "------------------------------------------------------"
	echo dialog start
	mkdir dialog
	cd dialog
	$1/gvnix.sh script --file $2/code/addon-web-dialog/src/main/resources/dialog.roo --lineNumbers true
	mkdir target
	mvn clean compile    
	cd ..
	echo dialog end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .



##
## gvNIX geo add-on
##
    echo "======================================================"
    echo "------------------------------------------------------"
	echo geo quickstart start
	mkdir geo
	cd geo
	$1/gvnix.sh script --file $2/code/addon-web-mvc-geo/addon/src/test/resources/geo.roo --lineNumbers true
	mvn clean compile
	cd ..
	echo geo quickstart end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .
	



##
## gvNIX i18n add-on
##
	
	## es-i18n
    echo "======================================================"
    echo "------------------------------------------------------"
	echo es-i18n start
	mkdir es-i18n
	cd es-i18n
	$1/gvnix.sh script --file $2/code/addon-web-i18n/src/main/resources/es-i18n.roo --lineNumbers true
	mkdir target
	mvn clean compile
	cd ..
	echo es-i18n end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

##
## gvNIX menu add-on
##
	
	## menu
    echo "======================================================"
    echo "------------------------------------------------------"
	echo menu start
	mkdir menu
	cd menu
	$1/gvnix.sh script --file $2/code/addon-web-menu/src/main/resources/menu.roo --lineNumbers true
	mvn clean compile
	cd ..
	echo menu end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .
	
	## base
    echo "======================================================"
    echo "------------------------------------------------------"
	echo base start
	mkdir base
	cd base
	$1/gvnix.sh script --file $2/code/addon-web-menu/src/test/resources/base.roo --lineNumbers true
	mvn clean compile
	cd ..
	echo base end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

##
## gvNIX occ add-on
##
	
	## occ
    echo "======================================================"
    echo "------------------------------------------------------"
	echo occ start
	mkdir occ
	cd occ
	$1/gvnix.sh script --file $2/code/addon-occ/src/main/resources/occ.roo --lineNumbers true
	mvn clean compile
	cd ..
	echo occ end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

##
## gvNIX jpa add-on
##
	
	## jpa-audit-test
    echo "======================================================"
    echo "------------------------------------------------------"
	echo jpa-audit-test start
	mkdir jpa-audit
	cd jpa-audit
	$1/gvnix.sh script --file $2/code/addon-jpa/src/test/resources/jpa-audit-test.roo --lineNumbers true
	mvn clean compile
	cd ..
	echo jpa-audit-test end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## jpa-audit-envers
    echo "======================================================"
    echo "------------------------------------------------------"
	echo jpa-audit-envers start
	mkdir jpa-audit-envers
	cd jpa-audit-envers
	$1/gvnix.sh script --file $2/code/addon-jpa/src/test/resources/jpa-audit-envers.roo --lineNumbers true
	mvn clean compile
	cd ..
	echo jpa-audit-envers end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## jpa-audit-multimodule
echo "======================================================"
    echo "------------------------------------------------------"
	echo jpa-audit-multimodule start
	mkdir jpa-audit-muldimodule
	cd jpa-audit-multimodule
	$1/gvnix.sh script --file $2/code/addon-jpa/addon/src/test/resources/jpa-audit-multimodule.roo --lineNumbers true
	# Create new pet
	mkdir target
	mvn test tomcat:run &
	wget --retry-connrefused -O target/petcreate.html http://localhost:8080/mvn/pets --post-data 'name=a&weight=1&type=Dog' & 
	kill -9 $MVN_TOMCAT_PID
	cd ..
	echo jpa-audit-multimodule end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .


	## jpa-audit-pkc 
echo "======================================================"
    echo "------------------------------------------------------"
	echo jpa-audit-pkc start
	mkdir jpa-audit-pkc
	cd jpa-audit-pkc
	$1/gvnix.sh script --file $2/code/addon-jpa/src/test/resources/jpa-audit-pkc.roo --lineNumbers true
	# Create new pet
	mvn test tomcat:run &
	wget --retry-connrefused -O target/petcreate.html http://localhost:8080/displayRelationTable --post-data 'name=a&weight=1&type=Dog' &
	kill -9 $MVN_TOMCAT_PID
	cd ..
	echo jpa-audit-pkc end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .



##
## gvNIX pattern add-on
##
	
	## pattern
    echo "======================================================"
    echo "------------------------------------------------------"
	echo pattern start
	mkdir pattern
	cd pattern
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/main/resources/pattern.roo --lineNumbers true
##  Reopen shell to generate pending pattern resources
	$1/gvnix.sh hint
	mvn test tomcat:run & 
	sleep 30
    MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
	cd ..
	echo pattern end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .
	
	## test-script-pkc
    echo "======================================================"
    echo "------------------------------------------------------"
	echo test-script-pkc start
	mkdir test-script-pkc
	cd test-script-pkc
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc.roo --lineNumbers true
##  Reopen shell to generate pending pattern resources
	$1/gvnix.sh hint
	mvn test tomcat:run &
	sleep 30
    MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
	cd ..
	echo test-script-pkc end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .
	
	## test-script-pkc2
    echo "======================================================"
    echo "------------------------------------------------------"
	echo test-script-pkc2 start
	mkdir test-script-pkc2
	cd test-script-pkc2
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc2.roo --lineNumbers true
##  Reopen shell to generate pending pattern resources
	$1/gvnix.sh hint
	mvn test tomcat:run &
	sleep 30
    MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
	cd ..
	echo test-script-pkc2 end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## test-script-pkc3
    echo "======================================================"
    echo "------------------------------------------------------"
	echo test-script-pkc3 start
	mkdir test-script-pkc3
	cd test-script-pkc3
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc3.roo --lineNumbers true
##  Reopen shell to generate pending pattern resources
	$1/gvnix.sh hint
	mvn test tomcat:run &
	sleep 30
    MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
	cd ..
	echo test-script-pkc3 end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## test-script-manytomany
    echo "======================================================"
    echo "------------------------------------------------------"
	echo test-script-manytomany start
	mkdir test-script-manytomany
	cd test-script-manytomany
	$1/gvnix.sh script --file $2/code/addon-web-pattern/src/test/resources/test-script-manytomany.roo --lineNumbers true
##  Reopen shell to generate pending pattern resources
	$1/gvnix.sh hint
	mvn test tomcat:run &
	sleep 30
    MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
	cd ..
	echo test-script-manytomany end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

##
## gvNIX report add-on
##
	
	## report
    echo "======================================================"
    echo "------------------------------------------------------"
	echo report start
	mkdir report
	cd report
	$1/gvnix.sh script --file $2/code/addon-web-report/src/main/resources/report.roo --lineNumbers true
	# Start tomcat, wait to start and execute selenium tests to insert data
	mvn test tomcat:run &
	sleep 30
	mkdir target
    MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
	cd ..
	echo report end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .
	
	## gvnix-test-report
    echo "======================================================"
    echo "------------------------------------------------------"
	echo gvnix-test-report start
	mkdir gvnix-test-report
	cd gvnix-test-report
	$1/gvnix.sh script --file $2/code/addon-web-report/src/test/resources/gvnix-test-report.roo --lineNumbers true
	mkdir target
	mvn clean compile
	cd ..
	echo gvnix-test-report end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

##
## gvNIX service add-on
##
	
	## bing
    echo "======================================================"
    echo "------------------------------------------------------"
	echo bing start
	mkdir bing
	cd bing
	$1/gvnix.sh script --file $2/code/addon-service/src/main/resources/bing.roo --lineNumbers true
	mvn test tomcat:run -Dmaven.tomcat.fork=true 
	cd ..
	echo bing end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## service
    echo "======================================================"
    echo "------------------------------------------------------"
	echo service start
	mkdir service
	cd service
	$1/gvnix.sh script --file $2/code/addon-service/src/main/resources/service.roo --lineNumbers true
	mvn clean compile 
	cd ..
	echo service end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .


	## gvnix-test-no-jpa-no-web
    echo "======================================================"
    echo "------------------------------------------------------"
	echo gvnix-test-no-jpa-no-web start
	mkdir gvnix-test-no-jpa-no-web
	cd gvnix-test-no-jpa-no-web
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-no-jpa-no-web.roo --lineNumbers true
	mvn test package
	cd ..
	echo gvnix-test-no-jpa-no-web end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## gvnix-test-no-jpa
    echo "======================================================"
    echo "------------------------------------------------------"
	echo gvnix-test-no-jpa start
	mkdir gvnix-test-no-jpa
	cd gvnix-test-no-jpa
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-no-jpa.roo --lineNumbers true
	mvn clean compile
	cd ..
	echo gvnix-test-no-jpa end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## gvnix-test-no-web
    echo "======================================================"
    echo "------------------------------------------------------"
	echo gvnix-test-no-web start
	mkdir gvnix-test-no-web
	cd gvnix-test-no-web
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-no-web.roo --lineNumbers true
	mvn test package
	cd ..
	echo gvnix-test-no-web end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .
	
	## gvnix-test
    echo "======================================================"
    echo "------------------------------------------------------"
	echo gvnix-test start
	mkdir gvnix-test
	cd gvnix-test
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test.roo --lineNumbers true
	mvn clean compile
	cd ..
	echo gvnix-test end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .


	## gvnix-test-entity
    echo "======================================================"
    echo "------------------------------------------------------"
	echo gvnix-test-entity start
	mkdir gvnix-test-entity
	cd gvnix-test-entity
	$1/gvnix.sh script --file $2/code/addon-service/src/test/resources/gvnix-test-entity.roo --lineNumbers true
	mkdir target
	mvn clean compile
	cd ..
	echo gvnix-test-entity end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

##
## gvNIX typicalsecurity add-on
##
	
	## typicalsecurity
    echo "======================================================"
    echo "------------------------------------------------------"
	echo typicalsecurity start
	mkdir typicalsecurity
	cd typicalsecurity
	$1/gvnix.sh script --file $2/code/addon-web-mvc-typicalsecurity/src/main/resources/typicalsecurity.roo --lineNumbers true
	mvn test tomcat:run &
	mkdir target
    MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
	cd ..
	echo typicalsecurity end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

##
## gvNIX monitoring add-on
##
	
	## monitoring
    echo "======================================================"
    echo "------------------------------------------------------"
	echo monitoring start
	mkdir monitoring
	cd monitoring
	$1/gvnix.sh script --file $2/code/addon-monitoring/src/main/resources/monitoring.roo --lineNumbers true
	mvn test tomcat:run &
	mkdir target
    MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
	cd ..
	echo monitoring end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .


##
## gvNIX add-ons
##
	
	## gvnix-sample
    echo "======================================================"
    echo "------------------------------------------------------"
	echo gvnix-sample start
	mkdir gvnix-sample
	cd gvnix-sample
	$1/gvnix.sh script --file $2/code/src/main/resources/gvnix-sample.roo --lineNumbers true
	mvn clean compile		
	cd ..
	echo gvnix-sample end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .

	## script
    echo "======================================================"
    echo "------------------------------------------------------"
	echo script start
	mkdir script
	cd script
	$1/gvnix.sh script --file $2/code/src/main/resources/script.roo --lineNumbers true
	mvn clean compile
	cd ..
	echo script end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .
	
	
	## tiendavirtual
   echo "======================================================"
    echo "------------------------------------------------------"
	echo tiendavirtual start
	mkdir tiendavirtual
	cd tiendavirtual
	$1/gvnix.sh script --file $2/code/src/main/resources/tiendavirtual.roo --lineNumbers true
##  Reopen shell to generate pending pattern resources
	$1/gvnix.sh hint
	mvn clean compile
	cd ..
	echo tiendavirtual end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .


	## safe 
    echo "======================================================"
    echo "------------------------------------------------------"
	echo safe start
	mkdir safe
	cd safe
	$1/gvnix.sh script --file $2/code/src/test/resources/security-safe.roo --lineNumbers true
	mvn test tomcat:run &
	mkdir target
    MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`
    kill -9 $MVN_TOMCAT_PID
	cd ..
	echo safe end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .



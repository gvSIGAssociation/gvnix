
## Run all integration tests from console and form jenkins integration system daily.
## Run example:
##  ${WORKSPACE}/src/test/resources/integration.sh ${BINARY_PATH} ${SOURCE_PATH}
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
	rm -r clinic
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
	rm -r embedding
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
	rm -r multimodule
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
	rm -r pizzashop
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
	rm -r vote
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
	rm  -r wedding
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
	rm -r binding 
	mkdir binding
	cd binding
	$1/gvnix.sh script --file $2/addon-web-mvc-binding/src/main/resources/binding.roo --lineNumbers true
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
	rm -r configuration
	mkdir configuration
	cd configuration
	$1/gvnix.sh script --file $2/addon-dynamic-configuration/src/main/resources/configuration.roo --lineNumbers true
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
	rm -r batch
	mkdir batch
	cd batch
	$1/gvnix.sh script --file $2/addon-web-mvc/addon/src/test/batch.roo --lineNumbers true
	mkdir target
	mvn test tomcat:run & 
	wget --retry-connrefused -O target/main.html http://localhost:8080/petclinic
   	MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`	
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
	rm -r jquery
	mkdir jquery
	cd jquery
	$1/gvnix.sh script --file $2/addon-web-mvc/addon/src/test/jquery.roo --lineNumbers true
	mkdir target
	mvn test tomcat:run &
	wget --retry-connrefused -O target/owners.html http://localhost:8080/petclinic/owners
   	 MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`	
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
	rm -r bootstrap
	mkdir bootstrap
	cd bootstrap
	$1/gvnix.sh script --file $2/addon-web-mvc-bootstrap/src/test/resources/bootstrap.roo--lineNumbers true
	mkdir target
	mvn test tomcat:run &
	wget --retry-connrefused -O target/main.html http://localhost:8080/petclinic 
   	MVN_TOMCAT_PID=`ps -eo "%p %c %a" | grep Launcher | grep tomcat:run | cut -b "1-6" | sed "s/ //g"`	
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
	rm -r datatables
	mkdir datatables
	cd datatables
	$1/gvnix.sh script --file $2/addon-web-mvc-datatables/addon/src/main/resources/datatables.roo --lineNumbers true
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
	rm -r datatables-multimodule
	mkdir datatables-multimodule
	cd datatables-multimodule
	$1/gvnix.sh script --file $2/addon-web-mvc-datatables/src/test/resources/datatables-multimodule.roo --lineNumbers true
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
	rm -r datatables-test
	mkdir datatables-test
	cd datatables-test
	$1/gvnix.sh script --file $2/addon-web-mvc-datatables/src/test/resources/datatables-test.roo --lineNumbers true
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
	rm -r datatables-pkc
	mkdir datatables-pkc
	cd datatables-pkc
	$1/gvnix.sh script --file $2/addon-web-mvc-datatables/src/test/resources/datatables-pkc.roo --lineNumbers true
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
	rm -r loupefield
	mkdir loupefield
	cd loupefield
	$1/gvnix.sh script --file $2/addon-web-mvc-loupefield/src/main/resources/loupe.roo --lineNumbers true
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
	rm -r dialog
	mkdir dialog
	cd dialog
	$1/gvnix.sh script --file $2/addon-web-dialog/src/main/resources/dialog.roo --lineNumbers true
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
	rm -r geo
	mkdir geo
	cd geo
	$1/gvnix.sh script --file $2/addon-web-mvc-geo/addon/src/test/resources/geo.roo --lineNumbers true
	mvn clean compile
	cd ..
	echo geo quickstart end
    echo "------------------------------------------------------"
    echo "======================================================"
    echo .
	




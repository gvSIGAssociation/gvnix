## Run all integration tests from console and form jenkins integration system daily.
## Run example:
##  ${WORKSPACE}/src/test/resources/integration.sh ${TEST_ROOT} ${ROO_COMMAND} ${GVNIX_HOME} ${TOMCAT_PORT}
## Where input parameters are:
##  * ${TEST_ROOT}: Output execution tests folder
##  * ${ROO_COMMAND}: Roo shell executable path
##  * ${GVNIX_HOME}: Path to gvNIX root source folder for access the test scripts
##  * ${$TOMCAT_PORT}: Tomcat port to use
## 

function usage {
cat << EOF
Run all integration tests from console and form jenkins integration system daily.

Usage:
    $0 TEST_ROOT ROO_COMMAND GVNIX_HOME TOMCAT_PORT

Where input parameters are:
  * TEST_ROOT: Output execution tests folder
  * ROO_COMMAND: Roo shell executable path. Should be "gvnix.sh"
  * GVNIX_HOME: Path to gvNIX root source folder for access the test scripts
  * TOMCAT_PORT: Tomcat port to use
EOF
}


## Exit script on any error (when a commad exits with non 0 status)
set -e 
## Echo all commands
#set -x 

## Start graphic environment emulator to start firefox for selenium integration tests
## startx -- `which Xvfb` :1 -screen 0 1024x768x24 2>&1 >/dev/null &
export DISPLAY=:1

export TEST_ROOT=$1
export ROO_COMMAND=$2
export GVNIX_HOME=$3
export TOMCAT_PORT=$4


## Get current file abs_path
function abs_path() {
    pushd . > /dev/null;
    if [ -d "$1" ]; then
      cd "$1";
      dirs -l +0;
    else
      cd "`dirname \"$1\"`";
      cur_dir=`dirs -l +0`;
      if [ "$cur_dir" == "/" ]; then
        echo "$cur_dir`basename \"$1\"`";
      else
        echo "$cur_dir/`basename \"$1\"`";
     fi;
   fi;
   popd > /dev/null;
}
THIS_FILE=$(abs_path $0)

THIS_FOLDER=`dirname "$THIS_FILE"`
source "$THIS_FOLDER/build-util.functions"


## Check parameters
if [ -z "$TEST_ROOT" ]; then
  usage
  echo ""
  echo "*** Missing Test root path"
  echo ""
  exit 1
fi

if [ ! -x "$ROO_COMMAND" ]; then
  usage
  show_message_problem "ERROR" "Missing gvnix executable"
  exit 1
fi

if [ ! -d "$GVNIX_HOME" ]; then
  usage
  show_message_problem "ERROR" "Missing gvNIX home"
  exit 1
fi

if [ ! -d "$GVNIX_HOME/addon-jpa" ]; then
  usage
  show_message_problem "ERROR" "Invalid gvNIX home" "Missing folder: $GVNIX_HOME/addon-jpa"
  exit 1
fi

if [ ! -f "$GVNIX_HOME/pom.xml" ]; then
  usage
  show_message_problem "ERROR" "Invalid gvNIX home" "Missing folder: $GVNIX_HOME/pom.xml"
  exit 1
fi

show_message_info "Cleaning test folder"
## Remove old potential temporal folder of previous test, create new one and go into
if [ -d $TEST_ROOT ]; then
  rm -rf $TEST_ROOT || true
fi
mkdir -p $TEST_ROOT

##
## Roo
##
show_message_info "Starting Roo scripts"
	
  ## XXX use gvNIX version of roo script as this files need to be fixed (project --> project setup)
	
	## clinic
  test_simple clinic $GVNIX_HOME/deployment-support/src/test/resources/clinic.roo $TOMCAT_PORT

	## embedding
  test_simple embedding $GVNIX_HOME/deployment-support/src/test/resources/embedding.roo $TOMCAT_PORT

	## multimodule
  ## TODO Check
  #test_simple multimodule $GVNIX_HOME/deployment-support/src/test/resources/multimodule.roo $TOMCAT_PORT

	## pizzashop
  test_simple pizzashop $GVNIX_HOME/deployment-support/src/test/resources/pizzashop.roo $TOMCAT_PORT

	## vote
  test_simple vote $GVNIX_HOME/deployment-support/src/test/resources/vote.roo $TOMCAT_PORT

	## wedding
  test_simple wedding $GVNIX_HOME/deployment-support/src/test/resources/wedding.roo $TOMCAT_PORT

##
## gvNIX binding add-on
##
show_message_info "Starting addon-web-mvc-binding scripts"
	
	## binding
  test_compile binding $GVNIX_HOME/addon-web-mvc-binding/addon/src/main/resources/binding.roo

##
## gvNIX dynamic configuration add-on
##	
show_message_info "Starting addon-dynamic-configuration scripts"

	## configuration
  test_compile configuration $GVNIX_HOME/addon-dynamic-configuration/src/main/resources/configuration.roo

##
## gvNIX Web MVC Addon
## 
show_message_info "Starting addon-web-mvc scripts"

	## batch
  test_page_available batch $GVNIX_HOME/addon-web-mvc/addon/src/test/batch.roo petclinic $TOMCAT_PORT

	## jquery
  test_page_available jquery $GVNIX_HOME/addon-web-mvc/addon/src/test/jquery.roo petclinic/owners $TOMCAT_PORT

##
## gvNIX bootstrap add-on
##
show_message_info "Starting addon-web-mvc-bootstrap scripts"

  test_page_available bootstrap $GVNIX_HOME/addon-web-mvc-bootstrap/src/test/resources/bootstrap.roo petclinic $TOMCAT_PORT

##
## gvNIX datatables add-on
##
show_message_info "Starting addon-web-mvc-datatables scripts"
	
	## datatables
  #test_page_available datatables $GVNIX_HOME/addon-web-mvc-datatables/addon/src/main/resources/datatables.roo petclinic/owners $TOMCAT_PORT
  test_simple datatables $GVNIX_HOME/addon-web-mvc-datatables/addon/src/main/resources/datatables.roo $TOMCAT_PORT

	## datatables-multimodule
  ## TODO requires fix of roo multimodule script!!!
  #test_simple datatables-multimodule $GVNIX_HOME/addon-web-mvc-datatables/addon/src/test/resources/datatables-multimodule.roo $TOMCAT_PORT

	## datatables-test
  test_simple "datatables-test" $GVNIX_HOME/addon-web-mvc-datatables/addon/src/test/resources/datatables-test.roo $TOMCAT_PORT

	## datatables-pkc
  test_simple "datatables-pkc" $GVNIX_HOME/addon-web-mvc-datatables/addon/src/test/resources/datatables-pkc.roo $TOMCAT_PORT

##
## gvNIX loupe add-on
##
show_message_info "Starting addon-web-mvc-loupefield scripts"
	
	## loupefield
  test_simple loupefield $GVNIX_HOME/addon-web-mvc-loupefield/addon/src/main/resources/loupe.roo $TOMCAT_PORT

##
## gvNIX dialog add-on
##
#show_message_info "Startin addon-web-mvc-dialog scripts"
	
	## dialog
  ## TODO To Check this
  # test_simple dialog $GVNIX_HOME/addon-web-mvc-dialog/addon/src/main/resources/dialog.roo $TOMCAT_PORT

##
## gvNIX geo add-on
##
show_message_info "Starting addon-web-mvc-geo scripts"

  ## Requires Geo DB it can't run test nor app
  test_compile geo $GVNIX_HOME/addon-web-mvc-geo/addon/src/test/resources/geo.roo 

##
## gvNIX i18n add-on
##
show_message_info "Starting addon-web-mvc-i18n scripts"
	
	## es-i18n
  test_simple "es-18n" $GVNIX_HOME/addon-web-mvc-i18n/src/main/resources/es-i18n.roo $TOMCAT_PORT

##
## gvNIX menu add-on
##
show_message_info "Starting addon-web-mvc-menu scripts"
	
	## menu
  test_simple menu $GVNIX_HOME/addon-web-mvc-menu/src/main/resources/menu.roo $TOMCAT_PORT
	 
	## base
  test_simple menu_base  $GVNIX_HOME/addon-web-mvc-menu/src/test/resources/base.roo $TOMCAT_PORT

##
## gvNIX occ add-on
##
show_message_info "Starting addon-occ scripts"
	
	## occ
  test_simple menu_base $GVNIX_HOME/addon-occ/addon/src/main/resources/occ.roo $TOMCAT_PORT

##
## gvNIX jpa add-on
##
show_message_info "Starting addon-jpa scripts"
	
	## jpa-audit-test
  test_simple "jpa-audit" $GVNIX_HOME/addon-jpa/addon/src/test/resources/jpa-audit-test.roo $TOMCAT_PORT

	## jpa-audit-envers
  test_simple "jpa-audit-envers" $GVNIX_HOME/addon-jpa/addon/src/test/resources/jpa-audit-envers.roo $TOMCAT_PORT

	## jpa-audit-multimodule
  ## TODO check
  #test_simple "jpa-audit-multimodule" $GVNIX_HOME/addon-jpa/addon/src/test/resources/jpa-audit-multimodule.roo $TOMCAT_PORT

	## jpa-audit-pkc 
  test_simple "jpa-audit-pkc" $GVNIX_HOME/addon-jpa/addon/src/test/resources/jpa-audit-pkc.roo $TOMCAT_PORT

##
## gvNIX report add-on
##
show_message_info "Starting addon-web-mvc-report scripts"
	
	## report
  test_simple report $GVNIX_HOME/addon-web-mvc-report/addon/src/main/resources/report.roo $TOMCAT_PORT
	
	## gvnix-test-report
  test_simple "gvnix-test-report" $GVNIX_HOME/addon-web-mvc-report/addon/src/test/resources/gvnix-test-report.roo $TOMCAT_PORT

##
## gvNIX service add-on
##
show_message_info "Starting addon-service scripts"
	
	## bing
  test_simple "service_bing" $GVNIX_HOME/addon-service/addon/src/main/resources/bing.roo $TOMCAT_PORT

	## service
  test_simple "service_service" $GVNIX_HOME/addon-service/addon/src/main/resources/service.roo $TOMCAT_PORT

	## gvnix-test-no-jpa-no-web
  test_compile "service_gvnix-test-no-jpa-no-web" $GVNIX_HOME/addon-service/addon/src/test/resources/gvnix-test-no-jpa-no-web.roo

	## gvnix-test-no-jpa
  ## TODO To check
  #test_compile "service_gvnix-test-no-jpa" $GVNIX_HOME/addon-service/addon/src/test/resources/gvnix-test-no-jpa.roo

	## gvnix-test-no-web
  test_compile "service_gvnix-test-no-web" $GVNIX_HOME/addon-service/addon/src/test/resources/gvnix-test-no-web.roo
	
	## gvnix-test
  test_compile "service_gvnix-test" $GVNIX_HOME/addon-service/addon/src/test/resources/gvnix-test.roo

	## gvnix-test-entity
  test_compile "service_gvnix-test-entity" $GVNIX_HOME/addon-service/addon/src/test/resources/gvnix-test-entity.roo

##
## gvNIX typicalsecurity add-on
##
show_message_info "Starting addon-web-mvc-typicalsecurity scripts"
	
	## typicalsecurity
  test_simple "typicalsecurity" $GVNIX_HOME/addon-web-mvc-typicalsecurity/src/main/resources/typicalsecurity.roo $TOMCAT_PORT

##
## gvNIX monitoring add-on
##
show_message_info "Starting addon-monitoring scripts"
	
	## monitoring
  test_simple "monitoring" $GVNIX_HOME/addon-monitoring/src/main/resources/monitoring.roo $TOMCAT_PORT

##
## gvNIX add-ons
##
show_message_info "Starting general scripts"
	
	## gvnix-sample
  test_simple "gvnix-sample" $GVNIX_HOME/src/main/resources/gvnix-sample.roo $TOMCAT_PORT


show_message_info "All integration script executed: DONE!!!"

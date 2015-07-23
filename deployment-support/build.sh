#!/bin/bash
#
# Script to test gvNIX release

## Exit script on any error
set -e 

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


GVNIX_DEPLOYMENT_SUPPORT_DIR=`dirname $THIS_FILE`
GVNIX_HOME=`dirname $GVNIX_DEPLOYMENT_SUPPORT_DIR`


## Load utils functions
source $GVNIX_DEPLOYMENT_SUPPORT_DIR/build-util.functions

# Gets gvNIX version from first version tag on root pom.xml
GVNIX_VERSION=`grep "[<]version[>]\K([^<]*)" $GVNIX_HOME/pom.xml -oPm1`
# Gets roo version from first version tag on root pom.xml
ROO_VERSION=`grep "[<]roo.version[>]\K([^<]*)" $GVNIX_HOME/pom.xml -oPm1`


function usage () {
cat << EOF

Usage: $0 spring-roo.zip [test|deploy|release] {--skipCleanRepo} {--skipSign} {--lowVerbose} {--tomcatPort} {-d | --debug}

Parameter:

    spring-roo.zip

              Path to Spring Roo instalation zip file to use for test
              gvNIX distribution.

Actions:

    test        
              Generates gvNIX package and runs CI test

    deploy    
              Generates gvNIX package, runs CI test and deploy all ".jar" 
              artifacts and Addon suite as SNAPSHOT

    release
        
              Generates gvNIX package, runs CI test and deploy all ".jar" 
              artifacts and Addon suite as a RELEASE

Options:

    --skipCleanRepo

              Skip cleaning of Maven local repository (~/.m2/repository)
              of any gvNIX and Spring Roo artifacts. Only available for
              "test" action.

    --skipSign

              Skip signing of gvNIX artifacts. Only available for "test" 
              action.

    --lowVerbose

              Reduces log info showed by CI tests

    --tomcatPort
            
            Tomcat port to use when execute CI tests. Default 8080

    -d,--debug

              Show executed commands

Description:

    Runs CI test and deploys artifacst ( ".jar" artifacts 
    on Maven Central and addon suite).

EOF

}


if [ -z "$1" ]; then
  usage
  exit 1
fi

# Get the Roo zip and shift paramters
export ROO_ZIP=$1
shift

if [ -z "$1" ]; then
  usage
  exit 1
fi

# Checks roo file exist
if [ ! -f $ROO_ZIP ]; then
      echo ""
      echo "*** Roo zip file not exist: $ROO_ZIP"
      echo ""
      usage
      exit 1
fi

## Prepare configuration variables
SKIP_LOCAL_REPO_CLEAN=no
SKIP_ARTIFACTS_SIGN=no
LOW_VERBOSE=no
#GENERATE_DOC=yes
RUN_CI=no
DEPLOY_JARS=no
DEPLOY_RELEASE=no
DEBUG=no
TOMCAT_PORT=8080

## identify action and options
ACTION="$1"
shift

while [[ $# > 0 ]]
do
  OPTION="$1"
    case $OPTION in
        --skipCleanRepo)
            SKIP_LOCAL_REPO_CLEAN=yes
            ;;
        --skipSign)
            SKIP_ARTIFACTS_SIGN=yes
            ;;
        --lowVerbose)
            LOW_VERBOSE=yes
            ;;
        --tomcatPort)
            ## getting tomcat port
            shift
            TOMCAT_PORT=$1            
            ;;
        -d|--debug)
            DEBUG=yes
            ;;
        *)
            echo ""
            echo "*** Invalid option: $OPTION"
            echo ""
            usage
            exit 1
            ;;
    esac
    ## next option
    shift 
done



case $ACTION in
test)
    RUN_CI=yes
    DEPLOY_JARS=no
  ;;
deploy)
    SKIP_LOCAL_REPO_CLEAN=no
    #GENERATE_DOC=yes
    RUN_CI=yes
    DEPLOY_JARS=yes
  ;;
release)
    SKIP_LOCAL_REPO_CLEAN=no
    #GENERATE_DOC=yes
    RUN_CI=yes
    DEPLOY_JARS=yes
    DEPLOY_RELEASE=yes
  ;;
*)
    echo ""
    echo "*** Invalid action: $ACTION"
    echo ""
    usage
    exit 1
  ;;
esac


## Echo all commands
if [ "$DEBUG" = "yes" ]; then
    set -x 
fi

### ROO PACKAGE
export WORK_DIR=/tmp/gvNIX2.0_test
show_message_info "Clean and initialize temporal dir ($WORK_DIR)"
rm -rf $WORK_DIR* || true
mkdir $WORK_DIR

export WORK_ROO=$WORK_DIR/roo
mkdir -p $WORK_ROO

show_message_info "Create temporal Roo instalation for test ($WORK_ROO)"
cd $WORK_ROO
unzip $ROO_ZIP

export ROO_COMMAND=`find $WORK_ROO -name roo.sh`

if [ ! -x "$ROO_COMMAND" ]; then
    show_message_problem "ERROR" "Error on roo Version" "Error on zip file $ROO_ZIP" "Not found or non-executable file 'roo.sh'"
    exit 1;
fi

## Check required roo version
if [ ! -d "$WORK_ROO/spring-roo-$ROO_VERSION" ]; then
    cur_version=`ls $WORK_ROO`
    show_message_problem "ERROR" "Error on roo Version" "Required roo version: spring-roo-$ROO_VERSION" "Provided roo version: $cur_version"
    exit 1
fi


if [ "$SKIP_LOCAL_REPO_CLEAN" = "yes" ]; then
    # Skip remove old roo and gvNIX installed dependencies
    show_message_info "Clean Maven local repository: Skip"
else
    # Remove old roo and gvNIX installed dependencies
    show_message_info "Clean Maven local repository (~/.m2/repository) of Spring Roo and gvNIX artifacts"
    rm -rf ~/.m2/repository/org/springframework/roo  || true
    rm -rf ~/.m2/repository/org/gvnix  || true
fi

# Install and package modules
show_message_info "Compiling and installing gvNIX"
cd $GVNIX_HOME
if [ "$SKIP_ARTIFACTS_SIGN" = "yes" ]; then
    mvn clean install -Dgpg.skip=true
else
    # Install and sign all artifacts
    mvn clean install
fi

## Locate index.xml of addon repository
export GVNIX_OSGI_BUNDLE_REPO_INDEX=$GVNIX_HOME/target/osgi-repository-bin/index.xml

if [ ! -f "$GVNIX_OSGI_BUNDLE_REPO_INDEX" ]; then
  show_message_problem "ERROR" "OSGi repository index not found!!!" "required location : $GVNIX_OSGI_BUNDLE_REPO_INDEX"
  exit 1
fi

cd $WORK_DIR
show_message_info "Installing gvNIX add-on suite into temporal roo installation"

## Generate script to install add-on suite
cat > $WORK_DIR/install_gvnix.roo << EOF
addon repository add --url file://$GVNIX_OSGI_BUNDLE_REPO_INDEX
addon suite install name --symbolicName org.gvnix.roo.addon.suite
EOF
## Install gvNIX
$ROO_COMMAND script --file install_gvnix.roo
if roo_script_fail; then
  echo ""
  echo ""
  echo "======= Fail to install gvNIX addon suite!!!! =============="
  echo "see $PWD/log.roo"
  echo ""
  echo ""
  exit 1
fi
## Clear log file
rm log.roo

show_message_info "Check installed add-ons"
## Generate script to check installed add-ons
echo "-- Geting installed addons"
cat > $WORK_DIR/list_addons.roo << EOF
addon list
EOF
$ROO_COMMAND script --file list_addons.roo | tee addons.txt

## check installed add-ons
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Dynamic Configuration"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Loupe Fields"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Web MVC (JSP) layer services"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - JPA layer services"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Web Datatables Addon"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Services Management"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Co-official languages of Spain"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Web MVC Dialogs"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Monitoring Support"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Bootstrap3 support"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - GEO Support"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Optimistic Concurrency Control"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Web Report"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Typical Security"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Web MVC Menu"
assert_contains_in_file addons.txt "Missing add-on:" "gvNIX - Addon - Web MVC Bindings"

# Run gvNIX integration test
if [ "$RUN_CI" = "yes" ]; then
    show_message_info "Run Integration test"
    if [ "$LOW_VERBOSE" = "yes" ]; then
      # Check if /tmp/gvnix_int exists
      if [ -d "/tmp/gvnix_int_test" ]; then
        rm -r /tmp/gvnix_int_test;
      fi
      # Creating gvnix integration test folder
      mkdir /tmp/gvnix_int_test;
      CI_LOG_FILE=/tmp/gvnix_int_test/ci.log.txt
      show_message_info "Redirect CI log to $CI_LOG_FILE"
      bash $GVNIX_DEPLOYMENT_SUPPORT_DIR/gvNIX-CI.sh /tmp/gvnix_int_test $ROO_COMMAND $GVNIX_HOME $TOMCAT_PORT > $CI_LOG_FILE
      CI_RESULT=$?  
      if [ $CI_RESULT -ne 0 ]; then
        show_message_problem "ERROR: CI test fail" "See $CI_LOG_FILE for full log" "Last 20 lines: "
        tail -n 20 $CI_LOG_FILE
        exit 1
      fi
    else
      bash $GVNIX_DEPLOYMENT_SUPPORT_DIR/gvNIX-CI.sh /tmp/gvnix_int_test $ROO_COMMAND $GVNIX_HOME $TOMCAT_PORT
    fi
else
    show_message_info "Run Integration test: Skip"
fi

# Deploy jars
if [ "$DEPLOY_JARS" = "yes" ]; then
  cd $GVNIX_HOME
  if [ "$DEPLOY_RELEASE" = "yes" ]; then
    show_message_info "Deploy gvNIX Jars and Addon suite as RELEASE"
    mvn deploy -P release
  else
    show_message_info "Deploy gvNIX Jars and Addon suite as SNAPSHOT"
    mvn deploy
  fi
else
    show_message_info "Deploy gvNIX Jars adn Addon suite: Skip"
fi

## Add roo/bin to path
ROO_BIN_DIR=`dirname $ROO_COMMAND`
export PATH=$ROO_BIN_DIR:$PATH

# Info messages
echo ""
echo ""
show_message_info " Build process: SUCCESS"
echo ""
echo "gvNIX installed on /tmp and setted on PATH for test purpouses."
echo "Type roo.sh to start"

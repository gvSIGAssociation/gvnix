#!/bin/bash
#
# Script to download Spring Roo .zip Distribution

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

# Preparing distribution folder
ROO_DISTRIBUTION_FOLDER="/tmp/roo-distribution-folder-"$ROO_VERSION;
if [ -d $ROO_DISTRIBUTION_FOLDER ]; then
  rm -r $ROO_DISTRIBUTION_FOLDER;
fi
mkdir $ROO_DISTRIBUTION_FOLDER;
# Downloading ROO
wget -O "$ROO_DISTRIBUTION_FOLDER"/spring-roo-"$ROO_VERSION".zip http://spring-roo-repository.springsource.org.s3.amazonaws.com/milestone/ROO/spring-roo-"$ROO_VERSION".zip

ROO_DISTRIBUTION_ZIP=$ROO_DISTRIBUTION_FOLDER"/spring-roo-"$ROO_VERSION".zip";

echo "####### ROO Distribution Zip location #######"
echo $ROO_DISTRIBUTION_ZIP;
echo "############################################"

# Starting tests
$GVNIX_DEPLOYMENT_SUPPORT_DIR/build.sh $ROO_DISTRIBUTION_ZIP test --skipCleanRepo

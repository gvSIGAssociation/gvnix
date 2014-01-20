#!/bin/bash
#
# Script to generate gvNIX release

# TODO: Comprobar versión de JDK (1.6+) y MVN (3.0+)

GVNIX_VERSION=1.3.0-SNAPSHOT
ROO_VERSION=1.2.4.RELEASE

DEPLOY='0';
TEST='1';

usage() {
cat << EOF
usage: $0 options

OPTIONS:
    -d   Deploy to Google Code
    -s   Skip tests
    -h   Show this message

DESCRIPTION:
    Creates the release ZIP. Automates building deployment ZIPs and deployment.

REQUIRES:
    roo-deploy.sh
EOF
}

while getopts "dsh" OPTION
do
    case $OPTION in
        h)
            usage
            exit 1
            ;;
        d)
            DEPLOY='1'
            ;;
        s)
            TEST='0'
            ;;
        ?)
            usage
            exit
            ;;
    esac
done

## Exit script on any error
#set -e 
## Echo all commands
#set -x 

# Remove old roo and gvNIX installed dependencies
rm -rf ~/.m2/repository/org/springframework/roo ## || true
rm -rf ~/.m2/repository/org/gvnix ## || true

### ROO PACKAGE

# Change to Roo folder
cd roo

# Remove bundles Felix cache
rm -rf bootstrap/target/osgi ## || true

# Test, install, site, assembly and package Roo modules
mvn clean install
cd deployment-support
mvn clean site
if [ "$TEST" = "1" ]; then
	./roo-deploy.sh -c assembly -tv
else 
	./roo-deploy.sh -c assembly -v
fi
cd ..
mvn clean package

### GVNIX PACKAGE

# Change to gvNIX folder
cd ..

# Install modules, build site documentation, package modules and deploy to google code
if [ "$DEPLOY" = "1" ]; then
	mvn clean install site package deploy 
else
	mvn clean install site package
fi

# Copy gvNIX build modules (except support, already included on each add-on) together with Roo build modules
rm -rf target/all/org.gvnix.support*
cp target/all/org.gvnix.* roo/target/all

# Change to Roo deployment folder 
cd roo/deployment-support

# Build site documentation and execute deployment script
mvn clean site
if [ "$TEST" = "1" ]; then
	./roo-deploy.sh -c assembly -Tv
else
	./roo-deploy.sh -c assembly -v
fi

# Change to gvNIX folder
cd ../..

# Remove old release
rm -rf /tmp/gvNIX*

# Unzip new release
unzip roo/target/roo-deploy/dist/spring-roo-$ROO_VERSION.zip -d /tmp
WORK_DIR="/tmp/spring-roo-$ROO_VERSION"

# Add gvNIX start scripts copying roo scripts
cp $WORK_DIR/bin/roo.sh $WORK_DIR/bin/gvnix.sh
cp $WORK_DIR/bin/roo.bat $WORK_DIR/bin/gvnix.bat

# Copy static gvNIX resources (readme, license, samples and doc) into Roo
cp src/main/assembly/readme.txt $WORK_DIR/readme_gvNIX.txt
cp LICENSE.TXT $WORK_DIR/LICENSE_gvNIX.TXT
cp src/main/resources/*.roo $WORK_DIR/samples
mkdir $WORK_DIR/docs/gvNIX
cp target/docbkx/pdf/index.pdf $WORK_DIR/docs/gvNIX
cp -r target/site/reference/html-single/* $WORK_DIR/docs/gvNIX

# Create dir to include new themes for related add-on
mkdir $WORK_DIR/themes

# Rename release to gvNIX, pack it and add installed release to path
mv /tmp/spring-roo-$ROO_VERSION /tmp/gvNIX-$GVNIX_VERSION
cd /tmp
zip -9 -r gvNIX-$GVNIX_VERSION.zip gvNIX-$GVNIX_VERSION
export PATH=/tmp/gvNIX-$GVNIX_VERSION/bin:$PATH

# Copy the release to target dir (avoid to lost it when shutdown)
cd -
mkdir -p target/gvnix-dist
cp /tmp/gvNIX-$GVNIX_VERSION.zip target/gvnix-dist/

# Info messages
echo '*************************'
echo " Build process: SUCCESS"
echo '************************'
echo ""
echo "gvNIX release created: ./target/gvnix-dist/gvNIX-$GVNIX_VERSION.zip"
echo "gvNIX installed on /tmp and setted on PATH for test purpouses."
echo "Type gvnix.sh to start"

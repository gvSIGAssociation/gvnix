#!/bin/bash
#
# Script to generate gvNIX release

# TODO: Comprobar versión de JDK (1.6+) y MVN (3.0+)

GVNIX_VERSION=0.7.0.RELEASE
ROO_VERSION=1.1.4.RELEASE

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

# Remove old roo and gvNIX installed dependencies
rm -rf ~/.m2/repository/org/springframework/roo
rm -rf ~/.m2/repository/org/gvnix

### ROO PACKAGE

# Change to Roo folder
cd roo

# Remove bundles Felix cache
rm -rf bootstrap/target/osgi

# Install OSGI wrapping jars
cd wrapping
mvn clean install
cd ..

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

# Copy gvNIX build modules together Roo build modules 
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

# Unzip new release, add gvNIX start scripts
unzip roo/target/roo-deploy/dist/spring-roo-$ROO_VERSION.zip -d /tmp
cp /tmp/spring-roo-$ROO_VERSION/bin/roo.sh /tmp/spring-roo-$ROO_VERSION/bin/gvnix.sh
cp /tmp/spring-roo-$ROO_VERSION/bin/roo.bat /tmp/spring-roo-$ROO_VERSION/bin/gvnix.bat

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
echo ""
echo "gvNIX release created: ./target/gvnix-dist/gvNIX-$GVNIX_VERSION.zip"
echo "gvNIX installed on /tmp and setted on PATH for test purpouses."
echo "Type gvnix.sh to start"


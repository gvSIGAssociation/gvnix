
# Remove old roo and gvNIX installed dependencies
rm -rf ~/.m2/repository/org/springframework/roo
rm -rf ~/.m2/repository/org/gvnix

# Change to Roo distribution
cd roo

# Remove bundles Felix cache
rm -rf bootstrap/target/osgi

# Install OSGI wrapping jars
cd wrapping
mvn clean install
cd ..

# Test, install, site, assembly and package Roo modules for make gvNIX distribution 
mvn clean install
cd deployment-support
mvn clean site
./roo-deploy.sh -c assembly -tv
cd ..
mvn clean package

# Change to gvNIX distribution
cd ..

# Install modules, build site documentation, package modules and deploy to google code
mvn clean install site package deploy 

# Copy gvNIX build modules together Roo build modules 
cp target/all/org.gvnix.* roo/target/all

# Change to Roo deployment folder 
cd roo/deployment-support

# Build site documentation and execute deployment script
mvn clean site
./roo-deploy.sh -c assembly -Tv

# Change to gvNIX folder
cd ../..

# Remove old release, unzip new release, add gvNIX start scripts, rename release to gvNIX, zip release and add release to path
rm -rf /tmp/gvNIX*
unzip roo/target/roo-deploy/dist/spring-roo-1.1.2.RELEASE.zip -d /tmp
cp /tmp/spring-roo-1.1.2.RELEASE/bin/roo.sh /tmp/spring-roo-1.1.2.RELEASE/bin/gvnix.sh
cp /tmp/spring-roo-1.1.2.RELEASE/bin/roo.bat /tmp/spring-roo-1.1.2.RELEASE/bin/gvnix.bat
mv /tmp/spring-roo-1.1.2.RELEASE /tmp/gvNIX-0.5.0.RELEASE
zip -9 -r /tmp/gvNIX-0.5.0.RELEASE.zip /tmp/gvNIX-0.5.0.RELEASE
export PATH=/tmp/gvNIX-0.5.0.RELEASE/bin:$PATH

# Info messages
echo ""
echo "Build on /tmp and setted on PATH"
echo "Type roo.sh to start"

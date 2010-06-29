#!/bin/sh
set -x
PRG="$0"

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/"$link"
    fi
done
ROO_HOME=`dirname "$PRG"`

# Absolute path
ROO_HOME=`cd "$ROO_HOME/.." ; pwd`

## Check if gvnix annotations jar is installed in maven local repository
local_annotation_jar="$HOME/.m2/repository/org/gvnix/org.gvnix.annotations/${version}/org.gvnix.annotations-${version}.jar"
if ( [ ! -f "$local_annotation_jar" ] ) ; then
	## Install jar
	mvn install:install-file -DgroupId=org.gvnix -DartifactId=org.gvnix.annotations -Dversion=${version} -Dpackaging=jar -Dfile="$ROO_HOME/annotations/org.gvnix.annotations-${version}.jar"

fi


# echo Resolved ROO_HOME: $ROO_HOME
# echo "JAVA_HOME $JAVA_HOME"

cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

# Build a classpath containing our two magical startup JARs
ROO_CP=`echo "$ROO_HOME"/bin/*.jar | sed 's/ /:/g'`
# echo ROO_CP: $ROO_CP

# Store file locations in variables to facilitate Cygwin conversion if needed

ROO_OSGI_FRAMEWORK_STORAGE="$ROO_HOME/cache"
# echo "ROO_OSGI_FRAMEWORK_STORAGE: $ROO_OSGI_FRAMEWORK_STORAGE"

ROO_AUTO_DEPLOY_DIRECTORY="$ROO_HOME/bundle"
# echo "ROO_AUTO_DEPLOY_DIRECTORY: $ROO_AUTO_DEPLOY_DIRECTORY"

ROO_CONFIG_FILE_PROPERTIES="$ROO_HOME/conf/config.properties"
# echo "ROO_CONFIG_FILE_PROPERTIES: $ROO_CONFIG_FILE_PROPERTIES"

cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

if [ "$cygwin" = "true" ]; then
	export ROO_HOME="`cygpath -wp $ROO_HOME`"
	export ROO_CP="`cygpath -wp $ROO_CP`"
	export ROO_OSGI_FRAMEWORK_STORAGE="`cygpath -wp $ROO_OSGI_FRAMEWORK_STORAGE`"
	export ROO_AUTO_DEPLOY_DIRECTORY="`cygpath -wp $ROO_AUTO_DEPLOY_DIRECTORY`"
	export ROO_CONFIG_FILE_PROPERTIES="`cygpath -wp $ROO_CONFIG_FILE_PROPERTIES`"
	# echo "Modified ROO_HOME: $ROO_HOME"
	# echo "Modified ROO_CP: $ROO_CP"
	# echo "Modified ROO_OSGI_FRAMEWORK_STORAGE: $ROO_OSGI_FRAMEWORK_STORAGE"
	# echo "Modified ROO_AUTO_DEPLOY_DIRECTORY: $ROO_AUTO_DEPLOY_DIRECTORY"
	# echo "Modified ROO_CONFIG_FILE_PROPERTIES: $ROO_CONFIG_FILE_PROPERTIES"
fi

# Hop, hop, hop...
java -Droo.home="$ROO_HOME" -Droo.args="$*" -DdevelopmentMode=false -Dorg.osgi.framework.storage="$ROO_OSGI_FRAMEWORK_STORAGE" -Dfelix.auto.deploy.dir="$ROO_AUTO_DEPLOY_DIRECTORY" -Dfelix.config.properties="file:$ROO_CONFIG_FILE_PROPERTIES" -cp "$ROO_CP" org.springframework.roo.bootstrap.Main
EXITED=$?
echo Roo exited with code $EXITED

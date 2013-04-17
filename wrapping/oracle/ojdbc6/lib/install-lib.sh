#!/bin/bash
echo "Oracle JDBC driver must be downloaded manually from http://www.oracle.com/technetwork/database/features/jdbc before build the wrapping."
echo "Wrapping is internally configured for use com.oracle.ojdbc6 11.2.0.3 driver, use another at your own risk."
read -e -p "What is the local path to the driver (/tmp/ojdbc6-11.2.0.3.jar by default) ? " -i "/tmp/ojdbc6-11.2.0.3.jar" file
read -e -p "What is the group identifier (groupId) of driver (com.oracle by default) ? " -i "com.oracle" groupId
read -e -p "What is the artifact identifier (artifactId) of driver (ojdbc6 by default) ? " -i "ojdbc6" artifactId
read -e -p "What is the version (version) of driver (11.2.0.3 by default) ? " -i "11.2.0.3" version
mvn install:install-file -Dfile=$file -DgroupId=$groupId -DartifactId=$artifactId -Dversion=$version -Dpackaging=jar 

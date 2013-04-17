#!/bin/bash
echo "Oracle JDBC driver must be downloaded manually from http://www.oracle.com/technetwork/database/features/jdbc before build the wrapping."
echo "Wrapping is internally configured for use com.oracle.ojdbc14 10.2.0.5 driver, use another at your own risk."
read -e -p "What is the local path to the driver (/tmp/ojdbc14-10.2.0.5.jar by default) ? " -i "/tmp/ojdbc14-10.2.0.5.jar" file
read -e -p "What is the group identifier (groupId) of driver (com.oracle by default) ? " -i "com.oracle" groupId
read -e -p "What is the artifact identifier (artifactId) of driver (ojdbc14 by default) ? " -i "ojdbc14" artifactId
read -e -p "What is the version (version) of driver (10.2.0.5 by default) ? " -i "10.2.0.5" version
mvn install:install-file -Dfile=$file -DgroupId=$groupId -DartifactId=$artifactId -Dversion=$version -Dpackaging=jar 

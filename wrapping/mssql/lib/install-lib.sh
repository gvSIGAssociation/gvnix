#!/bin/bash
echo "Microsoft SQL JDBC driver must be downloaded manually from http://msdn.microsoft.com/sqlserver before build the wrapping."
echo "Wrapping is internally configured for use mssql.mssql 4.0.2206.100 driver, use another at your own risk."
read -e -p "What is the local path to the driver (/tmp/sqljdbc4.jar by default) ? " -i "/tmp/sqljdbc4.jar" file
read -e -p "What is the group identifier (groupId) of driver (mssql by default) ? " -i "mssql" groupId
read -e -p "What is the artifact identifier (artifactId) of driver (mssql by default) ? " -i "mssql" artifactId
read -e -p "What is the version (version) of driver (4.0.2206.100 by default) ? " -i "4.0.2206.100" version
mvn install:install-file -Dfile=$file -DgroupId=$groupId -DartifactId=$artifactId -Dversion=$version -Dpackaging=jar 

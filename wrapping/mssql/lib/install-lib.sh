#!/bin/bash
mvn install:install-file -Dfile=sqljdbc4.jar -DgroupId=mssql -DartifactId=mssql -Dversion=4.0.2206.100 -Dpackaging=jar 

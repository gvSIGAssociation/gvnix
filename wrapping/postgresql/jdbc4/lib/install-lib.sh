#!/bin/bash
mvn install:install-file -Dfile=postgresql-9.1-902.jdbc4.jar -DgroupId=postgresql -DartifactId=postgresql -Dversion=9.1-902.jdbc4 -Dpackaging=jar 

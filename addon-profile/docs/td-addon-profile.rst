Profile Addon Technical Design
==============================

Introduction
------------

* Starting point idea is use maven profiles at pom.xml::

  <profiles>
    <profile>
      <id>value</id>
      <activation>...</activation>
      <build>...</build>
      <modules>...</modules>
      <repositories>...</repositories>
      <pluginRepositories>...</pluginRepositories>
      <dependencies>...</dependencies>
      <reporting>...</reporting>
      <dependencyManagement>...</dependencyManagement>
      <distributionManagement>...</distributionManagement>
      <properties>...</properties>
    </profile>
  </profiles>
 
This is a non extensive example, it could not have all profile posibilities.

More info: http://maven.apache.org/pom.html#Profiles

* Our principal consideration, almost is use the properties section to allow different property values, one for each environment::

  <properties>
    <name>value</name>
    ...
  </properties> 

* Replaced values location are defined by next section in pom.xml::  

 <resources>
  <resource>
   <directory>xxx</directory>
   <excludes>
    <exclude>xxx</exclude>
   </excludes>
  <filtering>xxx</filtering>
  </resource>
 </resources>

The filtering true value means the directory files not included in excludes are scanned and replaced properties with the selected profile value.   

* Replaced properties format on project files::

  ${xxx}

* Profilized files formats:

 * xml
 * properties
 * ...
 
¿ Only on resources folder or src too ?

Posibilities
============

# Profile addon overwrites some other addon values with the property format and the profile addon manage the pom.xml profiles section.
# Profile addon only manage the pom.xml profiles section and other addons would be modified to be registered on a osgi component that alert when a profile it property is modified.   

References
==========

* http://maven.apache.org/guides/introduction/introduction-to-profiles.html
* http://maven.apache.org/pom.html#Profiles

========================================================
 gvNIX Service Layer Add-on Status, Use Case and Steps
========================================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: DGTI - Generalitat Valenciana
:Author:    DISID Corporation, S.L.
:Revision:  $Rev: 394 $
:Date:      $Date: 2010-11-08 13:26:05 +0100 (lun, 08 nov 2010) $

.. contents::
   :depth: 2
   :backlinks: none

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit 
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to 
Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 
94105, USA.

Introduction - Status description.

Introduction
=============

Document to define Add-on status and depending features related with ROO that have been fixed temporarily manually.

Web Service export ws
=======================

--------
Status
--------

The project could replicate a Web Service using Contract First definition with these rules:

  * WSDL1.0 Compliant.
  * Communication protocol must be defined using ``SOAP11`` or ``SOAP12``. Only *one* protocol.
  * Can reproduce XSD Schema structure converting to ``@XmlElement``. 
  
      * Lists, Faults, Objects and simple types.
      * Inner Classes are created in a new class.
      * Enum types definitions are handled like simple ones.
  * Define Soap binding for Web Service and each of its operations.

Values that can't be replicated with ``service export ws`` operation:

  * Values for ``@XmlElement`` field from Generated Objects *can't* be represented in Java classes. We can define it using *@GvNIXXmlElementField* attributes for each field.

These features have been fixed in **GVNIX** patch waiting to be resolved by ROO in next versions:

  * ROO has to allow 'package info.java' class as correct format inside of a project. JIRA: https://jira.springsource.org/browse/ROO-1734
  * Inner classes generation using ClassPathOperation from ROO API. ROO Forum: http://forum.springsource.org/showthread.php?t=98379.
  * Allow annotation in enumeration classes. ROO Forum: http://forum.springsource.org/showthread.php?t=98382.
  * NullPointerException creating a constructor in a class. JIRA: https://jira.springframework.org/browse/ROO-1710.

----------------
Use Case Test
----------------

To check the correct Web Service generation using ``service export ws`` command we define the next uses case:

  * Create a server generating a java class using WSDL Contract First definition.

--------
Steps
--------

Create an application using GvNIX to replicate a Web Service Server using ``Service Layer Add-on``.

Requirements
---------------

* JDK-1.5.07
* Maven 2.0.9
* GvNIX 0.5.0
* Eclipse Galileo 3.5.1

To create and check a test application you have to:

  #. Create java 1.5 web project using GvNIX.
  #. Generate a server using the command::

        roo> remote service export ws --wsdl https://ws.xwebservices.com/XWebEmailValidation/XWebEmailValidation.asmx?wsdl
  #. Run the application::
  
        bash> mvn clean jetty:run-war -Dmaven.test.skip=true
  #. Run Eclipse Web Service Explorer an use the original Web Service URL to create a client. 
  
      * https://ws.xwebservices.com/XWebEmailValidation/XWebEmailValidation.asmx?wsdl
  #. Test the service to check that it works and the values of sent data in operations.
  #. Replace the original **endpoint** with the generated with the command in our published WSDL:

      * http://localhost:8080/service-layer-test/services/XWebEmailValidationSoap
  #. Check again the service operations tested before. 
  #. If the communication between the original service the replicated endpoint works, the test is OK.
  #. We can add logic to generated method from our replicated server to test the returned Objects involved in operations.
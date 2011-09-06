Hibernate 3.6.7 wrapper.

It has been tested with FUSE ESB 4.4 (Karaf).

TODO
======

* Documentar como instalar, usar como ref el archivo features.xml creado para FUSE::

    <?xml version="1.0" encoding="UTF-8"?>
    <features name="reconec-domain-features-repository">

    <feature name="jpa2-hibernate3" version="3.6.7.Final">
        <!-- Hibernate required dependencies -->
        <bundle dependency="true">mvn:org.antlr/com.springsource.antlr/2.7.7</bundle>
        <bundle dependency="true">mvn:org.objectweb.asm/com.springsource.org.objectweb.asm/1.5.3</bundle>
        <bundle dependency="true">mvn:net.sourceforge.cglib/com.springsource.net.sf.cglib/2.2.0</bundle>
        <bundle dependency="true">mvn:org.apache.commons/com.springsource.org.apache.commons.collections/3.2.1</bundle>
        <bundle dependency="true">mvn:org.dom4j/com.springsource.org.dom4j/1.6.1</bundle>
        <bundle dependency="true">mvn:org.apache.log4j/com.springsource.org.apache.log4j/1.2.16</bundle>
        <bundle dependency="true">mvn:org.slf4j/com.springsource.slf4j.api/1.6.1</bundle>
        <bundle dependency="true">mvn:org.slf4j/com.springsource.slf4j.log4j/1.6.1</bundle>
        <bundle dependency="true">mvn:org.apache.commons/com.springsource.org.apache.commons.dbcp/1.2.2.osgi</bundle>
        <bundle dependency="true">mvn:javassist/org.gvnix.javassist/3.12.1.GA</bundle>

        <!-- Hibernate Validator required dependencies -->
        <bundle dependency="true">mvn:org.joda/com.springsource.org.joda.time/1.6.0</bundle>
        <bundle dependency="true">mvn:org.jsoup/jsoup/1.6.1</bundle>

        <!-- Spring -->
        <bundle dependency="true">mvn:org.springframework/spring-jdbc/3.0.5.RELEASE</bundle>
        <bundle dependency="true">mvn:org.springframework/spring-orm/3.0.5.RELEASE</bundle>

        <!-- Specs -->
        <!-- IMPORTANT: Some Java EE specifications don't work well in OSGi

        The mean reason is that the implementation is discovered using 
        different mechanisms by the API. The most commonly used one is to find 
        a file on the classpath in the 'META-INF/services' directory and find 
        the main entry point class of the implementation (SPI mechanism).

        ServiceMix and Geronimo groups have done a good work on this area
        and it's recommendable/needed to use their bundles for JEE specs
        because these bundles include an OSGi specific discovery mechanism
        that let these spec bundles to be able to locate dynamically the 
        implementation to use.

        Ref: http://gnodet.blogspot.com/2008/05/jee-specs-in-osgi.html

        MVN repos:
          http://repo1.maven.org/maven2/org/apache/servicemix/specs/
          http://repo1.maven.org/maven2/org/apache/geronimo/specs/
        -->
        <bundle dependency="true">mvn:org.apache.servicemix.specs/org.apache.servicemix.specs.java-persistence-api-2.0/1.8.0</bundle>
        <bundle dependency="true">mvn:org.apache.geronimo.specs/geronimo-jta_1.1_spec/1.1.1</bundle>
        <bundle dependency="true">mvn:org.apache.geronimo.specs/geronimo-jacc_1.1_spec/1.0.2</bundle>
        <bundle dependency="true">mvn:org.apache.servicemix.specs/org.apache.servicemix.specs.jsr303-api-1.0.0/1.8.0</bundle>
        <bundle dependency="true">mvn:org.apache.servicemix.specs/org.apache.servicemix.specs.activation-api-1.1/1.8.0</bundle>

        <!-- Hibernate3 -->
        <bundle>mvn:org.hibernate/org.gvnix.org.hibernate.validator/4.2.0.Final/</bundle>
        <bundle>mvn:org.hibernate/org.gvnix.org.hibernate/3.6.7.Final</bundle>
    </feature>
    </features>

* Probar en un OSGi que no sea FUSE porque FUSE incluye muchos componentes como JAXB y hay que probar que el wrap funciona bien en todos los entornos OSGi

* Encontrar una forma de distribuir el features.xml


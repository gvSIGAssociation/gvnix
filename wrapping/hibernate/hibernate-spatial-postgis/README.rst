
Setup
======

This bundle has the common SPEC SPI problem described by Guillaume Nodet in Guillaume Nodet.

The way to solve this problem is as describen in Guillaume's article above.

Currently this bundle isn't fixed to run in OSGi and we included a work-around that will cause this bundle work:

  .. admonition:: Work-around

    The bundle will load the default *spatial dialect provider* and the default *dialect* from System properties.

To set this bundle up you have to include the properties ``hibernate.spatial.dialect`` and ``hibernate.spatial.dialect.provider`` in system configuration of your OSGi runtime. For example, to setup this bundle in ServiceMix 4 for PostGIS database:

# Edit *etc/system.properties*
# Add the properties below::

    # Hibernate Spatial Default Dialect
    hibernate.spatial.dialect=org.hibernate.dialect.PostgreSQLDialect
    hibernate.spatial.dialect.provider=org.hibernatespatial.postgis.DialectProvider

TODO
=====

* Upgrade the bundle to work in OSGi by fixing it as Guillaume explains in his article.


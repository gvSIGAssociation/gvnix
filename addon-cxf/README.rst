=========================================
 gvNIX CXF Addon for Spring Roo
=========================================

gvNIX is and Spring Roo based RAD tool for Conselleria d'Infraestructures i Transport - Generalitat Valenciana

`Spring Roo <http://www.springframework.org/roo>`_ is a next-generation rapid application development tool for Java developers. With Roo you can easily build full Java applications in minutes. It differs from other tools by focusing on:

* Higher Java productivity: Try the ten minute test and see for yourself (see video).
* Stock-standard Java: Roo uses the Java APIs and standards you already know and trust.
* Usable and learnable: Roo features an extremely high level of usability and an advanced shell.
* No engineering trade-offs: Roo has no runtime portion and does not impose any CPU, RAM or disk storage cost.
* Easy Roo removal: Roo can be easily removed from a user project in under five minutes.

This addon adds CXF WebService support to Spring Roo projects.

Current commands:

* ``cxf setup``
  
  - Adds CXF library dependencies to the project.
  - Create needed infraestructure to run your web services
  - Create a sample service.

How to install
===============

#. Download and setup `Spring Roo <http://www.springsource.com/download/community?project=Spring%20Roo>`_ .

#. Change to cxf addon directory

#. Run the Roo console

#. Execute the command ``perform assembly`` to build the addon

#. Execute the command ``addon install --url file:{absolute_path_to_zip_generated_from_assembly}`` to install the addon

That's all.

To remove the addon execute ``addon uninstall --pattern {name_of_addon_zip_file_without_path}`` in any Roo console


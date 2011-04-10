==================================
 gvNIX. Spring Roo based RAD tool 
==================================

Thank you for choosing gvNIX!

gvNIX is a set of additional productivity utilities that increase the 
flexibility and power of Spring Roo made by Conselleria d'Infraestructures i 
Transport of Generalitat Valenciana (Spain) http://www.cit.gva.es/.

This gvNIX version (${gvnix.version}) is based on Spring Roo ${roo.version}.

Before gvNIX install you need:

* A *nix machine (Windows users should be OK if they write a .bat)
* A proper installation of Java 5 or above
* Maven 2.0.9+ properly installed and working with your Java 5+
* Internet access so that Maven can download required dependencies

Install gvNIX:

* Unpack gvNIX release ZIP file to installation directory. For example::

    cd $HOME 
    # Note: Change VER to gvNIX version number you use
    unzip gvNIX-VER-RELEASE.zip                 # gvNIX-VER-RELEASE dir has been created
    export GVNIX_HOME="$PWD/gvNIX-VER-RELEASE"  # Recommended

* Add gvNIX bin directory to your PATH::

    echo export PATH=$GVNIX_HOME/bin:$PATH >> ~/.bashrc
    source ~/.bashrc

You're finished. To run gvNIX just type 'gvnix'.

Learn gvNIX:

* gvNIX is a Spring Roo distribution, just run gvNIX and use help and hint Luke.
* You have detailed documentation at *docs* directory (spanish)

You can find more information and resources at:

* gvNIX project page: http://www.gvpontis.gva.es/cast/gvnix/ (spanish)
* Spring Roo project page: http://www.springsource.org/roo 

Enjoy it!

gvNIX Team.

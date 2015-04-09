==================================
 gvNIX. Spring Roo based RAD tool
==================================

Thank you for choosing gvNIX!

gvNIX is an open source tool for rapid application development (RAD) with
which you can create Java web applications in minutes.

It is a distribution of Spring Roo that provides the set of Spring Roo tools
plus a suite of features that increase development productivity and improve
the user experience.

gvNIX is sponsored by the General Directorate for Information
Technologies (DGTI) of the Regional Ministry of Finance and Public
Administration of the Generalitat Valenciana (Valencian Community,
Spain), managed by gvSIG Association and led by DISID.

This gvNIX version (${gvnix.version}) is based on Spring Roo ${roo.version}.

Before gvNIX install you need:

* A proper installation of Java 6 or above
* Maven 2.0.9+ properly installed and working with your Java 6+
* Internet access so that Maven can download required dependencies

Install gvNIX:

* Unpack gvNIX release ZIP file to installation directory. For example, for *nix machines::

    cd $HOME
    # Note: Change VER to gvNIX version number you use
    unzip gvNIX-VER-RELEASE.zip                 # gvNIX-VER-RELEASE dir has been created
    export GVNIX_HOME="$PWD/gvNIX-VER-RELEASE"  # Recommended

* Add gvNIX bin directory to your PATH. For example, for *nix machines::

    echo export PATH=$GVNIX_HOME/bin:$PATH >> ~/.bashrc
    source ~/.bashrc

You're finished. To run gvNIX just type 'gvnix.sh' on *nix machines or 'gvnix.bat' on Windows machines.

Learn gvNIX:

* gvNIX is a Spring Roo distribution, just run gvNIX and use help and hint.
* You have detailed documentation at *docs* directory (spanish)

You can find more information and resources at:

* gvNIX project page:

  * Spanish: http://www.gvnix.org
  * English: http://gvnix.googlecode.org

* Spring Roo project page: http://www.springsource.org/roo

Enjoy it!

gvNIX Team.

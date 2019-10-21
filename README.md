Jam - Java-based Data Acquisition for Nuclear Physics
=====================================================

General Advice
--------------

* Make sure that the path to Java is in your system path. For Maven, it is also helpful
  to have it assigned to your `JAVA_HOME` environment variable.
* Help files are available from the Help menu once the application is
  launched.

To Launch Jam - Windows
-----------------------

This assumes you've downloaded a Jam release tarball and extracted to your filesystem.

* Windows - `jam.cmd`
* Linux - `jam.sh`


To Build and Run in Maven
-------------------------

This assumes you've pulled the Git repository or downloaded the source tarball from GitHub.

    > mvn package
    > mvn exec:java

After making a code change, sometimes it can be useful to execute the clean lifecycle before
the package phase:

    > mvn clean package

I am currently working towards generating an appropriate cross-platform build artifact that
includes all needed runtime and test libraries.

Jam - Java-based Data Acquisition for Nuclear Physics
=====================================================

Prerequisites
-------------

* To run: Java SE 11+ Java Runtime Environment (JRE). See
  [Oracle](https://www.oracle.com/technetwork/java/javase/overview/) or, better yet, install
  an OpenJDK JRE. See [OpenJDK](https://openjdk.java.net/) for more info. On Ubuntu 18.04 and
  later, simply execute `sudo apt install openjdk-11-jre`. 
* To develop/build: Java SE 11+ Java Development Kit (JDK). See above links for more info. On
  Ubuntu 18.04 and later, simply execute `sudo apt install openjdk-11-jdk`.

In all likelihood, Jam will run on later versions (12 and 13, as of this writing) of Java SE.
This has not been tested in any way, though.

General Advice
--------------

* Running: Make sure that the path to Java is in your system path.
* Building: Make sure you have set the `JAVA_HOME` environment variable.
* Help files are available from the Help menu once the application is
  launched.

To Launch Jam
-------------

The easiest way to launch Jam is to download the latest `Jam-X.y.z-jar-with-dependencies.jar`
from the project releases and launch it as follows:

    > java -jar Jam-X.y.z-jar-with-dependencies.jar

To Build and Run in Maven
-------------------------

This assumes you've pulled the Git repository or downloaded the source tarball from GitHub.

    > mvn package
    > mvn exec:java

After making a code change, sometimes it can be useful to execute the clean lifecycle before
the package phase:

    > mvn clean package

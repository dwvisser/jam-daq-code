Jam - Java-based Data Acquisition for Nuclear Physics
=====================================================

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dwvisser_jam-daq-code&metric=alert_status)](https://sonarcloud.io/dashboard?id=dwvisser_jam-daq-code)

Prerequisites
-------------

Jam requires the Java Development Kit (JDK) version 17 or later. See
[Oracle](https://www.oracle.com/technetwork/java/javase/overview/) or, better
yet, install [OpenJDK](https://openjdk.java.net/).

While it is possible to run Jam with just a Java Runtime Environment (JRE),
normal usage of Jam means authoring sort routines in Java, and compiling them
against Jam as a library (further details in the Jam User Guide).

Further User Documentation
--------------------------

See the User Guide in the Help menu once the application is launched.

To Launch Jam
-------------

**IMPORTANT**: For the command below to work, make sure that the path to `java`
is in your system path.

The easiest way to launch Jam is to download the latest
`Jam-X.y.z-jar-with-dependencies.jar` from the
[project releases](https://github.com/dwvisser/jam-daq-code/releases) and launch
it as follows:

    > java -jar Jam-X.y.z-jar-with-dependencies.jar

To Build and Run in Maven
-------------------------

**IMPORTANT**: Make sure you have set the `JAVA_HOME` environment variable.

This assumes you've pulled the Git repository or downloaded the source tarball
from GitHub.

    > mvn package
    > mvn exec:java

After making a code change, sometimes it can be useful to execute the clean
lifecycle before
the package phase:

    > mvn clean package

I am not a Maven expert, so I am recording here what works for me to create a
release executable jar file:

    > mvn clean package assembly:single

This will create a `target/Jam-X.y.z-jar-with-dependencies.jar` file that can be
run as described above in [To Launch Jam](#to-launch-jam).

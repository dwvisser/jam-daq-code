Jam - Java-based Data Acquisition for Nuclear Physics
=====================================================

General Advice
--------------

* Make sure that the path to Java is in your system path.
* Help files are available from the Help menu once the application is
  launched.

To Build and Run in Maven
-------------------------

    > mvn clean javadoc:javadoc package
    > mvn exec:java

I am currently working towards making the `javadoc` invocation automatic
for some appropriate Maven build phase, as well as generating an appropriate
cross-platform build artifact that includes all needed runtime and test libraries.

To Launch Jam - Windows
-----------------------

Use jam.cmd to launch Jam.

To Launch Jam - Linux, MacOS X, etc.
------------------------------------

Use jam.sh to launch Jam.

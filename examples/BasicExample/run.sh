#!/bin/sh

# this script should be run from the lib-java root folder


mvn package
cd examples/BasicExample/
javac -classpath ../../target/lib-0.1.0-jar-with-dependencies.jar src/main/java/BasicExample.java
cd target/classes/
java -classpath ../../../../target/lib-0.1.0-jar-with-dependencies.jar:./ BasicExample
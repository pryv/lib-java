#!/bin/sh

# this script should be run from the lib-java root folder


cd examples/JavaApp/
mvn package
java -jar target/JavaApp-0.1.0-jar-with-dependencies.jar
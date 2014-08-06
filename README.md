# Pryv library for Java and Android

Java and Android library to authorize, authenticate and access Pryv users data as defined in the [Pryv API](http://api.pryv.com/).

## Usage

### Maven Project

```
<dependency>
		<groupId>com.pryv</groupId>
		<artifactId>lib-java</artifactId>
		<version>1.0-SNAPSHOT</version>
</dependency>
```

If the library is not yet deployed to the remote repository, you first need to install Maven and execute `mvn install`.

### JAR

After running `mvn package`, include the `lib-java-1.0-SNAPSHOT-jar-with-dependencies.jar` file in your classpath.


## Contribute

### Installation

Install [Maven](http://books.sonatype.com/mvnref-book/reference/installation-sect-maven-install.html).

### Build and tests

`mvn compile` compiles the source code.

`mvn test` runs the tests.

`mvn package` builds the JAR with depencies file in the /target subdirectory and runs the tests.

`mvn install` installs the package into the local repository, makes it available for use as a dependency in other projects locally.

[additionnal information](http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)



## Examples

### Java Demo App

This is a Java demo JavaFX Application located in the examples/JavaApp/JavaApp subdirectory.

This App connects to the "web-app-test" application, requiring permissions to access all streams with "manage" level. After a successful login, the app allows to manipulate Streams and Events in a simple way.

to run it:

- `mvn package` to generate the executable JAR.

- `java -jar target/JavaApp-1.0-SNAPSHOT-jar-with-dependencies` to run the JAR.

## License

[Revised BSD license](https://github.com/pryv/documents/blob/master/license-bsd-revised.md)
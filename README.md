# Pryv library for Java and Android

Java and Android library to authorize, authenticate and access Pryv users data as defined in the [Pryv API](http://api.pryv.com/).

## Usage

### imports

#### Maven Project

```
<dependency>
		<groupId>com.pryv</groupId>
		<artifactId>lib</artifactId>
		<version>0.1.0</version>
</dependency>
```

If the library is not yet deployed to the remote repository, you first need to install Maven and execute `mvn install`.

#### JAR

After running `mvn package`, include the `lib-java-0.1.0-jar-with-dependencies.jar` file in your classpath.

### Docs

- [Getting started guide](http://pryv.github.io/getting-started/java/)

### Examples

```
// connect to the API

Permission testPermission = new Permission("*", Permission.Level.manage, null);
List<Permission> permissions = new ArrayList<Permission>();
    
AuthController authenticator = new AuthControllerImpl(REQUESTING_APP_ID, permissions, null, null, this);
```


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

### Java Example App

This is a Java demo JavaFX Application. It requires Java 1.8 to run.

This App connects to the "pryv-lib-java-example" application, requiring permissions to access all streams with "manage" level. After a successful login, the app allows to manipulate Streams and Events in a simple way.

to run it:

- `cd examples/JavaApp/JavaApp/` 

- `mvn package` to generate the executable JAR.

- `java -jar target/JavaApp-0.1.0-jar-with-dependencies.jar` to run the JAR.

## License

[Revised BSD license](https://github.com/pryv/documents/blob/master/license-bsd-revised.md)
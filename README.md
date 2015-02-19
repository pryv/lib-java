# Pryv library for Java and Android

Java library to authorize, authenticate and access Pryv users data as defined in the [Pryv API](http://api.pryv.com/).

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

#### JAR

After running `mvn package`, include the `lib-java-0.1.0-jar-with-dependencies.jar` file in your classpath.

### Docs

- [Getting started guide](http://pryv.github.io/getting-started/java/) ([examples](http://pryv.github.io/getting-started/java#Examples))

## Contribute

### Installation

Install [Maven](http://books.sonatype.com/mvnref-book/reference/installation-sect-maven-install.html).

### Build and tests

`mvn compile` compiles the source code.

`mvn test` runs the tests.

`mvn package` builds the JAR with depencies file in the /target subdirectory and runs the tests.

`mvn install` installs the package into the local repository, makes it available for use as a dependency in other projects locally.

[additional information](http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)


## License

[Revised BSD license](https://github.com/pryv/documents/blob/master/license-bsd-revised.md)
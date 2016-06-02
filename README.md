# Pryv library for Java and Android

Java library to authorize, authenticate and access Pryv users data as defined in the [Pryv API](http://api.pryv.com/).

### Docs

- [Getting started guide](http://pryv.github.io/getting-started/java/) ([examples](http://pryv.github.io/getting-started/java#Examples))

## Import

We have not yet published the library on Gradle, but you can load it in yor **Gradle** build using [JitPack](https://jitpack.io/) as follows:

```
repositories {
    maven {
        url "https://jitpack.io"
    }
	jcenter()
}

dependencies {
	compile 'com.github.Pryv:lib-java:master-SNAPSHOT'
}
```

To add a dependency using **Maven**, use the following:

```
<dependency>
		<groupId>com.pryv</groupId>
		<artifactId>lib</artifactId>
		<version>0.1.0</version>
</dependency>
```

## Contribute

### Gradle

`./gradlew` installs the appropriate version of gradle. You can also install it manually [here](https://docs.gradle.org/current/userguide/installation.html).

### Build and tests

`gradle compileJava` compiles the source files.

`gradle test` runs the tests.

`gradle -Dtest.single={TestClassName} test` runs a single test class.

`gradle jar` builds the JAR with

### Maven

Install [Maven](http://books.sonatype.com/mvnref-book/reference/installation-sect-maven-install.html).

### Build and tests

`mvn compile` compiles the source code.

`mvn test` runs the tests.

`mvn package` builds the JAR with depencies file in the /target subdirectory and runs the tests.

`mvn install` installs the package into the local repository, makes it available for use as a dependency in other projects locally.

[additional information](http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)

### JAR

After running `mvn package`, include the `lib-java-0.1.0-jar-with-dependencies.jar` file in your classpath.

## License

[Revised BSD license](https://github.com/pryv/documents/blob/master/license-bsd-revised.md)
# Pryv library for Java and Android

Java library to authorize, authenticate and access Pryv users data as defined in the [Pryv API](http://api.pryv.com/).

## Docs

- [Getting started guide](http://pryv.github.io/getting-started/java/) ([examples](http://pryv.github.io/getting-started/java#Examples))

## Import

To add a dependency using **Gradle**, use the following (according to the platform you are targeting):

Java project:
```
compile 'com.pryv:java:1.0.0'
```

Android project:
```
compile 'com.pryv:android:1.0.0'
```

To add a dependency using **Maven**, use the following (according to the platform you are targeting):

Java project:
```
<dependency>
  <groupId>com.pryv</groupId>
  <artifactId>java</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

Android project:
```
<dependency>
  <groupId>com.pryv</groupId>
  <artifactId>android</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

To add a dependency using **Ivy**, use the following (according to the platform you are targeting):

Java project:
```
<dependency org='com.pryv' name='java' rev='1.0.0'>
  <artifact name='$AID' ext='pom'></artifact>
</dependency>
```

Android project:
```
<dependency org='com.pryv' name='android' rev='1.0.0'>
  <artifact name='$AID' ext='pom'></artifact>
</dependency>
```

## Contribute

### Gradle

`./gradlew` installs the appropriate version of gradle. You can also install it manually [here](https://docs.gradle.org/current/userguide/installation.html).

#### Build and tests

`gradle compileJava` compiles the source files.

`gradle test` runs the tests.

`gradle -Dtest.single={TestClassName} test` runs a single test class.

`gradle jar` builds the JAR with

### Maven

Install [Maven](http://books.sonatype.com/mvnref-book/reference/installation-sect-maven-install.html).

#### Build and tests

`mvn compile` compiles the source code.

`mvn test` runs the tests.

`mvn package` builds the JAR with depencies file in the /target subdirectory and runs the tests.

`mvn install` installs the package into the local repository, makes it available for use as a dependency in other projects locally.

[additional information](http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)

## License

[Revised BSD license](https://github.com/pryv/documents/blob/master/license-bsd-revised.md)

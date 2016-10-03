# Pryv library for Java and Android

Java library to authorize, authenticate and access Pryv users data as defined in the [Pryv API](http://api.pryv.com/).

## Docs

- [Getting started guide](http://pryv.github.io/getting-started/java/) ([examples](http://pryv.github.io/getting-started/java#Examples))

## Import

Since this library aims to be compatible with Java and Android environment, it contains a Java library and an Android library that both have a Commons library as dependency. Thus, please import the dependency depending on the platform you are targeting as follows :

### Gradle

Java project:
```
compile 'com.pryv:java:1.0.3'
```

Android project:
```
compile 'com.pryv:android:1.0.3'
```

### Maven

Java project:
```
<dependency>
  <groupId>com.pryv</groupId>
  <artifactId>java</artifactId>
  <version>1.0.3</version>
  <type>pom</type>
</dependency>
```

Android project:
```
<dependency>
  <groupId>com.pryv</groupId>
  <artifactId>android</artifactId>
  <version>1.0.3</version>
  <type>pom</type>
</dependency>
```

### Jars, Aars

Please visit our [Bintray page](https://bintray.com/techpryv/maven/pryv-lib), where you can get the sources (Jar, Aar) of the lib by clicking on the *Files* tab.

Download and import the source files listed below in your libs folder.

Java project:
  * [pryv-lib-commons-with-dependencies-1.0.3.jar](https://bintray.com/techpryv/maven/download_file?file_path=com%2Fpryv%2Fcommons%2F1.0.3%2Fpryv-lib-commons-with-dependencies-1.0.3.jar)
  * [java-1.0.3.jar](https://bintray.com/techpryv/maven/download_file?file_path=com%2Fpryv%2Fjava%2F1.0.3%2Fjava-1.0.3.jar)

Android project:
  * [pryv-lib-commons-with-dependencies-1.0.3.jar](https://bintray.com/techpryv/maven/download_file?file_path=com%2Fpryv%2Fcommons%2F1.0.3%2Fpryv-lib-commons-with-dependencies-1.0.3.jar)
  * [android-1.0.3.aar](https://bintray.com/techpryv/maven/download_file?file_path=com%2Fpryv%2Fandroid%2F1.0.3%2Fandroid-1.0.3.aar)

## Java Integration
Please see [https://github.com/pryv/app-java-examples](https://github.com/pryv/app-java-examples).

## Android Integration

Please see [https://github.com/pryv/app-android-example](https://github.com/pryv/app-android-example).

## Contribute

`./gradlew` installs the appropriate version of gradle. You can also install it manually [here](https://docs.gradle.org/current/userguide/installation.html).

### Build and tests

`gradle compileJava` compiles the source files.

`gradle test` runs the tests.

`gradle -Dtest.single={TestClassName} test` runs a single test class.

`gradle jar` builds the JAR with

## License

[Revised BSD license](https://github.com/pryv/documents/blob/master/license-bsd-revised.md)

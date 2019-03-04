# Pryv library for Java and Android

Java library to authorize, authenticate and access Pryv users data as defined in the [Pryv API](http://api.pryv.com/).

## Docs

- [Getting started guide](http://pryv.github.io/getting-started/java/) ([examples](http://pryv.github.io/getting-started/java#Examples))

## Import

### Gradle

```
compile 'com.pryv:commons:2.1.1'
```

### Maven

```
<dependency>
  <groupId>com.pryv</groupId>
  <artifactId>commons</artifactId>
  <version>2.1.1</version>
  <type>pom</type>
</dependency>
```

### Jar

Our [Bintray page](https://bintray.com/techpryv/maven/pryv-lib) hosts the sources (jar) of the libs under the *Files* tab.

Download and import the following jar file listed below in your libs folder: [commons-2.1.1.jar](https://bintray.com/techpryv/maven/download_file?file_path=com%2Fpryv%2Fcommons%2F2.1.1%2Fcommons-2.1.1.jar
)

## Java Integration
Please see [https://github.com/pryv/app-java-examples](https://github.com/pryv/app-java-examples).

## Android Integration

Please see [https://github.com/pryv/app-android-example](https://github.com/pryv/app-android-example).

## Contribute

Use the Gradle Wrapper to run the following tasks:

`gradlew install` builds the project.

`gradlew test` runs the tests.

`gradlew test -i` runs the tests in verbose mode.

`gradlew -Dtest.single={TestClassName} test` runs a single test class.

## Support and waranty

Pryv provides this software for educational and demonstration purposes with no support or warranty.

## License

[Revised BSD license](https://github.com/pryv/documents/blob/master/license-bsd-revised.md)

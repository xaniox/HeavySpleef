# HeavySpleef-2.0

This is a completely recoded and revamped version of HeavySpleef. As the project is currently in the end-phase you may test the plugin and report bugs via the [GitHub Issue Tracker](https://github.com/matzefratze123/HeavySpleef-2.0/issues).

## Builds

If you want to help testing you may download versions from the build server as stated below:

[![Build Status](http://matzefratze123.de/jenkins/job/HeavySpleef%202.0/badge/icon)](http://matzefratze123.de/jenkins/job/HeavySpleef%202.0/)&nbsp;&nbsp;[Visit the Jenkins Build Server](http://matzefratze123.de/jenkins/job/HeavySpleef%202.0/)

## Compilation

We use Maven 3 to handle our dependencies

* Install [Maven 3](http://maven.apache.org/download.html)
* Check out this repo and:&nbsp;&nbsp;```mvn clean install```

## HeavySpleef as a dependency

If you want to add HeavySpleef to your dependencies for your project consider adding our Artifactory server to your repository list in your pom.xml:

```
<repositories>
  <repository>
    <id>matzes-repository</id>
    <url>http://matzefratze123.de/artifactory/snapshots/</url>
  </repository>
  <!-- And so on -->
</repositories>
```

And add HeavySpleef to your dependency list to automatically download the jar

```
<dependencies>
  <dependency>
    <groupId>de.matzefratze123</groupId>
    <artifactId>heavyspleef-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>provided</scope>
  </dependency>
  <!-- And so on -->
</dependencies>
```

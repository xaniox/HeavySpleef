# HeavySpleef-2.0

This is a completely recoded and revamped version of HeavySpleef. As the project is currently in the end-phase before release you may test the plugin and report bugs via the <a href="https://github.com/matzefratze123/HeavySpleef-2.0/issues" target="_blank">GitHub Issue Tracker</a>

## Testing

Thank you for your interest in helping to test HeavySpleef 2.0. Please make sure to not install current versions of this plugin on your live server. It is pretty unlikely that these versions corrupt your older HeavySpleef data, but you do that on your OWN RISK if you continue to use it on your live server.

You may find downloads for compiled versions for HeavySpleef 2.0 below:

[![Build Status](http://matzefratze123.de/jenkins/job/HeavySpleef/badge/icon)](http://matzefratze123.de/jenkins/job/HeavySpleef/)&nbsp;&nbsp;Visit the <a href="http://matzefratze123.de/jenkins/job/HeavySpleef%202.0/" target="_blank">Jenkins Build Server</a>

##### Addons

As you may notice there are different files available on the build server to download. Aside of the HeavySpleef 2.0 plugin file there are other jars like AddOn-InventoryGUI.jar or LeaderboardExtensions.jar. These files are add-ons for HeavySpleef 2.0 and may be dropped into the plugins/HeavySpleef/addons folder after running HeavySpleef 2.0 for at least one time. HeavySpleef will load these jars automatically and activate them once they are placed in the mentioned folder.

Please note that these add-ons are no Bukkit plugins and cannot be dropped into the default folder for Bukkit plugins.

##### Known caveats

We are currently aware that the team flag does not properly work. Please do not create any issues relating to the team flag.

## Compilation

We use Maven 3 to handle our dependencies

* Install [Maven 3](http://maven.apache.org/download.html)
* Check out this repo and:&nbsp;&nbsp;```mvn clean install```

## HeavySpleef as a dependency

If you want to add HeavySpleef to your dependencies in order to develop an add-on for it consider adding our Artifactory server to your repository list in your pom.xml:

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

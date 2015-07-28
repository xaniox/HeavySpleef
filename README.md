# HeavySpleef-2.0

This is a completely recoded and revamped version of HeavySpleef. Source code for versions below 2.0 can be found [here](https://github.com/matzefratze123/HeavySpleef-Legacy).

Spleef is a gamemode Minecraft where your goal is to knockout players by destroying the blocks under their feets. If a player touches water/lava or a so called "deathzone" he is out of the game. Last man standing wins...

HeavySpleef provides a fully automated environment for setting up Spleef games with a large variety of chooseable add-ons and game "flags" which customize the game or even introduce a new gamemode to Spleef.

For more information about the plugin itself and how to setup and use it and of course for release downloads, visit our [BukkitDev homepage](http://dev.bukkit.org/bukkit-plugins/heavyspleef/) or our [Spigot resource page](https://www.spigotmc.org/resources/heavyspleef.9836/)

## Development Builds / Continuous Integration

Development builds may be accessed by our Jenkins server. Please note that these builds may be unstable.

[![Build Status](http://matzefratze123.de/jenkins/job/HeavySpleef/badge/icon)](http://matzefratze123.de/jenkins/job/HeavySpleef/)&nbsp;&nbsp;Visit the <a href="http://matzefratze123.de/jenkins/job/HeavySpleef%202.0/" target="_blank">Jenkins Build Server</a>

##### Addons

As you may notice there are different files available on the build server to download. Aside of the HeavySpleef 2.0 plugin file there are other jars as AddOn-InventoryGUI.jar or LeaderboardExtensions.jar for instance. These files are add-ons for HeavySpleef 2.0 and may be dropped into the plugins/HeavySpleef/addons folder after running HeavySpleef 2.0 for at least one time. HeavySpleef will load these jars automatically and activate them once they are placed in the mentioned folder.

Please note that these add-ons are no Bukkit plugins and cannot be dropped into the default folder for Bukkit plugins.

## Compilation

We use Maven 3 to handle our dependencies

* Install [Maven 3](http://maven.apache.org/download.html)
* Check out this repo and:&nbsp;&nbsp;```mvn clean install```

## HeavySpleef as a dependency

If you want to add HeavySpleef to your dependencies in order to develop an add-on for it, consider adding our Artifactory server to your repository list in your pom.xml:

```
<repositories>
  <repository>
    <id>matzes-repo</id>
    <url>http://matzefratze123.de/artifactory/release/</url>
  </repository>
  <!-- And so on -->
</repositories>
```

And add HeavySpleef to your dependency list to automatically download the jar and its pom.xml with child dependencies:

```
<dependencies>
  <dependency>
    <groupId>de.matzefratze123</groupId>
    <artifactId>heavyspleef-api</artifactId>
    <version>2.0</version>
    <scope>provided</scope>
  </dependency>
  <!-- And so on -->
</dependencies>
```

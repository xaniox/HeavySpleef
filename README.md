# HeavySpleef-2.4.1

This is a completely recoded and revamped version of HeavySpleef. Source code for versions below 2.0 can be found [here](https://github.com/matzefratze123/HeavySpleef-Legacy).

Spleef is a gamemode Minecraft where your goal is to knockout players by destroying the blocks under their feets. If a player touches water/lava or a so called "deathzone" he is out of the game. Last man standing wins...

HeavySpleef provides a fully automated environment for setting up Spleef games with a large variety of choosable add-ons and game "flags" which customize the game or introduce a new gamemode to Spleef.

For more information about the plugin, how to set it up and release downloads, visit the [BukkitDev homepage](http://dev.bukkit.org/bukkit-plugins/heavyspleef/) or the [Spigot resource page](https://www.spigotmc.org/resources/heavyspleef.9836/)

## Development Builds / Continuous Integration

Development builds may be accessed on Jenkins. Please note that these builds may be unstable.

[![Build Status](https://ci.xaniox.de/job/HeavySpleef/badge/icon)](https://ci.xaniox.de/job/HeavySpleef/)&nbsp;&nbsp;Visit the <a href="https://ci.xaniox.de/job/HeavySpleef/" target="_blank">Jenkins Build Server</a>

##### Addons

As you may notice there are different files available on the build server to download. Aside of the HeavySpleef 2.0 plugin file there are other jars as AddOn-InventoryGUI.jar or LeaderboardExtensions.jar. These files are add-ons for HeavySpleef 2.0 and may be dropped into the plugins/HeavySpleef/addons folder after running HeavySpleef 2.0 for at least one time. HeavySpleef will load these jars automatically and enable them once they have been placed in the mentioned folder.

Please note that add-ons are no Bukkit plugins and can't be dropped into the default folder for Bukkit plugins.

## Compilation

We use Maven 3 to handle our dependencies

* Install [Maven 3](http://maven.apache.org/download.html)
* Check out this repo and:&nbsp;&nbsp;```mvn clean install```

## HeavySpleef as a dependency

If you want to add HeavySpleef to your dependencies in order to develop an add-on or for general purposes, consider adding our Artifactory server to your repository list in your pom.xml:

```
<repositories>
  <repository>
    <id>matzes-repo</id>
    <url>http://repo.xaniox.de/artifactory/release/</url>
  </repository>
  <!-- And so on -->
</repositories>
```

HeavySpleef dependency declaration:

```
<dependencies>
  <dependency>
    <groupId>de.matzefratze123</groupId>
    <artifactId>heavyspleef-api</artifactId>
    <version>2.4.1</version>
    <scope>provided</scope>
  </dependency>
  <!-- And so on -->
</dependencies>
```

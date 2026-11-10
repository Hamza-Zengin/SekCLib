# SekCLib (SekCore Library)

<div align="center">

![SekCLib Logo](logo.png)

**A lightweight, high-performance Minecraft Forge networking and capability synchronization library.**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3.0+-orange.svg)](https://files.minecraftforge.net/)
[![Version](https://img.shields.io/badge/Version-1.1.0-blue.svg)](https://github.com/Hamza-Zengin/SekCLib/releases)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-red.svg)](https://www.oracle.com/java/)

</div>

---

## 🌟 Overview

**SekCLib** is designed to eliminate boilerplate code when developing Minecraft Forge mods, specifically around:
- **Capability Synchronization**: Sync player and entity capabilities between server and client with declarative `@Sync` annotations.
- **Network Rate Throttling**: Configure minimum tick intervals (`minTicks`) and private vs. global tracking scopes to optimize server network bandwidth.
- **Server-to-Client Motion**: Apply velocity from server side with client-side prediction and ground drag avoidance.
- **Utility Tools**: Fast color conversions, hue shifting, and humanoid mesh generators for custom renderers.

---

## 📦 Features

### 1. Declarative Capability Sync (`@Sync`)
Annotate fields in your capability class to automatically track changes and broadcast them to tracking clients:

```java
import com.sekwah.sekclib.capabilitysync.capabilitysync.annotation.Sync;

public class MyPlayerData {

    // Sync to all tracking clients with a 5-tick minimum cooldown
    @Sync(minTicks = 5, syncGlobally = true)
    private float mana = 100f;

    // Sync only to the owner player (private UI data)
    @Sync(syncGlobally = false)
    private int secretQuestProgress = 0;

    public float getMana() { return mana; }
    public void setMana(float mana) { this.mana = mana; }
}
```

### 2. Supported Trackers Out of the Box
- `int` (`IntSyncTracker`)
- `float` (`FloatSyncTracker`)
- `boolean` (`BoolSyncTracker`)
- `byte` (`ByteSyncTracker`)
- `String` (`StringSyncTracker`)
- `Vec3` (`Vec3SyncTracker`)
- `ResourceLocation` (`ResourceLocationSyncTracker`)

### 3. Custom Sync Tracker Types
Register your own serializers for complex objects in your mod:

```java
@Mod.EventBusSubscriber(modid = "your_mod_id", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSyncTrackers {

    @SubscribeEvent
    public static void registerSyncTrackers(RegisterSyncTrackerTypeEvent event) {
        event.registerSyncTracker(MyCustomData.class, new MyCustomDataSyncTracker());
    }
}
```

### 4. Player Velocity & Motion
Set entity velocity from the server without jitter or ground drag:

```java
import com.sekwah.sekclib.util.PlayerUtil;
import net.minecraft.world.phys.Vec3;

// Propel player forward and upward, ignoring immediate ground friction
PlayerUtil.setVelocity(player, new Vec3(1.2, 0.8, 1.2), true);
```

### 5. Color Utilities
```java
import com.sekwah.sekclib.util.ColorUtil;
import java.awt.Color;

Color vibrant = ColorUtil.recalculateHue(180f, 65);
int mcColorInt = ColorUtil.colorToInt(vibrant);
```

---

## 🚀 Adding SekCLib as a Dependency

### Option A: Local Jar (Recommended for FlatDir)
Place `SekCLib-1.20.1-1.1.0-deobf.jar` inside your mod's `libs/` folder:

```groovy
dependencies {
    implementation fg.deobf(files("libs/SekCLib-1.20.1-1.1.0-deobf.jar"))
}
```

### Option B: Maven / GitHub Packages
```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/Hamza-Zengin/SekCLib")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation fg.deobf("com.sekwah:SekCLib-1.20.1:1.1.0:deobf")
}
```

### `mods.toml` Dependency Declaration
```toml
[[dependencies.your_mod_id]]
    modId="sekclib"
    mandatory=true
    versionRange="[1.1.0,2.0.0)"
    ordering="AFTER"
    side="BOTH"
```

---

## 🛡️ Multiplayer Safety & Architecture

- **Packet Validation**: `ClientCapabilitySyncPacket` enforces bounds checks on tracker and capability counts, mitigating corrupted packets or malformed network injections.
- **Null Safety**: Client packets safely discard payloads if the client world is unloading or unmapped capabilities are received.
- **Throttling**: The `minTicks` property on `@Sync` protects against packet flooding on high-frequency capability mutations.

---

## 🛠️ Building from Source

```bash
git clone https://github.com/Hamza-Zengin/SekCLib.git
cd SekCLib
./gradlew build
```

The resulting jars will be placed in `build/libs/`:
- `SekCLib-1.20.1-1.1.0-universal.jar` (reobfuscated for gameplay)
- `SekCLib-1.20.1-1.1.0-deobf.jar` (deobfuscated for mod development)
- `SekCLib-1.20.1-1.1.0-sources.jar` (source code)

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).
Originally developed by Sekwah, maintained and hardened by Hamza Zengin.

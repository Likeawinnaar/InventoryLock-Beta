# InventoryLock bouwen

Installeer een **JDK 21** en controleer met `java -version` en
`javac -version` welke versie actief is. Clone deze repository en voer de
Gradle Wrapper uit vanuit de projectmap.

Windows (PowerShell):

```powershell
.\gradlew.bat build
```

Linux en macOS:

```sh
./gradlew build
```

De build voert de automatische controles uit. De JAR voor Minecraft staat in
`build/libs/inventorylock-1.0.5.jar`.

Start de ontwikkelclient met `./gradlew runClient` of
`.\gradlew.bat runClient`. Voeg `-PwithModMenu` toe om Mod Menu, Fabric API en
Placeholder API in die ontwikkelclient te laden. Zie [TESTING.md](TESTING.md)
voor de uitgevoerde tests en de punten die nog openstaan.

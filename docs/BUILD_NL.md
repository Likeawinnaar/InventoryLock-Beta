# InventoryLock bouwen zonder Java op je eigen pc

Dit is een **voorbereide** GitHub Actions-workflow. Er is nog geen
volledig bronproject in deze repository, dus er kan nog geen echte
InventoryLock-JAR worden gemaakt.

## Stappen

1. Maak de repository **privé** via
   [Settings](https://github.com/Likeawinnaar/InventoryLock-Beta/settings)
   → General → Danger Zone → Change repository visibility.
2. Voeg daarna pas de echte Fabric-projectbestanden toe, waaronder
   `build.gradle`, `settings.gradle`, `gradlew`,
   `gradle/wrapper/gradle-wrapper.jar`, de Java-bestanden en
   `src/main/resources/fabric.mod.json`.
3. Open het tabblad **Actions** en selecteer
   **Build InventoryLock (private repository only)**.
4. Kies Java 17 of Java 21 zoals passend bij je Minecraft/Fabric-project.
5. Klik op **Run workflow**.
6. Alleen bij een geslaagde build verschijnt onder **Artifacts**
   `InventoryLock-Beta-jar`, inclusief SHA-256-controlebestand.

Zolang de repository openbaar is, wordt deze bouwtaak bewust
overgeslagen. Upload geen broncode naar een openbare repository.

**Belangrijk:** een geslaagde compilatie bewijst nog niet dat de mod
in Minecraft correct werkt. Test de JAR ook in-game.

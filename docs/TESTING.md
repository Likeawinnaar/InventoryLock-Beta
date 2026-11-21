# Manual test checklist

Record one result per environment. Use **Pass**, **Fail**, **Blocked**, or **Pending**. Use **Partial** when the completed test covered only part of a row.

Record the date, commit or JAR, Minecraft and Loader versions, Java version, OS, installed mods and versions, game mode, and world or server type. For failures add the observed result, reproduction steps, and a log excerpt or screenshot. Use a disposable world and ordinary stacks of distinguishable items. Include both occupied and empty protected slots wherever applicable.

## Executed checks

18 November 2026 · Linux · OpenJDK 21.0.13 · Gradle 8.10.2 · Fabric Loader 0.16.14 · Minecraft Java Edition 1.21.1 · InventoryLock 1.0.0.

- Gradle built the remapped JAR and passed the automated range, persistence, profile isolation, malformed-file preservation, and write-failure rollback checks.
- The remapped JAR reached the title screen and loaded a local world with Fabric Loader alone. The same profile retained its locks and cyan settings across client restarts. Vanilla account services were unavailable in the test workspace.
- With Mod Menu 11.0.5, Fabric API 0.116.7+1.21.1 and Placeholder API 2.4.2+1.21, the mod appeared in the list with the supplied icon and opened Configure. The packaged build displayed Dutch settings when the game language was Dutch.
- Compared the config checksum after canceling the confirmation and after confirming then canceling settings. Both preserved the file. Confirming and pressing Done cleared the active profile's locks while retaining its display settings.

## InventoryLock 1.0.1 retest · 19 November 2026

- Built the remapped JAR on Linux with Java 21. The automated checks passed.
- Loaded the packaged JAR with Mod Menu while Minecraft was set to Dutch. The mod settings, including ON, Done, and Cancel, and the Unlock All confirmation appeared in English.
- Verified that the closed padlock drew above occupied hotbar slots and on empty and offhand slots. Toggling an empty slot off removed its padlock.

## InventoryLock 1.0.2 retest · 19 November 2026

- Built the remapped JAR on Linux with Java 21. Automated checks passed for all 16 texture paths, saving and reloading each color, and mapping legacy Gold to Yellow.
- Verified the remapped JAR contains the 16 supplied padlock PNGs and the new square-canvas logo.
- Opened Mod Menu with the packaged JAR and confirmed the gold logo and version 1.0.2 appeared. Selected Pink in the English settings and confirmed that the new sprite rendered on occupied hotbar, empty inventory, and offhand slots.

## InventoryLock 1.0.3 retest · 19 November 2026

- Built and launched the remapped JAR on Linux with Java 21. Automated checks passed for profile-specific immediate clearing, preservation of unsaved settings, persistence on fresh config load, and the 16 textures.
- While the settings screen remained open, confirming Unlock All removed the saved locks immediately and displayed a success message. A different color selected in the draft did not reach disk. Canceling the settings screen left the confirmed unlock in effect. Canceling the confirmation itself preserved the original settings file and locks.
- Verified that the new cyan texture appeared on protected, occupied and empty slots in the player inventory and in a chest view.

## InventoryLock 1.0.4 retest

- Built the packaged JAR on Linux with Java 21. Automated checks passed for separate worlds and server addresses, default-port and case normalization, profile isolation, session-only scopes, persistence, and switching back to the shared lock set.
- With the packaged client and Mod Menu, enabled both scope toggles and set different locks in two local worlds. Switching the world toggle off and back on switched between the shared set and the current world's saved set.
- Confirmed that the new hotbar indicator respects **Icon and Overlay** and **Subtle Overlay**, including over occupied and empty slots. The inventory also displayed the overlay.
- Live multiplayer and Realms connections were not exercised in this run.

## InventoryLock 1.0.5 retest

- Automated checks verified that per-server locks default to ON for fresh installs and existing configurations without a saved choice, and that explicit OFF is preserved after restart.
- Opened the packaged mod in Mod Menu with a fresh configuration. **Per-Server Locks** showed ON and **Per-World Locks** showed OFF. Switched per-server locks OFF and verified that **Done** saved the choice.

## Manual test matrix

| ID | Procedure | Expected result | Result |
| --- | --- | --- | --- |
| F01 | Run the Windows wrapper build with JDK 21. | Gradle succeeds and produces the remapped `build/libs/inventorylock-1.0.5.jar`. | Pending |
| F02 | Run the Windows wrapper client and inspect the startup log. | Main menu opens, Loader lists `inventorylock` 1.0.5, and the initializer logs once. | Pending |
| L01 | Alt + left-click an empty slot and a filled slot. Toggle the filled slot again. | Locks apply independently. Each toggle leaves the cursor and item stack unchanged. Indicator disappears on unlock. | Pass · left and right Alt, empty and occupied slots, toggling back. |
| L02 | Lock multiple positions. Close the screen, open a chest, then quit, restart, and rejoin. | The same personal positions retain their locks. Chest storage remains available. | Pass · chest and local restart using the same test profile. |
| L03 | Disable the mod, interact with a previously locked slot, then re-enable it. | Actions behave normally while disabled; saved locks apply again on re-enable. | Partial · covered by the automated config test. In-game mutation after disabling remains pending. |
| I01 | Left-click and right-click a locked stack and a locked empty slot with a carried stack. Repeat on unlocked controls. | Protected stacks cannot be picked up, split, merged into, replaced, or filled. Unlocked controls behave normally. | Partial · tried left and right clicks on occupied locks and ordinary pickup on unlocked slots. |
| I02 | Shift-click a protected source. Shift-click unlocked items with protected occupied and empty positions on the destination route. | Protected positions stay unchanged. Unsafe quick moves are canceled. Unaffected routes work. | Partial · unlocked hotbar → chest succeeded. Chest → player was blocked with an eligible empty locked position. |
| I03 | Use hotbar number keys with a locked hovered slot, then only a locked hotbar target. Test with both unlocked. | Both protected cases are blocked and the unlocked swap follows vanilla behavior. | Partial · exercised the locked hotbar destination in survival and the creative catalog. |
| I04 | Distribute by left and right drag over locked and unlocked slots, ending once inside and once outside the grid. | Protected contents and counts stay unchanged. On cancellation, the cursor and permitted later actions remain consistent. | Partial · right-drag placed items into two unlocked slots while skipping the locked slot. |
| I05 | Double-click a matching stack near matching protected stacks. Repeat with shift-double-click moves. | Protected stacks remain in place. Affected collection or batch moves are canceled safely. | Partial · double-click with eight carried matching planks left the locked stack at 64. Shift-double-click remains pending. |
| I06 | Use Q and Ctrl+Q over protected and unprotected stacks. Test single-item and stack-drop bindings in the world. Repeat with Prevent Dropping off. | Applicable protected drops are blocked when enabled. Unlocked drops and drops with the setting off follow vanilla behavior. | Partial · screen Q, screen Ctrl+Q, and in-world drops were blocked in checked cases. The disabled-preference case remains pending. |
| I07 | Use the offhand swap binding over a protected slot and with a protected selected hotbar slot outside a screen. Repeat with both sides unlocked. | Protected swaps are blocked. Unlocked swaps work. Include a protected offhand case if it is supported. | Partial · in-world F left the locked hotbar item selected and did not fill the protected offhand. |
| C01 | Repeat toggle, normal click, and quick-move checks in a chest, barrel, and shulker box. | Each view targets the same personal slot indices, and the container grid cannot be locked. | Partial · a single chest was tested. Barrel and shulker box remain pending. |
| C02 | Repeat in a furnace, crafting table, and additional vanilla screen such as a hopper or anvil. | Only supported player slots qualify. Routing tests respect each handler's inputs, outputs, and player slots. | Pending |
| G01 | Select a locked hotbar slot. Mine, eat, place blocks, draw and fire a bow, and take armor wear. | Hotbar selection and all gameplay actions work. Durability, counts, and equipment update normally. | Partial · selected a locked stack, placed a plank, and observed the count drop from 64 to 63. Mining, eating, bows, and armor wear remain pending. |
| E01 | Try normal, number-key, drop, and quick-move interactions in supported creative tabs and the player-inventory tab. | Each case follows the documented creative scope. Returning to survival preserves the same personal lock positions. | Partial · creative catalog and inventory tabs displayed hotbar locks and blocked direct edits. Full pass remains pending. |
| E02 | Repeat quick move with full and empty inventory, compatible partial stacks, and protected empty positions. | Protection holds in both merge and empty-slot paths. Behavior stays consistent after closing and reopening the screen. | Pending |
| E03 | Start with Mod Menu present, then absent. With it present, open Configure and exercise keyboard navigation. | Both starts work. Configure opens a native screen when available. | Partial · installed and absent launches verified. Full keyboard navigation remains pending. |
| E04 | Test missing JSON, malformed JSON, unknown schema, invalid settings, and a non-writable config path. | Valid defaults load where appropriate. Existing damaged data is preserved, and write failures give feedback. | Partial · missing, malformed, invalid setting and write-failure paths passed automated checks. Full in-game feedback and an unknown schema case remain pending. |
| E05 | Change settings during a game. Use Done and restart. Change them again and Cancel or Esc. | Done applies and persists settings. Cancel and Esc discard edits. | Pass · in-game modifier and overlay changes persisted, and Cancel preserved the file. |
| E06 | Cancel Unlock All, then confirm it. Test modifier choices on both sides, all overlay styles, opacity bounds, colors, and visibility. | Canceling the prompt preserves locks. Confirming clears them immediately. Only the configured modifier toggles locks. Visual settings match their selected values. | Partial · confirmation, cancellation, both Alt keys and the cyan overlay were exercised. Remaining color and opacity variations are pending. |
| E07 | Switch Minecraft between English and Dutch and inspect InventoryLock settings, confirmation, and error messages. | InventoryLock controls and messages always appear in English. | Partial · English settings and confirmation verified with Minecraft set to Dutch. Error messages remain pending. |
| E08 | Run with two launcher profiles in the same instance. Change worlds or servers with one profile, then restart. | The same profile retains its locks across worlds and servers. The other profile has its own set. | Partial · isolated profiles passed automated checks and one launcher profile retained locks after a world reload. Multi-world and server coverage remains pending. |
| M01 | Repeat core interactions on a vanilla server and Fabric server with the mod installed only on the client. Try rapid swaps, drag, and quick moves under latency, then reopen the menu and rejoin. | Server and client agree on item counts and positions. Server-driven updates still apply normally. | Pending |
| M02 | Repeat smoke and core inventory tests with Fabric API, Mod Menu, Sodium, Lithium, and selected inventory utilities, testing each addition individually. | Record observed compatibility and conflicts for each exact version and combination. | Pending |

# InventoryLock beta — release checklist

The public beta JAR and its checksum are distributed through [GitHub Releases](https://github.com/Likeawinnaar/InventoryLock-Beta/releases).

## Before creating a release

1. Keep the **original source** and source artwork in a private repository,
   with access limited to trusted maintainers.
2. Confirm the Minecraft, Java, Fabric Loader and optional Mod Menu dependency versions.
3. Use a reproducible CI workflow in the private source repository. Grant
   the workflow minimal read permissions; do not expose tokens in logs.
4. Compile the actual mod; a ZIP renamed to `.jar` is not a build.
5. Verify the output is a valid JAR containing correct `fabric.mod.json`
   and the expected mod metadata. Run Minecraft to test the release.
6. Test slot locking, movement/swap/drop protection, visual indicators,
   Mod Menu configuration, different GUI scales and restarts.
7. Check server-controlled inventory changes and mod compatibility.
8. Check licenses for all dependencies and assets. Confirm Modrinth
   distribution and AI-content requirements before uploading.
9. Create a changelog stating only tested features and known limitations.
10. Create a SHA-256 checksum for the released JAR and retain it together
    with the build provenance.
11. Publish **only** the authorized release binary and necessary public
    metadata. Do not publish deobfuscation mappings, tokens, private source
    archives or unpublished texture masters.

## Optional obfuscation

Java obfuscation may slow casual copying but cannot make an openly
distributed Fabric JAR impossible to inspect or reverse engineer. It can
also break Fabric entrypoints, Mixins, reflection, Mod Menu integration or
stack traces. Only apply it after a successful plain build, preserve
required class names and mappings, and perform full compatibility tests.

## Distribution notes

For public users, prefer the approved Modrinth release (once it exists).
Keep internal source builds and logs private. A public GitHub release of the
JAR makes that JAR publicly downloadable.

## No local administrator rights needed

A CI runner can compile a Fabric mod with Java/Gradle installed on the
runner. However, CI must first have **real source files**. Do not enable a
fake build workflow that suggests a tested mod exists.

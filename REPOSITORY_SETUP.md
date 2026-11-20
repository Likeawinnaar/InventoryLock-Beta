# Repository setup — owner checklist

**Current security goal:** protect InventoryLock's source and original assets.

## Do this first: make the source repository private

The repository was **public** when these documents were added. This GitHub
connection can edit files but cannot change repository visibility.

To change it yourself:

1. Open `https://github.com/Likeawinnaar/InventoryLock-Beta/settings`.
2. Under **General**, scroll to **Danger Zone**.
3. Choose **Change repository visibility → Make private**.
4. Read the warning and confirm the change.

GitHub may change menu labels. Making a repository private does not erase
copies someone already downloaded while it was public.

**If a public project page is needed:** keep a *separate public documentation
repository*, while the Fabric source and working assets remain in a private
source repository.

## Recommended GitHub settings

- Give access only to trusted collaborators; review roles regularly.
- Enable two-factor authentication on the GitHub account.
- Protect the `main` branch using rulesets/branch protection where
  available. Require review for changes and passing build checks once the
  source repository has a real CI workflow.
- Enable Dependabot alerts and secret scanning if GitHub offers them for
  this repository.
- Do not grant untrusted GitHub Actions workflows write access or secrets.
- Do not commit API tokens, passwords, signing keys, `.env` files,
  customer files, private artwork or machine-specific paths.
- Keep any release signing keys out of the repository.

## Suggested public project metadata

**Description:** `InventoryLock (Beta) — client-side Fabric mod to protect individual inventory and hotbar slots from accidental moves, swaps and drops.`

**Topics:** `minecraft`, `fabric`, `inventory`, `client-side`,
`mod-menu`, `slot-locking`, `beta`.

**Homepage:** set this to the verified official Modrinth project URL, once
identified. Do not use an unrelated Minecraft mod URL.

## Important limits

- An "All rights reserved" notice communicates permitted use; it does
  not prevent a file from being downloaded.
- Source obfuscation is optional, not a guarantee.
- Publicly distributed Java JARs are inspectable. If protection is a
  priority, keep source private and distribute only what is necessary.

Reference: https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/managing-repository-settings/setting-repository-visibility

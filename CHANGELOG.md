# Changelog
## [0.2.1b] - 2026-09-30
### Fixed
- Crash on launch with MixinBooter 10.x (LinkageError). Should support versions 10.2+ like intended.

## [0.2.0b] - 2026-09-28
### Added
- Releases now get uploaded to CurseForge automatically.
### Changed
- Overhauled the world shader. It now only handles the geometry (waves, wiggles, distant deformation) and leaves the pixel colouring to Minecraft, so vanilla and other mods' rendering tricks should just work instead of needing patching one at a time.
- Moved surface fractals, pulses and world colourisation into a screen effect. They should look the same as before.
- Pressing F1 now only hides the HUD, drug visuals stay on.
### Fixed
- Held item/hand disappearing during Zero's digital effect.

## [0.1.1b] - 2026-09-28
### Changed
- Split shaders into simple and advanced in the config, switched the digital effect over to advanced.
### Fixed
- Simulated texGen in the world shader itself to add back the end portal starfield. Looks like it's also helped with dimensionalDoors.
- Stopped world shader overriding other mods' shaders. Only really tested on the midnight rift, will hopefully help other mods too.
- Pause world shaders during outline mode so teh glowing outlines render.

## [0.1.0b] - 2026-08-19
### Added
- Initial beta release.
- Github workflows to validate and release.
- See commits for additions and fixes, can't be on with listing them all. Will start doing it for additions and fixes going forward.
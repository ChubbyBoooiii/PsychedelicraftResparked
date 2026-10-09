# Changelog
## [0.3.1b] - 2026-10-09
### Fixed
- Dropped blocks showing unlit and grass showing grey on top in your hand with advanced shaders on (from the Storage Drawers fix in 0.3.0b).
- Bottle Workbench now faces you when placed, instead of always facing the same way.
- Bottles displayed on the Bottle Workbench using the old bottle texture layout.

## [0.3.0b] - 2026-10-09
### Added
- Placeable drink containers: shift-right-click a Bottle, Shot Glass, Glass Chalice or Wooden Mug onto a block top to place it (empty or filled, contents kept), right-click to pick it back up, shift-right-click with an empty hand to rotate it. Several fit on one block, and you can see the drink inside.
- Bottle Workbench: crafts bottles from any glass (stained glass or dye for the colour) and labels them. Two bottle shapes, Wine and Round, plus a clear glass variant.
- Bottle labels (Band and Sticker) in any dye colour, applied with paper and dye in the workbench's labelling tab. Labels stay on through Molotov crafting, drinking and placing.
- Bottles, Molotovs, Shot Glasses, Chalices, Mugs and Barrels are now 3D items. Molotovs have a rag instead of a cork.
- Ambient visuals from the original: sun lens flare, heat distortion in hot biomes, underwater distortion, and blur noise for Power. Each has its own config option.
- Drug effects (pulses, fractals, colour contrast) now show on the sky too (config `skyDrugEffects`).
### Changed
- Bottles are no longer made in the crafting table, use the Bottle Workbench.
- Barrel model rebuilt in Blockbench.
- Drying Table now takes 3 minutes (3600 ticks) by default.
- `/druglevels`, `/spawnrift` and `/hallucinate` are now subcommands of `/psyche`.
- Config options are grouped by what they do.
### Fixed
- Bloom effect throwing away its last blur pass.
- GL 1282 error spam from a filled Rift Jar.
- Immersive Railroading (UniversalModCore) models rendering black with advanced shaders on.
- Storage Drawers blocks showing dark in drawers with advanced shaders on.

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
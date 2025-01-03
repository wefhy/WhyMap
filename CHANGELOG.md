# WhyMap changelog

## [1.7.8]
 - Updated to Minecraft 1.21
 - Fixed crash when trying to run the game without ClothConfig
 - Updated to Kotlin 2.0+ and Ktor 3.0+
 - Fixed reading some old files
 - Support name migrations (eg. `grass` -> `short_grass`)
 - Store byteorder for better file interchangeability
 - Save last tile update in metadata, don't update it when just upgrading the tile version (this will be used later for better tile merging)

## [1.7.7]
- Updated to Minecraft 1.20.5 and 1.20.6
- Fixed dimension switching
- Improve tile saving reliability
- Fixed incorrect biomes after dimension switch
- Fixed very rare crash that could happen on ARM devices when saving tiles

## [1.7.6]
 - Fixed dimension switching
 - Improve tile saving reliability
 - Fixed incorrect biomes after dimension switch
 - Fixed very rare crash that could happen on ARM devices when saving tiles

## [1.7.5]
 - Minimap won't cause crashes or memory leaks
 - Minimap is no longer experimental
 - Fixed short grass rendering

## [1.7.4]
 - Updated to 1.20.3 and 1.20.4
 - Minor shading performance improvement

## [1.7.3]
 - Updated to 1.20.2
 - You can now select color of a waypoint (from web)
 - You can now edit waypoints (from web)
 - Improved rendering of pressure plates and some other blocks

## [1.7.2]
 - Fix changelog generation, delete temporary jars

## [1.7.1]
 - Use ShadowJAR to bundle dependencies
 - Reduced JAR file from 15MB to 8MB (proguard could apparently even reduce it to 3MB but seems unnecessary for now)

## [1.7.0]
 - Update 1.20 block textures
 - Improve many blocks rendering, notably bubble columns and sweet berries
 - Update library versions
 - Add 3D map preview
 - Fixed 'analyze area'
 - Show light level in block info
 - First attempts at adding diagnostics screen, elytra flight assistance, player path history (neither is enabled yet)

## [1.6.6]
 - Update to 1.20.1
 - Fixed dependencies

## [1.6.5]
 - Update to 1.20.1

## [1.6.4]
 - Add 16x scale rendering (block texture level)

## [1.6.3]
 - Drag on the map with right mouse button to select area
 - Export area to png or jpeg (will open in new tab)

   (Currently limited to about 50M block areas (7000x7000 if square) and only with default zoom of 1 block = 1 pixel)

## [1.6.2]
 - Configurable hud color
 - Improved minimap rendering (still experimental, don't use)

## [1.6.1]
 - Disable minimap by default
 - Add a warning saying minimap will cause crashes
 - Improve hud position on the screen

## [1.6.0]
 - Fixed minimap crashes-
 - Fixed minimap memory leaks
 - You can configure map though the UI now (click `M` or find it though ModMenu)
 - Simple HUD (not configurable yet)
 - Added clothconfig optional dependency (required to configure through UI)
 - Added modmenu optional dependency

## [1.5.0]
 - Added 1.19, 1.19.1, 1.19.2 port
 - Many technical changes so that mod is ready to quickly adapt to future minecraft updates
 - Greatly reduced background CPU usage
 - Support for Terralith, Nullscape, Incendium and many other mods that add non-vanilla biomes to the game
 - Improved rendering of infested, stripped and waxed blocks
 - Add config
 - Allow only Localhost connections by default (map won't be possible to access from your wifi unless you change this from settings)
 - Minimap position and internal http server is now configurable, check out minecraft/WhyMap/config.toml file and change whatever you need (more settings to come, they will be changeable from the UI in the future)
 - Improve stability

## [1.4.0]
 - Both 1.19.3 and 1.19.4 supported
 - Minimap should display correctly on high-dpi displays
 - Improved performance, minimap should affect fps/tps only a little bit
 - Should be compatible with more mods and datapacks.
 - If a mod or datapack you use adds non-vanilla blocks to the game, additional mappings will be created in minecraft/WhyMap/mappings-custom directory. If you use non-vanilla blocks and you want to share tiles with your friends, you'll need to share this custom mappings file as well.

## [1.3.1]
 - Minimap considered kinda stable (please give feedback)
 - Minimap is now square, won't be as glitchy
 - Minimap should have much improved performance

## [1.3.0]
 - Stability improvements
 - Experimental minimap rendering (press Y to enable or change rotation, you can change this in Options -> Controls -> Key Binds on the bottom)
 - Improve barrier rendering
 - Start working on new rendering engine
 - Improve dispatching (should improve responsiveness on slower machines)

## [1.2.1]
 - add waypoint deletion
 - improve rendering of anvil, iron bars, brewing stand
 - run on next available port if default port is taken
 - fix 'Invalid file path' error
 - improve performance
 - implement force wipe cache
 - show deaths

## [1.1.1]
 - Improve rendering of chests, composters, hoppers, redstone, lanterns, shulkerboxes, tripwires, glass
 - Water and water plants rendering overhaul

## [1.1.0]
 - add new waypoints from game (default keybinding: B, you can change it in Options -> Controls -> KeyBinds on the bottom)
 - Improve glass rendering
 - correctly set height when setting waypoint from the map
 - dimension detection on servers
 - add center on coordinates (type coordinates in search box)

## [1.0.3-1.19.3] RC
 - Fix unnecessary tile reloads

## [1.0.2-1.19.3] RC
 - fix real time updates that were broken by dimension changes in 1.0.1 RC

## [1.0.1-1.19.3] RC
 - support reload on dimension changes
 - reduce possibility of crash on dimension change

## [1.0.0-1.19.3] RC
First Release Candidate version!
 - improve rendering of certain blocks (most notably bamboo)
 - real time updates of regular size tiles (won't work on detail or zoom out yet)
 - follow player options
 - more settings on frontend
 - hype

## [0.9.3-1.19.3] beta
 - fix xaero waypoints parsing
 - update dependencies
 - 1.19.3 compatibility
 - added new 1.19 textures (now mangrove etc should display correctly on zoom)
 - improve certain block rendering (cobwebs, dripstone, cactus and a few more)

## [0.9.2-1.19] beta
 - Updated to 1.19
 - Added block ID migrations to keep compatibility with tiles generated by 1.18 versions of the mod
 - Sorting waypoints by distance to player
 - Showing distance to player in context menu
 - Better tile data error handling

## [0.9.1] beta
 - Fixed all chunk corruption errors
 - Fixed front-end crashes
 - Improved rendering of multiple blocks (mostly plants)
 - Show message to the player "See your map at http://localhost:7542"

## [0.8.1] alpha
 - Hotfix: tiles saving was sometimes broken due to new memory management
 - Fixed leaves rendering near water and lava

## [`0.8`] alpha 
Backend technologies:

    Fabric API
    Fabric Language Kotlin
    Ktor + CIO

Frontend technologies:

    LeafletJS
    Vue.JS

Features

    Web frontend
    Display map at multiple zoom levels
    Display block textures when zoomed in
    Display shading and biome colors
    Display top of the nether roof with biome colors
    Separate map for every world and dimension
    Xaero waypoints importing
    Creating waypoints from web
    Sorting waypoints
    Searching waypoints
    Centering on waypoints
    Showing player position
    Showing hover position
    Show right-clicked block data

KNOWN BUGS

    Error handling is not done. So if you for example upload faulty xaero waypoints file, you'll get no error message.
    If you click any waypoint or right-click on minimap, it causes something on the frontend to crash next time you zoom around. It'll cause zoomed-in tiles to not unload when you zoom out. And some waypoints may behave weirdly. Refresh the page if it happens.
    In some rare circumstances tiles can get corrupted
    Biomes are approximate

Current (temporary) limitations:

    Not tested for long distance travel (did fine for traveling like 20k away, both ways without huge memory usage)
    Not tested for using multiple accounts at the same time (you'll definitely be able to access only one from web, not sure about data corruption if you load the same region from both accounts)
    Most of the map won't update itself in real time. You need to reload the page (F5)
    Map doesn't save your view location when reloaded, it'll center on player again
    No proper error reporting
    Can't delete waypoints from map
    Can't browse not currently logged in worlds
    You can change waypoint color only though the waypoints.txt file in WhyMap folder
    No changable settings
    Birch leaves are tinted like regular leaves
    New waypoints are created at y60

NOT PLANNED features as of now:

    It can't find xaero waypoints itself, you need to import them from web
    No way to import xaero or voxel tiles
    Can't display map in game



[1.7.8]: https://github.com/wefhy/WhyMap/compare/1.7.8..1.7.7
[1.7.7]: https://github.com/wefhy/WhyMap/compare/1.7.7..1.7.6
[1.7.6]: https://github.com/wefhy/WhyMap/compare/1.7.6..1.7.5
[1.7.5]: https://github.com/wefhy/WhyMap/compare/1.7.5..1.7.4
[1.7.4]: https://github.com/wefhy/WhyMap/compare/1.7.4..1.7.3
[1.7.3]: https://github.com/wefhy/WhyMap/compare/1.7.3..1.7.2
[1.7.2]: https://github.com/wefhy/WhyMap/compare/1.7.2..1.7.1
[1.7.1]: https://github.com/wefhy/WhyMap/compare/1.7.1..1.7.0
[1.7.0]: https://github.com/wefhy/WhyMap/compare/1.7.0..1.6.6
[1.6.6]: https://github.com/wefhy/WhyMap/compare/1.6.6..1.6.5
[1.6.5]: https://github.com/wefhy/WhyMap/compare/1.6.5..1.6.4
[1.6.4]: https://github.com/wefhy/WhyMap/compare/1.6.4..1.6.3
[1.6.3]: https://github.com/wefhy/WhyMap/compare/1.6.3..1.6.2
[1.6.2]: https://github.com/wefhy/WhyMap/compare/1.6.2..1.6.1
[1.6.1]: https://github.com/wefhy/WhyMap/compare/1.6.1..1.6.0
[1.6.0]: https://github.com/wefhy/WhyMap/compare/1.6.0..1.5.0
[1.5.0]: https://github.com/wefhy/WhyMap/compare/1.5.0..1.4.0
[1.4.0]: https://github.com/wefhy/WhyMap/compare/1.4.0..1.3.1
[1.3.1]: https://github.com/wefhy/WhyMap/compare/1.3.1..1.3.0
[1.3.0]: https://github.com/wefhy/WhyMap/compare/1.3.0..1.2.1
[1.2.1]: https://github.com/wefhy/WhyMap/compare/1.2.1..1.1.1
[1.1.1]: https://github.com/wefhy/WhyMap/compare/1.1.1..1.1.0
[1.1.0]: https://github.com/wefhy/WhyMap/compare/1.1.0..1.0.3-1.19.3
[1.0.3-1.19.3]: https://github.com/wefhy/WhyMap/compare/1.0.3-1.19.3..1.0.2-1.19.3
[1.0.2-1.19.3]: https://github.com/wefhy/WhyMap/compare/1.0.2-1.19.3..1.0.1-1.19.3
[1.0.1-1.19.3]: https://github.com/wefhy/WhyMap/compare/1.0.1-1.19.3..1.0.0-1.19.3
[1.0.0-1.19.3]: https://github.com/wefhy/WhyMap/compare/1.0.0-1.19.3..0.9.3-1.19.3
[0.9.3-1.19.3]: https://github.com/wefhy/WhyMap/compare/0.9.3-1.19.3..0.9.2-1.19
[0.9.2-1.19]: https://github.com/wefhy/WhyMap/compare/0.9.2-1.19..0.9.1
[0.9.1]: https://github.com/wefhy/WhyMap/compare/0.9.1..0.8.1
[0.8.1]: https://github.com/wefhy/WhyMap/compare/0.8.1..`0.8`
[`0.8`]: https://github.com/wefhy/WhyMap/releases/tag/`0.8`
# WhyMap
[![GitHub](https://img.shields.io/github/license/wefhy/whymap?style=flat-square)](LICENSE)
[![Mod version](https://img.shields.io/badge/dynamic/json?label=Version&query=%24%5B%3F%28%40.featured%3D%3Dtrue%29%5D.name&url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fwhymap%2Fversion&style=flat-square)](https://modrinth.com/mod/whymap/versions)
[![Minecraft Version](https://img.shields.io/badge/dynamic/json?label=Minecraft%20Versions&query=%24.game_versions&url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fwhymap&style=flat-square)](#actively-supported-versions)



See interactive web demo on: https://wefhy.github.io/whymap-demo/map.html

You can zoom in close enough to see the textures of blocks.
It's old 0.8 version from September 2022 but should give you hint of what it feels like to browse the map in the browser.

High performance minecraft world map with web-based interface and infinite zoom.
Your map will be presented at http://localhost:7542

## Download the mod from:

[![Modrinth Download](https://img.shields.io/modrinth/dt/whymap?style=for-the-badge&label=Modrinth&labelColor=white&logo=modrinth)](https://modrinth.com/mod/whymap)
[![Github Download](https://img.shields.io/github/downloads/wefhy/whymap/total?style=for-the-badge&label=Github&labelColor=333333&color=darkgreen&logo=github)](https://github.com/wefhy/WhyMap/releases)
[![Curseforge Download](https://img.shields.io/badge/dynamic/json?label=CurseForge&query=%24.downloads.total&suffix=%2B&url=https%3A%2F%2Fapi.cfwidget.com%2F815690&style=for-the-badge&color&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/whymap)

### Join discord for support:

[![Discord](https://img.shields.io/badge/Discord-white?style=for-the-badge&logo=discord)](https://discord.gg/swfXTSvEVC)

## How to use it?
 - Install the mod and required dependencies ([Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) and [Fabric API](https://modrinth.com/mod/fabric-api))
 - All keyboard shortcuts can be configured in minecraft keybind settings
 - `Y` - toggle minimap
 - `M` - configuration (requires [Cloth Config](https://modrinth.com/mod/cloth-config))
 - `B` - create new waypoint
 - Open your browser at http://localhost:7542 to browse the map
 - Zoom in to see block textures
 - Click right mouse button on the map to see more options
 - Click on the waypoint to center the map on it (after you add waypoints)
 - You can search waypoints or coordinates

### Actively supported versions:
 - **1.20.3, 1.20.4**

### Other supported versions:
 - 1.20, 1.20.1, 1.20.2
 - 1.19, 1.19.1, 1.19.2, 1.19.3, 1.19.4
 - 1.16.5

## FAQ
#### How to use it?
 - Browse your map on http://localhost:7542
#### How to add waypoints?
 - You can import xaero's waypoints from file though browser
 - Press `B` in game to create new waypoint
 - Right-click on the map to create new waypoint
#### Is the mod uploading my world to the internet?
 - No, the mod is not uploading your world to the internet. It's running a local web server on your computer.
#### Who can access my map?
 - The map can only be accessed from your computer.
 - If you change it in the settings, the map can also be available to other computers and phones on your local network.
#### So how does it work?
 - The mod is using your browser to display files that are stored on your computer.
 - The mod creates a local web server that shows the tiles to your computer.
 - The mod is using [Leaflet](https://leafletjs.com/) to display the map.
 - Nothing goes online. All data is stored in WhyMap folder in your minecraft directory. You can freely delete it or move it to another computer.
#### Can I browse the map on my phone?
 - In order to browse the map on your phone, you need to change the settings of web server from `LOCALHOST_ONLY` to `EVERYWHERE`
 - Then you can browse the map on your phone by going to `http://[YourComputerLocalIp]:7542` (assuming you're connected to the same wifi)
#### How is WhyMap better than other map mods?
 - Incredibly detailed map that shows block textures when zoomed in.
 - High performance and doesn't lag when zoomed out.
 - Access your maps even when minecraft is not launched (coming soon).
 - It's fully open source.
 - You can browse the map on your phone.
#### Can I use it with Quilt?
 - I only actively support Fabric, however it should work with Quilt.
 - In order to run it with Quilt, you need to install [Quilted Fabric API](https://modrinth.com/mod/qsl) and [Quilt Kotlin Libraries](https://modrinth.com/mod/qkl) instead of Fabric API and Fabric Language Kotlin.
#### How big is the project?
- ![GitHub top language](https://img.shields.io/github/languages/top/wefhy/whymap?style=flat-square&logo=kotlin)
- ![Lines of code](https://img.shields.io/tokei/lines/github/wefhy/WhyMap?style=flat-square)


## Building from sources

First install yarn packages for frontend:

```bash
cd src-vue
yarn install
cd ..
```

To run the mod:

```bash
gradle runClient
```

To compile `jar` file:

```bash
gradle build
```

## License

[GPL-v3](LICENSE)

## Contributions
**I DO NOT accept contributions to this repository at this moment. Pull requests will be rejected.**

If you wish to help with frontend development, [frontend repository](https://github.com/wefhy/WhyMap-frontend) is open for contributions (or even a complete rewrite because I'm bad at frontend coding)

<!--suppress HtmlDeprecatedTag, XmlDeprecatedElement -->
<center><img alt="mod preview" src="https://cdn.modrinth.com/data/Y5Ve4Ui4/images/14513bf9172fa0d058e9486958de4884408ed4e4.png" /></center>

<center>
A hand-drawn client-side world map with biomes, structures, waypoints, and less!<br/>
A rewrite of <a href="https://modrinth.com/mod/antique-atlas">Antique Atlas</a> by <a href="https://github.com/Hunternif">Hunternif</a>, as continued by <a href="https://github.com/Kenkron">Kenkron</a>, <a href="https://github.com/asiekierka">asie</a>, and <a href="https://github.com/tyra314">tyra314</a>.<br/>
Utilizes <a href="https://modrinth.com/mod/surveyor">Surveyor Map Framework</a>.
<b>Requires <a href="https://modrinth.com/mod/connector">Connector</a> and <a href="https://modrinth.com/mod/forgified-fabric-api">FFAPI</a> on forge.</b><br/>
<i>Colloquially: Tinkerer's Atlas / antique-atlas</i>
</center>

---

Press **[M]** at any time to bring up the world map.

Drag the map to pan, scroll to zoom, and use the bookmark buttons to create, remove, or hide waypoints.

### Basic Features

- The map is rendered using hand-drawn "tiles" representing terrain, biomes, and structures.
- At their smallest, tiles represent an entire chunk - no peeking for resources or player bases!
- Totally server-optional - installing AA (or just [surveyor](https://modrinth.com/mod/surveyor)) on the server enables structures and sharing.
- Structures only appear on the map after you've stood in or looked at them in-game:

![structure discovery](https://cdn.modrinth.com/data/Y5Ve4Ui4/images/86054c7949fed59341cef60d0d9f27aee86ae6ef.gif)

- Markers for waypoints come in a variety of styles:

![marker styles](https://cdn.modrinth.com/data/Y5Ve4Ui4/images/f22a29ce8a00847e5a49d74d4c32f7b076a57692.png)

- Markers are automatically added for notable structures, player graves, and active nether portals:

![structure markers](https://cdn.modrinth.com/data/Y5Ve4Ui4/images/190cc4eaa2e8784dd0f46bee9c225228a05f191a.png)

- Map exploration can be shared with `/surveyor share [player]`, also revealing their position on the map:

![map sharing](https://cdn.modrinth.com/data/Y5Ve4Ui4/images/4422049c395a856c35bbc361c52e8bcd30e89523.png)

- All of the above is data-driven using resource packs.

#### Stylized Design

Antique Atlas is designed to feel like your own explorer's memory, or a permanent journal.</br>

- You can only see what you've seen before.
- You can't "lose" your map in any way.
- Flavour text is written subjectively:

![grave style euphemisms](https://cdn.modrinth.com/data/Y5Ve4Ui4/images/c6f5e20bcef2c26c40390e888e540dcdd89a1818.png)

#### Don't Play the Map

Antique Atlas is designed to keep your focus on your surroundings, not the map.

- The best view of the world is in-game - not the atlas!
- The atlas stays on its own easily-accessible screen, no distracting minimap.
- If you're always using your map as a compass, try [PicoHUD](https://modrinth.com/mod/picohud) as well.

---

### Configuration

Antique Atlas can be configured from `config/antique-atlas.toml`<br/>

`fullscreen` can be disabled to lock the size of the map screen based on your GUI scale.<br/>
`mapScale` can be adjusted to change the effective GUI scale of the tiles on the map.<br/>
`structureMarkers` can be edited to toggle markers for structures - this is automatically populated from the respack.<br/>
`graveStyle` will change the icon and tooltip for player graves - try each out to suit your pack's aesthetics.<br/>

### Resource Packs

> For more information, check out the [resource pack tutorial](https://github.com/sisby-folk/antique-atlas/wiki/Resource-Packs).

By default, Antique Atlas will use biome tags to approximate a builtin texture for any modded biomes.

To improve this, add `namespace/atlas/biome/path.json` with `{ "parent": "minecraft:biome" }` or [custom texturing](https://github.com/sisby-folk/antique-atlas/blob/1.20/src/main/resources/assets/minecraft/atlas/biome/badlands.json).

Structures can be similarly [tiled](https://github.com/sisby-folk/antique-atlas/blob/1.20/src/main/resources/assets/minecraft/atlas/structure/piece/jigsaw/single/pillager_outpost/watchtower.json) or [marked](https://github.com/sisby-folk/antique-atlas/blob/1.20/src/main/resources/assets/minecraft/atlas/structure/type/ocean_monument.json) - provided you're familiar with each type of structure identifier.

Tile textures are loaded from `textures/atlas/tile` and use an _autotile_-like format.<br/>
You can also add an [mcmeta file](https://github.com/sisby-folk/antique-atlas/blob/1.20/src/main/resources/assets/antique_atlas/textures/atlas/tile/structure/fortress/nether/nether_fortress_bridge_crossing.png.mcmeta) to adjust which other textures should "connect" and vice-versa.

#### Markers

Marker textures are loaded from `textures/atlas/marker` and are 32x32 by default.<br/>
You can similarly add an [mcmceta file](https://github.com/sisby-folk/antique-atlas/blob/1.20/src/main/resources/assets/antique_atlas/textures/atlas/marker/structure/end_city.png.mcmeta) to adjust size, offsets, and any custom mip levels.<br/>
To add a player-placeable marker, put the texture in the `custom/` subfolder.

To automatically mark a non-structure point of interest, use Surveyor's Landmark API - Like in [Surveystones](https://modrinth.com/mod/surveystones).

Textures for landmark types will be used automatically when named `namespace/textures/atlas/marker/landmark/type/path.png`. 

---

### Version History

This is a loveletter rewrite - we want to revitalize interest in antique atlas, and make it easier for others to maintain and add to.

`0.x` uses arch, keeps the original `antiqueatlas` ID, and should be save/network/API-compatible with [tyra's port](https://modrinth.com/mod/antique-atlas), sans the atlas item.

`1.x` uses fabric, and is API-incompatible with 0.x.

`2.x` uses fabric and [Surveyor](https://modrinth.com/mod/surveyor) - and is save, API, network, and respack-incompatible with older versions.<br/>
When upgrading to 2.x, map exploration and markers will be cleared - downgrade and take notes if needed!<br/>

---

### Afterword

All mods are built on the work of many others.

The art for antique atlas was created by [Hunternif](https://github.com/Hunternif) ([DA](https://www.deviantart.com/hunternif)) and [lumiscosity](https://github.com/lumiscosity) ([Neocities](https://lumiscosity.neocities.org/)).<br/>
[Click here](https://github.com/sisby-folk/antique-atlas/blob/1.20/credits.txt) for detailed art credit.

This mod is a fourth-gen rewrite, and relies heavily on contributions of many developers and artists before us.<br/>
We can't draw autotile to save our lives - feel free to [contribute](https://github.com/sisby-folk/antique-atlas/issues?q=is%3Aissue+is%3Aopen+label%3Atexturing)!

This mod is included in [Tinkerer's Quilt Plus](https://modrinth.com/modpack/tinkerers-quilt) - our modpack about rediscovering vanilla.

We're open to suggestions for how to implement stuff better - if you see something wonky and have an idea - let us know.

---

<center>
<b>Tinkerer's:</b> <a href="https://modrinth.com/modpack/tinkerers-quilt">Quilt</a> - <a href="https://modrinth.com/mod/tinkerers-smithing">Smithing</a> - <a href="https://modrinth.com/mod/origins-minus">Origins</a> - <a href="https://modrinth.com/mod/tinkerers-statures">Statures</a> - <a href="https://modrinth.com/mod/picohud">HUD</a><br/>
<b>Loveletters:</b> <a href="https://modrinth.com/mod/inventory-tabs">Tabs</a> - <i>Atlas</i> - <a href="https://modrinth.com/mod/portable-crafting">Portable Crafting</a> - <a href="https://modrinth.com/mod/drogstyle">Drogstyle</a><br/>
<b>Others:</b> <a href="https://modrinth.com/mod/switchy">Switchy</a> - <a href="https://modrinth.com/mod/crunchy-crunchy-advancements">Crunchy</a> - <a href="https://modrinth.com/mod/starcaller">Starcaller</a><br/>
</center>

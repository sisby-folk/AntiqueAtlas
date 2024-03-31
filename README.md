<!--suppress HtmlDeprecatedTag, XmlDeprecatedElement -->
<center><img alt="mod preview" src="https://cdn.modrinth.com/data/Y5Ve4Ui4/images/278bd4bd11e1bfca43de4b41969aacfbe7acfc5b.png" /></center>

<center>
A hand-drawn client-side world map with biomes, structures, waypoints, and less!<br/>
A rewrite of <a href="https://modrinth.com/mod/antique-atlas">Antique Atlas</a> by Hunternif, as continued by Kenkron, asiekierkierka, and tyra314.<br/>
Utilizes <a href="https://modrinth.com/mod/surveyor">Surveyor Map Framework</a>. Requires <a href="https://modrinth.com/mod/connector">Connector</a> and <a href="https://modrinth.com/mod/forgified-fabric-api">FFAPI</a> on forge.<br/>
<i>Colloquially: Tinkerer's Atlas / antique-atlas</i>
</center>

---

Press **[M]** at any time to bring up the world map.

Drag the map to pan, scroll to zoom, and use the bookmark buttons to create, remove, or hide waypoints.

### Basic Features

- The map is rendered using hand-drawn "tiles" representing terrain, biomes, and structures.
- At their smallest, tiles represent an entire chunk - no peeking for resources or player bases!
- Totally server-optional - installing AA (or just [surveyor](https://modrinth.com/mod/surveyor)) on the server only enables structures.
- Structures only appear on the map after you've stood in or looked at them in-game:

![structure discovery](https://cdn.modrinth.com/data/Y5Ve4Ui4/images/5dc0df1ef749b666bfd86140133b7c14c5193954.gif)

- Markers for waypoints come in a variety of styles:

![marker styles](https://cdn.modrinth.com/data/Y5Ve4Ui4/images/88f10f9321b2c2d0e082f7b2813fa5ffd59de9f3.png)

- Markers are automatically added for notable structures, player graves, and active nether portals:

![structure markers](https://cdn.modrinth.com/data/Y5Ve4Ui4/images/30e49ca4f2d36d422de13efcae88e7a9c2c9a5c9.png)

- All of the above is data-driven using resource packs.

#### Stylized Design

Antique Atlas is designed to feel like your own explorer's memory, or a permanent journal.</br>

- You can only see what you've seen before.
- You can't "lose" your map in any way.
- Death markers have a sense of humour.

![grave style euphemisms](https://cdn.modrinth.com/data/Y5Ve4Ui4/images/199d515c53c2a984eaf21bdc542fb6834c2770ff.png)

#### Don't Play the Map

Antique Atlas is designed to keep your focus on your surroundings, not the map.<br/>
Learning to navigate your surroundings and explore a minecraft world by eye is a lot of fun!<br/>

- The best view of the world is in-game - not the atlas!
- The atlas stays on its own easily-accessible screen, no distracting minimap.
- If you're always using your map as a compass, try [PicoHUD](https://modrinth.com/mod/picohud) as well.

---

### Configuration

Antique Atlas can be configured from `config/antique-atlas.toml`<br/>

`structureMarkers` can be edited to toggle markers for structures - this is automatically populated from the respack.<br/>
`graveStyle` will change the icon and tooltip for player graves - try each out to suit your pack's aesthetics.<br/>
`fullscreen` can also be enabled at a performance cost.

### Resource Packs

By default, Antique Atlas will use biome tags to approximate a builtin texture for any modded biomes.

To improve this, add `namespace/atlas/biome/path.json` with `{ "parent": "minecraft:biome" }` or [custom texturing](https://github.com/sisby-folk/antique-atlas/blob/1.20/src/main/resources/assets/minecraft/atlas/biome/badlands.json).

Structures can be similarly [tiled](https://github.com/sisby-folk/antique-atlas/blob/1.20/src/main/resources/assets/minecraft/atlas/structure/piece/jigsaw/single/pillager_outpost/watchtower.json) or [marked](https://github.com/sisby-folk/antique-atlas/blob/1.20/src/main/resources/assets/minecraft/atlas/structure/type/ocean_monument.json) - provided you're familiar with each type of structure identifier.

Textures in these files are loaded from `textures/gui/tiles` and use an _autotile_-like format.<br/>
You can also add an [mcmeta file](https://github.com/sisby-folk/antique-atlas/blob/1.20/src/main/resources/assets/antique_atlas/textures/gui/tiles/structure/fortress/nether/nether_fortress_bridge_crossing.png.mcmeta) to adjust which other textures should "connect" and vice-versa.

#### Markers

Textures for markers are loaded from `textures/gui/markers` and are 32x32 by default.<br/>
You can similarly add an [mcmceta file](https://github.com/sisby-folk/antique-atlas/blob/1.20/src/main/resources/assets/antique_atlas/textures/gui/markers/structure/end_city.png.mcmeta) to adjust size, offsets, and any custom mip levels.

To automatically mark a new non-structure point of interest, use Surveyor's [Landmark API](https://modrinth.com/mod/surveyor).

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

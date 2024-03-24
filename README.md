<!--suppress HtmlDeprecatedTag, XmlDeprecatedElement -->
<center><img alt="mod preview" src="https://cdn.modrinth.com/data/Y5Ve4Ui4/images/2196fd4a24aad1d58bd282c6d8e09bdfe0d102e8.png" /></center>

<center>
An always-accessible abstract world map.<br/>
A rewrite of <a href="https://modrinth.com/mod/antique-atlas">Antique Atlas</a> by Hunternif, as continued by Kenkron, asiekierkierka, and tyra314.<br/>
Utilizes <a href="https://modrinth.com/mod/surveyor">Surveyor Map Framework</a>. Requires <a href="https://modrinth.com/mod/connector">Connector</a> and <a href="https://modrinth.com/mod/forgified-fabric-api">FFAPI</a> on forge.<br/>
<i>Colloquially: Tinkerer's Atlas / antique-atlas</i>
</center>

---

Press **[M]** at any time to bring up the world map.

The map is abstract, with custom tiles and markers representing terrain, biomes, structures, and waypoints.

---

### Abstract Landscapes

At their smallest, tiles represent an entire chunk - via its biome, elevation, and any fluid or structure features.</br>
Markers for points of interest (e.g. lit nether portals) only appear once players have explored a chunk.<br/>
This means players can't "peek" at the map for caves, structures, resources, or player bases.<br/>

### Lay of the Land

The Atlas is less of a satellite view of the world, and more like an explorer's memory of where they've been.<br/>
Exploration progress is not lost on death, and the atlas doesn't occupy a physical slot.<br/>

### Don't Play the Map

Learning to navigate your surroundings and explore a minecraft world by eye is a lot of fun.<br/>
A lot of minimaps are a little _too_ good at precisely representing the world - and players miss out on that fun.
Because Antique Atlas is abstract and on its own screen, it's easier to focus on the world instead.<br/>
If that's not enough and you (like us) keep using the map as a compass, try [PicoHUD](https://modrinth.com/mod/picohud) as well.

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
You can also add an [.mcmeta file](https://github.com/sisby-folk/antique-atlas/blob/1.20/src/main/resources/assets/antique_atlas/textures/gui/tiles/structure/fortress/nether/nether_fortress_bridge_crossing.png.mcmeta) to adjust which other textures should "connect" and vice-versa.

#### Marker/Landmark API

Check out the [Surveyor](https://modrinth.com/mod/surveyor) readme for landmark integrations!

---

### Version History

This is a loveletter rewrite - we want to revitalize interest in antique atlas, and make it easier for others to maintain and add to.

`0.x` uses arch, keeps the original `antiqueatlas` ID, and should be save/network/API-compatible with [tyra's port](https://modrinth.com/mod/antique-atlas), sans the atlas item.

`1.x` uses fabric, and is API incompatible.

`2.x` uses fabric and [Surveyor](https://modrinth.com/mod/surveyor) - and is save, API, network, and respack-incompatible with older versions.<br/>
When upgrading to 2.x, map exploration and markers will be cleared - downgrade and take notes if needed!<br/>

---

### Afterword

All mods are built on the work of many others.

The art for antique atlas was created by [Hunternif](https://github.com/Hunternif) ([DA](https://www.deviantart.com/hunternif)) and [lumiscosity](https://github.com/lumiscosity) ([Neocities](https://lumiscosity.neocities.org/)).<br/>
[Click here](https://github.com/sisby-folk/antique-atlas/blob/1.20/credits.txt) for detailed art credit.

This mod is a fourth-gen offshoot, and relies heavily on contributions of many developers and artists before us.<br/>
[We can't draw autotile to save our lives](https://github.com/AntiqueAtlasTeam/AntiqueAtlas/wiki/Editing-Textures) - feel free to contribute!

This mod is included in [Tinkerer's Quilt Plus](https://modrinth.com/modpack/tinkerers-quilt) - our modpack about rediscovering vanilla.

We're open to suggestions for how to implement stuff better - if you see something wonky and have an idea - let us know.

<!--suppress HtmlDeprecatedTag, XmlDeprecatedElement -->
<center><img alt="mod preview" src="https://cdn.modrinth.com/data/Y5Ve4Ui4/images/2196fd4a24aad1d58bd282c6d8e09bdfe0d102e8.png" /></center>

<center>
An always-accessible abstract world map.<br/>
A shredded up port of <a href="https://modrinth.com/mod/antique-atlas">Antique Atlas</a> by hunternif, as continued by kenkron, asiekierkierka, and tyra314.<br/>
<i>Colloquially: Tinkerer's Atlas / Lay of the Land / antique-atlas</i>
</center>

---

Press **[M]** at any time to bring up the world map.

The map is an abstracted view of the world, with tiles representing biomes and structures.

It can be freely zoomed in and out to see as few as 4 chunks across, or as many as 400 chunks across.

Locations can be marked on the map using a selection of icons, and optionally a custom name.

### Design

<details>
<summary>Click to expand design notes</summary>

We didn't make this mod, but we do have opinions about it.

#### Abstract Landscapes

Tiles can reflect the biome, elevation, and water/lava content of that chunk. Structures will only appear once visited.<br/>
Because individual blocks are not reprseented, this means players can't "peek" at the map for caves, structures, resources, or player bases.<br/>

#### Lay of the Land

The Atlas is less of a satellite view of the world, and more like an explorer's memory of where they've been.<br/>
Exploration progress is not lost on death, and the atlas doesn't occupy a physical slot.<br/>
You can think of this as the player simply redrawing the map from memory - or that the atlas is less of a _physical_ object, and more of a representation of what the player already knows.

#### Personal Rambles: Don't play the map

We're bad at navigation in Minecraft - plain and simple. We get turned around while climbing mountains and wander for ages in the wrong direction.

Despite this, learning to navigate a procedurally generated world is really fun. Recognizing the shape of a hill or river between a base and a nearby village and being able to travel by eye - that's very satisfying.<br/>

The problem is often that minimaps, world maps, waypoint compasses, and even vanilla maps - are often _too_ good at helping to navigate from point A to B.</br>
We spend the entire time making sure we're aligned _exactly_ towards our destination, and miss out on the learning the route, admiring the landscape, and finding new locations in the process!

The atlas is pretty, but just bad enough at being a map to stop us from opening it every five seconds.

If that's not enough and you (like us) keep opening the map to use as a compass, try [PicoHUD](https://modrinth.com/mod/picohud) as well.

</details>

### Compatibility

`0.8.x` and `0.9.x` have a similar architecture to Antique Atlas for 1.16-1.18.<br/>
These are itemless ports to 1.19 and 1.20 as-is. They still have bugs, and _won't_ be updated.<br/>

`1.x` Uses the ID `antique-atlas`. Existing addons will not work, and the API should be considered unstable.<br/>
This is a work-in-progress modernization of the codebase on fabric 1.20<br/>
Further opinionated changes will be made as we refactor the mod - such as removing the pan buttons.

If `2.x` releases - that means we've broken the save format.<br/>
This would (possibly) be a partial rewrite of the mod to operate primarily on the client side.<br/>
The mod would still need to be on the server in order to identify visited structures, or share maps.

### Afterword

All mods are built on the work of many others.

This mod specifically is a fourth-gen offshoot, and relies heavily on the excellent tile art by Hunternif as well as the code contributions of many developers before us.<br/>[We can't draw autotile to save our lives](https://github.com/AntiqueAtlasTeam/AntiqueAtlas/wiki/Editing-Textures) - feel free to contribute!

This mod is included in [Tinkerer's Quilt Plus](https://modrinth.com/modpack/tinkerers-quilt) - our modpack about rediscovering vanilla.

We're open to suggestions for how to implement stuff better - if you see something wonky and have an idea - let us know.

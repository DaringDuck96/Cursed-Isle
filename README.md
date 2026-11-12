# Cursed Tides (Fabric 1.21.1), version 0.20.0

An original pirate adventure built around four pillars: **sailing, ship ownership,
island exploration and character progression**. Everything in it (islands, characters,
story, art) is original.

## Build it

You need **Java 21** (the JDK, e.g. from adoptium.net).

1. Unzip this folder.
2. Open a terminal in it and run `gradlew.bat build` (Windows) or `./gradlew build` (Mac/Linux).
3. The first build downloads Minecraft and Fabric (a few minutes).
4. Your mod is at `build/libs/cursed-tides-0.20.0.jar` (ignore the `-sources` jar).

Put that jar plus **Fabric API** for 1.21.1 into `.minecraft/mods`. To test without installing: `./gradlew runClient`.

Every push to `main` is also built by GitHub Actions (`.github/workflows/build.yml`), which starts a test server in the archipelago for a minute. The finished jar and the logs land on the `build-output` branch.

## Playing the Cursed Archipelago

Create a new world, click **World Type** until it says **Cursed Archipelago**, and create it.
Every Cursed Archipelago world has the same hand-placed islands, whatever the seed.
You start on Driftwood Island.

### The Shattered Reach

| Island | Where (x, z) | What's there |
|---|---|---|
| Driftwood Island | 0, 0 | Castaway start: lighthouse, Keeper Wren's shack, pier and boat, a wrecked raft |
| Saltmarrow | 230, -30 | The freeport: market square and well, tavern with rooms, harbor house, shipyard, houses, long pier, two Quartermasters |
| The Sunken Reach | 70, 240 | Coral reef with a drowned belltower (guarded) and a shipwreck |
| Ashfall Island | -210, 150 | Volcano with a lava crater and black sand beaches, the smugglers' forge, Tamsin Coil's hut |
| Hollowgrave Island | -110, -250 | Dead trees, a cemetery with buried treasure, and Thorne's temple |
| Ironwatch Isle | 430, 170 | The Tidewatch fortress: curtain walls, gun bastions, barracks, a brig, the Commodore's hall, a marine garrison and a navy pier |
| Mirefen Bayou | -320, -40 | Mangrove swamp of mud flats and black-water pools: Hookline Landing, a stilt village on boardwalks, with a trader, a pier and frogs |
| Port Meridian | 420, -200 | The Tidewatch colonial capital: Governor Harrow's mansion, a chapel, a plaza with roads and traders, marine guards |
| Blackthorn Isle | 150, -330 | Captain Rook Varga's pirate hideout: palisade camp, Tilda Crook the fence, a card table |
| Emerald Cay | -60, 420 | Dense jungle around a stepped temple; Ibbie Crane's naturalist camp |
| Whispering Key | -420, -280 | A haunted mansion where a shade walks; Sorrel the medium's hut |
| Goldwater Cay | 300, 400 | A gold-rush mining camp: Foreman Dagny Bell, Quill Hart the assayer, gold in the cliffs |

**The outer reaches** (added in 0.9.0; farther out, and harder):

| Island | Where (x, z) | What's there |
|---|---|---|
| Kingsport | 600, -20 | The colonial capital: plaza with clothier, gunsmith, armorer and chandler; tavern with a card table; the Bank of Kingsport; town hall with a clock tower; merchant warehouse; marines; long pier |
| Crownwatch | 820, -60 | Tidewatch naval headquarters: a great fortress where Admiral Hester Galloway keeps the Admiralty, a frigate on the slipway, the navy chandler |
| Saint Vesper | -560, 120 | A cathedral isle: stained-glass cathedral and bell tower, the infirmary where Mother Celestine heals anyone, a graveyard, an apothecary |
| Mariner's Rest | 40, -560 | Fishing village: huts on stilts, drying racks, a smokehouse with the fishmonger and the cook, a fishing pier with boats |
| Widow's Rock | -250, -500 | Sheer cliffs topped by a ruined watchtower, a wreckers' camp (bandits) and the widow's house. A tunnel on the east face leads to a ladder up |
| Serpent's Hollow | -330, 420 | Jungle ruins around a coiled serpent statue, a jungle raiders' camp, and a pool where **Old Gnasher**, a giant crocodile, waits |
| Bloodwake Cay | -40, -720 | A desert island and its pirate stronghold: walls, cannons, gallows, a keep, and **Red Maddox, Pirate Warlord** with his crew |
| Stormbreak Isle | 760, 330 | Storm-wracked crags ringed by sea stacks, with constant lightning. The Regatta Master runs a timed race around the island |
| Raven's Landing | -620, -160 | The smugglers' port: a fence and a smuggler in the square (the black market), a tavern with cards and a Brethren broker, contraband warehouses |
| Emberfall | -700, 620 | A huge volcano. An obsidian sanctum rings the crater, guarded by blazes and **The Cinder King** |
| The Forgotten Keys | 480, 700 | A scatter of islets with the ruins of the first sea-people: obelisks, column rings, fallen statues, buried caches of ancient coins, and an antiquarian |
| Blackveil Atoll | 900, -400 | A ring of pale sand round a lagoon. A spirit shrine on the middle islet holds **The Veil Witch**; a ghost-ship wreck lies in the lagoon; spectral sailors walk at night |
| Crooked Reef | 180, 560 | A ship graveyard: six wrecks on the reef with drowned guards, and a salvager's raft-house moored above |
| The Last Horizon | 0, 1100 | The edge of the chart: terraced slopes, a ruined observatory, the Wayfarer's camp, and the sealed Horizon Gate with seven empty sockets |

### Dungeons

Every island has one to three dungeons, 53 in all, each with its own layout and theme
(crypt, flooded crypt, sea cave, grotto, volcanic, smugglers' tunnels, old fort, bayou cellar, jungle temple,
prison, ancient sandstone halls, spectral obsidian vaults). Outer-reach dungeons are tier 3 to 5, with tougher bosses.
Look for a small ruined hut: a ladder inside goes down to the first room.

Inside: rooms full of spawners and guards, trap rooms (lava channels, magma floors, cobwebs,
flooded pits), arrow traps on pressure plates in the corridors, and treasure rooms. Somewhere
deep in each one a named **mini-boss** guards a chest with a **Rusted Key**. The key opens the
**Sealed Gate** (an unbreakable iron-bound door) in front of the **vault**, which holds the best
loot in the dungeon and sometimes a named legendary weapon.

| Island | Dungeons |
|---|---|
| Driftwood Island | Wrecker's Cellar, Tide Caves |
| Saltmarrow | Bilge Tunnels, Old Rum Vaults, Smugglers' Sea Cave |
| The Sunken Reach | Drowned Chapel, Coral Grotto (both **flooded**: bring water breathing) |
| Ashfall Island | Magma Vents, Smugglers' Deep Mine, Obsidian Shrine |
| Hollowgrave Island | The Ossuary, Catacombs of the Drowned (flooded), Thorne's Crypt |
| Ironwatch Isle | Old Casemates, Sealed Powder Store |
| Mirefen Bayou | Sunken Plantation Cellar, Gator Warrens, Root Hollow |
| Port Meridian | Governor's Wine Cellars, Meridian Sewers |
| Blackthorn Isle | Blackthorn Smuggler Caves, Drowned Cove |
| Emerald Cay | Serpent Stair, Overgrown Vault (boss: the Temple Guardian) |
| Whispering Key | Ashgrove Crypts, Haunted Undercroft |
| Goldwater Cay | The Deep Seam, Collapsed Galleries |
| Kingsport | Kingsport Gaol, Counting-House Vaults |
| Crooked Reef | The Hull Warrens (flooded) |
| Saint Vesper | Reliquary Crypt, Penitents' Tunnels |
| Mariner's Rest | Wreckers' Grotto |
| Widow's Rock | Widow's Sea Cave, Wreckers' Cistern |
| Serpent's Hollow | The Coiled Temple, Serpent's Gullet |
| Bloodwake Cay | Bloodwake Dungeons, Red Maddox's Hoard |
| Stormbreak Isle | Thunderwell |
| Raven's Landing | Contraband Cellars, The Rookery |
| Emberfall | Cinder Halls, The Ashen Throne, Obsidian Labyrinth |
| The Forgotten Keys | Halls of the First Tide, The Drowned Library |
| Blackveil Atoll | The Veiled Deep |
| Crownwatch | Admiralty Magazine, Crownwatch Hulks |
| The Last Horizon | Stair of Ages, The Sunset Vault |

Quartermasters sell long water-breathing potions and turtle helmets for the flooded ones.

### Treasure maps

Find treasure maps in chests, dungeon vaults and sunken ships, or buy one from a Quartermaster.
**Use** an unread map to reveal its X on one of the islands. **Hold** it to see the distance and
bearing. Stand on the X, use the map again, and **dig two blocks down**.
Collect three **Map Fragments** and use them to piece together a **Legendary Treasure Map**, whose
chest holds far richer treasure.

Between them: open warm sea with coral, kelp, dolphins, fish and drowned. Parrots, ocelots,
chickens and pigs live on the islands. Ore veins run under every island and the seabed.

### Your pirate

The first time you join, you make your pirate: a **name** (type one or roll a random one), an
**outfit colour**, and a **background**:

| Background | You start with |
|---|---|
| Castaway Sailor | A full sailor's outfit in your colour, a spyglass, a head start in Sailing |
| Tidewatch Deserter | A Tidewatch coat, a flintlock and shot, a head start in Firearms. The Navy starts out cool toward you; the Brethren warmer |
| Smuggler's Child | A sailor's outfit, a dagger and throwing knives, extra doubloons, a head start in Blades. The Brethren know your family |
| Bayou-born Hexer | Bayou oilskins, a hex staff, a head start in Hexes |

`/pirate appearance` lets you change your name later. Your name and background head the journal (J).

**Clothing** is worn in the armour slots and shows on your character (light protection only):
Sailor's garb (any colour: dye it like leather), Captain's attire, the Tidewatch uniform and Bayou
oilskins. Quartermasters sell sailor's garb and oilskins, and sailor's garb can be crafted from wool
and leather. The captain's attire comes with Brethren rank 300; the Tidewatch uniform with Navy rank 300.

### Combat

- **Parry:** right-click with a rapier or dagger (sneak + right-click with a cutlass). For a moment, the
  next melee blow is turned aside: the attacker is knocked back, slowed and weakened, and you get a
  brief burst of strength for the riposte. The rapier's window is longest.
- **Dodge roll:** Left Alt (rebindable). Rolls left, right or back depending on the direction you're
  holding, and slips any attack for a split second. Costs a little hunger; short cooldown.
- **Rapier:** fast and precise, the best parry. **Dagger:** very fast, with a +7 damage backstab when you
  hit from behind. **Boarding Axe:** slow and heavy, every hit staggers, and it knocks shields aside.
  **Throwing Knives:** right-click to throw; pick them up where they land.
- All of these train the **Blades** proficiency (formerly Cutlass).

| Item | Recipe |
|---|---|
| Rapier | 2 iron diagonal, a gold nugget guard, a stick hilt |
| Dagger | Iron ingot over a stick |
| Boarding Axe | 2 iron, an iron block and 2 sticks |
| Throwing Knives (x4) | Shapeless: iron ingot and 2 iron nuggets |
| Sailor's garb | Wool (bandana, shirt, breeches with a leather belt) and leather (boots) in armour shapes |

### New in 0.20: the old pirate games' scale (start a new world)

The whole archipelago is rebuilt at the scale of the old pirate games, measured from their released world data.
**Old worlds won't line up with the new map, so start a new world.**

- **Distances:** islands are now about 3,600 blocks from their nearest neighbour (they were about 250). The map
  is about 29,000 blocks across, and a hop between neighbours takes two to three minutes under full sail.
  Driftwood stays at the centre as the starter isle, a short row (about 600 blocks) from Saltmarrow.
- **Ships:** a sloop under full sail in a fair wind makes about 25 to 30 blocks a second, as fast as the old
  game's ships. Heavier hulls are slower. At the helm, a line above your health bar names the nearest island,
  how far it is and which way.
- **Islands:** every island except the reefs and the atoll has gained a hinterland of hills, jungle and wilds
  stretching away from its harbour, which brings it up to about 250 to 450 blocks across. Town hearts, harbours
  and dungeons stay where they were on each island. The wilds are full of enemy camps (more islands, more camps).
- **Towns:** Saltmarrow, Port Meridian and Saint Vesper grow to about 180 blocks across, with roads running
  inland lined with houses, townhouses, shops, warehouses and markets, plus more jetties. Kingsport, Ironwatch and
  Crownwatch get barracks, watchtowers, storehouses and drill yards. Raven's Landing, Blackthorn and Bloodwake
  sprawl with shacks, tents and fire pits.
- Quest directions, the chart, whirlpools, currents and fog banks have all moved to match.

### New in 0.20: Keeper Wren's lessons (the tutorial)

The story now opens the way the old pirate games did: the first quests teach you to play. Talk to Keeper Wren
by the lighthouse, and before she sends you to Saltmarrow she takes you through, one lesson at a time:

1. **Blade:** clear three crabs off the beach with the cutlass (swing again and again for combos and finishers).
2. **Guard:** a cursed deckhand comes out of the surf. Parry with sneak + right-click, dodge-roll with Left Alt.
3. **Pistol:** she hands you her flintlock; land three shots on the deckhands climbing the rocks.
4. **Voodoo doll:** use it on a foe to attune, then use it in the air to hex.
5. **Firebombs:** throw one into a knot of deckhands.
6. **Your sloop:** she gives you a sloop deed. Launch it from the pier and take the helm.
7. **Sailing:** raise sail (R) and round the striped marker buoy off the south shore.
8. **Gunnery:** turn side-on and put three hits from your broadside (Z/X) into the old hulk by the buoy.

Each lesson hands over the tool it teaches, shows the keys on screen and gives a little reputation. Then she
points you east to Saltmarrow, and the main story carries on from there. Your journal and the goal line always show
the current lesson, and talking to Wren again repeats it (and replaces anything you've lost).

### New in 0.19: fighting and levelling the old way

- **Enemy levels:** every foe has a level after its name (`Cursed Pirate [Lv 7]`) set by where it lives. The
  starting beaches are level 1 to 5, the far reaches 40 to 50, and the grand caverns run a few levels above
  their island. Health and hits scale with level.
- **Enemy camps instead of random spawns:** foes no longer spawn at random in the dark. Each island has fixed
  camps out in the wilds (never in town) with two to four foes each. They come back to the same spot about half
  a minute after you kill them, and if you drag them too far they give up and go home healed. The undead here
  don't burn in daylight.
- **Health in numbers:** the hearts are replaced by a health bar with numbers (you start at 100), a voodoo bar
  beneath it, and your notoriety level beside it. The foe you're fighting shows its health in the top corner,
  and damage numbers float up off whatever you hit.
- **Reputation and notoriety:** you earn reputation in a weapon by dealing damage with it, not just by landing
  the killing blow. Tough foes give more and foes far beneath you give almost nothing. All your reputation
  together sets your notoriety (level 1 to 50), and each notoriety level raises your maximum health. Weapon
  mastery also makes that weapon hit harder.
- **Defeat:** you keep everything (the mod turns on `keepInventory`) and wake up in the nearest port's gaol at
  full health, with the cell door hanging open. Gaols are at Driftwood, Saltmarrow, Port Meridian, Kingsport,
  Saint Vesper, Ironwatch, Blackthorn, Goldwater, Raven's Landing and Crownwatch. Being arrested while wanted
  still sends you to the Tidewatch brig.

### New in 0.18: the grand caverns

Seven islands have a vast cave system far below them, much bigger than the old dungeons: eight or so huge
domed chambers (stalagmites, pillars, pools, a chasm with a rope bridge) joined by long winding tunnels,
with a hidden treasure grotto off to one side and a great hall at the bottom.

- **Getting in:** look for a ring of tumbled rock round a dark pit with a beam and an old rope. Use the rope
  to climb down; use the rope in the camp at the bottom to climb back out.
- **Inside:** the chambers wake as you enter them, and packs of whatever lives down there come out of the dark
  (they return a few minutes later). Spawners and chests are scattered through the halls.
- **The great hall:** step into it and its master rises, with a boss bar and a few guards. Beat it for
  doubloons, a high-tier weapon, maybe a trinket, plus the vault behind the dais. It comes back after 15 minutes.

| Cavern | Island | Master |
|---|---|---|
| The Weeping Catacombs | Hollowgrave Island | The Bone Abbot |
| Cinderdeep | Ashfall Island | The Cinder Matron |
| The Serpent's Throat | Serpent's Hollow | Old Gnasher |
| The Motherlode | Goldwater Cay | Claim-Jumper Grubb |
| The Drowned Grotto | Whispering Key | Mother Brine |
| The Furnace Below | Emberfall | The Slag Beast |
| Hall of the First Light | The Last Horizon | The Lantern Warden |

New feats: Spelunker, Master of the Deep, Deep Delver. Ops can use `/cursedtides cavern <0-6> [arena]` to jump
to a cave mouth or great hall. The server log lists each cavern's coordinates at startup.

### New in 0.17

- **The weekly siege:** every Saturday at 8pm (server time) the cursed fleet attacks one of the great ports, a
  different one each week (`/siege` tells you which). Six waves land from three beaches, a ghost ship shells the
  harbour in the fourth, and the Dread Captain comes ashore at the end. A bar shows the town's morale; every invader
  that reaches the plaza wears it down, and at zero the port falls. Hold it and the town pays every defender by their
  tally, the top three handsomely. Admins can start one with `/cursedtides siege`.
- **Voodoo dolls** (Hexes): use one on a creature or person to attune (up to three), then use it in the air: Poke,
  Mend, Swarm (8), Cure (12), Shackles (16) and Life Drain (22). Sneak-use to choose. Foes are hurt; friends are healed.
- **Grenades** (Gunnery): firebombs, stinkpots, smoke bombs (you vanish) and blast charges (which hurt ships). Sneak
  to lob them further. Gunsmiths sell them; craft them from gunpowder.
- **Weapon skills:** sneak-use a firearm to Take Aim (+50% on the next shot); a heavy weapon to Taunt; dual blades to
  Brawl (a kick that sends foes sprawling).
- **Old Hundred-Arms:** far out in deep water, something can rise under your ship and wrap it in tentacles. Cut them
  all before the hull gives way.
- **Dread ships and treasure fleets:** named pirate galleons with legendary loot, and gold-laden treasure galleons
  under Tidewatch escort.
- **Ramming:** drive your bow into another ship at speed.
- **Ship of the Line:** a fourth heavy hull: 560 hull, seven guns a side, three masts. Trade up from a frigate.
- **Sail emblems:** sneak-use any flag on your ship to paint its design across the mainsail.
- **Cannon Defense:** at Saltmarrow harbour, Sergeant Kettle lends you a cannon to hold the docks against eight waves.
- **Repair and brewing mini-games:** a Carpenter's Mallet (shipwright) opens a quick patching game aboard a damaged
  ship; a Brewer's Kit opens a match-three board, and the better you play, the better the tonic.
- **Twenty-One** at the card dealer and in taverns.
- **Treasure collections:** pearls, jewels, old coins and Brethren keepsakes drop from the cursed and bosses; use them
  to collect (`/collections`), complete a set for a reward.
- **Ferries:** `/ferry` from any port to any port you've already found, for a fare.
- **The brig:** wanted pirates who fall wake up in a cell on Ironwatch. Serve two minutes, `/bail`, or try a lockpick.
- **The Privateer War:** two teams fight over Bloodwake Cay's signal mast (`/privateer join`, two players needed).
- **The Lookout:** `/lookout join <activity>` gathers a group and carries everyone there together.
- **Barbers and tattooists** in Saltmarrow and Kingsport: after your first look, restyling takes a visit (5 doubloons).
- **Parrots** on your shoulder chatter now and then (peddlers sell them).

### New in 0.16

- **Hexes:** the staff now holds seven: Hex Bolt, Withering Curse (Hex 5), Spirit Ward (10), Drowned Grasp (15),
  Soul Siphon (20), Ghost Walk (25) and Storm Hex (30). Sneak and use to choose; use to cast.
- **Hidden islands:** the Isle That Isn't, Kingsrest and Gullscream Rock aren't on any chart until revealed (by the
  story, by a Hidden Chart, or by sailing into them). Fog banks hide two of them.
- **Tailoring, Jewelcrafting and Cartography:** craft sailcloth, bandanas, gloves, belts, eyepatches and cloaks; cut
  rough gems (found in rock while mining, and in puzzle chests) and set rings, earrings, pendants and charms. From
  level 10 a crafted piece can come out Fine (+25%), from 20 Masterwork (+50%). Use Blank Charts on islands to draw
  surveys the archivist buys; from Cartography 10, Hidden Charts reveal secret islands; from 15, surveys sometimes
  turn up a treasure map.
- **Figureheads:** the shipwright carves the Mermaid (+10% speed), Golden Lion (+15% hull), Grinning Skull (+1 gun
  each side) and Sea Eagle (spots hostile ships); the Kraken (shrugs off whirlpools and currents) must be carved
  yourself from an ancient relic. Use one on your ship to mount it at the bow.
- **Fort bombardment:** Ironwatch and Crownwatch fire on wanted pirates' ships, Blackthorn on Tidewatch loyalists.
  Three cannon hits silence a battery; silence them all and the fort stays quiet for a day, and you're paid for it.
- **Trade events:** shortages become relief runs (bonus pay per crate until the need is met, a reward for whoever
  brings the most); ports can fall under quarantine (markets shut, black markets pay double for spices); and goods
  boom across the whole Reach.
- **More puzzles:** song altars (play its four notes back on the chime stones) and moon seals (open only at night).
- **Player-vs-player poker:** everyone who sits at the same dealer plays at the same table, with house players filling
  empty seats. Players who join mid-hand are dealt in next hand; a slow player is acted for after 45 seconds.
- **Feats:** Fort Breaker, The Hollow Crown, Off the Charts and Storm Caller.

### Fighting style

- **Combos:** melee blows landed in quick succession grow stronger (+10% each). The **fourth** is a
  *finishing blow*: 1.6x damage, hurls the foe back and staggers them. A finisher on a foe under a fifth of its
  health (not a boss or a player) executes it outright.
- **Critical hits:** 5% base, +0.4% per Blades level, +5% with an eyepatch. Crits deal 1.5x (1.8x with heavy weapons).
- **Heavy attacks:** crouch as you strike for 1.35x damage and a stagger, at the cost of a moment's slowness.
- **Stagger:** staggered foes are slowed, weakened and can't press an attack for a moment. Stacked bonuses cap at 3x.
- **New weapons:** the **Saber** (a faster cutlass with the same skills), **Dual Blades** (combos last twice as long;
  use for a three-cut Flurry), the **Heavy Broadsword** (use: a wide Cleave) and the **War Hammer** (use: Ground Slam
  that throws everything around you into the air). Heavy weapons stagger on every blow.
- **New firearms:** the **Double-Barrel Pistol** fires twice before reloading; the **Hand Cannon**'s shot bursts where
  it lands, hurting and staggering everything nearby (and kicking you back a step).
- **Ultimate abilities (U):** once any proficiency reaches 20, your best one grants a signature move on a five-minute
  cooldown: Blade Storm (Blades), Dead Eye Volley (Firearms), Barrage (Gunnery), Cursed Tide (Hexes), Full Canvas
  (Sailing: a long burst of speed for your ship) or Treasure Sense (Exploration).
- **Gear grades:** weapons found in vaults, on bosses and on the cursed come Crude, Common, Fine, Rare, Famed or
  Legendary, with enchantments, a maker's mark and (for Legendary) a name of their own.
- **Leadership** (a sixth profession): earn it when your crew and hired hands fight beside you and when you sink or
  take ships. From level 5, crewmates and shipmates within 16 blocks fight with strength; resistance from 15;
  regeneration from 25.

### Your look (O) and accessories

Press **O** (or `/pirate look`) for the looking glass: skin tone, build, hair style and colour, facial hair, scars and
tattoos, and clothes. Everyone sees your pirate as you made them ("Use my own skin" goes back to your normal skin).
**Accessories** go in nine slots (head, eyes, ear, neck, ring, trinket, gloves, belt, cloak): use one to wear it.
Bandanas, eyepatches and earrings show on your character. Each grants a bonus: luck, health, armour, speed,
crit chance (eyepatch), exploration xp (compass pendant), faster voodoo (voodoo charm). `/pirate accessories` lists
what you wear; `/pirate unequip <slot>` takes one off. Clothiers and antiquarians sell some; the rest are found.
Write your story with `/pirate bio <text>`; read another's with `/pirate whois <player>`.

### Conversations

Click any character, shopkeeper, marine or hired hand to talk. A conversation window opens with their portrait, their
words typed out, and numbered replies: click one or press its number (Space or a click skips the typing; Esc walks
away). Sneak-click a shopkeeper to skip straight to their shop.

What people say depends on the moment:
- **Time and weather:** dawn, day, dusk and night greetings; remarks about rain and storms.
- **Your story:** lines change through the Drowned Bell and the Seven Tides, after side quests, and with your ending.
- **Your standing:** Navy, Brethren and Smuggler reputation, being wanted, notoriety.
- **Rumours:** everyone has a "Heard any rumours?" reply drawing from a shared pool that reacts to the story, the
  weather, the time and the state of the gate.

Story choices (the ledger, Harrow's bargain, the three endings) are now replies marked "(Permanent)" instead of sneak-clicks.

**Writing your own lines.** Conversations are data files in `data/cursedtides/dialogue/` (one per character; story
characters use their role name, shopkeepers `vendor_<trade>`, plus `marine`, `crewmate`, `shipwright` and the shared
`common`). A data pack can replace or add to them. Each file has `nodes`; each node has `text` (variants, one picked at
random), optional `text_if` overrides and `aside` afterthoughts with conditions, and `options` (replies) with `goto`,
`action` (`trade`, `market`, `quest`, `quest_alt`, `quest_third`, `quest_screen`, `close`), `if`, `set` and `once`.
Conditions include `night`, `raining`, `wanted`, `navy>=300`, `story>=2`, `arc==2`, `relics>=4`, `ending:sealed`,
`ledger==3`, `fishing>=10`, `has:<item>`, `holding:deed`, `flag:<name>` and `chance:0.3`; prefix `!` to negate.
Placeholders: `{player}`, `{time}`, `{weather}`, `{island}`, `{title}`, `{relics}`, `{npc}`.

### Main story: "The Drowned Bell"

1. **Keeper Wren** (Driftwood Island) tells you about the bell that raises the dead and gives you a
   sea chart, weapons, a boat and food.
2. **Harbormistress Ysolde Vane** (Saltmarrow harbor house) tells you Captain Ashmere Thorne's story
   and sends you after two shards of his cursed bell.
3. Recover the **Reef Shard** from the belltower at The Sunken Reach, and the **Ember Shard** from the
   smugglers' forge on Ashfall Island. Both are guarded.
4. **Tamsin Coil**, the last bellfounder (Ashfall's east shore), casts them into **the Stilling Bell**.
5. Ring it at the altar on **Hollowgrave Island** to summon **Captain Ashmere Thorne**: a boss with a
   boss bar, 220 health, sunlight immunity, and a crew he calls up at half health.

Type `/pirate` any time for your current goal. Right-click the Sea Chart for the distance and
direction to every island.

### Story, part two: "The Seven Tides"

Out at the edge of the chart, the Horizon Gate was built by the First Tide, a people the sea drowned long ago. Seven relics
open it. A secret society, the **Lantern Court**, has been buying up everything the First Tide left behind. Captain
Rook Varga is gathering the free captains into an **Accord** of his own. And the flagship of the lost **Meridian Fleet**
is still out there, forty years under the water. Spoilers below.

1. **The Wayfarer** at the Last Horizon (0, 1100) gives you the Dawn Relic and sends you to **Professor Ansel Thwaite**,
   camped on the Forgotten Keys. He reads the relics, names where the other six went, and gives you the log of the
   *Sovereign Dawn*.
2. **Gather the relics**, in any order:

   | Relic | Where |
   |---|---|
   | Reef | The vault of the Hull Warrens, Crooked Reef |
   | Serpent | The vault of the Coiled Temple, Serpent's Hollow |
   | Ember | The Cinder King, Emberfall's crater |
   | Veil | The Veil Witch, Blackveil Atoll |
   | Sovereign | Read the fleet log at night over Crooked Reef to raise the ghost galleon *Sovereign Dawn* (armoured, extra guns, takes half damage), then sink her |
   | Lantern | Governor Harrow in Port Meridian is the Court's agent. Once you hold four relics she makes you an offer: **bargain** (sneak-click) for the relic and the Lampwright's Seal, or **expose** her (click) and take it from her bodyguard, the Lampwright's Hand. Exposing her earns a reward from Admiral Galloway. |

   Rook Varga at Blackthorn offers you a place in his Accord while you hunt. Crewmates nearby share each relic as it's
   found. If you emptied those vaults before the hunt began, the professor will find the relics in your notes.
3. **The gate.** Bring all seven to the Wayfarer. Rook arrives with his Accord to take them: defeat **Rook Varga, the
   Turncoat**. Then set the relics: the gate opens (for the whole server) and **the Tide Sovereign**, the drowned king of
   the First Tide, comes through.
4. **The ending** is your choice, and it is permanent:

   | Ending | How | What changes |
   |---|---|---|
   | **Seal the gate** | Click the Wayfarer | Navy +200, Brethren +100, 200 doubloons. Ghost ships rarely trouble you and the cursed no longer invade towns around you. Title: Warden of the Horizon |
   | **Claim the tide** | Sneak-click | Brethren +300, Navy -400 (you're wanted). Ghost ships never attack you, and you breathe and see underwater (conduit power). Title: Sovereign of the Seventh Tide |
   | **Give it to the Court** | Only if you bargained: hold the Lampwright's Seal and click | Navy and Brethren -150, Smugglers +150, 500 doubloons. The Tidewatch never searches you again. Title: Lantern-Bearer of the Court |

   The whole server hears which ending you chose. Your title shows in the journal.

### Story, part three: "The Hollow Crown"

Once the Seven Tides are settled, proclamations start appearing on posts across the Reach: a Pretender has found the
crown of drowned Vesh Amaru and means to rule the cursed and the free alike. Show the proclamation to Odalys Quill,
the archivist at Kingsport, then recover the crown's three shards: **Salt** from the grotto beneath the drowned
city's temple, **Ash** from the Ashen Herald who climbs out of Emberfall's crater, and **Mist** from the shrine on
the hidden Isle That Isn't (only at night). With all three, Quill points you east to **Kingsrest**, where the
Pretender waits with a crown guard. Afterwards, choose: **shatter** the crown (a legendary blade and the thanks of
both the Tidewatch and the Brethren; title *Crownbreaker*), **wear** it (two extra hearts for good, the Tidewatch's
undying hostility; *the Uncrowned Sovereign*), or **return** it to the deep (the sea's own strength whenever you
swim; *Warden of the Drowned*).

### Ships

Ysolde Vane gives you your first ship, a Sloop Deed, when the story reaches Saltmarrow.
Shipwright Hollis Pike at the Saltmarrow shipyard sells sloops and trades up: your old deed plus
64 doubloons buys the next size.

| Class | Hull | Guns a side | Cargo | Crew seats | Speed |
|---|---|---|---|---|---|
| Sloop | 120 | 2 | 27 slots | 3 | Fastest |
| Brigantine | 220 | 3 | 36 slots | 4 | Medium |
| Galleon | 380 | 4 | 54 slots | 6 | Slowest |
| Schooner | 160 | 2 | 36 slots | 4 | Fastest of all (two masts) |
| Brig | 280 | 4 | 36 slots | 5 | Medium (two masts) |
| Frigate | 340 | 5 | 45 slots | 6 | Quick for her size (three masts) |

The shipwright also trades a sloop deed plus 32 doubloons for a **schooner**, a brigantine deed plus 40 for a **brig**,
and a brig deed plus 64 for a **frigate**. The Tidewatch sails frigates and brigs; merchants sail schooners.

- **Launch** a ship by using its deed on open water. **Stow** her by sneak-punching her: she goes back
  into the deed with her name, hull condition and cargo.
- **Name** your ship by renaming the deed in an anvil.
- **Ownership:** only the owner can take the helm. Friends can ride along as crew and man the guns.
- **Sails** (R to raise, V to lower): furled, half or full. Raised sails drive the ship on their own, and
  how much depends on the **wind**, which shifts around the compass and gusts over time. Running with the
  wind behind you is fastest; heading straight into it is slowest. Your Sailing level adds speed.
- **Broadsides** (Z for port/left, X for starboard/right): every gun on that side fires at once. Uses
  cannonballs from the cargo hold first, then your pockets. Cannon level shortens the reload.
- **Cargo hold:** press B at the helm, or sneak-right-click the ship from outside.
- **Repairs:** right-click your ship with planks. Below 30% hull she trails smoke. At zero she sinks
  and her cargo floats free.
- The sailing display along the bottom of the screen shows hull, sails, shot, wind, weather and speed.
- **Anchor** (N): drops anchor so she holds her position (and furls the sails); press again to weigh anchor.
- **Swivel gun** (G): fit one (a Swivel Gun from the shipwright, right-click your ship) and anyone aboard
  can fire a cone of grapeshot wherever they're looking. Each shot uses one grapeshot from the hold or your pack.
- **Flags**: right-click your ship with a flag to fly it from the masthead (you keep the flag). Shipwrights
  sell the Skull and Cutlasses, Brethren Red, Tidewatch Ensign, Raven of the Landing and Merchant's Stripes. The
  rare ones (Cursed Tide, Golden Sun, Kraken's Coil, Last Horizon) come only from the outer-reach bosses.
  Navy, pirate, merchant and ghost ships fly their own colours.

**Below deck** (H on or beside your ship): every ship has a walkable interior built to her class.

| Class | Rooms below |
|---|---|
| Sloop | Captain's quarters, cargo hold |
| Schooner | Quarters, galley, hold |
| Brigantine | Quarters, crew quarters, galley, hold |
| Brig | Quarters, crew quarters, galley, brig, hold, and a gun deck above |
| Galleon, Frigate | Quarters, chart room, crew quarters, galley, brig, hold, and a gun deck above |

Below deck, the barrel by the hold wall opens the ship's real cargo, the cartography table opens the sea chart,
the lectern opens your journal, the galley has a crafting table and smoker for cooking, and the beds work. The
ladder and trapdoor (or H) take you back up on deck. Going below drops anchor and keeps her loaded while you're
down there. Owners and crew can rearrange and add furniture as they like; nobody else can.

**Shot** (press C to cycle what the guns are loaded with):

| Shot | Effect |
|---|---|
| Round shot (Cannonball) | Hull damage and a small blast |
| Chain Shot | Little hull damage, but tears the target's sails: she slows to a crawl for a while |
| Grapeshot | A spray of small balls: deadly to crews and anyone on deck, weak against hulls |
| Explosive Shell | A big burst: the heaviest hull damage |

**Upgrades.** Buy fittings from the Shipwright and right-click your ship with them:
Hull Plating (+30% hull, up to 3), Gun Carriage (+1 gun a side, up to 2), Fine Canvas
(+8% speed, up to 3) and Cargo Bulkhead (+1 row of hold, up to 6 rows). Upgrades stay with the
ship when you stow her. **Dye** your sails by right-clicking the ship with any dye.

**Hired crew.** Buy articles from the Shipwright and right-click your ship to sign a hand aboard
(each takes a seat):
- **Gunner:** fires broadsides at ghost ships (and at the Navy, if they're after you) by themselves,
  using shot from the hold.
- **Bosun:** patches the hull with planks from the hold.
- **Navigator:** +10% speed under sail.

Crew draw **2 doubloons a day each** from the cargo hold. Leave them unpaid and they walk off. Sneak-right-click
a crewmate to dismiss them. Crew come along when you stow and relaunch the ship.

**Weather.** Rain freshens the wind; storms make it stronger still but gusty, throw the ship about,
roughen the sea, and sometimes call lightning down on your mast (hull damage and torn sails).

H goes below deck, N drops or weighs anchor and G fires the swivel gun. All keys can be changed under Options > Controls > Cursed Tides: Ships.

**Ghost ships.** While you're out on a boat, ghost ships may appear on the horizon (more often at
night). They hunt you, turn to bring their guns to bear, and fire broadsides. Sink one for doubloons,
cannonballs, Cannon XP and sometimes a Heart of the Sea. Or **board her**: jump onto her deck, cut down
her crew, then sneak-right-click the ship to **capture** her. A captured ghost ship keeps her ghostly
look and becomes yours.

### Journal and chart

- **J** opens the **Captain's Journal**: your crew, story goal, side quests, proficiencies, standing and bounty, contract,
  the treasure maps you carry, every dungeon (and which vaults you've opened), and every island.
- **M** opens the **Sea Chart**: the islands you've discovered (named and coloured; the rest are
  question marks), the dungeon entrances you know of, your treasure X's, and where you are.
- **K** opens your **skill trees**.

### Factions

Two powers keep score of what you do: the **Tidewatch Navy** (Ironwatch Isle) and the
**Saltmarrow Brethren** (the freeport's pirates). Reputation runs from -1000 to 1000.

| What you do | Tidewatch | Brethren |
|---|---|---|
| Kill a cursed pirate | +3 | +1 |
| Sink or capture a ghost ship | +40 | +20 / +30 |
| Defeat Captain Thorne | +100 | +100 |
| Hit a marine or navy ship (while not wanted) | -4 per hit | |
| Kill a Tidewatch marine | -25 | +5 |
| Sink a navy patrol | -120 | +40 |
| Capture a navy ship | -150 | +60 |

**Ranks.** Tidewatch: Unknown, Friend of the Tidewatch (100), Privateer (300), Commodore's Right Hand (600).
Brethren: Outsider, Brethren Deckhand (100), Sworn Brethren (300), Brethren Captain (600).
Reaching 300 earns a gun: the **Tidewatch Musket** (long range, heavy hit) or the **Brethren Blunderbuss**
(a close-range spray of shot). Reaching 600 earns 64 doubloons.

**Wanted.** Fall to -100 with the Tidewatch and you're **wanted**, with a bounty of 15 to 64 doubloons.
Marines fire on sight, and navy patrols come about to hunt you. Pay the bounty to **Commodore Aldous Vey**
at Ironwatch for a pardon, if you dare walk in.

**Contracts.** Commodore Vey (Ironwatch hall) and **Mags Tallow, the Brethren Broker** (Saltmarrow tavern)
hand out repeatable jobs: put down cursed pirates, sink ghost ships, kill marines, sink navy patrols.
One contract at a time; turn it in to whoever gave it for doubloons and reputation.
`/pirate contract abandon` drops a contract (for a small standing loss).

Your choices close doors: a Privateer of the Tidewatch is thrown out of the Brethren's tavern, and the
Commodore won't hire known Brethren.

**Tidewatch at sea.** Navy patrols (sloops, brigantines and galleons crewed by marines) sail the Reach,
thicker near Ironwatch. They leave honest sailors alone, attack ghost ships on sight, and hunt anyone
wanted or anyone who fires on them. Ghost ships attack the Navy too.

`/pirate` shows your standing, rank, bounty and current contract.

**Older worlds:** new islands (Ironwatch Isle, Mirefen Bayou, the five added in 0.8.0 and the fourteen added in 0.9.0) and dungeons only appear in parts of the sea you haven't explored yet; for the full set, start a new Cursed Archipelago world. Ironwatch Isle appears in any part of the sea not yet explored. To add the Broker to an
existing Saltmarrow tavern (or replace a lost story character), stand nearby and run
`/cursedtides restore_npcs` (needs cheats/operator).

### Progression

Six proficiencies, each level 0-30. Together they make your **notoriety** and title
(Landlubber through Legend of the Tides).

| Proficiency | How it grows | What it does |
|---|---|---|
| Blades | Kills with cutlass, rapier, dagger, boarding axe or throwing knives; parries | More cutlass skill damage, shorter cooldown; Whirling Blade at 5 |
| Firearms | Kills with pistol, musket or blunderbuss | More damage, faster reload |
| Cannon | Kills | Shorter cooldown |
| Hexes | Kills | Stronger hexes, more voodoo energy; Wither at 5 |
| Sailing | Every 25 blocks sailed; capturing ships | +1% ship speed per level |
| Exploration | Discovering islands (and finishing the story) | Counts toward notoriety |

Discovering an island shows its name on screen and pays 5 doubloons.

### Skill trees

Press **K** to open your skill trees. Each proficiency has a tree of six perk slots in three tiers,
opening at levels 5, 10 and 20; in each slot you choose one of two perks (36 in all). Examples:
saving a pistol shot, harder parries, a longer dodge window, cheaper hexes, voodoo regeneration,
faster reloading broadsides, better plank repairs, more XP, bigger discovery rewards. Choices are
permanent until you pay 32 doubloons to `/pirate respec`.

### Side quests

| Quest | Start with | What happens |
|---|---|---|
| The Governor's Ledger | Governor Harrow (Port Meridian) | Rook Varga stole the Governor's private ledger. Find it in the Blackthorn camp. Return it for a reward and Navy standing, or **sneak-click Rook** to sell it back to him for Brethren standing. The choice is permanent. |
| Specimens of the Reach | Ibbie Crane (Emerald Cay) | Bring a bucket of tropical fish, a sea pickle and a blue orchid |
| The Restless Shade | Sorrel (Whispering Key) | Lay the Shade of Lady Morrow to rest in the haunted mansion, then report back |
| Down the Deep Seam | Foreman Dagny Bell (Goldwater Cay) | Open the vault at the bottom of The Deep Seam |

Side quests show up in the journal (J).

### Wildlife

The islands and the sea are alive. Creatures appear around you as you explore (in Cursed Archipelago worlds):

| Where | What you'll meet |
|---|---|
| Open sea | **Sharks** (more at night: they go for swimmers, not boats), dolphins |
| Reefs, atolls, the Keys | Sea turtles, tropical fish, the odd shark |
| Jungles and ancient islands | **Jaguars** (pounce; hunt farther after dark), venomous **snakes**, **monkeys**, parrots, **wild boars** |
| Bayou | **Alligators** in the pools, snakes, frogs |
| Beaches | **Crabs** |
| Desert | Snakes |
| Cursed islands and Blackveil, at night | Spectral sailors |

Boars and crabs only fight back. **Monkeys can be tamed**: feed one melon slices, cocoa beans or sweet berries until it
trusts you. Then it follows you; click it to make it sit or stand, and feed it to heal it. Sharks drop shark teeth
(fishmongers buy them). Every creature has a spawn egg in the Cursed Tides creative tab.

### Bosses of the outer reaches

Old Gnasher (Serpent's Hollow), Red Maddox (Bloodwake Cay), The Cinder King (Emberfall) and The Veil Witch (Blackveil Atoll)
are big, armoured and announced to the server when they fall. Each drops a hoard of doubloons, an ancient relic, a
treasure map and sometimes a map fragment.

### Professions

Five peacetime trades, each levelled 0 to 30 apart from your fighting proficiencies. Your levels are in the journal (J).

| Profession | How it grows | What skill buys you |
|---|---|---|
| **Fishing** | Every fish you reel in with any rod | Rare catches more often; double catches from 15; the Golden Marlin (open sea) from 25; bigger fish |
| **Cooking** | Cooking meals at a crafting table | Fine (+50% effect time) and Masterwork (double) dishes |
| **Brewing** | Brewing tonics, grog, and distilling rum | Fine and Masterwork drinks, as with cooking |
| **Smithing** | Honing, rifling and reinforcing gear | Tier II upgrades from 10, tier III from 20 |
| **Hunting** | Killing wild creatures | Extra hides, teeth and meat |

**The Reach's fish.** Fishing anywhere can bring up one of the local species as well as the usual catch. Each has its
own waters, and every fish has a weight:

| Fish | Where |
|---|---|
| Silverfin Snapper | Open sea and most coasts |
| Reef Parrotfish | Reefs, the Keys, Blackveil Atoll |
| Bayou Catfish | The bayou and jungle islands |
| Storm Swordfish | Stormbreak Isle's waters |
| Lantern Eel | Anywhere, at night |
| Ghostfin | Cursed waters and Blackveil, at night (Fishing 5+) |
| Golden Marlin | The deep open sea, far from land (Fishing 25+). The server hears about it |

**Fishing contest.** Every two days at Mariner's Rest, Marlowe Finch weighs your heaviest single fish (hold it up and talk to
him). The winner gets 40 doubloons and the Fishing Trophy; second and third get 20 and 10. Prizes wait with him if you're away.

**Cooking and brewing** happen at any crafting table (shapeless recipes):

| Dish or drink | Recipe | Effect |
|---|---|---|
| Hardtack (x2) | 2 wheat, sugar | Very filling |
| Fish Stew | Bowl, any fish, potato, carrot | Regeneration |
| Spiced Snapper (x4) | 2 Silverfin Snapper, a Chest of Spices | Strength |
| Bayou Gumbo | Bowl, Bayou Catfish, brown mushroom, carrot | Fire resistance, regeneration |
| Reef Ceviche (x2) | Reef Parrotfish, sweet berries, kelp | Water breathing, speed |
| Marlin Steak (x6) | Golden Marlin | Absorption II, strength |
| Captain's Feast | Marlin Steak, Spiced Snapper, Hardtack, Grog, cake | Regeneration II, strength, resistance, speed |
| Grog (x4) | Cask of Rum, sugar, 4 glass bottles | Resistance (and a little seasickness) |
| Sea Legs Tonic | Glass bottle, kelp, prismarine crystals, sugar | Dolphin's grace, water breathing |
| Lantern Tonic | Glass bottle, glow berries, Lantern Eel | Night vision |
| Ironhide Tonic | Glass bottle, iron ingot, shark tooth, leather | Resistance II |
| Powder Monkey Tonic (x3) | Glass bottle, Sack of Coffee, sugar | Haste II, speed |
| Voodoo Draught | Glass bottle, Ghostfin, bone | Restores 50 voodoo energy |
| Cask of Rum | 2 Sacks of Sugar, water bucket | (a trade good: distil it yourself) |

**Smithing.** Use a kit with the gear held in your other hand:
- **Whetstone** (flint + 2 smooth stone, or from armorers): blades +1 attack damage a tier.
- **Gunsmith's Rifling Kit** (iron ingot, 2 nuggets, leather, or from gunsmiths): firearms +10% damage a tier.
- **Reinforcing Kit** (2 iron ingots, leather, string, or from armorers): clothing and armour +1 armour a tier.

Up to tier III; tier II needs Smithing 10 and tier III Smithing 20. Improved gear shows "Honed", "Rifled" or "Reinforced".

### Trade and the economy

**Coin.** Ten silver pieces make a doubloon. Merchants make change, so you can pay in either.

**Cargo.** Thirteen kinds of crated cargo: rum, sugar, spices, silk, coffee, timber, cotton, salt fish and ironware,
plus four kinds of contraband (untaxed rum, smuggled powder, cursed idols, stolen silver plate). Each crate's tooltip
says where it's cheap and where it's wanted.

**Markets.** Click a merchant, trader or purser for the port market (sneak-click for their ordinary shop). You can buy
and sell crates, and cargo in your own ship's hold (within 40 blocks) counts, so you can sell straight from the hold.
Bought crates go to your pockets, or to the hold when your pockets are full.

| Port | Cheap here | Wanted here |
|---|---|---|
| Saltmarrow | Rum, salt fish | Silk, ironware, timber |
| Port Meridian | Cotton, sugar | Spices, coffee, rum |
| Kingsport | Ironware, silk | Sugar, coffee, rum, cotton |
| Mirefen Bayou | Sugar, coffee | Salt fish, ironware |
| Mariner's Rest | Salt fish, timber | Rum, cotton, sugar |
| Saint Vesper | Spices, coffee | Salt fish, silk, timber |
| Goldwater Cay | Ironware | Rum, salt fish, timber, coffee |
| Crownwatch (navy purser) | | Timber, ironware, salt fish, cotton |
| Raven's Landing (black market) | Untaxed rum, smuggled powder | Cursed idols, stolen plate, silk |
| Blackthorn Isle (black market) | Stolen plate, spices | Untaxed rum, smuggled powder, cursed idols |

Prices move: every crate you buy pushes the local price up, every crate you sell pushes it down, and prices drift back
over time. Every so often a port has a **shortage** (double price) or a **glut** (half price) for a day; the whole server
hears about it, and the market screen lists the current news.

**Smuggling.** Only black markets deal in contraband. Cursed idols come from cursed pirates and ghost ships; stolen
plate turns up in merchantmen's holds. If a Tidewatch patrol comes within 40 blocks of a boat carrying contraband
it orders you to **heave to**. Stay within 70 blocks for 15 seconds and you're searched: the contraband is seized and
your Navy standing drops. Get out of range in time and you've given them the slip, which the smugglers admire.
Marines in port sometimes turn out the pockets of passing sailors too.

**The smugglers.** A third faction, the **Raven's Landing Smugglers** (Stranger, Runner, Smuggler, Shadow Captain).
Isadora Vance, the Smugglers' Queen, in a Raven's Landing warehouse, gives contracts: sell contraband, slip
inspections, fence cursed idols, capture a merchantman. Higher rank gets better black-market prices, and at 300
standing she sends hidden-hold bulkheads and a crate of shells. She won't deal with Tidewatch favourites.

**Auction house.** Barnaby Lott in Kingsport's town hall runs the board. To sell, hold an item near him and type
`/auction sell <doubloons>`. Talk to him to browse and buy. Sellers are paid on the spot if online, or collect
their takings from him later; the house keeps 5%. Also `/auction mine` and `/auction cancel <id>`.

**Player trading.** `/trade offer <player> <doubloons>` puts the stack in your hand up for sale to that player
(held safely until they answer); `/trade accept` or `/trade decline`. `/pay <player> <doubloons>` sends coin.

**Ships for sale, and insurance.** Sneak-click a shipwright while holding a deed (with an empty hold) to sell the
ship back for half her value. Talk to the Kingsport banker while holding a deed to **insure** her (a fifth of her value).
If an insured ship sinks, you get a replacement deed with her name, sails and fittings (not her cargo); if you were
offline, the banker has it waiting.

### Services and shops

- **Specialist vendors** sell by trade: clothiers (sailor, bayou and captain clothes), gunsmiths, armorers,
  chandlers (shot, hull plating, canvas), fishmongers and cooks (they buy your catch), apothecaries (potions),
  salvagers, antiquarians (buy ancient coins and relics), assayers (buy gold), and the black market: fences and
  smugglers pay the best prices for plunder and sell explosives.
- **Bank of Kingsport**: the banker opens your strongbox (the same storage as your ender chest).
- **Mother Celestine** at Saint Vesper heals you fully and draws out poison and worse, once every quarter-day,
  wanted or not.
- **Admiral Galloway** at Crownwatch gives Tidewatch contracts, and at 600 Navy standing grants a commission:
  a galleon deed and the full navy uniform.

### The Stormbreak regatta

Talk to Pip Halyard at Stormbreak Isle to start the clock, get in a boat, and round all eight buoys in order (yellow
first, then sunwise, threading the sea stacks) and back to the yellow buoy. Under 75 seconds pays 30 doubloons;
slower times pay less. Personal bests and the server record are kept, and a new record is announced to everyone.
Talk to him again to give up a run.

### Tavern games

**Texas Hold'em.** Talk to a **Card Dealer** (the taverns in Saltmarrow, Kingsport and Raven's Landing, or Rook's camp on Blackthorn Isle) with
10 to 64 doubloons in your inventory to buy in. You sit at a four-seat table with three islanders,
each with their own style (cautious, reckless, bluffers, sharks). Standard no-limit rules: blinds 1/2,
two hole cards, flop, turn, river, side pots for all-ins, best five of seven at showdown. Fold,
check/call, raise or go all-in from the table screen; **Deal** starts the next hand, **Leave** cashes
out your chips as doubloons (so does disconnecting).

**Dice.** Hold up to 16 doubloons and talk to Lucky Jory Bones in the Saltmarrow tavern: two dice each,
high roll doubles your stake.

### World events

Every five minutes or so the sea may throw something at a player, and the whole server hears about it:

- **Merchant convoy**: two unarmed merchantmen with holds full of trade goods. They flee; board one
  with no crew aboard and she's yours. Raiding costs Navy standing and earns Brethren standing.
- **Rival pirates**: a crewed pirate ship outside the Brethren that fires on everyone, the Navy included.
- **Tidewatch blockade**: three warships around Saltmarrow.
- **The Brinewyrm**: an elder guardian with an escort, surfacing near your ship. It drops doubloons,
  a Heart of the Sea and a treasure map.
- **Cursed invasion**: at night, cursed pirates storm a town's plaza. Each one drops doubloons.

Operators can stage one on the spot with `/cursedtides event <convoy|rival|blockade|monster|invasion>`.

### Duels

- `/duel <player>` challenges someone; `/duel accept`, `/duel decline` and `/duel yield`. The blow that
  would kill the loser leaves them at half a heart instead. Walking more than 48 blocks off forfeits,
  and after three minutes it's a draw. The winner earns Blades XP. Needs PvP enabled on the server.
- `/crew create <name>`, `/crew invite <player>`, `/crew join`, `/crew leave`, `/crew info`,
  `/crew say <message>`. Up to 8 hands. Shipmates can open each other's ships' holds, and your crew
  shows in the journal.

### A living archipelago

- **Opening hours:** shops keep daylight hours. Fences and smugglers deal only after dark. Quartermasters and cooks
  never close.
- **Town prosperity:** each port is booming, doing well, getting by, struggling or on its knees. Brisk trade,
  delivered parcels and beaten-off invasions lift it; raids and blockades drag it down. Struggling towns charge more;
  booming ones less. People talk about it.
- **Festivals** (by the real calendar): the Spring Tide Fair (late March), Regatta Week (late June), Saint Vesper's
  Feast (mid-August), the Night of Lanterns (late October), the Salt Harvest (mid-November) and the Midwinter Feast
  (late December). Each gives +50% xp to a pastime and fills the town skies at night.
- **Visitors:** a wandering peddler sets up in a port now and then with rare stock, gone by nightfall. On a beach at
  night you might meet the Old Mariner, who has a gift for those who find him.
- **Bounties:** the wanted draw bounty hunters out of the brush. Beat a wanted player (when you're not wanted
  yourself) to claim their bounty. `/bounties` or a Wanted Poster shows the list; the bounty clerk lets you pay your
  own off.
- **New residents** settle in as you visit: Grista Vell (the sword tournament) and Corporal Flint (the shooting range)
  at Saltmarrow, Bess Tankard's taverns, bounty clerks, land agents, Odalys Quill the archivist at Kingsport,
  Skip Lanyard at Stormbreak, and salvage divers at Crooked Reef and the Sunken Reach.

### Pastimes, challenges and feats

- **Sword tournament:** five waves in the ring at Saltmarrow. Nobody dies in the pit.
- **Shooting range:** sixty seconds, targets that pop up downrange; distance and speed score.
- **Stack Run:** a foot race over Stormbreak's crags, ten flags in order.
- **Drinking contest:** tankard for tankard against the house champion; Brewing helps you hold it.
- **Parcels:** tavern keepers have sealed parcels to carry to other ports, paid in full if on time.
- **Emotes (P):** wave, bow, salute, cheer, laugh, point, shrug, dance, drink, sit, sing a shanty, toast, spit, sleep,
  flex and cry. `/emote <name>` works too. Sneak to stand up after sitting.
- **Daily and weekly challenges** (`/challenges`), the same for everyone, with doubloons and accessories as rewards.
- **Feats:** 30 achievements in their own tab of the advancements screen.
- **Leaderboards:** `/leaderboard arena|range|course|drinking|duels|regatta|fishing|sinkings|rival_kills`.

### Exploration

- **Dungeon puzzles:** some rooms hold a glyph altar and three glyph stones. Turn the stones (use them) until all match
  the altar, and the rune seals around the treasure crumble.
- **Vesh Amaru, the Drowned City:** a whole city under the sea, south-west, with streets, broken houses, a temple and a
  grotto beneath the temple floor.
- **Brass diving helmet:** sold by salvage divers. It breathes for you under water (four minutes of air, refilling
  when you surface) and lets you see in the deep. Wear boots with it to walk the bottom faster.
- **Sea hazards:** five whirlpools drag and spin ships (their hearts grind the hull), four currents carry you along
  (or fight you), four fog banks swallow the horizon, and sailing into the shallows at speed runs you aground.
- **Curios and the Encyclopedia:** 32 curios to find: messages in bottles (washed up on beaches, or on a fishing
  line), ships' logs (sunken ships, vaults), paintings (vaults, drowned halls) and instruments (great bosses). Use one
  to read it; it goes into your **Encyclopedia of the Reach** (`/encyclopedia`), alongside the islands you've charted.
  The archivist buys the originals.

### Homes and crews

- **Deeds** from land agents: a house (60 doubloons), a hideout (45), a tavern (150), or a crew headquarters (300, paid
  from the crew chest by its captain). Use the deed on open ground away from town buildings: the building goes up and
  the plot is yours. Only you (or your crew, for a headquarters) can break or open things inside; guests can still
  use doors and seats. Taverns earn daily takings, more in a thriving port and during festivals. `/home`,
  `/home go` (walk home, now and then), `/home collect`, `/home sell`.
- **Crew ranks:** captain, officers, bosuns and hands (`/crew promote|demote|kick <player>`). Officers can sign on hands
  and draw from the **crew chest** (`/crew bank`, `/crew deposit|withdraw <silver>`).
- **Allies and rivals:** `/crew ally|rival|neutral <crew>`. Allied crews (both sides must agree) can't hurt each other;
  besting a rival earns Leadership. `/crew color <colour>` sets your crew's colours on members' names.

### Sound and music

Four original shanties, played across the sea and on four music discs: *The Wrecker's Reel*, *Down Where the Lanterns
Drown*, *Saltmarrow Hornpipe* and *The Ninth Wave*. New effects for clashing blades, crits and finishers, cannon fire,
ship timbers, gulls over the ports, tavern chatter, the distant Drowned Bell, whirlpools and coin.

### Gear and crafting

| Item | Recipe |
|---|---|
| Cutlass | 2 iron diagonal, gold nugget + stick at the base |
| Flintlock Pistol | Flint and steel + 2 iron in a row, planks below the middle |
| Pistol Shot (x4) | Shapeless: 2 iron nuggets, gunpowder, paper |
| Ship's Cannon | Iron blocks, gunpowder, logs |
| Cannonball (x2) | 4 iron around gunpowder |
| Hex Staff | Stick diagonal, 2 bones and an eye of ender at the top |

Quartermasters sell all of it for doubloons and buy gold, emeralds, bones and Hearts of the Sea.

## Roadmap

The full feature checklist is organized by its 10 development phases, with the four pillars at the
center. Status after this version:

| Phase | Focus | Done so far | Next |
|---|---|---|---|
| 1 | World, islands, ocean, sailing | Fixed archipelago world type, 30 islands (the full roster, a drowned city and three hidden islands, incl. a bayou, jungles, cliffs, a desert island, an atoll, sea stacks, a ship graveyard and scattered keys), reefs, volcano, black/white sand beaches, rocky coasts, discovery rewards, Sailing XP, wind, rain and storms, fog banks, whirlpools, currents, running aground, the drowned city | More hidden places |
| 2 | Character, combat, progression | Character creation, appearance editor, nine accessory slots, biographies, 6 proficiencies with skill trees (36 perks), notoriety titles, blades, sabers, dual blades, heavy swords, war hammers, firearms incl. double-barrel and hand cannon, hexes, parry and dodge, combos, crits, finishers, heavy attacks, stagger, six ultimates, gear grades | |
| 3 | **Ships, naval combat, crew** | 6 ship classes (one-, two- and three-masted), walkable interiors below deck, anchors, swivel guns, ten flags, deeds, naming, stowing, upgrades, dyed sails, hired gunners/bosuns/navigators with wages, wind and sails, cargo holds, hull damage, repairs, sinking, four kinds of shot, broadsides, ghost ships, navy patrols, merchant convoys, rival pirates, boarding and capture | Ship-to-ship boarding animations |
| 4 | NPCs, settlements, economy | Saltmarrow freeport, Port Meridian, a mining camp, a pirate hideout, Kingsport, Raven's Landing, Saint Vesper, Mariner's Rest, Crownwatch; specialist vendors, a black market, a bank, healing; silver and doubloons; 13 trade goods with port supply and demand, moving prices, shortages and gluts; auction house; ship buy-back and insurance; opening hours, town prosperity, peddlers, bounty hunters, wanted posters, a secret wanderer, festivals | |
| 5 | Quests, factions, story | Main story "The Drowned Bell"; second arc "The Seven Tides" (seven relics, a secret society, a pirate coalition's betrayal, a ghost flagship, a final boss, three endings with lasting effects); Tidewatch Navy, Saltmarrow Brethren and Raven's Landing Smugglers with reputation, ranks, rewards, bounties, pardons and repeatable contracts; four side quests, one with a permanent choice | A fourth story arc |
| 6 | Dungeons, bosses, supernatural | 53 themed multi-room dungeons with traps, spawners, mini-bosses, keys, sealed vaults and legendary loot; Captain Thorne; a haunted mansion; ghost ships; the Brinewyrm; four outer-reach bosses; wildlife (sharks, crocodiles, jaguars, snakes, boars, crabs, tameable monkeys); glyph puzzles | |
| 7 | Crafting, professions, equipment | Weapon and ammo recipes, clothing sets, ship upgrades and crew articles; five professions (fishing with 7 local species and a contest, cooking, brewing, smithing upgrades, hunting) and Leadership; a diving helmet | |
| 8 | Multiplayer, guilds, social | Per-player progress, crews with shared holds and chat, duels, Texas Hold'em and dice, player trading with escrow, an auction house; crew ranks, a crew chest, alliances and rivalries, crew colours, houses, hideouts, taverns and crew headquarters with claims; emotes and sitting; a sword tournament, shooting range, foot race, drinking contests and parcels; leaderboards | |
| 9 | Endgame, events, collectibles | Treasure maps, fragments and legendary maps; named legendary weapons; journal and chart screens; five world events; 32 curios and the Encyclopedia; seasonal festivals; daily and weekly challenges; 30 feats | More collections |
| 10 | Polish | Four original shanties and music discs, sound effects, ambience, emote motions | Custom models and player animations |

## Notes

- Cannonball blasts hurt mobs but don't break blocks. To change that, edit `CannonballEntity.java`
  (`World.ExplosionSourceType.NONE` to `TNT`).
- Version numbers live in `gradle.properties`. If Gradle can't find one, check https://fabricmc.net/develop
  for current 1.21.1 values.

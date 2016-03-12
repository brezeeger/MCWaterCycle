# MCWaterCycle
This will eventually be a minecraft mod to make water act realistically. Water will be finite and flow more realistically. It will soak into the ground, evaporate, and replenish from rain, etc. With any luck, I can do this very efficiently with little lag, but it's my first mod.

I've never programmed in Java, and obviously have never made a mod before. However, I've been programming for just shy of 20 years and don't expect picking it up to be too obnoxious. It's just a matter of learning the ins/outs of the forge API.

Water will be split into 8 levels of wetness - just as source and the 7 states of flowing water. One bucket is the equivalent of taking out one level. This means you can take water from flowing water. One water 'source' should provide 8 buckets of water.

Water will flow to lower levels, provided there is a difference of two (prevent bouncing back and forth). When it flows, water depletes from the source and rises in the target. A water level of 1 on ground adjacent to an air block will fall off the ledge. It will not search for ledges as minecraft currently operates. If water attempts to flow from one spot to another, and water is above it, it will search from above and steal from the topmost connected water (stopping at falling water).

Falling water landing on water will become flowing water only if the block below is a water source.

During rain, the highest y below cloud level will randomly have a bucket of water placed on them to represent a puddle. Lots of rain will replenish lakes and flow to low areas.

Water in the ocean at y=63 will never deplete, making it the only 'infinite' water source.

Rivers are also at y=63.  They should be depletable. As a first approximation, I will make them SLOWLY refill to a full y=63 value by adding 1 water content to surface water blocks in river biomes to represent flow from upstream. It's a very passive, flat river.

There will not be a concept of pressure allowing water to shoot up. That would require too many calculations and bog down the system.

Addition of blocks: Mud (dirt), wet grass, wet sand (sand), wet cobble (cobble), and wet gravel (gravel).  These will be blocks with water a water content in them. Breaking them will subtract 1 water content, give you the block (with 1 water content), and leave behind the rest of the water (divide by 2, round down).  Water will seep into dirt, grass, sand, gravel, and cobblestone. Maybe more in the future. In this sense, the water can travel underground. If wet blocks are next to or above air blocks, the water may leak into the air (as if placing a bucket in the adjacent tile). Likewise, it may convert more dirt into mud. Placing these blocks in or next to a running furnace will result in going back to the original dry block (except wet grass-->dirt).

Placing the dry counterparts on spaces with water will result in that water being absorbed in the block. AKA, placing dirt on a water source will result in mud with a wetness of 8 being placed. Digging that mud will give you a mud with water 1 and leave 3 waters begind. Placing the mud back in the water will result in mud with 4 water. Digging it back will result in mud in your inventory, leaving 1 water behind. Placing the mud back will then be mud with 2 water. Digging it will result in a mud in your inventory and zero water left behind. Further placing and digging of mud will just put mud in and out of your inventory. It needs to dry out before becoming dirt.

Placing other blocks in water will displace the water. The water will preferentially displace downward. Then to the sides, and then above. If this fails, it will attempt to convert any adjacent blocks to their wet equivalents. If this somehow fails (which would require the water being totally closed in), all the water will splash out to the block the player's head is in. This will startle the player, causing him or her to knock off/drop any items that the player's head may overlap with (signs, sugar cane/reeds, torches, etc.).  Any excess water is just lost.  The water god of mass conservation may get angry and create a thunderstorm with permanently destructive lightning damage, focusing around the player and the chunk this occurs in.

If there are 'non-sealing' dry blocks over water, it may evaporate. Evaporation occurs slowly based on the water level of the block. Evaporation will not occur while it is raining outside as the humidity is high. This also applies to water in wet blocks. The odds of evaporation decrease based on how far below dry air it is. After a certain point, it can't evaporate. Certain blocks will act as a sealant. This occurs if it's not ocean water, and it's daytime light level > 9 (enough 'heat').

1 block of snow will melt into 1 bucket of water. Snow ground cover that melts will turn into one bucket of water, preferentially seeping into the ground beneath. This means breaking the block beneath will not result in any water being left. Snow covered blocks receiving snow will transform into snow blocks (slowly) during 'rain' and 'thunder' storms.

Given a river dam, I have no clue which direction is upstream and which is downstream, so I have no idea which side should increase in water and which side shoud decrease. I suppose the best way is for the river pressure to eventually break the dam at random locations until the river flow can be handled. Eventually. Although it sure would be the ultimate/hilarious grief to build a dam and then flood somebody...

I plan to eventually add a hydro dam to generate MJ, where the power output would be the water outflow rate (0-8) times the minimum of (water height column or dam height) immediately next to the dam. The dam would be able to 'push' water sources up to the height of the water on the other side (the other side height will reduce the power output). If it can't (no air to push water into), the emergency stop will kick in and the user will have to manually reset the emergency stop.  In order for the dam to function, it must be rectangular. Any adjacent non-water blocks that would extend the dam can not touch wet blocks (or water). This means the dam must be a rectangle completely blocking the water and extending sufficiently into the surrounding ground.

Doing all this by tick would be obnoxious. I instead will follow a kinetic monte carlo style of using a random number generator to determine when to update blocks for things like seepage, and process the block based on immediate surroundings appropriately. It will either generate another block update in the future, or say "I'm steady state" and ignore further processing on the block.

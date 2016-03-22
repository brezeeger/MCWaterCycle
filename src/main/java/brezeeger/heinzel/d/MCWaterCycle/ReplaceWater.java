package brezeeger.heinzel.d.MCWaterCycle;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import brezeeger.heinzel.d.MCWaterCycle.MCWaterCycle;
import net.minecraft.block.state.IBlockState;//withProperty;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidFinite;

public class ReplaceWater {

// for some reason the PopulateChunkEvents are fired on the main EVENT_BUS
// even though they are in the terraingen package
	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(PopulateChunkEvent.Pre event)
	{
		// replace all blocks of a type with another block type
	    // diesieben07 came up with this method (http://www.minecraftforge.net/forum/index.php/topic,21625.0.html)
        
		Chunk chunk = event.world.getChunkFromChunkCoords(event.chunkX, event.chunkZ);
		Block fromBlock = Blocks.water; // change this to suit your need
		Block toBlock = MCWaterCycle.finiteWater; // change this to suit your need
//		System.out.println("Replacing water in chunk");

		for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) 
		{
		    if (storage != null) 
		    {
		        for (int x = 0; x < 16; ++x) 
		        {
		            for (int y = 0; y < 16; ++y) 
		            {
		                for (int z = 0; z < 16; ++z) 
		                {
		                    if (storage.getBlockByExtId(x, y, z) == Blocks.water) //replace all source blocks with full finite water
		                        storage.set(x, y, z,MCWaterCycle.finiteWater.getDefaultState().withProperty(BlockFluidBase.LEVEL, 7));

							if (storage.getBlockByExtId(x, y, z) == Blocks.flowing_water) //replace all flowing water (nothings) with air
								storage.set(x,y,z,Blocks.air.getDefaultState());
		                }
		            }
		        }
		    }
		} 
//		System.out.println("Done replacing water in a chunk");
	    chunk.setChunkModified(); // this is important as it marks it to be saved
		return;
	}
}

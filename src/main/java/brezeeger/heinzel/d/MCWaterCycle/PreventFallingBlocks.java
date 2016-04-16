package brezeeger.heinzel.d.MCWaterCycle;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraft.block.BlockLiquid;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import brezeeger.heinzel.d.MCWaterCycle.MCWaterCycle;
import net.minecraft.block.state.IBlockState;//withProperty;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraft.world.World;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;

import java.util.Random;


public class PreventFallingBlocks implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		//0 is main world, 1 is end, -1 is nether
		if(world.provider.getDimensionId() == 0)
		{

			//chunkGenerator should be what chunk is generating, and chunkProvider is what is requesting the generation
			Chunk chunk = chunkProvider.provideChunk(chunkX, chunkZ);
			boolean modified = false;


			ExtendedBlockStorage[] allStorage = chunk.getBlockStorageArray();
			int size = allStorage.length;
			char flags;
			for (int i=0; i<size; i++) 
			{
				if (allStorage[i] != null) 
				{
			        for (int x = 0; x < 16; ++x) 
				    {
					    for (int y = 0; y < 16; ++y) 
					    {
							for (int z = 0; z < 16; ++z) 
							{
								if(y==0 && i>0)
								{
									if(allStorage[i-1] != null)
									{
										if(allStorage[i-1].getBlockByExtId(x,15,z) == Blocks.air || allStorage[i-1].getBlockByExtId(x,15,z) == Blocks.water || allStorage[i-1].getBlockByExtId(x,15,z) == Blocks.flowing_water
											|| allStorage[i-1].getBlockByExtId(x,15,z) == Blocks.lava || allStorage[i-1].getBlockByExtId(x,15,z) == Blocks.flowing_lava)
										{
											if(allStorage[i].getBlockByExtId(x, y, z) == Blocks.sand)
											{
												allStorage[i-1].set(x,15,z,Blocks.sandstone.getDefaultState());
												modified = true;
											}
											else if(allStorage[i].getBlockByExtId(x, y, z) == Blocks.gravel)
											{
												allStorage[i-1].set(x,15,z,Blocks.stone.getDefaultState());
												modified = true;
											}
										}
									}
								}
								else if(y>0)
								{
									if(allStorage[i].getBlockByExtId(x,y-1,z) == Blocks.air || allStorage[i].getBlockByExtId(x,y-1,z) == Blocks.water || allStorage[i].getBlockByExtId(x,y-1,z) == Blocks.flowing_water
									|| allStorage[i].getBlockByExtId(x,y-1,z) == Blocks.lava || allStorage[i].getBlockByExtId(x,y-1,z) == Blocks.flowing_lava)
									{
										if(allStorage[i].getBlockByExtId(x, y, z) == Blocks.sand)
										{
											allStorage[i].set(x,y-1,z,Blocks.sandstone.getDefaultState());
											modified = true;
										}
										if(allStorage[i].getBlockByExtId(x, y, z) == Blocks.gravel)
										{
											allStorage[i].set(x,y-1,z,Blocks.stone.getDefaultState());
											modified = true;
										}
									}
								}
							}
						}
					}
				}
			}

			
			if(modified)
				chunk.setChunkModified(); 
				
		}	//end it is the main world
	}	//end generate function
}

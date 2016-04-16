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


public class ReplaceBlock implements IWorldGenerator {

	Block srcBlock;
	Block trgBlock;
	

	ReplaceBlock(Block src, Block trg)
	{
		srcBlock = src;
		trgBlock = trg;
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		//0 is main world, 1 is end, -1 is nether
		if(world.provider.getDimensionId() == 0)
		{
		
//			Go this route if directly modifying the chunk does not work!
//This works, but it causes block updates on all the blocks.
/*
			int baseX = chunkX * 16;
			int baseZ = chunkZ * 16;
			for (int x = baseX; x < baseX+16; ++x) 
			{
				for (int y = 0; y < 255; ++y) 
				{
					for (int z = baseZ; z < baseZ+16; ++z) 
					{
						BlockPos pos = new BlockPos(x,y,z);
						if (world.getBlockState(pos).getBlock() == srcBlock)
						{
							world.setBlockState(pos, trgBlock.getDefaultState(), 0);	//change block, but don't update neighbors. Don't mark block as updated either!
							//does not prevent bad things from happening
						}
					}
				}
			}
		
//			System.out.println("Replacing water in chunk: "+chunkX+", "+chunkZ);
*/
			//chunkGenerator should be what chunk is generating, and chunkProvider is what is requesting the generation
			Chunk chunk = chunkProvider.provideChunk(chunkX, chunkZ);
			boolean modified = false;
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
								if(srcBlock == Blocks.water || srcBlock == Blocks.flowing_water)
								{
									if (storage.getBlockByExtId(x, y, z) == srcBlock) //replace all source blocks with full finite water
									{
										IBlockState state = storage.get(x,y,z);
										int lvl = ((Integer)state.getValue(BlockLiquid.LEVEL)).intValue();	//0-15, 0=minimal, 8=full falling.
										if(lvl==0)
											storage.set(x, y, z,trgBlock.getDefaultState());
										else
											storage.set(x,y,z,Blocks.air.getDefaultState());

										modified = true;
									}
								}
								else if (storage.getBlockByExtId(x, y, z) == srcBlock) //replace all source blocks with full finite water
								{
									storage.set(x, y, z,trgBlock.getDefaultState());
									modified = true;
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

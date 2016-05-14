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

import com.google.common.collect.Maps;
import java.util.Map;

import brezeeger.heinzel.d.MCWaterCycle.Fluids.FiniteFluid;
import brezeeger.heinzel.d.MCWaterCycle.Blocks.WetBlock;

import java.util.Random;

public class ConvertSafeFiniteWater implements IWorldGenerator {

	protected final static Map<Block, Boolean> FallBlocks = Maps.newHashMap();	//things sand/gravel may fall into
	protected final static Map<Block, Boolean> ContainWater = Maps.newHashMap();
//	protected final static Map<Block, Boolean> ReplaceWater = Maps.newHashMap();
	final static char BLOCKNY = 1;
	final static char BLOCKPY = 2;
	final static char BLOCKPX = 4;
	final static char BLOCKNX = 8;
	final static char BLOCKPZ = 16;
	final static char BLOCKNZ = 32;
	final static char BLOCKADDED = 63;
	final static char ADJ_WATER = 64;

	ConvertSafeFiniteWater()
	{
		super();
		FallBlocks.put(Blocks.air, false);
		FallBlocks.put(Blocks.water, false);
		FallBlocks.put(Blocks.flowing_water, false);
		FallBlocks.put(Blocks.lava, false);
		FallBlocks.put(Blocks.flowing_lava, false);
		FallBlocks.put(MCWaterCycle.finiteWater, false);
		

//		ContainWater.putAll(((FiniteFluid)MCWaterCycle.finiteWater).GetDisplacements());
//		ContainWater.put(Blocks.air, true);	//true means make into stone
		ContainWater.put(Blocks.grass,false);	//false means there is a wetblock equivalent
		ContainWater.put(Blocks.dirt,false);	//false means there is a wetblock equivalent
		ContainWater.put(Blocks.sand,false);	//false means there is a wetblock equivalent
		ContainWater.put(Blocks.cobblestone,false);	//false means there is a wetblock equivalent
		ContainWater.put(Blocks.gravel,false);	//false means there is a wetblock equivalent

//		ReplaceWater.put(Blocks.water,false);
//		ReplaceWater.put(Blocks.flowing_water,false);
//		ReplaceWater.put(MCWaterCycle.finiteWater,false);

	}

	private char containFiniteWater(int x, int y, int z, ExtendedBlockStorage miniChunk, ExtendedBlockStorage mchunkPX, ExtendedBlockStorage mchunkNX,
							ExtendedBlockStorage mchunkPZ, ExtendedBlockStorage mchunkNZ, ExtendedBlockStorage mchunkPY, ExtendedBlockStorage mchunkNY)
	{
		char flags=0;
		Block BlkCur = miniChunk.getBlockByExtId(x,y,z);
		Block BlkPX = (x<15) ? miniChunk.getBlockByExtId(x+1,y,z) : ((mchunkPX!=null) ? mchunkPX.getBlockByExtId(0,y,z) : null);
		Block BlkPY = (y<15) ? miniChunk.getBlockByExtId(x,y+1,z) : ((mchunkPY!=null) ? mchunkPY.getBlockByExtId(x,0,z) : null);
		Block BlkPZ = (z<15) ? miniChunk.getBlockByExtId(x,y,z+1) : ((mchunkPZ!=null) ? mchunkPZ.getBlockByExtId(x,y,0) : null);
		Block BlkNX = (x>0) ? miniChunk.getBlockByExtId(x-1,y,z) : ((mchunkNX!=null) ? mchunkNX.getBlockByExtId(15,y,z) : null);
		Block BlkNY = (y>0) ? miniChunk.getBlockByExtId(x,y-1,z) : ((mchunkNY!=null) ? mchunkNY.getBlockByExtId(x,15,z) : null);
		Block BlkNZ = (z>0) ? miniChunk.getBlockByExtId(x,y,z-1) : ((mchunkNZ!=null) ? mchunkNZ.getBlockByExtId(x,y,15) : null);
		
		//test the primary block for water, and contain it if necessary
		boolean replaceBlock;
		boolean containBlock;
		if(BlkCur == MCWaterCycle.finiteWater)
		{
			//first, test PX BLock.
			if(BlkPX != null)
			{
				replaceBlock = false;
				containBlock = false;
				if(((FiniteFluid)MCWaterCycle.finiteWater).canDisplace(BlkPX))
					containBlock = true;
				else if(ContainWater.containsKey(BlkPX))
					replaceBlock = true;

				if(containBlock)
				{
					if(x<15)
						miniChunk.set(x+1,y,z,Blocks.stone.getDefaultState());
					else
						mchunkPX.set(0,y,z,Blocks.stone.getDefaultState());

					flags = ((char)(flags | BLOCKPX));
				}
				else if(replaceBlock)
				{
					IBlockState state;
					if(BlkPX == Blocks.grass)
						state = MCWaterCycle.WetGrass.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetGrass).MaxWater-1);
					else if(BlkPX == Blocks.dirt)
						state = MCWaterCycle.WetDirt.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetDirt).MaxWater-1);
					else if(BlkPX == Blocks.sand)
						state = MCWaterCycle.WetSand.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetSand).MaxWater-1);
					else if(BlkPX == Blocks.gravel)
						state = MCWaterCycle.WetGravel.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetGravel).MaxWater-1);
					else if(BlkPX == Blocks.cobblestone)
						state = MCWaterCycle.WetCobble.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetCobble).MaxWater-1);
					else
						state = Blocks.stone.getDefaultState();

					if(x<15)
						miniChunk.set(x+1,y,z,state);
					else
						mchunkPX.set(0,y,z,state);
					flags = ((char)(flags | BLOCKPX));
				}
			}
			/////////////////////////////////////////////////////
			if(BlkPZ != null)
			{
				replaceBlock = false;
				containBlock = false;
				if(((FiniteFluid)MCWaterCycle.finiteWater).canDisplace(BlkPZ))
					containBlock = true;
				else if(ContainWater.containsKey(BlkPZ))
					replaceBlock = true;

				if(containBlock)
				{
					if(z<15)
						miniChunk.set(x,y,z+1,Blocks.stone.getDefaultState());
					else
						mchunkPZ.set(x,y,0,Blocks.stone.getDefaultState());

					flags = ((char)(flags | BLOCKPZ));
				}
				else if(replaceBlock)
				{
					IBlockState state;
					if(BlkPZ == Blocks.grass)
						state = MCWaterCycle.WetGrass.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetGrass).MaxWater-1);
					else if(BlkPZ == Blocks.dirt)
						state = MCWaterCycle.WetDirt.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetDirt).MaxWater-1);
					else if(BlkPZ == Blocks.sand)
						state = MCWaterCycle.WetSand.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetSand).MaxWater-1);
					else if(BlkPZ == Blocks.gravel)
						state = MCWaterCycle.WetGravel.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetGravel).MaxWater-1);
					else if(BlkPZ == Blocks.cobblestone)
						state = MCWaterCycle.WetCobble.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetCobble).MaxWater-1);
					else
						state = Blocks.stone.getDefaultState();

					if(z<15)
						miniChunk.set(x,y,z+1,state);
					else
						mchunkPZ.set(x,y,0,state);

					flags = ((char)(flags | BLOCKPZ));
				}
			}
			///////////////////////////////////////////////////////////
			if(BlkNX != null)
			{
				replaceBlock = false;
				containBlock = false;
				if(((FiniteFluid)MCWaterCycle.finiteWater).canDisplace(BlkNX))
					containBlock = true;
				else if(ContainWater.containsKey(BlkNX))
					replaceBlock = true;

				if(containBlock)
				{
					if(x>0)
						miniChunk.set(x-1,y,z,Blocks.stone.getDefaultState());
					else
						mchunkNX.set(15,y,z,Blocks.stone.getDefaultState());
					flags = ((char)(flags | BLOCKNX));
				}
				else if(replaceBlock)
				{
					IBlockState state;
					if(BlkNX == Blocks.grass)
						state = MCWaterCycle.WetGrass.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetGrass).MaxWater-1);
					else if(BlkNX == Blocks.dirt)
						state = MCWaterCycle.WetDirt.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetDirt).MaxWater-1);
					else if(BlkNX == Blocks.sand)
						state = MCWaterCycle.WetSand.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetSand).MaxWater-1);
					else if(BlkNX == Blocks.gravel)
						state = MCWaterCycle.WetGravel.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetGravel).MaxWater-1);
					else if(BlkNX == Blocks.cobblestone)
						state = MCWaterCycle.WetCobble.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetCobble).MaxWater-1);
					else
						state = Blocks.stone.getDefaultState();

					if(x>0)
						miniChunk.set(x-1,y,z,state);
					else
						mchunkNX.set(15,y,z,state);

					flags = ((char)(flags | BLOCKNX));
				}
			}
			/////////////////////////////////////////////////////
			if(BlkNZ != null)
			{
				replaceBlock = false;
				containBlock = false;
				if(((FiniteFluid)MCWaterCycle.finiteWater).canDisplace(BlkNZ))
					containBlock = true;
				else if(ContainWater.containsKey(BlkPZ))
					replaceBlock = true;

				if(containBlock)
				{
					if(z>0)
						miniChunk.set(x,y,z-1,Blocks.stone.getDefaultState());
					else
						mchunkNZ.set(x,y,15,Blocks.stone.getDefaultState());

					flags = ((char)(flags | BLOCKNZ));
				}
				else if(replaceBlock)
				{
					IBlockState state;
					if(BlkNZ == Blocks.grass)
						state = MCWaterCycle.WetGrass.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetGrass).MaxWater-1);
					else if(BlkNZ == Blocks.dirt)
						state = MCWaterCycle.WetDirt.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetDirt).MaxWater-1);
					else if(BlkNZ == Blocks.sand)
						state = MCWaterCycle.WetSand.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetSand).MaxWater-1);
					else if(BlkNZ == Blocks.gravel)
						state = MCWaterCycle.WetGravel.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetGravel).MaxWater-1);
					else if(BlkNZ == Blocks.cobblestone)
						state = MCWaterCycle.WetCobble.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetCobble).MaxWater-1);
					else
						state = Blocks.stone.getDefaultState();

					if(z>0)
						miniChunk.set(x,y,z-1,state);
					else
						mchunkNZ.set(x,y,15,state);

					flags = ((char)(flags | BLOCKNZ));
				}
			}
			/////////////////////////////////////////////////////
			if(BlkNY != null)
			{
				replaceBlock = false;
				containBlock = false;
				if(((FiniteFluid)MCWaterCycle.finiteWater).canDisplace(BlkNY))
					containBlock = true;
				else if(ContainWater.containsKey(BlkNY))
					replaceBlock = true;

				if(containBlock)
				{
					if(y>0)
						miniChunk.set(x,y-1,z,Blocks.stone.getDefaultState());
					else
						mchunkNY.set(x,15,z,Blocks.stone.getDefaultState());

					flags = ((char)(flags | BLOCKNY));
				}
				else if(replaceBlock)
				{
					IBlockState state;
					if(BlkNY == Blocks.grass)
						state = MCWaterCycle.WetGrass.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetGrass).MaxWater-1);
					else if(BlkNY == Blocks.dirt)
						state = MCWaterCycle.WetDirt.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetDirt).MaxWater-1);
					else if(BlkNY == Blocks.sand)
						state = MCWaterCycle.WetSand.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetSand).MaxWater-1);
					else if(BlkNY == Blocks.gravel)
						state = MCWaterCycle.WetGravel.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetGravel).MaxWater-1);
					else if(BlkNY == Blocks.cobblestone)
						state = MCWaterCycle.WetCobble.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetCobble).MaxWater-1);
					else
						state = Blocks.stone.getDefaultState();

					if(y>0)
						miniChunk.set(x,y-1,z,state);
					else
						mchunkNY.set(x,15,z,state);

					flags = ((char)(flags | BLOCKNY));
				}
			}
		}
		else //the current block was not finite water, so test the neighborin blocks if they are finite water and this one needs to be modified.
		{
			if(x == 0 || x == 15 || y == 0 || y ==15 || z == 0 || z == 15)	//was this block on a boundary
			{
				if(((FiniteFluid)MCWaterCycle.finiteWater).canDisplace(BlkCur))
					containBlock = true;
				else if(ContainWater.containsKey(BlkCur))
					containBlock = true;
				else
					containBlock = false;

				if(containBlock)	//it is a block that needs to be changed if adjacent to water
				{
					if(BlkPY == Blocks.water || BlkPY == Blocks.flowing_water || BlkPY == MCWaterCycle.finiteWater || //water above from a previous chunk that may have not been loaded yet?
					BlkPX == Blocks.water || BlkPX == Blocks.flowing_water || BlkPX == MCWaterCycle.finiteWater || 
					BlkPZ == Blocks.water || BlkPZ == Blocks.flowing_water || BlkPZ == MCWaterCycle.finiteWater || 
					BlkNX == Blocks.water || BlkNX == Blocks.flowing_water || BlkNX == MCWaterCycle.finiteWater || 
					BlkNZ == Blocks.water || BlkNZ == Blocks.flowing_water || BlkNZ == MCWaterCycle.finiteWater )
					{ //it was adjacent to water
						IBlockState state;
						if(BlkCur == Blocks.grass)
							state = MCWaterCycle.WetGrass.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetGrass).MaxWater-1);
						else if(BlkCur == Blocks.dirt)
							state = MCWaterCycle.WetDirt.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetDirt).MaxWater-1);
						else if(BlkCur == Blocks.sand)
							state = MCWaterCycle.WetSand.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetSand).MaxWater-1);
						else if(BlkCur == Blocks.gravel)
							state = MCWaterCycle.WetGravel.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetGravel).MaxWater-1);
						else if(BlkCur == Blocks.cobblestone)
							state = MCWaterCycle.WetCobble.getDefaultState().withProperty(WetBlock.LEVEL,((WetBlock)MCWaterCycle.WetCobble).MaxWater-1);
						else
							state = Blocks.stone.getDefaultState();

						miniChunk.set(x,y,z,state);
					}
				}

			}
		}
		//test the neighboring x/z blocks if x/y/z=0/15 and change the source block if need be
		
		return flags;
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		Chunk chunkPX = null;	//these might not have been loaded yet! If you cause them to load by doing provide chunk
		Chunk chunkNX = null;	//it will keep generating chunks indefinitely! Not cool!
		Chunk chunkPZ = null;
		Chunk chunkNZ = null;

		Chunk chunkPXPZ = null;	//these might not have been loaded yet! If you cause them to load by doing provide chunk
		Chunk chunkPXNZ = null;	//it will keep generating chunks indefinitely! Not cool!
		Chunk chunkNXPZ = null;
		Chunk chunkNXNZ = null;

		ExtendedBlockStorage mChunkPX = null;
		ExtendedBlockStorage mChunkNX = null;
		ExtendedBlockStorage mChunkPZ = null;
		ExtendedBlockStorage mChunkNZ = null;
		ExtendedBlockStorage mChunkNY = null;
		ExtendedBlockStorage mChunkPY = null;

		ExtendedBlockStorage mChunkPXPZ = null;	//these might not have been loaded yet! If you cause them to load by doing provide chunk
		ExtendedBlockStorage mChunkPXNZ = null;	//it will keep generating chunks indefinitely! Not cool!
		ExtendedBlockStorage mChunkNXPZ = null;
		ExtendedBlockStorage mChunkNXNZ = null;
		ExtendedBlockStorage mChunkADJNY = null;

		if(chunkProvider.chunkExists(chunkX+1,chunkZ))
			chunkPX = chunkProvider.provideChunk(chunkX+1,chunkZ);

		if(chunkProvider.chunkExists(chunkX-1,chunkZ))
			chunkNX = chunkProvider.provideChunk(chunkX-1,chunkZ);

		if(chunkProvider.chunkExists(chunkX+1,chunkZ))
			chunkPZ = chunkProvider.provideChunk(chunkX,chunkZ+1);

		if(chunkProvider.chunkExists(chunkX+1,chunkZ))
			chunkNZ = chunkProvider.provideChunk(chunkX,chunkZ-1);



		if(chunkProvider.chunkExists(chunkX+1,chunkZ+1))
			chunkPXPZ = chunkProvider.provideChunk(chunkX+1,chunkZ+1);

		if(chunkProvider.chunkExists(chunkX-1,chunkZ+1))
			chunkNXPZ = chunkProvider.provideChunk(chunkX-1,chunkZ+1);

		if(chunkProvider.chunkExists(chunkX+1,chunkZ-1))
			chunkPXNZ = chunkProvider.provideChunk(chunkX+1,chunkZ-1);

		if(chunkProvider.chunkExists(chunkX-1,chunkZ-1))
			chunkNXNZ = chunkProvider.provideChunk(chunkX-1,chunkZ-1);

		Chunk chunk = chunkProvider.provideChunk(chunkX, chunkZ);

		if(world.provider.getDimensionId() == 0)
		{
			boolean modified = false;
			Block testBlock = null;
			Block blockBelow = null;
			Block blkxP = null;

			ExtendedBlockStorage[] allStorage = chunk.getBlockStorageArray();
			int size = allStorage.length;
			char flags;
			for (int i=0; i<size; i++) 
			{
				if (allStorage[i] != null) 
				{
					mChunkNY = (i>0) ? allStorage[i-1] : null;
					mChunkPY = (i<size-1) ? allStorage[i+1] : null;

					//get access to all the potential adjacent chunks
					mChunkNX = (chunkNX != null) ? chunkNX.getBlockStorageArray()[i] : null;
					mChunkPX = (chunkPX != null) ? chunkPX.getBlockStorageArray()[i] : null;
					mChunkNZ = (chunkNZ != null) ? chunkNZ.getBlockStorageArray()[i] : null;
					mChunkPZ = (chunkPZ != null) ? chunkPZ.getBlockStorageArray()[i] : null;

					mChunkPXPZ = (chunkPXPZ != null) ? chunkPXPZ.getBlockStorageArray()[i] : null;	//these might not have been loaded yet! If you cause them to load by doing provide chunk
					mChunkNXPZ = (chunkNXPZ != null) ? chunkNXPZ.getBlockStorageArray()[i] : null;
					mChunkPXNZ = (chunkPXNZ != null) ? chunkPXNZ.getBlockStorageArray()[i] : null;
					mChunkNXNZ = (chunkNXNZ != null) ? chunkNXNZ.getBlockStorageArray()[i] : null;

					

			        for (int x = 0; x < 16; ++x) 
				    {
					    for (int y = 0; y < 16; ++y) 
					    {
							for (int z = 0; z < 16; ++z) 
							{

								testBlock = allStorage[i].getBlockByExtId(x,y,z);
								if (y>0)
									blockBelow = allStorage[i].getBlockByExtId(x,y-1,z);
								else if(mChunkNY != null)
									blockBelow = mChunkNY.getBlockByExtId(x,15,z);
								else
									blockBelow = null;


								//Convert water source to finite fluid
								if(testBlock == Blocks.water || testBlock == Blocks.flowing_water)
								{
									IBlockState state = allStorage[i].get(x,y,z);
									int lvl = ((Integer)state.getValue(BlockLiquid.LEVEL)).intValue();	//0-15, 0=minimal, 8=full falling.
									if(lvl==0 || lvl==8)
										allStorage[i].set(x, y, z,MCWaterCycle.finiteWater.getDefaultState());
									else
										allStorage[i].set(x,y,z,Blocks.air.getDefaultState());	//if air, water over falling water will not be caught!

									//test surrounding blocks
								}
								
								if(FallBlocks.containsKey(blockBelow))
								{
									if(testBlock == Blocks.sand)
										allStorage[i].set(x, y, z,Blocks.sandstone.getDefaultState());	
									else if(testBlock == Blocks.gravel)
										allStorage[i].set(x,y,z,Blocks.stone.getDefaultState());
								}
								
								char flg = containFiniteWater(x,y,z, allStorage[i], mChunkPX, mChunkNX, mChunkPZ, mChunkNZ, mChunkPY, mChunkNY);
							}
						}
					}
				}
			}

			
			if(modified)
				chunk.setChunkModified(); 
		}
	}
}

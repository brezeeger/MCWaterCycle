package brezeeger.heinzel.d.MCWaterCycle;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
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

import brezeeger.heinzel.d.MCWaterCycle.Fluids.FiniteFluid;

public class ContainFiniteFluid implements IWorldGenerator {

	final Block fluidBlock;
	final Block containBlock;
	final static char BLOCKNY = 1;
	final static char BLOCKPY = 2;
	final static char BLOCKPX = 4;
	final static char BLOCKNX = 8;
	final static char BLOCKPZ = 16;
	final static char BLOCKNZ = 32;
	final static char BLOCKADDED = 63;
	final static char ADJ_WATER = 64;
	

	ContainFiniteFluid(Block src, Block contain)
	{
		if(src instanceof FiniteFluid)	//guarantee it only applies to finite fluids!
			fluidBlock = src;
		else
			fluidBlock = null;

		containBlock = contain;
	}

	private char containBlock(int x, int y, int z, ExtendedBlockStorage miniChunk, ExtendedBlockStorage mchunkPX, ExtendedBlockStorage mchunkNX,
							ExtendedBlockStorage mchunkPZ, ExtendedBlockStorage mchunkNZ, ExtendedBlockStorage mchunkPY, ExtendedBlockStorage mchunkNY,
							World world, int chunkX, int chunkZ)
	{
		//if it is connected to at least one other finite fluid, it must be contained
		//first check if this is a fluid block!
		char flags = 0;
		if (miniChunk.getBlockByExtId(x, y, z) == fluidBlock)	//getBlockByExtId cannot ever be null, so this will fail if improperly fluid passed in!
		{
			//test above
			if(y>=15)	//it's in the above mini chunk
			{
				if(mchunkPY != null)	//there is no above mini chunk
				{
					if (mchunkPY.getBlockByExtId(x, 0, z) == fluidBlock)
						flags = ((char)(flags | ADJ_WATER));
					else
					{
						if(((FiniteFluid)fluidBlock).canDisplace(world, new BlockPos(16*chunkX+x, mchunkPY.getYLocation(), 16*chunkZ+z)))
							flags = ((char)(flags | BLOCKPY));
					}
					
				}
				else
				{
//					System.out.println("Error, y+1>15 but no upper chunk!");
				}
			}
			else
			{
				if (miniChunk.getBlockByExtId(x, y+1, z) == fluidBlock)
					flags = ((char)(flags | ADJ_WATER));
				else if(((FiniteFluid)fluidBlock).canDisplace(world, new BlockPos(16*chunkX+x, miniChunk.getYLocation()+y+1, 16*chunkZ+z)))
					flags = ((char)(flags | BLOCKPY));
			}

			//test below
			if(y<=0)	//it's in the below mini chunk
			{
				if(mchunkNY != null)	//there is no above mini chunk
				{
					if (mchunkNY.getBlockByExtId(x, 15, z) == fluidBlock)
						flags = ((char)(flags | ADJ_WATER));
					else
					{
						if(((FiniteFluid)fluidBlock).canDisplace(world, new BlockPos(16*chunkX+x, mchunkNY.getYLocation()+15, 16*chunkZ+z)))
							flags = ((char)(flags | BLOCKNY));
					}
					
				}
				else
				{
//					System.out.println("Error, y<0 but no lower chunk!");
				}
			}
			else
			{
				if (miniChunk.getBlockByExtId(x, y-1, z) == fluidBlock)
					flags = ((char)(flags | ADJ_WATER));
				else if(((FiniteFluid)fluidBlock).canDisplace(world, new BlockPos(16*chunkX+x, miniChunk.getYLocation()+y-1, 16*chunkZ+z)))
					flags = ((char)(flags | BLOCKNY));
			}
			///////////////////////////////

			//test PX
			if(x>=15)	//it's in the above mini chunk
			{
				if(mchunkPX != null)	//there is no above mini chunk
				{
					if (mchunkPX.getBlockByExtId(0, y, z) == fluidBlock)
						flags = ((char)(flags | ADJ_WATER));
					else
					{
						if(((FiniteFluid)fluidBlock).canDisplace(world, new BlockPos(16*(chunkX+1), miniChunk.getYLocation()+y, 16*chunkZ+z)))
							flags = ((char)(flags | BLOCKPX));
					}
					
				}
				else
				{
		//			System.out.println("Error, x>=15 but no chunk!");
				}
			}
			else
			{
				if (miniChunk.getBlockByExtId(x+1, y, z) == fluidBlock)
					flags = ((char)(flags | ADJ_WATER));
				else if(((FiniteFluid)fluidBlock).canDisplace(world, new BlockPos(16*chunkX+x+1, miniChunk.getYLocation()+y, 16*chunkZ+z)))
					flags = ((char)(flags | BLOCKPX));
			}

			//test NX
			if(x<=0)	//it's in the above mini chunk
			{
				if(mchunkNX != null)	//there is no above mini chunk
				{
					if (mchunkNX.getBlockByExtId(15, y, z) == fluidBlock)
						flags = ((char)(flags | ADJ_WATER));
					else
					{
						if(((FiniteFluid)fluidBlock).canDisplace(world, new BlockPos(16*(chunkX)-1, miniChunk.getYLocation()+y, 16*chunkZ+z)))
							flags = ((char)(flags | BLOCKNX));
					}
					
				}
				else
				{
		//			System.out.println("Error, x<=0 but no chunk!");
				}
			}
			else
			{
				if (miniChunk.getBlockByExtId(x-1, y, z) == fluidBlock)
					flags = ((char)(flags | ADJ_WATER));
				else if(((FiniteFluid)fluidBlock).canDisplace(world, new BlockPos(16*chunkX+x-1, miniChunk.getYLocation()+y, 16*chunkZ+z)))
					flags = ((char)(flags | BLOCKNX));
			}

			/////////////////////////
			//test PZ
			if(z>=15)	//it's in the above mini chunk
			{
				if(mchunkPZ != null)	//there is no above mini chunk
				{
					if (mchunkPZ.getBlockByExtId(x, y, 0) == fluidBlock)
						flags = ((char)(flags | ADJ_WATER));
					else
					{
						if(((FiniteFluid)fluidBlock).canDisplace(world, new BlockPos(16*chunkX+x, miniChunk.getYLocation()+y, 16*(chunkZ+1))))
							flags = ((char)(flags | BLOCKPZ));
					}
					
				}
				else
				{
		//			System.out.println("Error, z>=15 but no chunk!");
				}
			}
			else
			{
				if (miniChunk.getBlockByExtId(x, y, z+1) == fluidBlock)
					flags = ((char)(flags | ADJ_WATER));
				else if(((FiniteFluid)fluidBlock).canDisplace(world, new BlockPos(16*chunkX+x, miniChunk.getYLocation()+y, 16*chunkZ+z+1)))
					flags = ((char)(flags | BLOCKPZ));
			}

			//test NZ
			if(z<=0)	//it's in the above mini chunk
			{
				if(mchunkNZ != null)	//there is no above mini chunk
				{
					if (mchunkNZ.getBlockByExtId(x, y, 15) == fluidBlock)
						flags = ((char)(flags | ADJ_WATER));
					else
					{
						if(((FiniteFluid)fluidBlock).canDisplace(world, new BlockPos(16*(chunkX)+x, miniChunk.getYLocation()+y, 16*chunkZ-1)))
							flags = ((char)(flags | BLOCKNZ));
					}
					
				}
				else
				{
		//			System.out.println("Error, z<=0 but no chunk!");
				}
			}
			else
			{
				if (miniChunk.getBlockByExtId(x, y, z-1) == fluidBlock)
					flags = ((char)(flags | ADJ_WATER));
				else if(((FiniteFluid)fluidBlock).canDisplace(world, new BlockPos(16*chunkX+x, miniChunk.getYLocation()+y, 16*chunkZ+z-1)))
					flags = ((char)(flags | BLOCKNZ));
			}
		}


//		if((flags & ADJ_WATER) != 0)	//it needs to update the blocks!
//		{
			if((flags & BLOCKNZ) != 0)
			{
				if(mchunkNZ != null)
				{
					mchunkNZ.set(x, y, 15, containBlock.getDefaultState());
//					System.out.println("Block replaced (X,Y,Z): ("+(chunkX*16+x)+","+(miniChunk.getYLocation()+y)+","+(chunkZ*16+z-1)+")");
				}
				else if(z>0)	//need to include in case z=0. but nchunkNZ is null because the chunk hasn't been generated yet!
				{
					miniChunk.set(x,y,z-1, containBlock.getDefaultState());
//					System.out.println("Block replaced (X,Y,Z): ("+(chunkX*16+x)+","+(miniChunk.getYLocation()+y)+","+(chunkZ*16+z-1)+")");
				}
			}

			if((flags & BLOCKPZ) != 0)
			{
				if(mchunkPZ != null)
				{
					mchunkPZ.set(x, y, 0, containBlock.getDefaultState());
//					System.out.println("Block replaced (X,Y,Z): ("+(chunkX*16+x)+","+(miniChunk.getYLocation()+y)+","+(chunkZ*16+z+1)+")");
				}
				else if(z<15)
				{
					miniChunk.set(x,y,z+1, containBlock.getDefaultState());
//					System.out.println("Block replaced (X,Y,Z): ("+(chunkX*16+x)+","+(miniChunk.getYLocation()+y)+","+(chunkZ*16+z+1)+")");
				}
			}




			if((flags & BLOCKNX) != 0)
			{
				if(mchunkNX != null)
				{
					mchunkNX.set(15, y, z, containBlock.getDefaultState());
//					System.out.println("Block replaced (X,Y,Z): ("+(chunkX*16+x-1)+","+(miniChunk.getYLocation()+y)+","+(chunkZ*16+z)+")");
				}
				else if(x>0)
				{
					miniChunk.set(x-1,y,z, containBlock.getDefaultState());
//					System.out.println("Block replaced (X,Y,Z): ("+(chunkX*16+x-1)+","+(miniChunk.getYLocation()+y)+","+(chunkZ*16+z)+")");
				}
			}

			if((flags & BLOCKPX) != 0)
			{
				if(mchunkPX != null)
				{
					mchunkPX.set(0, y, z, containBlock.getDefaultState());
//					System.out.println("Block replaced (X,Y,Z): ("+(chunkX*16+x+1)+","+(miniChunk.getYLocation()+y)+","+(chunkZ*16+z)+")");
				}
				else if(x<15)
				{
					miniChunk.set(x+1,y,z, containBlock.getDefaultState());
//					System.out.println("Block replaced (X,Y,Z): ("+(chunkX*16+x+1)+","+(miniChunk.getYLocation()+y)+","+(chunkZ*16+z)+")");
				}
			}

			if((flags & BLOCKNY) != 0)
			{
				if(mchunkNY != null)
				{
					mchunkNY.set(x, 15, z, containBlock.getDefaultState());
//					System.out.println("Block replaced (X,Y,Z): ("+(chunkX*16+x)+","+(miniChunk.getYLocation()+y-1)+","+(chunkZ*16+z)+")");
				}
				else if(y>0)
				{
					miniChunk.set(x,y-1,z, containBlock.getDefaultState());
//					System.out.println("Block replaced (X,Y,Z): ("+(chunkX*16+x)+","+(miniChunk.getYLocation()+y-1)+","+(chunkZ*16+z)+")");
				}
			}
		//	if((flags & BLOCKADDED) != 0)
	//		{
//				System.out.println("Block replaced in chunk: ("+chunkX+","+chunkZ+") at y: "+miniChunk.getYLocation());
				
//			}

//		}

		return(flags);
	}


	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
//		System.out.println("Checking chunk: ("+chunkX+","+chunkZ+")");
		Chunk chunk = chunkProvider.provideChunk(chunkX, chunkZ);	//only guaranteed one!
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
					if(x==0 && chunkNX != null)
						mChunkNX = chunkNX.getBlockStorageArray()[i];
					if(x==15 && chunkPX != null)
						mChunkPX = chunkPX.getBlockStorageArray()[i];
				    for (int y = 0; y < 16; ++y) 
		            {
						if(y==0 && i > 0)
							mChunkNY = allStorage[i-1];
						if(y==15 && i < size-1)
							mChunkPY = allStorage[i+1];
						for (int z = 0; z < 16; ++z) 
						{
							if(z==0 && chunkNZ != null)
							{
								mChunkNZ = chunkNZ.getBlockStorageArray()[i];
								if(x==0 && chunkNXNZ != null)
								{
									mChunkNXNZ = chunkNXNZ.getBlockStorageArray()[i];
								}
								else if(x==15 && chunkPXNZ != null)
								{
									mChunkPXNZ = chunkPXNZ.getBlockStorageArray()[i];
								}
							}
							if(z==15 && chunkPZ != null)
							{
								mChunkPZ = chunkPZ.getBlockStorageArray()[i];
								if(x==0 && chunkNXPZ != null)
								{
									mChunkNXPZ = chunkNXPZ.getBlockStorageArray()[i];
								}
								else if(x==15 && chunkPXPZ != null)
								{
									mChunkPXPZ = chunkPXPZ.getBlockStorageArray()[i];
								}
							}

							flags = containBlock(x, y, z, allStorage[i],  mChunkPX,  mChunkNX, mChunkPZ,  mChunkNZ,  mChunkPY,  mChunkNY, world, chunkX, chunkZ);

							if(/*(flags & ADJ_WATER) != 0 && */(flags & BLOCKADDED) != 0)
								modified = true;

							//if it's on one of the edges of the chunk, we need to check to see if this new interface will have put holes next to adjacent water that might drain
							
							if(mChunkPX != null)	//then x was 15
							{
								mChunkADJNY = (i>0) ? chunkPX.getBlockStorageArray()[i-1] : null;

								flags = containBlock(0, y, z, mChunkPX,  null,  allStorage[i], mChunkPXPZ,  mChunkPXNZ,  null,  mChunkADJNY, world, chunkX, chunkZ);
								if(/*(flags & ADJ_WATER) != 0 && */(flags & BLOCKADDED) != 0)
									modified = true;
							}	

							if(mChunkNX != null)	//then x was 0
							{
								mChunkADJNY = (i>0) ? chunkNX.getBlockStorageArray()[i-1] : null;
								flags = containBlock(15, y, z, mChunkNX, allStorage[i], null, mChunkNXPZ,  mChunkNXNZ,  null,  mChunkADJNY, world, chunkX, chunkZ);
								if(/*(flags & ADJ_WATER) != 0 && */(flags & BLOCKADDED) != 0)
									modified = true;
							}

							if(mChunkPZ != null)	//then z was 15
							{
								mChunkADJNY = (i>0) ? chunkPZ.getBlockStorageArray()[i-1] : null;
								flags = containBlock(x, y, 0, mChunkPZ,  mChunkPXPZ,  mChunkNXPZ,  null, allStorage[i], null,  mChunkADJNY, world, chunkX, chunkZ);
								if(/*(flags & ADJ_WATER) != 0 && */ (flags & BLOCKADDED) != 0)
									modified = true;
							}	

							if(mChunkNZ != null)	//then z was 0
							{
								mChunkADJNY = (i>0) ? chunkNZ.getBlockStorageArray()[i-1] : null;
								flags = containBlock(x, y, 15, mChunkNZ, mChunkPXNZ, mChunkNXNZ, allStorage[i], null,  null,  mChunkADJNY, world, chunkX, chunkZ);
								if(/*(flags & ADJ_WATER) != 0 && */(flags & BLOCKADDED) != 0)
									modified = true;
							}

							if(mChunkNY != null)	//then y was 0
							{
								flags = containBlock(x, 15, z, mChunkNY, null, null, null, null,  allStorage[i],  null, world, chunkX, chunkZ);
								if(/*(flags & ADJ_WATER) != 0 && */(flags & BLOCKADDED) != 0)
									modified = true;
							}

							if(mChunkPY != null)	//then y was 15
							{
								flags = containBlock(x, 0, z, mChunkPY, null, null, null, null,  null,  allStorage[i], world, chunkX, chunkZ);
								if(/*(flags & ADJ_WATER) != 0 && */(flags & BLOCKADDED) != 0)
									modified = true;
							}


							mChunkNXNZ = null;
							mChunkNXPZ = null;
							mChunkPXPZ = null;
							mChunkPXNZ = null;

							mChunkNZ = null;
							mChunkPZ = null;
							
						}
						mChunkNY = null;	//after it changes value, it will always become invalid
						mChunkPY = null;
					}
					mChunkNX = null;	//after it changes value, it will always become invalid
					mChunkPX = null;
				}
			}
		}




		if(modified)
		{
			chunk.setChunkModified(); 
			if(chunkPX != null)
				chunkPX.setChunkModified();
			if(chunkNX != null)
				chunkNX.setChunkModified();
			if(chunkPZ != null)
				chunkPZ.setChunkModified();
			if(chunkNZ != null)
				chunkNZ.setChunkModified();

//			System.out.println("Modified Chunk: "+chunkX+ ", "+chunkZ);
		}
	}
}

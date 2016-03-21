package brezeeger.heinzel.d.MCWaterCycle.Fluids;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBucket;
//import net.minecraft.item.IIcon;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.world.World;
//import net.minecraft.world.scheduleBlockUpdate;
import net.minecraft.util.BlockPos;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.init.Items;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenOcean;
import net.minecraft.world.biome.BiomeGenRiver;
import net.minecraft.world.IBlockAccess;

import brezeeger.heinzel.d.MCWaterCycle.MCWaterCycle;

//import net.minecraft.util.IIcon;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.Fluid;

//import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class FiniteFluid extends BlockFluidFinite implements IFluidBlock {

	//the name of the liquid
	protected final String name;

	//a link to the type of fluid it is
	protected final Fluid fluid;

	protected final static int SEALEVEL=62;

//	protected TextureAtlasSprite IconStill;
//	protected TextureAtlasSprite IconFlow;

	//capacity of block in mB
	public final int capacity;	//can't be any more than 16! 8 if include falling liquids

	//what is the default amount to remove from this block when attempting to remove liquid?
	protected final FluidStack stack;

	//an ID for this particular liquid Should match fluid registry
	public final int id;

	public FiniteFluid(Material mat, Fluid flu, int visc, int density, int light, int capacity, String nm)
	{
		super(flu, mat);	//this is actually creating a new block!
		fluid = flu;
		FluidRegistry.registerFluid(fluid);
		fluid.setViscosity(visc);
		fluid.setDensity(density);
		fluid.setLuminosity(light);
		fluid.setTemperature(295);
		fluid.setGaseous(false);
		this.setTickRandomly(false);	//finite fluids will be calculated at specific times!
		this.setTickRate(visc/200);
		this.id = FluidRegistry.getFluidID(fluid.getName());
		this.name=nm;
		if(capacity > 8 || capacity < 1)	//default to relatively standard behavior
			capacity = 8;
		this.capacity = capacity;
		this.setLightOpacity(10);
		stack = new FluidStack(flu, capacity * FluidContainerRegistry.BUCKET_VOLUME / 8);	//just for easy reference! (8 from 8 levels of water to render)
		setUnlocalizedName(nm);
		setCreativeTab(CreativeTabs.tabBlock);
		GameRegistry.registerBlock(this, nm);	//add the block to the registry
		this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, 7));	//default to a full block! This already happens because of finite fluid
		//testing with a full block
	}

	//@Override
	public String getName() { return(this.name); }

	@Override
	public Fluid getFluid()
	{
		return(this.fluid);
	}

	@Override
	public boolean canDrain(World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock() == this)
		{
			if(this.getAmountLiquid(state) > 0)	//there is at least one bucket present!
				return true;
		}
		return false;
	}

	@Override
	public float getFilledPercentage(World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);

		if (state.getBlock().isAir(world, pos))
            return 0;	//there is no liquid for this!

		if(state.getBlock() instanceof FiniteFluid == false)
			return -1;	//flag that there is no level associated with this

		int lvl = ((Integer)state.getValue(LEVEL)).intValue();	//0-15, 0=minimal, 8=full falling.
		
		lvl = lvl>=8 ? lvl-7 : lvl+1;	//remove the falling water flag(-8) and add 1
		//it always varies on 8 levels, though the total capacity may be different
		return (lvl/8.0f);
	}

	@Override
	public FluidStack drain(World world, BlockPos pos, boolean doDrain)
	{
		IBlockState state = world.getBlockState(pos);
		if (canDrain(world, pos) == false)
        {
            return null;
        }

        if (doDrain)
        {
			//if it made it past canDrain, the block is of type finite fluid
			
			int lvl = ((Integer)state.getValue(LEVEL)).intValue();
			if(lvl > 0 && lvl!=8)	//it is not the lowest value for draining!
			{
				world.setBlockState(pos, state.withProperty(LEVEL,lvl-1), 2);
			}
			else
			{
				world.setBlockToAir(pos);
			}
			world.scheduleUpdate(pos, this, tickRate);
		    world.notifyNeighborsOfStateChange(pos, this);
        }

		return this.stack.copy();	//blocks with smaller capacity drain smaller amounts!

	}

	//returns the level for the blockState.
	public int getLvlFromAmountmB(int amountmB, boolean restrictOneBlock)
	{
		int max = this.capacity * FluidContainerRegistry.BUCKET_VOLUME;
		if(restrictOneBlock && amountmB >= max)
			return(7);
		if(amountmB <= 0)
			return(-1);

		//if max=8,000 (default)
		return((amountmB*8)/max);
	}

	private int addLiquid(World world, BlockPos pos, FluidStack fl)
	{
	//0 = source
	//1-7 = 'flowing', 0=lowest
	//8-15 = falling. Add 8 to the flowing for the falling...
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock().isAir(world,pos))
		{
			//do something!
			int lvl = getLvlFromAmountmB(fl.amount, false);
			int above=0;
			while(lvl>=0)
			{
				if(lvl < 8)
					world.setBlockState(pos.add(0,above,0), this.getDefaultState().withProperty(LEVEL, lvl));	//rough work for now...
				else
					world.setBlockState(pos.add(0,above,0), this.getDefaultState().withProperty(LEVEL, 7));
				lvl -= 8;
				above++;
			}
		}
		else if(!(state.getBlock() instanceof FiniteFluid))
		{
			return(-1);
		}
		else //we are adding fluid to a finite fluid. Have it push the blocks up if it exceeds capacity!
		{
			int lvl = getLvlFromAmountmB(fl.amount + getAmountLiquid(state), false);
			int above=0;
			while(lvl>=0)
			{
				if(lvl < 8)
					world.setBlockState(pos.add(0,above,0), this.getDefaultState().withProperty(LEVEL, lvl));	//rough work for now...
				else
					world.setBlockState(pos.add(0,above,0), this.getDefaultState().withProperty(LEVEL, 7));
				lvl -= 8;
				above++;
			}
		}

		return 0;
	}

	private int addLiquid(World world, BlockPos pos, int amt, boolean falling)
	{
	//0 = source
	//1-7 = 'flowing', 0=lowest
	//8-15 = falling. Add 8 to the flowing for the falling...
		if(amt==0)
			return(0);
		if(amt < 0)
		{
			amt = -removeLiquid(world, pos, -amt);
			return(amt);
		}
		int addFalling = falling ? 8:0;
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock().isAir(world,pos))
		{
			if(amt < 8)
			{
				world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, amt-1+addFalling));	//rough work for now...
				amt=0;
			}
			else
			{
				world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, 7+addFalling));
				amt -= 8;
			}
		}
		else if (state.getBlock() == this)
        {
			int lvl = ((Integer)state.getValue(LEVEL)).intValue();
			if(lvl >= 8 && lvl<15)	//is it falling water we're adding into?	8 is one falling water, 15 is 8 falling water
			{
				int newTot = lvl + amt;
				if(newTot > 15)
				{
					world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, 15));
					amt = newTot - 15;
				}
				else
				{
					world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, newTot));
					amt = 0;	//none left over
				}
			}
			else if(lvl < 7) //lvl is 0-7, if 7, can't add any liquid so NOTHING changes. don't initiate any new calls!
			{
				int newTot = lvl + amt;
				if(newTot > 7)
				{
					world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, 7+addFalling));
					amt = newTot - 7;
				}
				else
				{
					world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, newTot+addFalling));
					amt = 0;	//none left over
				}
			}
        }
		else if(state.getBlock().getMaterial() == Material.lava)
		{
			if(((Integer)state.getValue(LEVEL)).intValue() != 0)
				world.setBlockState(pos,Blocks.cobblestone.getDefaultState());
			else
				world.setBlockState(pos,Blocks.obsidian.getDefaultState());
			
			amt--;
		}
		else if (displaceIfPossible(world, pos))
        {
			if(amt < 8)
			{
				world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, amt-1+addFalling));	//rough work for now...
				amt=0;
			}
			else
			{
				world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, 7+addFalling));
				amt -= 8;
			}
		}
//        if (!state.getBlock().getMaterial().blocksMovement() && state.getBlock().getMaterial() != Material.portal)
 //       {
  //          return true;
   //     }


		return amt;
	}

	//returns amount not removed
	private int removeLiquid(World world, BlockPos pos, int amt)
	{
	//0 = source
	//1-7 = 'flowing', 0=lowest
	//8-15 = falling. Add 8 to the flowing for the falling...
		if(amt==0)
			return(0);
		if(isInfiniteSourceWater(world, pos))
			return(0);	//don't remove water from an infinite source! but as far as cpu is concerned, act like it did

		if(amt < 0)
		{
			amt = -addLiquid(world, pos, -amt, false);
			return(amt);
		}
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == this)
        {
			int lvl = ((Integer)state.getValue(LEVEL)).intValue();
			if(lvl >= 8)	//is it falling water we're adding into?	8 is one falling water, 15 is 8 falling water
			{
				int newTot = lvl - amt;
				if(newTot < 8)
				{
					world.setBlockToAir(pos);
					amt = 7 - newTot;
				}
				else
				{
					world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, newTot));
					amt = 0;	//none left over
				}
			}
			else //lvl is 0-7
			{
				int newTot = lvl - amt;
				if(newTot < 0)
				{
					world.setBlockToAir(pos);
					amt = -1-newTot;
				}
				else
				{
					world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, newTot));
					amt = 0;	//none left over
				}
			}
        }
		return amt;
	}

	//returns amount failed on transfer (or -1 if no water at source)
	public int transferAllLiquid(World world, BlockPos source, BlockPos target)
	{
		IBlockState srcState = world.getBlockState(source);
		if(srcState.getBlock() != this)	//there was no liquid!
			return -1;	//signify that it could
		int amt = ((Integer)srcState.getValue(LEVEL)).intValue();
		amt = amt>=8 ? amt-7 : amt+1;
		amt = transferLiquid(world, source, target, amt);
		return amt;
	}

	//returns amount failed on transfer
	public int transferLiquid(World world, BlockPos source, BlockPos target, int amt)
	{
		IBlockState srcState = world.getBlockState(source);
		IBlockState trgState = world.getBlockState(target);
		
		//there's gotta be water here!
		if(srcState.getBlock() != this)
			return amt;
		int maxRemove = ((Integer)(srcState.getValue(LEVEL))).intValue()+1;
		int maxReceive = 8;
		if(trgState.getBlock() == this)
			maxReceive = 7 - ((Integer)(trgState.getValue(LEVEL))).intValue();
		else if(world.getBlockState(target).getBlock().getMaterial() == Material.lava)
			maxReceive = 1;
		else if(canDisplace(world, target)==false)
			maxReceive = 0;


		int maxChange = (maxRemove < maxReceive) ? maxRemove:maxReceive;
		int fail = amt > maxChange ? amt-maxChange : 0;
		int transfer = amt > maxChange ? maxChange : amt;
		if(transfer > 0)
		{
			removeLiquid(world, source, transfer);	//don't let any block updates occur needlessly
			addLiquid(world, target, transfer, (target.getY() < source.getY()) );
			world.scheduleUpdate(target, this, tickRate);
			world.scheduleUpdate(source, this, tickRate);
		}
		return (fail);
	}

	public boolean isFiniteFluidFalling(World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		
		if(state.getBlock() != this)
			return false;

		IBlockState belowS = world.getBlockState(pos.add(0,-1,0));
		Block below = belowS.getBlock();
		Material matbel = below.getMaterial();
		
		if (below == Blocks.air)
			return true;
		if (below == this)
        {
			if(((Integer)belowS.getValue(LEVEL)).intValue() >= 8)	//is the water below falling, then this is falling
				return true;
            if(this.getAmountLiquid(belowS) < this.capacity)
				return true;
        }
		if (displacements.containsKey(below))
        {
			if(displacements.get(below))
				return true;
        }
        if (!matbel.blocksMovement() && matbel != Material.portal)
        {
            return true;
        }
		else
		{
			int density = getDensity(world, pos.add(0,-1,0));
			if (density == Integer.MAX_VALUE)
			{
				 return false;
			}
			else if (this.density > density)
			{
				return true;
			}
		}

		return false;
	}

	

	//returns the amount of mB for the liquid!
	public int getAmountLiquid(IBlockState state)
	{
		if (state.getBlock().getMaterial() == Material.air)
            return 0;	//there is no liquid for this!

		if(!(state.getBlock() instanceof FiniteFluid))
			return 0;

		int lvl = ((Integer)state.getValue(LEVEL)).intValue();	//0-15, 0=minimal block, 7=full, 8=minimal falling.
		lvl = lvl>=8 ? lvl-7 : lvl+1;	//remove the falling water flag(-8) and add 1

		return (this.capacity * FluidContainerRegistry.BUCKET_VOLUME * lvl / 8);
	}

	public boolean isInfiniteSourceWater(World world, BlockPos pos)
	{
		if(world.getBlockState(pos).getBlock() != this)	//will need to be updated to be just the water finite fluid later.
			return false;

		if(this != MCWaterCycle.finiteWater)
			return false;

		boolean isOcean = (world.getChunkFromBlockCoords(pos).getBiome(pos, world.getWorldChunkManager()) instanceof BiomeGenOcean);
		if(pos.getY()==SEALEVEL && isOcean)
			return true;
		return false;
	}

	private BlockPos blockToDrain(World world, BlockPos pos)
	{
		IBlockState srcState = world.getBlockState(pos);
		int numAbove = 0;
		boolean isOcean = (world.getChunkFromBlockCoords(pos).getBiome(pos, world.getWorldChunkManager()) instanceof BiomeGenOcean);
		//if it's ocean, when it drains the uppermost block that, it will return null on what blockstate to drain
		
		if(((Integer)srcState.getValue(LEVEL)).intValue() == 7)	//this is a non-falling full water block, so there might be water above it!
		{	
			IBlockState trgState;
			do
			{	
				numAbove++;
				trgState = world.getBlockState(pos.up(numAbove));
				if(trgState.getBlock() != this)
					break;
				int lvl = ((Integer)trgState.getValue(LEVEL)).intValue();
				if(lvl!=7)	//if it is not a completely full block, and moving downwards doesn't count!
					break;
			}while(trgState.getBlock()==this);	//it should always break before this...
			numAbove--;
		}
		
		if(isInfiniteSourceWater(world, pos.up(numAbove)))
			return null;	//it is coming from an infinite source, so don't actually drain any block.
		return pos.up(numAbove);
	}

	private BlockPos getTopFallLiquid(World world, BlockPos pos)
	{

		return pos;
	}

	//this assumes the pos is a falling liquid block, and the state below is initially air
	private int fallRemainingLiquidAbove(World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		IBlockState statebelow = world.getBlockState(pos.down());
		if(state.getBlock()!=this)// || statebelow.getBlock().getMaterial()!=Material.air)
			return 0;

		
		int offset = 0;
		do
		{
			int lvl = ((Integer)state.getValue(LEVEL)).intValue();
			if(lvl < 8)
				lvl += 8;
			if(isInfiniteSourceWater(world, pos.up(offset)))
			{
				world.setBlockState(pos.up(offset-1), state.withProperty(LEVEL, 15));
				break;
			}

			if(isInfiniteSourceWater(world, pos.up(offset-1))==false)
				world.setBlockState(pos.up(offset-1), state.withProperty(LEVEL, lvl));
			else
				break;

			offset++;
			state = world.getBlockState(pos.up(offset));
			statebelow = world.getBlockState(pos.up(offset-1));
		}while(state.getBlock()==this);
		world.setBlockToAir(pos.up(offset-1));	//yeah, important line or you get infinite water when it falls.
		return 0;
	}

	//returns null if no falling liquid, otherwise returns the bottom liquid in the stack
	private BlockPos getBottomFallLiquid(World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock()!=this)
		{
			return null;
		}
		int lvl = ((Integer)state.getValue(LEVEL)).intValue();
		if(lvl < 8)
		{
			return null;
		}
		int below=0;
		do
		{
			below++;
			state = world.getBlockState(pos.down(below));
			if(state.getBlock()!=this)
				break;
			lvl = ((Integer)state.getValue(LEVEL)).intValue();
		}while(lvl >= 8);

		below--;
		return pos.down(below);
	}

	//returns how much is left to input
	@Override
	public int tryToFlowVerticallyInto(World world, BlockPos pos, int amtToInput)
    {
		IBlockState below = world.getBlockState(pos.down(1));	//other stuff will change if it needs to go any lower than directly beneath
		BlockPos posd = pos.down(1);
		if (posd.getY() < 0 || posd.getY() >= world.getHeight())
        {
            world.setBlockToAir(pos);
            return 0;
        }
		if(below.getBlock().getMaterial() == Material.air)
		{
			if(amtToInput > 8)
			{
				world.setBlockState(posd, this.getDefaultState().withProperty(LEVEL, 7));
				world.scheduleUpdate(posd, this, tickRate);
				return(amtToInput-8);
			}
			else
			{
				world.setBlockState(posd, this.getDefaultState().withProperty(LEVEL, amtToInput-1));
				world.scheduleUpdate(posd, this, tickRate);
				return 0;
			}
		}
		else if(below.getBlock() == this)
		{
			int lvl = ((Integer)below.getValue(LEVEL)).intValue();
			if(lvl>=8)	//just get it to be the total quanta!
				lvl -= 8;

			int avail = 7 - lvl;
			if(amtToInput <= avail)
			{
				world.setBlockState(posd, below.withProperty(LEVEL, lvl+amtToInput));
				world.scheduleUpdate(posd, this, tickRate);
				return 0;
			}
			else
			{
				world.setBlockState(posd, below.withProperty(LEVEL, 7));
				world.scheduleUpdate(posd, this, tickRate);
				return amtToInput-avail;
			}
		}

		return amtToInput;
	}
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		//because it is an Override, we know the state
		//to test behavior, just make it evaporate
		state = world.getBlockState(pos);	//make sure it's the correct state...
		
		if(state.getBlock() != this)
			return;

		IBlockState below = world.getBlockState(pos.down(1));
		Block bbelow = below.getBlock();
		int lvl = ((Integer)state.getValue(LEVEL)).intValue();
		if(bbelow==this)
		{
			if(((Integer)below.getValue(LEVEL)).intValue() >= 8 && lvl < 8)	//if falling water below
				lvl += 8;	//trick system into thinking this water is falling and do essentially nothing

		}

		//make entire column fall!
		//Falling water does not get events scheduled unless it is the lowest y flowing water in the bunch!
		//vertically flowing water does not flow horizontally at all
		boolean keepGoing = true;
		if(lvl >= 8)	//it is already falling water or on top of falling water
		{
			BlockPos lowest = this.getBottomFallLiquid(world, pos);	//this is the lowest falling water.
			
			if(pos.equals(lowest)) //process the entire stack, starting from the bottom and working the way up
			{ //note, must do pos incase lowest is NULL, at which point it fails miserably
				int yOffset=0;
				int notFall = transferAllLiquid(world, pos.up(yOffset), pos.up(yOffset-1));	//this will put water below
				int wlvl;
				while(notFall > 0)	//up MUST be a source for notFall to be > 0
				{
					IBlockState down = world.getBlockState(pos.up(yOffset-1));
					IBlockState up = world.getBlockState(pos.up(yOffset));
					if(down.getBlock() != this)	//the block below isn't water, so this must be still water now
					{
						wlvl = ((Integer)up.getValue(LEVEL)).intValue();
						if(wlvl >= 8)
						{
							world.setBlockState(pos.up(yOffset), up.withProperty(LEVEL, wlvl-8), 2);
							world.scheduleUpdate(pos.up(yOffset), this, tickRate);
						}
					}
					else //the block below is water, but it failed to transfer it all, so the block below must be full (source)
					{
						world.setBlockState(pos.up(yOffset-1), down.withProperty(LEVEL, 7), 2);
						world.scheduleUpdate(pos.up(yOffset-1), this, tickRate);
						//if the block below is full, then this water ought to not be falling anymore!
						wlvl = ((Integer)up.getValue(LEVEL)).intValue();
						if(wlvl >= 8)
						{
							world.setBlockState(pos.up(yOffset), up.withProperty(LEVEL, wlvl-8), 2);
							world.scheduleUpdate(pos.up(yOffset), this, tickRate);
						}
					}
					yOffset++;
					notFall = transferAllLiquid(world, pos.up(yOffset), pos.up(yOffset-1));	//continue transferring water down
					//such that it creates new sources
				}
				//at this point, it has transferred all of one falling block into an empty block, so it stayed falling
				yOffset++;	//it is now pointing to a (supposedly) falling fluid block, and there is an air block beneath it.
				fallRemainingLiquidAbove(world, pos.up(yOffset));
				world.scheduleUpdate(pos, this, tickRate);
			}
			keepGoing = false;
		}
		if(keepGoing)
		{
			int failFall = transferAllLiquid(world, pos, pos.down());
//			System.out.println("Failed to output " + failFall + " water below");
			if(failFall > 0)
			{
				//easiest way is to prioritize flow attempts in the remaining four directions.
				//largest number means transfer water, negative means it cannot at all.
				//the number will be the delta, with a bonus +8 if it goes to displacable with water/air beneath.
				//cycle through the four sides until all are < 1.
				//create 5 array to adjust water quickly instead of via a bunch of world updates
				//always test flow 1

				//this didn't work.

				//go back to just averaging all the water in adjacent blocks
				//rank the blocks in order for where the extra water will go (up to 4 spots - or it would otherwise be the average)


				//attempt flowing
				//flow will be determined by first going over cliffs, defined as the block beneath and offset being water or air.
				//to flow over, it must have water content greater than the adjacent spot.
				
				//debug code

				BlockPos above = pos.up();
				IBlockState stAbove = world.getBlockState(above);
				if(stAbove.getBlock()==this)
				{
					int abovelvl = ((Integer)world.getBlockState(above).getValue(LEVEL)).intValue();
					int thislvl = ((Integer)world.getBlockState(pos).getValue(LEVEL)).intValue();
					//if(thislvl != 7)
//					System.out.println("Water source being processed with water above it.");
				}

//				System.out.println("Water being processed");

				//end debug code
				int[] depth;
				boolean[] priority;
				BlockPos[] trgPos;
				depth = new int[5];
				priority = new boolean[5];
				int highPriority=0;
				trgPos = new BlockPos[5];	//stop dealing with all these enums in the following code
				boolean[] change;
				change = new boolean[5];	//flag to mark if depth changes in block. Initially flag to mark highest priority
				depth[4] = failFall;	//the current lvl of the position
				trgPos[4] = pos;
				change[4] = true;
				priority[4] = false;
				if(world.getBlockState(pos.down()).getBlock() == this || canDisplace(world, pos.down()))
				{
					priority[4] = true;
					highPriority++;
				}
				int i=0;
				int totWater = failFall;
				int totBlocks = 1;
				//initialize everything
				for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
				{
					trgPos[i] = pos.offset(side);
					priority[i] = false;
					if(world.getBlockState(trgPos[i]).getBlock()==this)
					{
						depth[i] = ((Integer)world.getBlockState(trgPos[i]).getValue(LEVEL)).intValue()+1;
						if(depth[i] > 8)
						{
							depth[i] = -1;
							change[i] = false;
							i++;
							continue;
						}
						totWater += depth[i];
						change[i] = true;
						totBlocks++;
						if(world.getBlockState(trgPos[i].down()).getBlock() == this || canDisplace(world, trgPos[i].down()))
						{
							priority[i] = true;
							highPriority++;
						}
						
					}
					else if(world.getBlockState(trgPos[i]).getBlock().getMaterial() == Material.lava && this == MCWaterCycle.finiteWater)
					{
						if(((Integer)world.getBlockState(trgPos[i]).getValue(LEVEL)).intValue() == 0)
							world.setBlockState(trgPos[i],Blocks.obsidian.getDefaultState());
						else
							world.setBlockState(trgPos[i],Blocks.cobblestone.getDefaultState());
						depth[i]=-1;
						change[i]=false;
						totWater--;
						
					}
					else if (canDisplace(world, trgPos[i])) //it can go into the adjacent block, which is not a liquid
					{
						depth[i] = 0;
						totBlocks++;
						change[i] = true;
						if(world.getBlockState(trgPos[i].down()).getBlock() == this || canDisplace(world, trgPos[i].down()))
						{
							priority[i] = true;
							highPriority++;
						}
					}
					else
					{
						depth[i] = -1;
						change[i] = false;
					}
					
					i++;
				}
				if(totWater < 0)
					totWater = 0;
				int avgDepth = totWater / totBlocks;
				int Remainder = totWater % totBlocks;	//how many blocks need to have an additional water added
				int numExtra = Remainder - highPriority;
				int maxDepth;
				int index;
				while(numExtra > 0)
				{
					//find the blocks with the highest depth that aren't in priority array
					maxDepth = 0;
					index = -1;
					for(i=0; i<5; i++)
					{
						if(!change[i] || priority[i])	//is this block valid, and does it already have priority?
							continue;

						if(depth[i] > maxDepth)	//tend to keep water where it is if it won't auto flow off a cliff
						{
							maxDepth = depth[i];
							index = i;
						}
					}
					if(index != -1)
					{
						priority[index] = true;
						numExtra--;
					}
					else //basically, everything sucks, so just fill them in
					{
						for(i=0; i<5; i++)
						{
							if(!change[i] || priority[i])
								continue;

							priority[i] = true;
							numExtra--;
							if(numExtra<=0)
								break;
						}
					}
				}
				
				if(numExtra < 0)
				{
					int[] hpindices;
					hpindices = new int[highPriority];
					int j=0;
					for(i=0; i<5; i++)	//want to keep priority spots that 1) have air/water underneath. 2) the highest depth among those
					{
					//everything that has priority has air/water beneath
						if(priority[i])
						{
							hpindices[j] = i;
							j++;
						}
					}
					while(numExtra < 0)
					{
						int smallest = 9;
						int remove=-1;
						for(i=0;i<highPriority;i++)
						{
							if(hpindices[i] < 5)
							{
								if(depth[hpindices[i]] < smallest)
								{
									remove=i;
									smallest = depth[hpindices[i]];
								}
							}
						}
						if(remove != -1)
						{
							priority[hpindices[remove]] = false;
							hpindices[remove] = 5;
						}
						else
						{
							System.out.println("Somehow removing priorities failing");
							priority[0]=priority[1]=priority[2]=priority[3]=priority[4]=false;
							numExtra=0;
						}
						numExtra++;
					}

				}


				//now it's time to set the adjacent water levels
				for(i=0;i<5;i++)
				{
					if(change[i]) //this spot can displace or have the water level adjusted
					{
						int newLvl = avgDepth + (priority[i]?0:-1);
						if(newLvl >= 0)
						{
							if(newLvl > 7)
								newLvl = 7;
							displaceIfPossible(world, trgPos[i]);
							if(newLvl != depth[i]-1)	//only update water block and look for updates if it changes
							{
								world.setBlockState(trgPos[i], state.withProperty(LEVEL, newLvl), 2);
								world.scheduleUpdate(trgPos[i], this, tickRate);
							}
						}
						else
							world.setBlockToAir(trgPos[i]);
						
					}
				}
			}
			else //all the water fell down
			{
				world.scheduleUpdate(pos.down(), this, tickRate);
				keepGoing = false;
			}
			//it is normal water - first attempt causing a fall down
			//then attempts flowing
			
		
		}
		if(keepGoing)
		{
			//it then attempts to seep
			keepGoing = false;
		}
		if(keepGoing)
		{
			//it finally attempts to evaporate
			keepGoing=false;
		}
		
//		world.notifyNeighborsOfStateChange(pos, this);	//will only do this when water becomes a source block
		
	//Event.EntityEvent.LivingEvent.PlayerEvent.FillBucketEvent -- gotta interrupt water to make things work better!
	//Event.FluidContainerRegisterEvent
	//Event.FluidEvent.FluidDrainingEvent
	//Event.FluidEvent.FluidFillingEvent
	//Event.FluidEvent.FluidMotionEvent
	//Event.FluidEvent.FluidSpilledEvent
	//Event.FluidRegisterEvent
		//import net.minecraftforge.fluids ? 1.7.2.

	}

	@Override
	public Vec3 getFlowVector(IBlockAccess world, BlockPos pos)
	{
		Vec3 vec = new Vec3(0.0D, 0.0D, 0.0D);
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock() != this)
			return(vec);



		//finite fluids don't really flow unless water is moving.
		int thisLvl = getQuantaValue(world, pos);
		if(thisLvl > 8)	//it is falling down!
		{
			vec = vec.addVector(0, -1D, 0);
			return(vec);
		}
		int adjLvl;
		int difference;
		for (int side = 0; side < 4; ++side)
        {
            int x2 = pos.getX();
            int z2 = pos.getZ();

            switch (side)
            {
                case 0: --x2; break;
                case 1: --z2; break;
                case 2: ++x2; break;
                case 3: ++z2; break;
            }

            BlockPos pos2 = new BlockPos(x2, pos.getY(), z2);
			adjLvl = getQuantaValue(world, pos);
			if(adjLvl>=0)	//it is not some random block
			{
				difference = thisLvl - adjLvl;
				//a driving force of 1 does not induce a current
				if(difference > 0)
					difference--;
				else if(difference < 0)
					difference++;
				vec = vec.addVector((pos2.getX() - pos.getX()) * difference, 0, (pos2.getZ() - pos.getZ()) * difference);
			}
		}
		vec.normalize();
		return(vec);
	}
}

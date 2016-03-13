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
import net.minecraft.init.Items;
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
	//1-7 = flowing, 0=lowest
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

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		//because it is an Override, we know the state
		//to test behavior, just make it evaporate
		state = world.getBlockState(pos);	//make sure it's the correct state...
		
		if(!(state.getBlock() instanceof FiniteFluid))
			return;
		
		
		int lvl = ((Integer)state.getValue(LEVEL)).intValue();

		
		System.out.println(Integer.toString(lvl));
		if(lvl > 0)
		{
			world.setBlockState(pos, state.withProperty(LEVEL, lvl-1), 2);
		}
		else if(lvl == 0)
		{
			world.setBlockToAir(pos);
		}
		
		world.notifyNeighborsOfStateChange(pos, this);
		world.scheduleUpdate(pos, this, 40);


/*
        // check adjacent block levels if non-source
        if (quantaRemaining < quantaPerBlock)
        {
            if (worldIn.getBlockState(pos.add( 0, -densityDir,  0)).getBlock() == this ||
                worldIn.getBlockState(pos.add(-1, -densityDir,  0)).getBlock() == this ||
                worldIn.getBlockState(pos.add( 1, -densityDir,  0)).getBlock() == this ||
                worldIn.getBlockState(pos.add( 0, -densityDir, -1)).getBlock() == this ||
                worldIn.getBlockState(pos.add( 0, -densityDir,  1)).getBlock() == this)
            {
                expQuanta = quantaPerBlock - 1;
            }
            else
            {
                int maxQuanta = -100;
                maxQuanta = getLargerQuanta(worldIn, pos.add(-1, 0,  0), maxQuanta);
                maxQuanta = getLargerQuanta(worldIn, pos.add( 1, 0,  0), maxQuanta);
                maxQuanta = getLargerQuanta(worldIn, pos.add( 0, 0, -1), maxQuanta);
                maxQuanta = getLargerQuanta(worldIn, pos.add( 0, 0,  1), maxQuanta);

                expQuanta = maxQuanta - 1;
            }

            // decay calculation
            if (expQuanta != quantaRemaining)
            {
                quantaRemaining = expQuanta;

                if (expQuanta <= 0)
                {
                    worldIn.setBlockToAir(pos);
                }
                else
                {
                    worldIn.setBlockState(pos, state.withProperty(LEVEL, quantaPerBlock - expQuanta), 2);
                    worldIn.scheduleUpdate(pos, this, tickRate);
                    worldIn.notifyNeighborsOfStateChange(pos, this);
                }
            }
        }
        // This is a "source" block, set meta to zero, and send a server only update
        else if (quantaRemaining >= quantaPerBlock)
        {
            worldIn.setBlockState(pos, this.getDefaultState(), 2);
        }

        // Flow vertically if possible
        if (canDisplace(worldIn, pos.up(densityDir)))
        {
            flowIntoBlock(worldIn, pos.up(densityDir), 1);
            return;
        }

        // Flow outward if possible
        int flowMeta = quantaPerBlock - quantaRemaining + 1;
        if (flowMeta >= quantaPerBlock)
        {
            return;
        }

        if (isSourceBlock(worldIn, pos) || !isFlowingVertically(worldIn, pos))
        {
            if (worldIn.getBlockState(pos.down(densityDir)).getBlock() == this)
            {
                flowMeta = 1;
            }
            boolean flowTo[] = getOptimalFlowDirections(worldIn, pos);

            if (flowTo[0]) flowIntoBlock(worldIn, pos.add(-1, 0,  0), flowMeta);
            if (flowTo[1]) flowIntoBlock(worldIn, pos.add( 1, 0,  0), flowMeta);
            if (flowTo[2]) flowIntoBlock(worldIn, pos.add( 0, 0, -1), flowMeta);
            if (flowTo[3]) flowIntoBlock(worldIn, pos.add( 0, 0,  1), flowMeta);
        }
		*/
	}


	//Event.EntityEvent.LivingEvent.PlayerEvent.FillBucketEvent -- gotta interrupt water to make things work better!
	//Event.FluidContainerRegisterEvent
	//Event.FluidEvent.FluidDrainingEvent
	//Event.FluidEvent.FluidFillingEvent
	//Event.FluidEvent.FluidMotionEvent
	//Event.FluidEvent.FluidSpilledEvent
	//Event.FluidRegisterEvent
		//import net.minecraftforge.fluids ? 1.7.2.

}

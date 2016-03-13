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
	public final int capacity;

	//how many mB are in the block?
	protected FluidStack stack;

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
		this.capacity = capacity;
		stack = new FluidStack(fluid, capacity);
		this.setLightOpacity(10);
		setUnlocalizedName(nm);
		setCreativeTab(CreativeTabs.tabBlock);
		GameRegistry.registerBlock(this, nm);	//add the block to the registry
		System.out.println("Created Finite fluid!");
		
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
		if(state.getBlock()==this)
		{
			if(((FiniteFluid)(state.getBlock())).getAmountLiquid() > 0)
				return true;
		}
		//((Integer)world.getBlockState(pos).getValue(LEVEL)).intValue() == 0
		return false;
	}

	@Override
	public float getFilledPercentage(World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
        if (state.getBlock().isAir(world, pos))
        {
            return 0;
        }

        if (state.getBlock() != this)	//in case the block updates before this is called!
        {
            return -1;
        }
		FiniteFluid thisBlock = ((FiniteFluid)(state.getBlock()));
		
		if(thisBlock.capacity == 0)
			return 0;

		return (((float)thisBlock.getAmountLiquid())/((float)thisBlock.capacity));
	}

	@Override
	public FluidStack drain(World world, BlockPos pos, boolean doDrain)
	{
		IBlockState state = world.getBlockState(pos);
		if (canDrain(world, pos) == false)
        {
            return null;
        }
		
		FluidStack ret;
		int amount;

		//if it made it past canDrain, the block is of type finite fluid
		FiniteFluid block = (FiniteFluid)state.getBlock();

		if(block.stack.amount >= FluidContainerRegistry.BUCKET_VOLUME)
			amount = FluidContainerRegistry.BUCKET_VOLUME;	//how much is returned by the drain. default to a bucket.
		else
			amount = block.stack.amount;


        if (doDrain)
        {
			//if it made it past canDrain, the block is of type finite fluid
			block.stack.amount -= amount;

			int lvl = getLevel(world, pos, state);
			if(lvl >= 0)
			{
				world.setBlockState(pos, state.withProperty(LEVEL,lvl), 2);
			}
			else
			{
				world.setBlockToAir(pos);
			}
			world.scheduleUpdate(pos, this, tickRate);
		    world.notifyNeighborsOfStateChange(pos, this);
        }

		return new FluidStack(getFluid(), amount);

	}

	private int getLevel(World world, BlockPos pos, IBlockState state)
	{
	//0 = source
	//1-7 = flowing, 1=highest
	//8-15 = falling. Add 8 to the flowing for the falling...
		float percentage = getFilledPercentage(world, pos);
		if(percentage <= 0)
			return -1;

		int lvl;
		if(percentage == 1.0f)
			lvl = 0;	//it is a full block
		else if(percentage>=0.875f)
		{
			lvl = 1;
		}
		else if(percentage>=0.75f)
		{
			lvl = 2;
		}
		else if(percentage>=0.625f)
		{
			lvl = 3;
		}
		else if(percentage>=0.5f)
		{
			lvl = 4;
		}
		else if(percentage>=0.375f)
		{
			lvl = 5;
		}
		else if(percentage>=0.25f)
		{
			lvl = 6;
		}
		else
		{
			lvl = 7;
		}
		if(isFiniteFluidFalling(world, pos))
			lvl += 8;
		
		return lvl;
	}

	public boolean isFiniteFluidFalling(World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		Block blk = state.getBlock();
		if(blk != this)
			return false;

		Block below = world.getBlockState(pos.add(0,-1,0)).getBlock();
		Material matbel = below.getMaterial();
		
		if (below == Blocks.air)
			return true;
        if (below == this)
        {
            if(((FiniteFluid)below).getAmountLiquid() < this.capacity)
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

	public int getAmountLiquid()
	{
		return stack.amount;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		//because it is an Override, we know the state
		//to test behavior, just make it evaporate
		state = world.getBlockState(pos);	//make sure it's the correct state...
		Block blk = state.getBlock();
		if(blk != this)
			return;
		FiniteFluid thisBlk = (FiniteFluid)blk;

		int oldLvl = ((Integer)state.getValue(LEVEL)).intValue();
		System.out.println(Integer.toString(thisBlk.stack.amount));
		thisBlk.stack.amount -= 50;
		if(thisBlk.stack.amount < 0)
			thisBlk.stack.amount = 0;

		int lvl = getLevel(world, pos, state);
		System.out.println(Integer.toString(lvl));
		if(lvl >= 0 && lvl != oldLvl)
		{
			world.setBlockState(pos, thisBlk.getBlockState().getBaseState().withProperty(LEVEL, lvl), 2);
		}
		else if(lvl < 0)
		{
			world.setBlockToAir(pos);
		}
		
		world.notifyNeighborsOfStateChange(pos, thisBlk);
		world.scheduleUpdate(pos, thisBlk, tickRate);


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

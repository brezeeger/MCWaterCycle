package brezeeger.heinzel.d.MCWaterCycle.Items;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBucket;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.world.World;
import net.minecraft.util.BlockPos;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.init.Items;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.*;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraft.stats.StatList;
import net.minecraftforge.fluids.BlockFluidBase;

import net.minecraftforge.fml.common.eventhandler.Event.Result;

import brezeeger.heinzel.d.MCWaterCycle.MCWaterCycle;
import brezeeger.heinzel.d.MCWaterCycle.Fluids.FiniteFluid;

import net.minecraftforge.event.entity.player.FillBucketEvent;

public class WaterBucket extends ItemBucket {

	private final String name;
	public Fluid fluid;
	

	//public static WaterBucket INSTANCE = new WaterBucket(MCWaterCycle.flfinwater, "Water Bucket");
    public Map<Block, Item> buckets = new HashMap<Block, Item>();

    public WaterBucket(Fluid fl, String nm) {
		super(fl.getBlock());
		this.name = nm;
		fluid = fl;
		setUnlocalizedName(/*MCWaterCycle.MODID+":"+*/nm);
		if(nm=="finite_water_bucket")
			setCreativeTab(null);	//make the block invisible in creative mode
		GameRegistry.registerItem(this, this.name);	//add the block to the registry
		FluidContainerRegistry.registerFluidContainer(fl, new ItemStack(this), new ItemStack(Items.bucket));
		buckets.put(fl.getBlock(), this);
    }

	/*
	These need to go somewhere!
	BucketHandler.INSTANCE.buckets.put(yourFluidBlock, yourBucket);
	MinecraftForge.EVENT_BUS.register(BucketHandler.INSTANCE);
	*/
    @SubscribeEvent
    public void onBucketFill(FillBucketEvent event) {	//thisis called whenever a bucket is right clicked, regardless of whether it has fluid in it or not!

//		System.out.println("Started custom bucket processing");
        ItemStack result = processBucket(event.world, event.target, event.current, event.entityPlayer);
		if(result == event.current)
		{
			event.setCanceled(true);
//			System.out.println("Bucket Processing cancelled");
			return;
		}
        if (result == null)
			return;
//		System.out.println("Done bucket processing");
        event.result = result;
        event.setResult(Result.ALLOW);	//say this event is done being processed
    }

    private ItemStack processBucket(World world, MovingObjectPosition pos, ItemStack currentItem, EntityPlayer player) {

		if(currentItem.getItem() == Items.bucket)
		{
			BlockPos bpos = pos.getBlockPos();
			IBlockState state = world.getBlockState(bpos);
			Block block = state.getBlock();
//			System.out.println("Attempting to fill with: " +block.getUnlocalizedName());
			if(block.getMaterial().isLiquid() == false)	//if it is not full, it may do a different block
			{
				bpos = bpos.offset(pos.sideHit);
				state = world.getBlockState(bpos);
				block = state.getBlock();
//				System.out.println("Now attempting to fill with: " +block.getUnlocalizedName());
			}
			if(block == MCWaterCycle.finiteWater)	//if it's this block, it will definitely succeed. If infinite source, no amount of buckets will save you.
			{
//				Item bucket = buckets.get(block);
				int fail = ((FiniteFluid)block).removeLiquid(world, bpos, 1);
//				System.out.println("Removed " + (1-fail) + " bucket of finite water");
				player.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
			    //world.setBlockToAir(pos.getBlockPos());
			    return new ItemStack(Items.water_bucket);
			}
			else if(block == Blocks.water)	//whatever water gets out there, replace it with finite water in the bucket
			{
//				Item bucket = buckets.get(MCWaterCycle.finiteWater);
//				System.out.println("Removed one bucket of minecraft water");
				int fail = ((FiniteFluid)MCWaterCycle.finiteWater).removeLiquid(world, bpos, 1);
//				world.setBlockState(bpos, MCWaterCycle.finiteWater.getDefaultState().withProperty(BlockFluidBase.LEVEL, 6));
				player.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
                
			    return new ItemStack(Items.water_bucket);
			}
			else
				return null;
		}
		else if(currentItem.getItem() == Items.water_bucket)	//it was a qater bucket - put out the finite liquid instead
		{
			BlockPos bpos = pos.getBlockPos().offset(pos.sideHit);
//			System.out.println("Using water bucket!");
			int fail = ((FiniteFluid)MCWaterCycle.finiteWater).addLiquid(world, bpos, 1, false, false);
			if(fail==0)
			{
				bpos = ((FiniteFluid)MCWaterCycle.finiteWater).getTopLiquid(world, bpos);	//make sure the update tick goes to the correct spot
				if(bpos != null)
					world.scheduleUpdate(bpos, MCWaterCycle.finiteWater, MCWaterCycle.finiteWater.tickRate(world));
//				System.out.println("Added Finite water instead of normal water");
				return new ItemStack(Items.bucket);
			}
			else
			{
//				System.out.println("Failed using water bucket!");
				return currentItem;
			}
		}

		return null;
	}
	public String getName() { return(name); }
}

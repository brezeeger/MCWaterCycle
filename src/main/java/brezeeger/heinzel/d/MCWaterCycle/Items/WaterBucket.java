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
//		set_UnlocalizedName(Reference.MODID
		fluid = fl;
		setUnlocalizedName(nm);
//		setCreativeTab(CreativeTabs.tabBlock);	//make the block visible in creative mode
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
    public void onBucketFill(FillBucketEvent event) {

        ItemStack result = fillCustomBucket(event.world, event.target);

        if (result == null)
                return;

        event.result = result;
        event.setResult(Result.ALLOW);	//say this event is done being processed
    }

    private ItemStack fillCustomBucket(World world, MovingObjectPosition pos) {

		IBlockState state = world.getBlockState(pos.getBlockPos());
        Block block = state.getBlock();
		if(block == MCWaterCycle.finiteWater)
		{
			Item bucket = buckets.get(block);
			((FiniteFluid)block).removeLiquid(world, pos.getBlockPos(), 1);
            //world.setBlockToAir(pos.getBlockPos());
            return new ItemStack(bucket);
		}
		else if(block == Blocks.water)	//whatever water gets out there, replace it with finite water in the bucket
		{
			Item bucket = buckets.get(MCWaterCycle.finiteWater);
			world.setBlockToAir(pos.getBlockPos());
            return new ItemStack(bucket);
		}
		else
	        return null;
    }

	public String getName() { return(name); }
}

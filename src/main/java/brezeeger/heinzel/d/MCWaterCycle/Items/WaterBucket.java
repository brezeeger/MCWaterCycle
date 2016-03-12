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

//import brezeeger.heinzel.d.MCWaterCycle;
import net.minecraftforge.event.entity.player.FillBucketEvent;

public class WaterBucket extends ItemBucket {

	private final String name;
	public Fluid fluid;


//	public static WaterBucket INSTANCE = new WaterBucket(new Fluid());
    public Map<Block, Item> buckets = new HashMap<Block, Item>();

    public WaterBucket(Fluid fl, String nm) {
		super(Blocks.water);
		this.name = nm;
//		set_UnlocalizedName(Reference.MODID
		fluid = fl;
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
//        event.setResult(Result.ALLOW);
    }

    private ItemStack fillCustomBucket(World world, MovingObjectPosition pos) {

		IBlockState state = world.getBlockState(pos.getBlockPos());
        Block block = state.getBlock();

        Item bucket = buckets.get(block);
        if (bucket != null && state != null) {	//want to test no meta data?
                world.setBlockToAir(pos.getBlockPos());
                return new ItemStack(bucket);
        } else
	        return null;
    }

	public String getName() { return(name); }
}

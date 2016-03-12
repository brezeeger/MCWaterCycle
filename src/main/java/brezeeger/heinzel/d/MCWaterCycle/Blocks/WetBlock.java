package brezeeger.heinzel.d.MCWaterCycle.Blocks;

import net.minecraft.block.Block;
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
import net.minecraft.item.ItemStack;
import net.minecraft.client.Minecraft;

public class WetBlock extends Block {
	private final String name;

	//how much water can the block hold in buckets
	public final int MaxWater;

	//rate the block updates at
	public final int tickRate;

	//when seeping, how quickly does it enter into the material
	public final int SeepFactor;

	//what should the block become when it runs out of water inside
	public final Block deadBlock;

	//how much of the block is filled. When determining flows, go from higher percent to lower percent
	public float percentFilled;

	//how many mB of water are present in the block
	public int WaterPresent;


	public WetBlock(Block blkdead, int mx, int tick, int seep, String nme)
	{
		super(blkdead.getMaterial());
		this.deadBlock = blkdead;
		this.MaxWater = mx;
		this.tickRate = tick;
		this.SeepFactor = seep;
		this.name = nme;
		this.WaterPresent = 1;
		
		setUnlocalizedName(nme);
		setCreativeTab(CreativeTabs.tabBlock);	//make the block visible in creative mode
		GameRegistry.registerBlock(this, this.name);	//add the block to the registry
		GameRegistry.addSmelting(new ItemStack(this,1,1), new ItemStack(blkdead,1,1), 0.1F);	//let it be smelted to it's original value
		GameRegistry.addShapelessRecipe(new ItemStack(this,1), new ItemStack(blkdead,1), new ItemStack(Items.water_bucket,1));	//note this consumes the bucket!
	}

	public String getName() { return name; }
}

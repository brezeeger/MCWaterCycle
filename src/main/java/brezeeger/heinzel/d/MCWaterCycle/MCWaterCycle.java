package brezeeger.heinzel.d.MCWaterCycle;

import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.ResourceLocation;

import brezeeger.heinzel.d.MCWaterCycle.Blocks.WetBlock;
import brezeeger.heinzel.d.MCWaterCycle.Fluids.FiniteFluid;
import brezeeger.heinzel.d.MCWaterCycle.Items.WaterBucket;
import net.minecraftforge.fluids.Fluid;

@Mod(modid = MCWaterCycle.MODID, version = MCWaterCycle.VERSION)

public class MCWaterCycle /*extends DummyModContainer*/ {
    public static final String MODID = "MCWaterCycle";
	public static final String MODNAME = "MC Water Cycle";
    public static final String VERSION = "0.0.1";


	public static Block WetGrass;
	public static Block WetDirt;
	public static Block WetSand;
	public static Block WetCobble;
	public static Block WetGravel;
	public static Block finiteWater;
	public static Fluid flfinwater;
	public static Item watBucket;

    
	@Instance
	public static MCWaterCycle instance = new MCWaterCycle();

	@SidedProxy(clientSide="brezeeger.heinzel.d.MCWaterCycle.ClientProxy", serverSide="brezeeger.heinzel.d.MCWaterCycle.ServerProxy")
	public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
		// some example code
//        System.out.println("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
		proxy.preInit(event);
//		System.out.println("Called method: PreInit MCWaterCycle");
    }

	@EventHandler
    public void init(FMLInitializationEvent  event) {
		//System.out.println("Called method: Init MCWaterCycle");
		proxy.init(event);

	}
	@EventHandler
    public void postInit(FMLPostInitializationEvent  event){
//		System.out.println("Called method: PostInit MCWaterCycle");
		proxy.postInit(event);
	}

	static
	{
		
	}

}
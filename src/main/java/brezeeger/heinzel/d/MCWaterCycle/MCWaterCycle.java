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
		System.out.println("Called method: PreInit MCWaterCycle");
    }

	@EventHandler
    public void init(FMLInitializationEvent  event) {
		System.out.println("Called method: Init MCWaterCycle");
		proxy.init(event);

		WetGrass = new WetBlock(Block.getBlockFromName("grass"), 8, 20, 2, "wetgrass");
		WetDirt = new WetBlock(Block.getBlockFromName("dirt"), 8, 20, 2, "mud");
		WetSand = new WetBlock(Block.getBlockFromName("sand"), 4, 20, 3, "wetsand");
		WetCobble = new WetBlock(Block.getBlockFromName("cobblestone"), 2, 20, 1, "wetcobblestone");
		WetGravel = new WetBlock(Block.getBlockFromName("gravel"), 6, 20, 4, "wetgravel");	
//		flfinwater = new Fluid("text");
		flfinwater = new Fluid("finiteWater", new ResourceLocation("blocks/water_still"), new ResourceLocation("blocks/water_flow"));//.setBlock(Blocks.water);
		finiteWater = new FiniteFluid(Material.water, flfinwater, 1000, 1000, 0, 8000, "finiteWater");
		

		if(event.getSide() == Side.CLIENT)
		{
			RenderItem rend = Minecraft.getMinecraft().getRenderItem();
			System.out.println("Registering textures");

			rend.getItemModelMesher().register(Item.getItemFromBlock(WetGrass), 0, new ModelResourceLocation(MODID+":"+((WetBlock)WetGrass).getName(), "inventory"));
			rend.getItemModelMesher().register(Item.getItemFromBlock(WetDirt), 0, new ModelResourceLocation(MODID+":"+((WetBlock)WetDirt).getName(), "inventory"));
			rend.getItemModelMesher().register(Item.getItemFromBlock(WetSand), 0, new ModelResourceLocation(MODID+":"+((WetBlock)WetSand).getName(), "inventory"));
			rend.getItemModelMesher().register(Item.getItemFromBlock(WetCobble), 0, new ModelResourceLocation(MODID+":"+((WetBlock)WetCobble).getName(), "inventory"));
			rend.getItemModelMesher().register(Item.getItemFromBlock(WetGravel), 0, new ModelResourceLocation(MODID+":"+((WetBlock)WetGravel).getName(), "inventory"));
			rend.getItemModelMesher().register(Item.getItemFromBlock(finiteWater), 0, new ModelResourceLocation(MODID+":"+((FiniteFluid)finiteWater).getName(), "inventory"));
		}

	}
	@EventHandler
    public void postInit(FMLPostInitializationEvent  event){
		System.out.println("Called method: PostInit MCWaterCycle");
		proxy.postInit(event);
	}

	static
	{
		
	}
}
package brezeeger.heinzel.d.MCWaterCycle;

import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.Fluid;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.common.MinecraftForge;

import brezeeger.heinzel.d.MCWaterCycle.Items.WaterBucket;
import brezeeger.heinzel.d.MCWaterCycle.Blocks.WetBlock;
import brezeeger.heinzel.d.MCWaterCycle.MCWaterCycle;
import brezeeger.heinzel.d.MCWaterCycle.Fluids.FiniteFluid;


public class CommonProxy {

    public void preInit(FMLPreInitializationEvent e) {
		//create items
		//create blocks
    }
	@EventHandler
    public void init(FMLInitializationEvent e) {
		MCWaterCycle.WetGrass = new WetBlock(Block.getBlockFromName("grass"), 8, 20, 2, "wetgrass");
		MCWaterCycle.WetDirt = new WetBlock(Block.getBlockFromName("dirt"), 8, 20, 2, "mud");
		MCWaterCycle.WetSand = new WetBlock(Block.getBlockFromName("sand"), 4, 20, 3, "wetsand");
		MCWaterCycle.WetCobble = new WetBlock(Block.getBlockFromName("cobblestone"), 2, 20, 1, "wetcobblestone");
		//WetCobble = new WetBlock(Block.getBlockFromName("cobblestone"), 2, 20, 1, MODID.toLowerCase()+":"+"wetcobblestone");
		MCWaterCycle.WetGravel = new WetBlock(Block.getBlockFromName("gravel"), 6, 20, 4, "wetgravel");	
		MCWaterCycle.flfinwater = new Fluid(/*MODID.toLowerCase()+":"+*/"finiteWater", new ResourceLocation("blocks/water_still"), new ResourceLocation("blocks/water_flow"));
		//flfinwater = new Fluid("finiteWater", new ResourceLocation(MODID.toLowerCase()+":"+"blocks/finiteWater"),
		// new ResourceLocation(MODID.toLowerCase()+":"+"blocks/finiteWater"));
		MCWaterCycle.finiteWater = new FiniteFluid(Material.water, MCWaterCycle.flfinwater, 1000, 1000, 0, 8000, "finiteWater");
		MCWaterCycle.watBucket = new WaterBucket(MCWaterCycle.flfinwater, "Water Bucket");

		MinecraftForge.EVENT_BUS.register(new ReplaceWater());
		MinecraftForge.EVENT_BUS.register(MCWaterCycle.watBucket);
		
    }

    public void postInit(FMLPostInitializationEvent e) {

    }
}

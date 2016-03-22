package brezeeger.heinzel.d.MCWaterCycle;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import brezeeger.heinzel.d.MCWaterCycle.CommonProxy;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.client.resources.model.ModelResourceLocation;

import brezeeger.heinzel.d.MCWaterCycle.MCWaterCycle;
import brezeeger.heinzel.d.MCWaterCycle.Blocks.WetBlock;
import brezeeger.heinzel.d.MCWaterCycle.Fluids.FiniteFluid;
import brezeeger.heinzel.d.MCWaterCycle.Items.WaterBucket;

public class ClientProxy extends CommonProxy{

	@Override
    public void preInit(FMLPreInitializationEvent e) {
		super.preInit(e);
    }

	@Override
    public void init(FMLInitializationEvent e) {
		super.init(e);
		RenderItem rend = Minecraft.getMinecraft().getRenderItem();
			//System.out.println("Registering textures");

		rend.getItemModelMesher().register(Item.getItemFromBlock(MCWaterCycle.WetGrass), 0, new ModelResourceLocation(MCWaterCycle.MODID+":"+((WetBlock)MCWaterCycle.WetGrass).getName(), "inventory"));
		rend.getItemModelMesher().register(Item.getItemFromBlock(MCWaterCycle.WetDirt), 0, new ModelResourceLocation(MCWaterCycle.MODID+":"+((WetBlock)MCWaterCycle.WetDirt).getName(), "inventory"));
		rend.getItemModelMesher().register(Item.getItemFromBlock(MCWaterCycle.WetSand), 0, new ModelResourceLocation(MCWaterCycle.MODID+":"+((WetBlock)MCWaterCycle.WetSand).getName(), "inventory"));
		rend.getItemModelMesher().register(Item.getItemFromBlock(MCWaterCycle.WetCobble), 0, new ModelResourceLocation(MCWaterCycle.MODID+":"+((WetBlock)MCWaterCycle.WetCobble).getName(), "inventory"));
		rend.getItemModelMesher().register(Item.getItemFromBlock(MCWaterCycle.WetGravel), 0, new ModelResourceLocation(MCWaterCycle.MODID+":"+((WetBlock)MCWaterCycle.WetGravel).getName(), "inventory"));
		rend.getItemModelMesher().register(Item.getItemFromBlock(MCWaterCycle.finiteWater), 0, new ModelResourceLocation(MCWaterCycle.MODID+":"+((FiniteFluid)MCWaterCycle.finiteWater).getName(), "inventory"));
    }

	@Override
    public void postInit(FMLPostInitializationEvent e) {
		super.postInit(e);
    }
}

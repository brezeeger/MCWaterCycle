package brezeeger.heinzel.d.MCWaterCycle;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.common.MinecraftForge;


public class CommonProxy {

    public void preInit(FMLPreInitializationEvent e) {
		//create items
		//create blocks
    }
	@EventHandler
    public void init(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(new ReplaceWater());
    }

    public void postInit(FMLPostInitializationEvent e) {

    }
}

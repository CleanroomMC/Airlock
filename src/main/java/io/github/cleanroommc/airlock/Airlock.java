package io.github.cleanroommc.airlock;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Airlock.ID, name = Airlock.NAME, version = Airlock.VERSION)
public class Airlock {

    public static final String ID = "airlockapi";
    public static final String NAME = "AirlockAPI";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(AirlockAPI.class);
    }

}

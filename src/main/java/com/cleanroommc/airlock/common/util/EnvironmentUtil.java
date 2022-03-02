package com.cleanroommc.airlock.common.util;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

/**
 * This utility class provides methods related to Minecraft's environment.
 *
 * Logical Sides, Obfuscation state etc.
 */
public class EnvironmentUtil {

    public static boolean isClient() {
        return FMLLaunchHandler.side().isClient();
    }

    public static boolean isServer() {
        return FMLLaunchHandler.side().isServer();
    }

    public static boolean isClient(World world) {
        return world.isRemote;
    }

    public static boolean isServer(World world) {
        return !world.isRemote;
    }

    public static boolean isObfuscated() {
        return !FMLLaunchHandler.isDeobfuscatedEnvironment();
    }

    public static boolean isDeobfuscated() {
        return FMLLaunchHandler.isDeobfuscatedEnvironment();
    }

    private EnvironmentUtil() { }

}

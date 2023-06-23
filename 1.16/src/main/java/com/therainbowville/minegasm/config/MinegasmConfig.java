package com.therainbowville.minegasm.config;

import com.therainbowville.minegasm.common.Minegasm;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod.EventBusSubscriber(modid = Minegasm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MinegasmConfig {
    private static final Logger LOGGER = LogManager.getLogger();

    // Client
    public static String serverUrl;

    public static boolean vibrate;
    public static Enum<ClientConfig.GameplayMode> mode;
    public static int attackIntensity;
    public static int hurtIntensity;
    public static int mineIntensity;
    public static int xpChangeIntensity;
    public static int harvestIntensity;
    public static int vitalityIntensity;

    // Server
    // -- none at the moment

}

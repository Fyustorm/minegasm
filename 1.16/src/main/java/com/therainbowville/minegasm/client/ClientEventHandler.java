package com.therainbowville.minegasm.client;

import com.mojang.authlib.GameProfile;
import com.therainbowville.minegasm.common.Minegasm;
import com.therainbowville.minegasm.config.ClientConfig;
import com.therainbowville.minegasm.config.MinegasmConfig;

import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;

import java.util.*;

@Mod.EventBusSubscriber(modid = Minegasm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    private static String playerName = null;
    private static UUID playerID = null;
    private static final int TICKS_PER_SECOND = 20;
    private static int tickCounter = -1;
    private static int clientTickCounter = -1;
    private static final double[] state = new double[1200];
    private static boolean paused = false;

    private static void clearState() {
        playerName = null;
        playerID = null;
        tickCounter = -1;
        clientTickCounter = -1;
        Arrays.fill(state, 0);
        paused = false;
    }

    private static int getStateCounter() {
        return tickCounter / 20;
    }

    private static void setState(int start, int duration, int intensity, boolean decay) {
        if (duration <= 0) {
            return;
        }

        if (decay) {
            int safeDuration = Math.max(0, duration - 2);
            for (int i = 0; i < safeDuration; i++) {
                setState(start+i, intensity);
            }
            setState(start+safeDuration, intensity/2);
            setState(start+safeDuration+1, intensity/4);
        } else {
            for (int i = 0; i < duration; i++) {
                setState(start+i, intensity);
            }
        }
    }

    private static void setState(int counter, int intensity) {
        boolean accumulate = false; //XXX reserved for future use
        setState(counter, intensity, accumulate);
    }

    private static void setState(int counter, int intensity, boolean accumulate) {
        int safeCounter = counter % state.length;
        if (accumulate) {
            state[safeCounter] = Math.min(1.0, state[safeCounter] + (intensity / 100.0));
        } else {
            state[safeCounter] = Math.min(1.0, Math.max(state[safeCounter], (intensity / 100.0)));
        }
    }

    private static int getIntensity(String type) {
        Map<String, Integer> normal = new HashMap<>();
        normal.put("attack", 60);
        normal.put("hurt", 0);
        normal.put("mine", 80);
        normal.put("xpChange", 100);
        normal.put("harvest", 0);
        normal.put("vitality", 0);

        Map<String, Integer> masochist = new HashMap<>();
        masochist.put("attack", 0);
        masochist.put("hurt", 100);
        masochist.put("mine", 0);
        masochist.put("xpChange", 0);
        masochist.put("harvest", 0);
        masochist.put("vitality", 10);

        Map<String, Integer> hedonist = new HashMap<>();
        hedonist.put("attack", 60);
        hedonist.put("hurt", 10);
        hedonist.put("mine", 80);
        hedonist.put("xpChange", 100);
        hedonist.put("harvest", 20);
        hedonist.put("vitality", 10);

        Map<String, Integer> custom = new HashMap<>();
        custom.put("attack", MinegasmConfig.attackIntensity);
        custom.put("hurt", MinegasmConfig.hurtIntensity);
        custom.put("mine", MinegasmConfig.mineIntensity);
        custom.put("xpChange", MinegasmConfig.xpChangeIntensity);
        custom.put("harvest", MinegasmConfig.harvestIntensity);
        custom.put("vitality", MinegasmConfig.vitalityIntensity);

        if (MinegasmConfig.mode.equals(ClientConfig.GameplayMode.MASOCHIST)) {
            return masochist.get(type);
        } else if (MinegasmConfig.mode.equals(ClientConfig.GameplayMode.HEDONIST)) {
            return hedonist.get(type);
        } else if (MinegasmConfig.mode.equals(ClientConfig.GameplayMode.CUSTOM)) {
            return custom.get(type);
        } else {
            return normal.get(type);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (tickCounter >= 0) {
                if (tickCounter != clientTickCounter) {
                    clientTickCounter = tickCounter;
                    paused = false;
                } else {
                    if (!paused) {
                        paused = true;
                        LOGGER.debug("Pausing");
                        ToyController.setVibrationLevel(0);
                    }

                    if (paused) {
                        LOGGER.trace("Paused");
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event)
    {

    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event)
    {
        LOGGER.debug("Critical: " + event.isVanillaCritical());
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event)
    {

    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event)
    {

    }

    @SubscribeEvent
    public static void onHarvest(PlayerEvent.HarvestCheck event)
    {

    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event)
    {

    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event)
    {
        LOGGER.info("Pickup item: " + event.getItem().toString());
    }

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event)
    {
        //LOGGER.info("Pickup XP: " + event.getOrb().xpValue);
    }

    @SubscribeEvent
    public static void onXpChange(PlayerXpEvent.XpChange event)
    {

    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        clearState();
        ToyController.setVibrationLevel(0);
        populatePlayerInfo();
    }

    private static void populatePlayerInfo() {
        GameProfile profile = Minecraft.getInstance().getUser().getGameProfile();
        playerName = profile.getName();
        playerID = profile.getId();
        System.out.println("Current player: " + playerName + " " + playerID.toString());
    }

    @SubscribeEvent
    public static void onWorldLoaded(WorldEvent.Load event) {

    }

    @SubscribeEvent
    public static void onWorldEntry(EntityJoinWorldEvent event) {
        ToyController.connectDevice();

    }

    @SubscribeEvent
    public static void onWorldExit(EntityLeaveWorldEvent event) {

    }
}


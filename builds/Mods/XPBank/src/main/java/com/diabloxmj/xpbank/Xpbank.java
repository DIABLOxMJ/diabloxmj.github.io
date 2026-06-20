package com.diabloxmj.xpbank;

import net.fabricmc.api.ModInitializer;

import com.diabloxmj.xpbank.network.ConfigSyncPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Xpbank implements ModInitializer {
	public static final String MOD_ID = "xpbank";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initialisation du mod XPBank !");
		ModConfig.load();
		ModItems.registerModItems();
		ModEvents.registerEvents();

		PayloadTypeRegistry.playS2C().register(com.diabloxmj.xpbank.network.ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ConfigSyncPayload payload = new ConfigSyncPayload(
					ModConfig.INSTANCE.small_Vial_Max_Capacity,
					ModConfig.INSTANCE.medium_Vial_Max_Capacity,
					ModConfig.INSTANCE.large_Vial_Max_Capacity,
					ModConfig.INSTANCE.small_super_Vial_Max_Capacity,
					ModConfig.INSTANCE.medium_super_Vial_Max_Capacity,
					ModConfig.INSTANCE.large_super_Vial_Max_Capacity
			);
			sender.sendPacket(payload);
		});
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}

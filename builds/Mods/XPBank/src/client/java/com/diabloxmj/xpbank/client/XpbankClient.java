package com.diabloxmj.xpbank.client;

import com.diabloxmj.xpbank.ModConfig;
import com.diabloxmj.xpbank.network.ConfigSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class XpbankClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				ModConfig.INSTANCE.small_Vial_Max_Capacity = payload.smallMax();
				ModConfig.INSTANCE.medium_Vial_Max_Capacity = payload.mediumMax();
				ModConfig.INSTANCE.large_Vial_Max_Capacity = payload.largeMax();
				ModConfig.INSTANCE.small_super_Vial_Max_Capacity = payload.smallSuperMax();
				ModConfig.INSTANCE.medium_super_Vial_Max_Capacity = payload.mediumSuperMax();
				ModConfig.INSTANCE.large_super_Vial_Max_Capacity = payload.largeSuperMax();
			});
		});
	}
}
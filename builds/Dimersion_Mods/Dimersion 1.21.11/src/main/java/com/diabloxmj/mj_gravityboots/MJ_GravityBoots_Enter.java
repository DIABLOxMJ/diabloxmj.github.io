package com.diabloxmj.mj_gravityboots;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class MJ_GravityBoots_Enter implements ModInitializer {

    @Override
    public void onInitialize() {
        // 1. Enregistrement technique de l'item dans le jeu
        MJ_GravityBoots_Items.registerModItems();

        // 2. Événement de Tick : s'exécute à chaque tick du serveur pour tous les joueurs
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

                // On récupère l'item situé dans le slot des pieds (bottes)
                ItemStack feetStack = player.getEquippedStack(EquipmentSlot.FEET);
                boolean isWearingGravityBoots = !feetStack.isEmpty() && feetStack.isOf(MJ_GravityBoots_Items.GRAVITY_BOOTS);

                if (isWearingGravityBoots) {
                    // Si le joueur met les bottes et que le droit de vol n'est pas encore actif
                    if (!player.getAbilities().allowFlying) {
                        player.getAbilities().allowFlying = true;
                        player.sendAbilitiesUpdate(); // Synchronise instantanément le changement avec le PC du joueur
                    }
                } else {
                    // Si le joueur ne porte PAS les bottes, mais qu'il n'est ni en Créatif ni en Spectateur
                    if (!player.isCreative() && !player.isSpectator()) {
                        // Si le vol était encore actif, on doit le désactiver pour empêcher la triche
                        if (player.getAbilities().allowFlying) {
                            player.getAbilities().allowFlying = false;
                            player.getAbilities().flying = false; // Désactive le vol immédiat s'il était en l'air
                            player.sendAbilitiesUpdate(); // Synchronise instantanément avec le PC du joueur
                        }
                    }
                }
            }
        });
    }
}
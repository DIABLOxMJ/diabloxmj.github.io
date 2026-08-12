package com.diabloxmj.mj_xpbottle;

import net.fabricmc.fabric.api.event.player.UseItemCallback; // Importe l'intercepteur Fabric capturant le clic droit global avec n'importe quel objet
import net.minecraft.entity.player.PlayerEntity; // Importe l'entité représentant le joueur de base
import net.minecraft.item.ItemStack; // Importe la structure gérant l'item et sa quantité
import net.minecraft.item.Items; // Importe le catalogue d'objets natifs de Minecraft Vanilla
import net.minecraft.server.network.ServerPlayerEntity; // Importe l'entité joueur gérée spécifiquement par le serveur
import net.minecraft.sound.SoundCategory; // Importe la catégorie d'attribution des sons (blocs, joueurs, météo...)
import net.minecraft.sound.SoundEvents; // Importe la bibliothèque de sons natifs de Minecraft
import net.minecraft.text.Text; // Importe l'outil de gestion des chaînes textuelles traduisibles
import net.minecraft.util.ActionResult; // Importe la structure indiquant le résultat d'une action (Succès, Échec, Passer)
import net.minecraft.util.Hand; // Importe l'énumération représentant la main utilisée (Main principale ou secondaire)
import net.minecraft.world.World; // Importe la structure représentant la dimension (Overworld, Nether, End)

public class MJ_XPBottle_Event {

    // Méthode de configuration globale des écoutes d'événements du mod
    public static void registerEvents() {
        // Enregistre un écouteur sur le callback global de clic droit avec un objet
        UseItemCallback.EVENT.register((PlayerEntity player, World world, Hand hand) -> {
            // Récupère l'objet actuellement manipulé par la main qui clique
            ItemStack itemStack = player.getStackInHand(hand);

            // Condition de déclenchement : Le joueur doit s'accroupir ET tenir une fiole en verre vide de Minecraft Vanilla
            if (player.isSneaking() && itemStack.isOf(Items.GLASS_BOTTLE)) {
                // S'assure que le traitement se fait exclusivement côté serveur pour sécuriser la modification d'XP
                if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
                    // Étape 1 : Récupère la totalité de l'XP brute actuelle du joueur via notre utilitaire mathématique
                    int playerTotalXp = MJ_XPBottle_Math.getPlayerTotalXp(serverPlayer);

                    // Étape 2 : Vérifie si le joueur possède au moins les 8 points d'XP nécessaires à la transformation
                    if (playerTotalXp < 8) {
                        // Si le joueur est trop pauvre en XP, envoie un message d'erreur dans son actionbar
                        serverPlayer.sendMessage(Text.translatable("chat.dimersion.not_enough_xp_converter"), true);
                        return ActionResult.FAIL; // Bloque l'action et empêche de lever le bras
                    }

                    // Étape 3 : Soustrait les 8 points d'XP de la réserve du joueur et ré-applique le nouveau total
                    MJ_XPBottle_Math.setPlayerTotalXp(serverPlayer, playerTotalXp - 8);

                    // Étape 4 : Consomme et retire 1 bouteille en verre vide de la main du joueur
                    itemStack.decrement(1);

                    // Étape 5 : Crée un tout nouvel ItemStack contenant 1 bouteille d'expérience magique Vanilla (Splash)
                    ItemStack expBottle = new ItemStack(Items.EXPERIENCE_BOTTLE);

                    // Étape 6 : Tente de glisser la bouteille d'XP directement dans l'inventaire du joueur
                    if (!serverPlayer.getInventory().insertStack(expBottle)) {
                        // Si l'inventaire est saturé, la bouteille d'XP est matérialisée au sol devant lui
                        serverPlayer.dropItem(expBottle, false);
                    }

                    // Étape 7 : Joue le bruit de l'alambic qui infuse à l'emplacement exact du joueur pour l'immersion
                    world.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                            SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.PLAYERS, 1.0F, 1.0F);

                    // Étape 8 : Notifie le joueur de la réussite du processus de sacrifice d'XP
                    serverPlayer.sendMessage(Text.translatable("chat.dimersion.converter_success"), true);
                }
                // Indique à Minecraft que l'action a été consommée avec succès (évite les comportements étranges en simultané)
                return ActionResult.SUCCESS;
            }
            // Si le joueur ne s'accroupit pas ou ne tient pas de fiole vide, laisse Minecraft exécuter son comportement par défaut
            return ActionResult.PASS;
        });
    }
}
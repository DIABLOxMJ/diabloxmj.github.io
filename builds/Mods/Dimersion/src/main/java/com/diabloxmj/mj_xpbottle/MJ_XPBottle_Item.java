package com.diabloxmj.mj_xpbottle;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import java.util.function.Consumer;

// Définition de la classe principale de notre fiole d'XP qui hérite de la classe générique Item de Minecraft
public class MJ_XPBottle_Item extends Item {

    // Variable textuelle interne servant à identifier le format de la fiole ("lvl1", "lvl2", etc.)
    private final String bottleType;

    // Constructeur initialisant l'item avec ses paramètres (Settings) et son type de fiole attitré
    public MJ_XPBottle_Item(Settings settings, String bottleType) {
        super(settings); // Appelle le constructeur de la classe mère (Item)
        this.bottleType = bottleType; // Assigne le type reçu à notre variable interne
    }

    // Méthode dynamique retournant la capacité maximale de la fiole en lisant directement le fichier de configuration actuel
    public int getMaxCapacity() {
        return switch (this.bottleType) {
            case "lvl1" -> MJ_XPBottle_Config.INSTANCE.XPBottle_lvl1_Max_Capacity; // Capacité max configurée pour les petites fioles
            case "lvl2" -> MJ_XPBottle_Config.INSTANCE.XPBottle_lvl2_Max_Capacity; // Capacité max configurée pour les fioles moyennes
            case "lvl3" -> MJ_XPBottle_Config.INSTANCE.XPBottle_lvl3_Max_Capacity; // Capacité max configurée pour les grandes fioles
            case "lvl4" -> MJ_XPBottle_Config.INSTANCE.XPBottle_lvl4_Max_Capacity; // Capacité max configurée pour les petites super fioles
            case "lvl5" -> MJ_XPBottle_Config.INSTANCE.XPBottle_lvl5_Max_Capacity; // Capacité max configurée pour les super fioles moyennes
            case "lvl6" -> MJ_XPBottle_Config.INSTANCE.XPBottle_lvl6_Max_Capacity; // Capacité max configurée pour les grandes super fioles
            default -> 128; // Valeur de secours par défaut si aucun type ne correspond
        };
    }

    // Méthode déclenchée lorsque le joueur fait un clic droit avec l'item en main
    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        // Récupère l'ItemStack (le groupe d'objets) actuellement présent dans la main du joueur
        ItemStack itemStack = player.getStackInHand(hand);

        // Bloque l'exécution si on est sur le client (visuel) ou si le joueur n'est pas un joueur serveur physique
        if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {

            // Isole une seule fiole du stack pour appliquer les changements uniquement sur celle-ci et éviter les duplications
            ItemStack singleBottle = itemStack.copyWithCount(1);
            // Récupère la quantité d'XP actuellement stockée à l'intérieur de cette fiole isolée
            int currentStoredXp = getStoredXp(singleBottle);

            // Vérifie dans la configuration si cette fiole fonctionne en mode Niveaux (true) ou en mode Points (false)
            boolean isLevelMode = checkBottleIsLevelMode();
            // Détermine la quantité d'XP actuelle du joueur selon le mode actif (son niveau ou ses points totaux)
            int playerCurrentPool = isLevelMode ? serverPlayer.experienceLevel : MJ_XPBottle_Math.getPlayerTotalXp(serverPlayer);

            // Récupère la capacité maximale actualisée de cette fiole précise
            int currentMaxCapacity = getMaxCapacity();

            // CAS 1 : Le joueur s'accroupit (Sneak) + Clic Droit -> Il DÉPOSE de l'XP dans la fiole
            if (serverPlayer.isSneaking()) {
                // Si la réserve d'XP du joueur est vide, on annule l'action
                if (playerCurrentPool <= 0) {
                    serverPlayer.sendMessage(Text.translatable("chat.dimersion.player_empty"), true); // Message d'erreur discret dans l'actionbar
                    return ActionResult.FAIL; // Retourne un échec de l'action
                }
                // Si la fiole a déjà atteint ou dépassé sa capacité maximale actuelle, on annule l'action
                if (currentStoredXp >= currentMaxCapacity) {
                    serverPlayer.sendMessage(Text.translatable("chat.dimersion.bottle_full"), true); // Message d'erreur indiquant que la fiole est pleine
                    return ActionResult.FAIL; // Retourne un échec de l'action
                }

                // Calcule l'espace encore disponible dans la fiole
                int spaceLeftInBottle = currentMaxCapacity - currentStoredXp;
                // Détermine la quantité exacte à transférer (le minimum entre ce que possède le joueur et l'espace restant)
                int toDeposit = Math.min(playerCurrentPool, spaceLeftInBottle);

                // Soustrait l'XP calculée de la réserve du joueur selon le mode configuré
                if (isLevelMode) {
                    serverPlayer.setExperienceLevel(playerCurrentPool - toDeposit); // Applique le nouveau niveau réduit au joueur
                } else {
                    MJ_XPBottle_Math.setPlayerTotalXp(serverPlayer, playerCurrentPool - toDeposit); // Applique le nouveau total de points réduits au joueur
                }

                // Ajoute l'XP transférée dans les données internes de notre fiole isolée
                setStoredXp(singleBottle, currentStoredXp + toDeposit);

                // FORCE LA SYNCHRONISATION DE L'XP : Renvoie un paquet réseau au client pour actualiser instantanément l'ATH (la barre verte) du joueur
                serverPlayer.addExperience(0);
                // Ligne de secours native : Demande explicitement à Minecraft de resynchroniser toutes les propriétés de l'ATH du joueur (Barre d'XP, Nourriture, etc.)
                serverPlayer.currentScreenHandler.syncState();

                // Envoie un message de confirmation au joueur dans l'actionbar
                serverPlayer.sendMessage(Text.translatable("chat.dimersion.deposit_success"), true);
            }
            // CAS 2 : Le joueur fait un Clic Droit simple -> Il RÉCUPÈRE l'XP de la fiole
            else {
                // Si la fiole ne contient aucune expérience, on stoppe l'action
                if (currentStoredXp <= 0) {
                    serverPlayer.sendMessage(Text.translatable("chat.dimersion.bottle_empty"), true); // Message indiquant que la fiole est vide
                    return ActionResult.FAIL; // Retourne un échec de l'action
                }

                // Ajoute l'XP de la fiole à la réserve du joueur selon le mode configuré
                if (isLevelMode) {
                    serverPlayer.setExperienceLevel(serverPlayer.experienceLevel + currentStoredXp); // Ajoute le contenu sous forme de niveaux directs
                } else {
                    MJ_XPBottle_Math.setPlayerTotalXp(serverPlayer, playerCurrentPool + currentStoredXp); // Ajoute le contenu sous forme de points bruts
                }

                // Vide complètement l'expérience contenue dans notre fiole isolée
                setStoredXp(singleBottle, 0);

                // FORCE LA SYNCHRONISATION DE L'XP : Solution essentielle qui pousse le serveur à réévaluer et renvoyer la valeur exacte au client pour rafraîchir l'affichage
                serverPlayer.addExperience(0);
                // Ligne de secours native : Demande explicitement à Minecraft de resynchroniser toutes les propriétés de l'ATH du joueur (Barre d'XP, Nourriture, etc.)
                serverPlayer.currentScreenHandler.syncState();

                // Envoie un message de confirmation de retrait au joueur
                serverPlayer.sendMessage(Text.translatable("chat.dimersion.withdraw_success"), true);
            }

            // GESTION ET REMPLACEMENT DE L'ITEM DANS LA MAIN DU JOUEUR
            // Si le joueur ne tenait qu'une seule fiole dans sa main
            if (itemStack.getCount() == 1) {
                player.setStackInHand(hand, singleBottle); // Remplace directement l'item en main par notre fiole modifiée (singleBottle)
            } else {
                itemStack.decrement(1); // Réduit de 1 la quantité du groupe de fioles que le joueur tenait
                // Tente d'insérer notre fiole modifiée (singleBottle) dans un emplacement libre de l'inventaire du joueur
                if (!player.getInventory().insertStack(singleBottle)) {
                    player.dropItem(singleBottle, false); // Si l'inventaire est plein, la fiole modifiée est jetée au sol devant lui
                }
            }

            return ActionResult.SUCCESS; // Retourne que l'action s'est déroulée avec succès
        }

        return ActionResult.SUCCESS; // Retourne un succès par défaut pour le côté client
    }

    // Méthode permettant d'extraire la valeur numérique d'XP stockée dans le composant NBT CUSTOM_DATA de l'item
    public int getStoredXp(ItemStack stack) {
        // Récupère le composant de données personnalisées (NBT) attaché à l'ItemStack
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            var nbt = customData.copyNbt(); // Extrait une copie modifiable du NbtCompound standard de l'objet
            // Vérifie si la balise spécifique "stored_xp" est enregistrée dans le NBT
            if (nbt.contains("stored_xp")) {
                return nbt.getInt("stored_xp").orElse(0); // Extrait l'entier de manière sécurisée (renvoie 0 s'il y a un problème)
            }
        }
        return 0; // Renvoie 0 si l'item n'a aucune donnée d'XP enregistrée
    }

    // Méthode permettant d'enregistrer l'XP dans l'item et de mettre à jour son index de texture pour les modèles
    public void setStoredXp(ItemStack stack, int amount) {
        // Applique l'écriture de l'entier "stored_xp" dans le composant de données CUSTOM_DATA de l'ItemStack
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbtComponent -> {
            var nbt = nbtComponent.copyNbt(); // Copie le conteneur NBT existant
            nbt.putInt("stored_xp", amount); // Écrit ou écrase la clé "stored_xp" avec la nouvelle quantité d'XP
            return NbtComponent.of(nbt); // Enregistre et verrouille le nouveau composant dans l'objet
        });

        float textureIndex = 0.0F; // Initialise l'index visuel par défaut à 0.0F (Texture de la fiole vide)
        int currentMaxCapacity = getMaxCapacity(); // Récupère la capacité maximale de la fiole pour calculer le pourcentage

        // Structure conditionnelle calculant le palier de remplissage visuel de la fiole (Index de 0 à 3)
        if (amount > 0) {
            if (amount >= currentMaxCapacity) {
                textureIndex = 3.0F; // Palier 3 : La fiole est totalement pleine (100%)
            } else {
                float percentage = (float) amount / currentMaxCapacity; // Calcule le taux de remplissage en float (0.0 à 1.0)
                if (percentage >= 0.50F) {
                    textureIndex = 2.0F; // Palier 2 : La fiole est remplie à la moitié ou plus (50% à 99%)
                } else {
                    textureIndex = 1.0F; // Palier 1 : La fiole est remplie au quart (1% à 49%)
                }
            }
        }

        // Crée une liste immuable contenant notre index sous forme de Float pour le moteur graphique
        java.util.List<Float> floatList = java.util.List.of(textureIndex);
        // Assigne l'index calculé au composant CUSTOM_MODEL_DATA de l'item afin que le fichier JSON charge instantanément le bon modèle
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(
                floatList, // Injecte notre float de texture en première position (Index 0 lu par le range_dispatch du JSON)
                java.util.List.of(), // Liste de booléens vide
                java.util.List.of(), // Liste de chaînes de caractères vide
                java.util.List.of()  // Liste d'entiers vide
        ));
    }

    // Méthode interne vérifiant si le type de cette fiole doit manipuler des Niveaux (LEVELS) ou des Points (POINTS)
    private boolean checkBottleIsLevelMode() {
        // Évalue la configuration associée au type de notre fiole pour récupérer sa chaîne de format
        String type = switch (this.bottleType) {
            case "lvl1" -> MJ_XPBottle_Config.INSTANCE.XPBottle_lvl1_Xp_Type;
            case "lvl2" -> MJ_XPBottle_Config.INSTANCE.XPBottle_lvl2_Xp_Type;
            case "lvl3" -> MJ_XPBottle_Config.INSTANCE.XPBottle_lvl3_Xp_Type;
            case "lvl4" -> MJ_XPBottle_Config.INSTANCE.XPBottle_lvl4_Xp_Type;
            case "lvl5" -> MJ_XPBottle_Config.INSTANCE.XPBottle_lvl5_Xp_Type;
            case "lvl6" -> MJ_XPBottle_Config.INSTANCE.XPBottle_lvl6_Xp_Type;
            default -> "POINTS"; // Format par défaut si aucune correspondance n'est trouvée
        };
        return "LEVELS".equalsIgnoreCase(type); // Renvoie true si la chaîne configurée correspond textuellement à "LEVELS"
    }

    // Méthode gérant l'affichage des lignes d'informations (Infobulles / Tooltips) lorsque le joueur survole l'item
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, net.minecraft.component.type.TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        int current = getStoredXp(stack); // Extrait la quantité actuelle d'XP présente dans l'item survolé
        String unit = checkBottleIsLevelMode() ? " Lvl" : " Exp"; // Sélectionne le suffixe texte approprié selon le mode de la fiole

        // Construit le texte de l'infobulle en couleur verte (§a) affichant "Unité: Actuel / MaxActuel" (Ex: Exp: 250 / 500)
        textConsumer.accept(Text.literal("§a" + unit + ": " + current + " / " + getMaxCapacity()));
        // Appelle la méthode d'affichage parente pour préserver les autres infobulles natives de Minecraft si nécessaire
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
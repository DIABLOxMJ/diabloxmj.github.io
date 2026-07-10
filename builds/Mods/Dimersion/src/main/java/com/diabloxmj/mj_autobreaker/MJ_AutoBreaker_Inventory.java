package com.diabloxmj.mj_autobreaker; // Déclare le dossier (package) où se trouve ce fichier de code

import net.minecraft.entity.player.PlayerEntity; // Importe l'entité du joueur (utile pour vérifier qui ouvre le bloc)
import net.minecraft.inventory.Inventory; // Importe l'interface d'inventaire standard de Minecraft
import net.minecraft.item.ItemStack; // Importe l'objet ItemStack (un groupe d'items, ex: 64 Diamants)
import net.minecraft.util.collection.DefaultedList; // Importe une liste spéciale de Minecraft qui se remplit automatiquement par défaut
import net.minecraft.util.math.Direction; // Importe les directions pour la gestion des faces (entonnoirs/tubes)
import org.jetbrains.annotations.Nullable; // Importe l'annotation indiquant qu'un paramètre peut être inexistant (null)

public interface MJ_AutoBreaker_Inventory extends Inventory { // Notre interface hérite de l'Inventory officiel de Minecraft

    // C'est la SEULE méthode obligatoire que notre BlockEntity devra implémenter. Elle donne accès à la liste physique des objets.
    DefaultedList<ItemStack> getItems();

    @Override
    default int size() { // Renvoie la taille totale de l'inventaire
        return getItems().size(); // Demande la taille de la liste (dans notre cas, ce sera 19 slots)
    }

    @Override
    default boolean isEmpty() { // Vérifie si la machine est totalement vide
        for (int i = 0; i < size(); i++) { // Allez, on parcourt chaque slot un par un
            ItemStack stack = getStack(i); // On regarde ce qu'il y a dans le slot
            if (!stack.isEmpty()) { // Si on trouve ne serait-ce qu'un seul item...
                return false; // ...alors l'inventaire n'est pas vide !
            }
        }
        return true; // Si la boucle s'est finie sans rien trouver, c'est que c'est bien vide.
    }

    @Override
    default ItemStack getStack(int slot) { // Récupère l'objet présent dans un slot précis
        return getItems().get(slot); // Va chercher l'objet dans la liste au numéro demandé
    }

    @Override
    default ItemStack removeStack(int slot, int count) { // Retire une quantité précise d'objets d'un slot (ex: quand on prend une demi-pile)
        // La méthode "splitStack" de Minecraft coupe la pile en deux : elle prend la quantité demandée et laisse le reste
        ItemStack result = net.minecraft.inventory.Inventories.splitStack(getItems(), slot, count);
        if (!result.isEmpty()) markDirty(); // Si on a effectivement retiré quelque chose, on signale que l'inventaire a changé
        return result; // Renvoie les items retirés
    }

    @Override
    default ItemStack removeStack(int slot) { // Vide complètement un slot d'un coup (ex: clic molette ou shift+clic)
        if (slot == 0) return ItemStack.EMPTY; // Sécurité : On interdit le vidage automatique brutal du slot d'outil par ce biais
        return net.minecraft.inventory.Inventories.removeStack(getItems(), slot); // Retire et renvoie tout le contenu du slot
    }

    @Override
    default void setStack(int slot, ItemStack stack) { // Force un objet à aller dans un slot précis
        getItems().set(slot, stack); // Écrase le contenu du slot avec le nouvel objet
        if (stack.getCount() > stack.getMaxCount()) { // Sécurité : Si on essaie de mettre 99 items dans une pile limitée à 64
            stack.setCount(stack.getMaxCount()); // On bride la pile à son maximum légal (64 ou 16 selon l'item)
        }
        markDirty(); // Signale le changement au jeu pour la sauvegarde
    }

    @Override
    default void clear() { // Vide absolument TOUS les slots de la machine
        getItems().clear(); // Efface la liste complète
        markDirty(); // Signale le changement
    }

    @Override
    default void markDirty() {} // Méthode vide ici, elle sera remplacée (override) par la vraie méthode de sauvegarde du bloc

    @Override
    default boolean canPlayerUse(PlayerEntity player) { // Vérifie si le joueur triche ou est trop loin pour fouiller la machine
        return true; // Autorise l'accès par défaut (la distance sera revérifiée proprement par le ScreenHandler)
    }

    // Déclaration des deux méthodes de SidedInventory (gestion des entonnoirs/hoppers) qui seront codées dans la BlockEntity
    int[] getAvailableSlots(Direction side);
    boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir);
}
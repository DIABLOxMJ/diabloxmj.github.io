package com.diabloxmj.mj_autobreaker; // Déclare le dossier (package) où se trouve ce fichier de code

import com.diabloxmj.mj_dimersion.MJ_Dimersion_Enter; // Importe l'initialiseur principal pour récupérer le MOD_ID ("dimersion")
import net.minecraft.block.AbstractBlock; // Importe la classe de base des paramètres de configuration des blocs
import net.minecraft.block.Block; // Importe l'objet Bloc générique de Minecraft
import net.minecraft.block.Blocks; // Importe la liste de tous les blocs Vanilla (Stone, Dirt, Observer, etc.)
import net.minecraft.block.entity.BlockEntityType; // Importe le type de structure nécessaire pour attacher des données/inventaires aux blocs
import net.minecraft.item.BlockItem; // Importe l'objet Item capable de poser un bloc quand on fait un clic droit
import net.minecraft.item.Item; // Importe l'objet Item générique de Minecraft
import net.minecraft.registry.Registries; // Importe la liste de tous les registres du jeu (le grand catalogue de Minecraft)
import net.minecraft.registry.Registry; // Importe l'outil qui permet d'inscrire un élément dans un registre
import net.minecraft.registry.RegistryKey; // Importe la structure de clé d'identification unique pour les registres
import net.minecraft.registry.RegistryKeys; // Importe la table contenant les catégories de clés (Clé de bloc, clé d'item...)
import net.minecraft.util.Identifier; // Importe l'objet permettant de fabriquer des IDs au format "modid:nom_objet"

public class MJ_AutoBreaker_Blocks { // Début de notre classe qui va stocker nos blocs

    public static Block AUTO_BREAKER_BLOCK; // Variable globale qui contiendra l'instance unique de notre bloc AutoBreaker
    public static BlockEntityType<MJ_AutoBreaker_BlockEntity> AUTO_BREAKER_BLOCK_ENTITY_TYPE; // Variable globale qui contiendra le type enregistré de notre inventaire/données de bloc

    public static void registerModBlocks() { // Méthode appelée au lancement du jeu pour exécuter les enregistrements
        Identifier blockId = Identifier.of(MJ_Dimersion_Enter.MOD_ID, "mj_autobreaker"); // Fabrique l'identifiant unique "dimersion:mj_autobreaker"
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, blockId); // Crée la clé d'enregistrement officielle dans la catégorie des Blocs
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, blockId); // Crée la clé d'enregistrement officielle dans la catégorie des Items (pour le bloc dans l'inventaire)

        // 1. Création et enregistrement du Bloc physique
        AUTO_BREAKER_BLOCK = Registry.register( // Demande à Minecraft d'ajouter notre bloc à son catalogue officiel
                Registries.BLOCK, blockKey, // Dans le registre des BLOCS, à l'emplacement défini par notre clé
                new MJ_AutoBreaker_Block(AbstractBlock.Settings.copy(Blocks.OBSERVER) // Crée l'objet en copiant les caractéristiques de base de l'Observer
                        .registryKey(blockKey) // Assigne sa clé de registre au bloc (obligatoire en 1.21+)
                        .strength(2.5F, 200.0F) // Écrase les valeurs copiées : 2.5F = Dureté face aux outils, 200.0F = Très forte résistance aux explosions
                )
        );

        // 2. Enregistrement de l'Item associé (pour pouvoir le tenir en main, l'avoir dans l'inventaire créatif et le poser)
        Registry.register(
                Registries.ITEM, itemKey, // Dans le registre des ITEMS, à l'emplacement défini par notre clé d'item
                new BlockItem(AUTO_BREAKER_BLOCK, new Item.Settings().registryKey(itemKey)) // Crée un BlockItem lié à notre bloc physique
        );

        // 3. Liaison officielle de l'inventaire (BlockEntity) via Fabric
        AUTO_BREAKER_BLOCK_ENTITY_TYPE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE, // Dans le registre des types d'entités de bloc (inventaires de blocs)
                Identifier.of(MJ_Dimersion_Enter.MOD_ID, "mj_autobreaker"), // À l'identifiant "dimersion:mj_autobreaker"
        net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create( // Utilise l'outil Fabric pour construire le type
                MJ_AutoBreaker_BlockEntity::new, // Indique au jeu quelle classe instancier (le constructeur de notre BlockEntity)
                AUTO_BREAKER_BLOCK // Indique à quel bloc physique cette BlockEntity a le droit de s'attacher
        ).build(null) // Construit le type (le paramètre null est requis par l'API de validation des types de données de données héritées)
        );
    }
}
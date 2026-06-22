package com.diabloxmj.xpbank; // Définit l'adresse logique (package) du fichier dans l'arborescence

import net.fabricmc.api.ModInitializer; // Importe l'interface obligatoire de Fabric pour initialiser un mod

import com.diabloxmj.xpbank.network.ConfigSyncPayload; // Importe la structure de notre paquet réseau personnalisé
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry; // Importe le registre réseau pour déclarer nos paquets
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents; // Importe les déclencheurs (events) de connexion réseau
import net.minecraft.util.Identifier; // Importe l'outil de création d'ID de Minecraft (namespace:path)

import org.slf4j.Logger; // Importe l'outil de gestion des logs standard de Java
import org.slf4j.LoggerFactory; // Importe la fabrique pour générer le gestionnaire de logs

// Classe principale qui implémente ModInitializer : elle contient le commutateur de démarrage
public class Xpbank implements ModInitializer {
	// Identifiant textuel unique de ton mod (utilisé pour les textures, les paquets, le JSON...)
	public static final String MOD_ID = "xpbank";

	// Crée une instance de Logger liée au MOD_ID pour afficher de beaux messages propres dans la console de Minecraft
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Méthode s'exécutant automatiquement au démarrage du serveur de jeu (Solo ou Dédié)
	@Override
	public void onInitialize() {
		LOGGER.info("Mod initialization XPBank!"); // Envoie un message informatif dans la console de log
		ModConfig.load(); // Déclenche la lecture et le chargement du fichier de configuration JSON
		ModItems.registerModItems(); // Déclenche l'enregistrement de l'intégralité de nos fioles d'XP
		ModEvents.registerEvents(); // Déclenche l'écoute de nos événements personnalisés

		// Enregistre officiellement notre paquet "ConfigSyncPayload" dans le registre réseau de type S2C (Server-To-Client)
		PayloadTypeRegistry.playS2C().register(com.diabloxmj.xpbank.network.ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);

		// Événement s'activant dès qu'un joueur passe l'étape de connexion et apparaît physiquement dans le monde
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			// Le serveur crée une enveloppe réseau (Payload) contenant toutes ses propres valeurs de capacité maximale
			ConfigSyncPayload payload = new ConfigSyncPayload(
					ModConfig.INSTANCE.XPBottle_lvl1_Max_Capacity,
					ModConfig.INSTANCE.XPBottle_lvl2_Max_Capacity,
					ModConfig.INSTANCE.XPBottle_lvl3_Max_Capacity,
					ModConfig.INSTANCE.XPBottle_lvl4_Max_Capacity,
					ModConfig.INSTANCE.XPBottle_lvl5_Max_Capacity,
					ModConfig.INSTANCE.XPBottle_lvl6_Max_Capacity
			);
			// Le serveur expédie l'enveloppe sur le réseau à destination unique du joueur qui vient d'entrer
			sender.sendPacket(payload);
		});
	}

	// Méthode utilitaire permettant de générer rapidement un identifiant au format standardisé "xpbank:nom_de_l_element"
	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path); // Construit l'identifiant avec le namespace de ton mod
	}
}
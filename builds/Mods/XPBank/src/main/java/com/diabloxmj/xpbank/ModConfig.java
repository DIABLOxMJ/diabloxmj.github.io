package com.diabloxmj.xpbank; // Intégré au package principal

import com.google.gson.Gson; // Importe l'outil Google GSON chargé de transcrire du texte JSON en objet Java et inversement
import com.google.gson.GsonBuilder; // Importe le configurateur de format pour le moteur GSON
import net.fabricmc.loader.api.FabricLoader; // Importe l'API Fabric permettant d'extraire les chemins d'accès système du jeu

import java.io.File; // Importe la structure de manipulation de fichier système standard de Java
import java.io.FileReader; // Importe l'outil de lecture de fichier texte
import java.io.FileWriter; // Importe l'outil d'écriture de fichier texte
import java.io.IOException; // Importe la gestion des erreurs d'Entrées/Sorties disque

public class ModConfig {
    // Initialise le moteur GSON en lui ordonnant d'écrire le JSON de manière aérée et lisible (setPrettyPrinting)
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Détermine l'emplacement physique du fichier de config sur le disque (ex: .minecraft/config/diabloxmj_xpbank.json)
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "diabloxmj_xpbank.json");

    // VARIABLES DE STOCKAGE : Valeurs par défaut écrites si le fichier JSON n'existe pas encore
    public int XPBottle_lvl1_Max_Capacity = 2500;
    public int XPBottle_lvl2_Max_Capacity = 5000;
    public int XPBottle_lvl3_Max_Capacity = 10000;
    public int XPBottle_lvl4_Max_Capacity = 25000;
    public int XPBottle_lvl5_Max_Capacity = 50000;
    public int XPBottle_lvl6_Max_Capacity = 100000;

    // VARIABLES DE TYPE : Déterminent le comportement de calcul ("POINTS" ou "LEVELS")
    public String XPBottle_lvl1_Xp_Type = "POINTS";
    public String XPBottle_lvl2_Xp_Type = "POINTS";
    public String XPBottle_lvl3_Xp_Type = "POINTS";
    public String XPBottle_lvl4_Xp_Type = "POINTS";
    public String XPBottle_lvl5_Xp_Type = "POINTS";
    public String XPBottle_lvl6_Xp_Type = "POINTS";

    // Conteneur Statique Global (Singelton) : C'est cette variable précise que tout le mod interroge en permanence
    public static ModConfig INSTANCE = new ModConfig();

    // Méthode de lecture appelée au démarrage du mod
    public static void load() {
        // Si le fichier existe déjà sur le disque de la machine
        if (CONFIG_FILE.exists()) {
            // Ouvre un flux de lecture sur le fichier de manière sécurisée
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                // Déserialize : Transforme le texte du JSON pour écraser et remplir les variables de notre objet INSTANCE
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                // Sécurité : Si le fichier était corrompu ou vide, recrée une instance propre par défaut
                if (INSTANCE == null) {
                    INSTANCE = new ModConfig();
                }
            } catch (IOException e) {
                // Enregistre l'erreur dans la console si le disque est inaccessible ou verrouillé
                Xpbank.LOGGER.error("Unable to read XPBank config file, using default values.", e);
            }
        } else {
            // Si le fichier est absent (premier lancement du mod), appelle la méthode d'écriture pour le générer
            save();
        }
    }

    // Méthode d'écriture permettant de figer les valeurs actuelles de la mémoire vers le disque dur
    public static void save() {
        // Ouvre un flux d'écriture sur le fichier cible
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            // Serialize : Traduit l'objet INSTANCE de notre mémoire Java en texte structuré JSON et l'écrit dans le fichier
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            // Enregistre l'erreur en console en cas d'impossibilité d'écriture
            Xpbank.LOGGER.error("Unable to save the XPBank configuration file.", e);
        }
    }
}
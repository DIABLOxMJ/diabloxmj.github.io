package com.diabloxmj.mj_excavator;

import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
        import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class MJ_Excavator_Config {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("mj_excavator.properties");

    // On utilise une Map ordonnée pour stocker nos configurations et leurs commentaires
    private static final Map<String, Integer> DURABILITIES = new LinkedHashMap<>();
    private static final Map<String, String> COMMENTS = new LinkedHashMap<>();

    public static void load() {
        // 1. Définition des valeurs par défaut et de leurs commentaires explicatifs
        setupDefault("wood_durability", 59 * 3, "Durabilité des outils en Bois (Pickavator, Shocavator, Axecavator). Par défaut: Wood Vanilla x 3");
        setupDefault("stone_durability", 131 * 3, "Durabilité des outils en Pierre. Par défaut: Stone Vanilla x 3");
        setupDefault("iron_durability", 250 * 3, "Durabilité des outils en Fer. Par défaut: Iron Vanilla x 3");
        setupDefault("copper_durability", 180 * 3, "Durabilité des outils en Cuivre (si présent).");
        setupDefault("gold_durability", 32 * 3, "Durabilité des outils en Or. Par défaut: Gold Vanilla x 3");
        setupDefault("diamond_durability", 1561 * 3, "Durabilité des outils en Diamant. Par défaut: Diamond Vanilla x 3");
        setupDefault("netherite_durability", 2031 * 3, "Durabilité des outils en Netherite. Par défaut: Netherite Vanilla x 3");

        // 2. Lecture ou écriture du fichier
        if (Files.exists(CONFIG_PATH)) {
            readConfig();
        } else {
            saveConfig();
        }
    }

    private static void setupDefault(String key, int defaultValue, String comment) {
        DURABILITIES.put(key, defaultValue);
        COMMENTS.put(key, comment);
    }

    private static void readConfig() {
        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    if (DURABILITIES.containsKey(key)) {
                        try {
                            DURABILITIES.put(key, Integer.parseInt(parts[1].trim()));
                        } catch (NumberFormatException e) {
                            MJ_Excavator_Enter.LOGGER.error("Valeur invalide dans la config pour la clé: " + key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            MJ_Excavator_Enter.LOGGER.error("Impossible de lire le fichier de config de l'Excavator", e);
        }
    }

    public static void saveConfig() {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            writer.write("# ==================================================\n");
            writer.write("#        CONFIGURATION DU MOD MJ_EXCAVATOR          \n");
            writer.write("# ==================================================\n\n");

            for (String key : DURABILITIES.keySet()) {
                // Écriture du commentaire automatisé avant la variable
                writer.write("# " + COMMENTS.get(key) + "\n");
                writer.write(key + "=" + DURABILITIES.get(key) + "\n\n");
            }
        } catch (IOException e) {
            MJ_Excavator_Enter.LOGGER.error("Impossible de sauvegarder le fichier de config de l'Excavator", e);
        }
    }

    // Sécurité pour récupérer facilement la durabilité partout dans le code du mod
    public static int getDurability(String key) {
        return DURABILITIES.getOrDefault(key, 100);
    }
}
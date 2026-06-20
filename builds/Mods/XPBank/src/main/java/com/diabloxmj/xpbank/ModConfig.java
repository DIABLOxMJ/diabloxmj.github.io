package com.diabloxmj.xpbank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "diabloxmj_xpbank.json");

    public int small_Vial_Max_Capacity = 2500;
    public int medium_Vial_Max_Capacity = 5000;
    public int large_Vial_Max_Capacity = 10000;
    public int small_super_Vial_Max_Capacity = 5;
    public int medium_super_Vial_Max_Capacity = 30;
    public int large_super_Vial_Max_Capacity = 150;

    // Types d'XP : "POINTS" ou "LEVELS"
    public String small_Vial_Xp_Type = "POINTS";
    public String medium_Vial_Xp_Type = "POINTS";
    public String large_Vial_Xp_Type = "POINTS";
    public String small_super_Vial_Xp_Type = "LEVELS";
    public String medium_super_Vial_Xp_Type = "LEVELS";
    public String large_super_Vial_Xp_Type = "LEVELS";

    public static ModConfig INSTANCE = new ModConfig();

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new ModConfig();
                }
            } catch (IOException e) {
                Xpbank.LOGGER.error("Unable to read XPBank config file, using default values.", e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            Xpbank.LOGGER.error("Unable to save the XPBank configuration file.", e);
        }
    }
}
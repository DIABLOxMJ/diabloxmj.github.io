package com.diabloxmj.lavasky;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LavaSkyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "diabloxmj_lavasky.json");

    public boolean ActiveLavaSky = true;
    public boolean NoFogViewer = true;
    public boolean BestPerformance = true;

    private static LavaSkyConfig instance;

    public static LavaSkyConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                instance = GSON.fromJson(reader, LavaSkyConfig.class);
            } catch (IOException e) {
                instance = new LavaSkyConfig();
            }
        } else {
            instance = new LavaSkyConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) instance = new LavaSkyConfig();
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package com.diabloxmj.mj_skyislava;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MJ_LavaSky_Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "diabloxmj_lavasky.json");

    public boolean ActiveLavaSky = false;
    public boolean NoFogViewer = true;
    public boolean BestPerformance = false;

    private static MJ_LavaSky_Config instance;

    public static MJ_LavaSky_Config get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                instance = GSON.fromJson(reader, MJ_LavaSky_Config.class);
            } catch (IOException e) {
                instance = new MJ_LavaSky_Config();
            }
        } else {
            instance = new MJ_LavaSky_Config();
            save();
        }
    }

    public static void save() {
        if (instance == null) instance = new MJ_LavaSky_Config();
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
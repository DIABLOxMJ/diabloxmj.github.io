package com.diabloxmj.mj_fogvision;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MJ_FogVision_Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "diabloxmj_fogvision.json");

    public boolean NoFogViewer = true;

    private static MJ_FogVision_Config instance;

    public static MJ_FogVision_Config get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                instance = GSON.fromJson(reader, MJ_FogVision_Config.class);
            } catch (IOException e) {
                instance = new MJ_FogVision_Config();
            }
        } else {
            instance = new MJ_FogVision_Config();
            save();
        }
    }

    public static void save() {
        if (instance == null) instance = new MJ_FogVision_Config();
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
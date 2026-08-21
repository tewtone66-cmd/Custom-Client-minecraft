package com.client.config;

import com.client.ClientCore;
import com.client.module.Module;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigManager {
    private final File configFile;

    public ConfigManager() {
        Path dir = FabricLoader.getInstance().getConfigDir();
        configFile = dir.resolve("custom_client.properties").toFile();
    }

    public void saveConfig() {
        Properties p = new Properties();
        for (Module m : ClientCore.getInstance().getModuleManager().getModules()) {
            p.setProperty(m.getName() + "_enabled", Boolean.toString(m.isEnabled()));
        }
        try (Writer w = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
            p.store(w, "Custom Client");
        } catch (IOException e) {
            ClientCore.LOGGER.error("Failed to save config", e);
        }
    }

    public void loadConfig() {
        if (!configFile.exists()) return;
        Properties p = new Properties();
        try (Reader r = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            p.load(r);
            for (Module m : ClientCore.getInstance().getModuleManager().getModules()) {
                String value = p.getProperty(m.getName() + "_enabled");
                if (value != null) m.setEnabled(Boolean.parseBoolean(value));
            }
        } catch (IOException e) {
            ClientCore.LOGGER.error("Failed to load config", e);
        }
    }
}

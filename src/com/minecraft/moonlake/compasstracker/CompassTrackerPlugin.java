/*
 * Copyright (C) 2016 The MoonLake Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.minecraft.moonlake.compasstracker;

import com.minecraft.moonlake.compasstracker.commands.CommandCompassTracker;
import com.minecraft.moonlake.compasstracker.listeners.PlayerListener;
import com.minecraft.moonlake.compasstracker.manager.CompassTrackerManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class CompassTrackerPlugin extends JavaPlugin {

    private String prefix;
    private CompassTrackerManager compassTrackerManager;

    public CompassTrackerPlugin() {
    }

    @Override
    public void onEnable() {
        this.compassTrackerManager = new CompassTrackerManager(this);

        this.initFolder();
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getCommand("compasstracker").setExecutor(new CommandCompassTracker(this));
        this.getLogger().info("指南针跟踪器 CompassTracker 插件 v" + getDescription().getVersion() + " 成功加载.");
    }

    @Override
    public void onDisable() {
        getCompassTrackerManager().close();
    }

    private void initFolder() {
        if(!getDataFolder().exists())
            getDataFolder().mkdirs();
        File config = new File(getDataFolder(), "config.yml");
        if(!config.exists())
            saveDefaultConfig();
        reloadPrefix();
    }

    public void reloadPrefix() {
        this.prefix = toColor(getConfig().getString("Prefix", "&f[&a指南针跟踪器&f] "));
    }

    public String getMessage(String key, Object... args) {
        return toColor(prefix + String.format(getConfig().getString("Messages." + key, ""), args));
    }

    public String toColor(String src) {
        return ChatColor.translateAlternateColorCodes('&', src);
    }

    public CompassTrackerManager getCompassTrackerManager() {
        return compassTrackerManager;
    }
}

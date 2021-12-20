package com.cuddletheif.commandmanager;

import org.bukkit.plugin.java.JavaPlugin;


public class CommandManager extends JavaPlugin
{

    @Override
    public void onEnable() {
        getLogger().info(this.getDataFolder().getAbsolutePath());
    }

    @Override
    public void onDisable() {
        getLogger().info("See you again, SpigotMC!");
    }

}

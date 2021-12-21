package com.cuddletheif.commandmanager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

/**
 * Listens for commands for hidding commands and disabling commands
 */
public class CommandListener implements Listener{

    private FileConfiguration config;

    public CommandListener(FileConfiguration config){
        this.config = config;
    }
    
    public void reload(FileConfiguration config){
        this.config = config;
    }

}

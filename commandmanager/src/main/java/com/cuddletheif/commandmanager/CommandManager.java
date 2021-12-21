package com.cuddletheif.commandmanager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;


public class CommandManager extends JavaPlugin
{

    CommandListener commandListener;

    @Override
    public void onEnable() {
        // Load the config
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        // Ready the command listener
        commandListener = new CommandListener(config);
        this.getServer().getPluginManager().registerEvents(commandListener, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

        // Check if reload command
        if(args.length==1 && args[0]=="reload"){

            // Reload the config file from disk
            this.reloadConfig();
            this.saveDefaultConfig();
            FileConfiguration config = this.getConfig();

            // Update the command listener
            commandListener.reload(config);

            // Let user know reloaded config file and return success
            sender.sendMessage("Reloading Config file");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        var commands = new ArrayList<String>();
        if(args.length<1)
            commands.add("reload");
        return commands;
    }

}

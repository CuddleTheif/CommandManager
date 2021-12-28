package com.cuddletheif.commandmanager;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

/**
 * A custom command for command redirects
 */
public class CustomCommand extends BukkitCommand {

    public CustomCommand(String name, String description, String permission, List<String> aliases) {

        // Set given values in the command
        super(name);
        if(description!=null)
            this.description = description;
        this.usageMessage = "Please do not run these commands through the plugin name";
        if(permission!=null)
            this.setPermission(permission);
        if(aliases!=null)
            this.setAliases(aliases);

    }

    /**
     * This command should never get run so ignore it
     */
    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        return false;
    }

}
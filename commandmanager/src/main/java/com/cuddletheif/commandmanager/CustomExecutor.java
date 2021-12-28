package com.cuddletheif.commandmanager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CustomExecutor implements CommandExecutor{
    
    /**
     * This command should never get run so ignore it
     */
    @Override
    public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        return true;
    }
}

package com.cuddletheif.commandmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;

/**
 * Listens for commands for hidding, disabling, and rerouting commands
 */
public class CommandListener implements Listener{

    private CommandData[] commands;

    /**
     * Creates a command listener from the config given
     * @param config Config holding the command data
     */
    public CommandListener(FileConfiguration config){
        reload(config);
    }
    
    /**
     * Loads the config into the stored commands
     * @param config The config to load
     */
    public void reload(FileConfiguration config){

        // Get the commands
        ConfigurationSection cmdSection = config.getConfigurationSection("commands");
        if(cmdSection!=null){
            Set<String> cmdKeys = cmdSection.getKeys(false);
            commands = new CommandData[cmdKeys.size()];
            var i = 0;
            for(String cmdKey : cmdKeys)
                commands[i++] = new CommandData(cmdSection.getConfigurationSection(cmdKey), true);
        }

    }


    /**
     * When the server sends the player the list of commands remove any hidden
     * 
     * @param e the event triggered
     */
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandSend(PlayerCommandSendEvent e){

        // Check each command
        Collection<String> cmdNames = e.getCommands();
        for(CommandData cmd : commands){
            
            if(cmd.isHidden(e.getPlayer())){
                ArrayList<String> removes = new ArrayList<String>();
                for(String cmdName : cmdNames)
                    if(cmd.isFullCommand(cmdName))
                        removes.add(cmdName);
                for(String toRemove : removes)
                    cmdNames.remove(toRemove);
            }

        }

    }

     /**
     * When the server sends the player the list of commands to tab complete
     * 
     * @param e the event triggered
     */
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTabComplete(TabCompleteEvent e){

        // Get the command and new buffer
        String buffer = e.getBuffer();
        String command = buffer.split(" ")[0].substring(1).trim();
        if(command.length()+2<buffer.length())
            buffer = buffer.substring(command.length()+2);
        else
            buffer = "";

        // Get the tab lists if any 
        for(CommandData cmd : commands)
            if(cmd.isFullCommand(command))
                cmd.getTabList(buffer, e.getSender(), e.getCompletions());

    }

    /**
     * Before a player command gets processed check if it's disabled or needs to be redirected
     * 
     * @param e the event triggered
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreprocessComand(PlayerCommandPreprocessEvent e){
        
        if(this.processCommand((CommandSender)e.getPlayer(), e.getMessage().substring(1)))
            e.setCancelled(true);

    }

    /**
     * Before a server command gets processed check if it's disabled or needs to be redirected
     * 
     * @param e the event triggered
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreprocessComand(ServerCommandEvent e){
        
        if(this.processCommand(e.getSender(), e.getCommand()))
            e.setCancelled(true);

    }

    /**
     * Handles the processing of an event, disabling and calling new commands if any
     * 
     * @param boolean if the command is disabled
     */
    private boolean processCommand(CommandSender sender, String fullCommand){

        // Check to see if the command is handled speficially 
        CommandData cmdData = null;
        for(CommandData cmd : commands){
            cmdData = cmd.getCommandData(sender, fullCommand);
            if(cmdData!=null)
                break;
        }

        // Try to run the new commands if any
        if(cmdData!=null)
            cmdData.runNewCommands(sender);

        // Return if the default is disabled
        String command = fullCommand.split(" ")[0];
        String args = fullCommand.substring(command.length()).trim();
        for(CommandData cmd : commands)
            if(cmd.isFullCommand(command) && !cmd.canRunDefault(sender, args))
                return true;

        // If the command isn't handled just return false
        return false;

    }

}

package com.cuddletheif.commandmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.material.Torch;

/**
 * Holds the data for a command to hide, redirect, and/or disable
 */
public class CommandData implements Listener{

    // The names of the command
    private String[] names;

    // The permission of the user using the command to affect
    private String permission = "";

    // If the default action of the command should be disabled
    private boolean disabled;

    // If the command should be hidden from tab completion
    private boolean hidden;

    // If the sub commands of this command should also be disabled
    private boolean disableSub;

    // If the sub commands of this command be hidden from tab completion
    private boolean hideSub;

    // The command to run when this command is run
    private String newCommand;

    // The subcommands of this command to add to tab completion
    private CommandData[] subCommands;

    
    /**
     * Gets and stores the command data from a config section
     * 
     * @param section Section holding all the data of the command
     */
    public CommandData(ConfigurationSection section){
        
        // Get all the data for this command
        this.names = section.getString("name").split("[|]");
        if(section.contains("permission"))
            this.permission = section.getString("permission");
        System.out.print(this.permission);
        this.disabled = section.getBoolean("disabled");
        this.hidden = section.getBoolean("hidden");
        this.disableSub = section.getBoolean("disabled-sub");
        this.hideSub = section.getBoolean("hide-sub");
        this.newCommand = section.getString("new-command");
        
        // Get all the sub commands of this command
        if(section.contains("subcommands")){
            ConfigurationSection subSections = section.getConfigurationSection("subcommands");
            Set<String> sectKeys = subSections.getKeys(false);
            this.subCommands = new CommandData[sectKeys.size()];
            var  i = 0;
            for(String key : sectKeys)
                this.subCommands[i++] = new CommandData(subSections.getConfigurationSection(key));
        }
        else
            this.subCommands = new CommandData[0];

    }

    /**
     * Checks if the player running a command can run it (i.e. it's not disabled and a parent is not disabled)
     * 
     * @param command The full text of the command
     * @param sender The player who is trying to run the command
     * @return If the player can run the command
     */
    public boolean canRun(String command, Player sender){

        // Check if this command applies to the player
        if(this.permission==null || sender.hasPermission(this.permission)){
            
            String commandAfter = "";
            for(String name : this.names){
                var cmdI = command.indexOf(name);
                if(cmdI!=-1)
                    commandAfter = command.substring(cmdI+name.length());
            }
            
            // Check if it's this command or a sub command
            if(commandAfter.trim().length()==0)
                return !this.disabled;
            else{

                // If all sub commands are disabled return false otherwise check each sub command
                if(!this.disableSub && this.subCommands!=null)
                    for(CommandData subCmd : this.subCommands)
                        if(!subCmd.canRun(commandAfter, sender))
                            return false;

                // If the sub commands are not disabled then the command is good to go
                return true;
            }

        }
    
        // If this command doesn't apply to the player it's good to go
        return true;

    }

    /**
     * Checks if the tab list should be edited for the command
     * 
     * @param buffer The full text of the command so far
     * @param sender The player who is trying to run the command
     * @param completions The tab list before editing
     * @return The new tab list
     */
    public List<String> getTabList(String buffer, Player sender, List<String> completions){

        // Check if this command applies to the player
        if(this.permission==null || sender.hasPermission(this.permission)){

            sender.sendMessage("BUFFER:"+buffer+"SPACE:"+(buffer.indexOf(" ")==-1));
            // Check if it's this command or a sub command
            if(buffer.indexOf(" ")==-1){

                // Check if all sub commands should be hidden
                if(this.hideSub)
                    completions.clear();
                else{

                    // hide any subcommands that are hidden
                    for(CommandData subCmd : this.subCommands){

                        if(subCmd.isHidden(sender)){
                            ArrayList<String> removes = new ArrayList<String>();
                            for (String completion : completions)
                                if(subCmd.isCommand(completion))
                                    removes.add(completion);
                            for(String toRemove : removes)
                                completions.remove(toRemove);
                        }

                    }
                    
                }

                // Add any unhidden sub commands
                for(CommandData subCmd : this.subCommands)
                    if(!subCmd.isHidden(sender) && completions.indexOf(subCmd.getName())==-1) 
                        completions.add(subCmd.getName());

            }
            else{

                // Get the name of the sub command and the new buffer
                var command = buffer.substring(0, buffer.indexOf(" ")).trim();
                if(command.length()+1<buffer.length())
                    buffer = buffer.substring(command.length()+1);
                else
                    buffer = "";

                sender.sendMessage(":CMD:"+command+":NEW:"+buffer);

                // Check if it is a sub command and return their tab list instead
                for(CommandData subCmd : this.subCommands)
                    if(subCmd.isFullCommand(command))
                        return subCmd.getTabList(buffer, sender, completions);
            }

        }
    
        // Return the completions
        return completions;

    }

    /**
     * Checks if the command is the buffer of command given
     * 
     * @param partialCmd The text of the command so far
     * @return If it is this command
     */
    public boolean isCommand(String partialCmd){
        for(String name : this.names)
            if(name.startsWith(partialCmd))
                return true;
        return false;
    }
    
    /**
     * Checks if the command is the full name of the command given
     * 
     * @param partialCmd The text name of the command
     * @return If it is this command 
     */
    public boolean isFullCommand(String cmd){
        for(String name : this.names)
            if(name.equals(cmd))
                return true;
        return false;
    }

    /**
     * Gets the basic name of the command
     * 
     * @return the basic name of the command
     */
    public String getName(){
        return this.names[0];
    }

    /**
     * Checks if this command should be hidden from the Tab menu
     * 
     * @param sender The player trying to run the command
     * @return If it is command should be hidden
     */
    public boolean isHidden(Player sender){
        return this.hidden && (this.permission==null || sender.hasPermission(this.permission));
    }

}

package com.cuddletheif.commandmanager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Holds the data for a command to hide, redirect, and/or disable
 */
public class CommandData implements Listener{

    // The names of the command
    private String[] names;

    // The permission of the user using the command to affect
    private String permission = null;

    // If the default action of the command should be disabled
    private boolean disabled;

    // If the default action of the command should be enabled (This takes priority over disabled)
    private boolean enabled;

    // If the command should be hidden from tab completion
    private boolean hidden;

    // If the command should not be hidden from tab completion (This takes priority over hidden)
    private boolean unhidden;

    // If the sub commands of this command should also be disabled
    private boolean disableSub;

    // If the sub commands of this command should also be enabled
    private boolean enableSub;

    // If the sub commands of this command be hidden from tab completion
    private boolean hideSub;

    // If the sub commands of this command not be hidden from tab completion (This takes priority over hideSub)
    private boolean unhideSub;

    // If the new commands should be run as the server
    private boolean server;

    // The command to run when this command is run
    private String[] newCommands;

    // The subcommands of this command to add to tab completion
    private CommandData[] subCommands;

    // Description of the command used when registering it
    private String description;
    
    /**
     * Gets and stores the command data from a config section
     * 
     * @param section Section holding all the data of the command
     * @param topCommand If this command is a top command and should be registered
     */
    public CommandData(ConfigurationSection section, boolean topCommand){
        
        // Get all the data for this command
        this.names = section.getString("name").split("[|]");
        if(section.contains("permission"))
            this.permission = section.getString("permission");
        System.out.print(this.permission);
        this.disabled = section.getBoolean("disabled");
        this.enabled = section.getBoolean("enabled");
        this.hidden = section.getBoolean("hidden");
        this.unhidden = section.getBoolean("unhidden");
        this.disableSub = section.getBoolean("disabled-sub");
        this.enableSub = section.getBoolean("enabled-sub");
        this.hideSub = section.getBoolean("hide-sub");
        this.unhideSub = section.getBoolean("unhide-sub");
        this.server = section.getBoolean("server");
        this.description = section.getString("description");
        if(section.contains("new-commands")){
            if(topCommand && this.disabled)
                this.registerCommand();
            this.newCommands = section.getStringList("new-commands").toArray(new String[0]);
        }
        else
            this.newCommands = new String[0];
        
        // Get all the sub commands of this command
        if(section.contains("subcommands")){
            ConfigurationSection subSections = section.getConfigurationSection("subcommands");
            Set<String> sectKeys = subSections.getKeys(false);
            this.subCommands = new CommandData[sectKeys.size()];
            var  i = 0;
            for(String key : sectKeys)
                this.subCommands[i++] = new CommandData(subSections.getConfigurationSection(key), this);
            
        }
        else
            this.subCommands = new CommandData[0];

    }

    /**
     * Registers the command to bukkit for the tab list
     */
    private void registerCommand(){

        // Check if the command already exists and if it does just override it
        PluginCommand command = Bukkit.getPluginCommand(this.getName());
        if(command!=null){
            if(this.description!=null)
                command.setDescription(this.description);
            if(this.permission!=null)
                command.setPermission(this.permission);
            if(this.names.length>1){
                ArrayList<String> aliases = new ArrayList<>();
                for(int i=1;i<names.length;i++)
                    aliases.add(names[i]);
                command.setAliases(aliases);
            }
            else
                command.setAliases(new ArrayList<>());
            command.setExecutor(new CustomExecutor());
        }
        // If the command does not exist just try to add it
        else{

            try{   
                
                Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

                bukkitCommandMap.setAccessible(true);
                CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

                ArrayList<String> aliases = new ArrayList<>();
                for(int i=1;i<names.length;i++)
                    aliases.add(names[i]);
                commandMap.register("seen", new CustomCommand(this.getName(), this.description, this.permission, aliases));
                
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {}
            
        }
    }

     /**
     * Gets and stores the command data from a config section and can override the data based on the parent
     * 
     * @param section Section holding all the data of the command
     * @param parent The parent command data for copying hide sub and disable sub, etc.
     */
    private CommandData(ConfigurationSection section, CommandData parent){
        
        // Call the normal constructor
        this(section, false);

        // Override the given values
        if(parent.hideSub && !section.contains("hidden"))
            this.hidden = true;
        if(parent.unhideSub && !section.contains("unhidden"))
            this.unhidden = true;
        if(parent.disableSub && !section.contains("disabled"))
            this.disabled = true; 
        if(parent.enableSub && !section.contains("enabled"))
            this.enabled = true; 
        if(parent.permission!=null && !section.contains("permission"))
            this.permission = parent.permission; 

    }

    /**
     * Gets the command data of the given command if it's this one or a sub command of this one
     * 
     * @param sender The sender trying to run the command
     * @param fullCommand The full text of the command
     * @return The command data of the command (If it's this one or a sub command) null if not found
     */
    public CommandData getCommandData(CommandSender sender, String command){

        // Split the command into the name and args
        String[] cmdParts = command.split(" ");

        // Check if it's even this command and the sender has perms
        if(this.isFullCommand(cmdParts[0]) && this.hasPermission(sender)){

            // Check if it's this command or a sub command
            if(cmdParts.length==1)
                return this;
            else{

                // Check each sub command to see if one matches and returns it
                String commandAfter = command.substring(cmdParts[0].length()).trim();
                if(this.subCommands!=null){
                    for(CommandData subCmd : this.subCommands){

                        CommandData subData = subCmd.getCommandData(sender, commandAfter);
                        if(subData!=null)
                            return subData;

                    }
                }

            }
        }

        // If reached here no command data was found so return null
        return null;

    }

    /**
     * Checks if the tab list should be edited for the command
     * 
     * @param buffer The full text of the command so far
     * @param sender The sender who is trying to run the command
     * @param completions The tab list before editing
     * @return The new tab list
     */
    public List<String> getTabList(String buffer, CommandSender sender, List<String> completions){

        // Check if this command applies to the player
        if(this.hasPermission(sender)){

            // Check if it's this command or a sub command
            if(buffer.indexOf(" ")==-1){

                // Check if all sub commands should be hidden
                if(!this.unhideSub && this.hideSub)
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

                // Add any unhidden sub commands that apply
                for(CommandData subCmd : this.subCommands)
                    if(!subCmd.isHidden(sender) && subCmd.isCommand(buffer) && completions.indexOf(subCmd.getName())==-1) 
                        completions.add(subCmd.getName());

            }
            else{

                // Get the name of the sub command and the new buffer
                var command = buffer.substring(0, buffer.indexOf(" ")).trim();
                if(command.length()+1<buffer.length())
                    buffer = buffer.substring(command.length()+1);
                else
                    buffer = "";

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
     * @param sender The sender trying to run the command
     * @return If it is command should be hidden
     */
    public boolean isHidden(CommandSender sender){
        return !this.unhidden && this.hidden && this.hasPermission(sender);
    }

    /**
     * Runs the new commands of this command by the given sender (If they have the permission)
     * 
     * @param sender The sender of the command to run the new commands
     */
    public void runNewCommands(CommandSender sender){

        // Make sure the sender can run the command
        if(this.hasPermission(sender))
            for(String command : newCommands){
                
                // Replace any placeholders
                String finalCommand = command.replaceAll("%sender%", sender.getName());

                // Run the command (as the sender or server)
                Bukkit.getServer().dispatchCommand((this.server ? Bukkit.getConsoleSender() : sender), finalCommand);
            }

    }

    
    /**
     * Checks if the given sender has permissions for this command
     * 
     * @param sender The sender of this command
     * @return if the player has permission for this command
     */
    public boolean hasPermission(CommandSender sender){
        return !(sender instanceof Player) || this.permission==null || sender.hasPermission(this.permission);
    }

    /**
     * Checks if the given sender can run the given command's default version (If it is not this command or a sub command will return true)
     * 
     * @param sender The sender trying to run the command
     * @param args The full text of the arguments of the command
     * @return If the command's default version can be run
     */
    public boolean canRunDefault(CommandSender sender, String args){

        // Check if it's this command or a sub command
        if(args=="")
            return this.hasPermission(sender) && (this.enabled || !this.disabled);
        else{

            // Check each sub command to see if one matches and check that one if so instead
            String subCommand = args.split(" ")[0].trim();
            String subArgs = args.substring(subCommand.length()).trim();
            if(this.subCommands!=null)
                for(CommandData subCmd : this.subCommands)
                    if(subCmd.isFullCommand(subCommand) && !subCmd.canRunDefault(sender, subArgs))
                        return false;
            
            // If none of the sub commands are set just return the disable sub setting
            return this.hasPermission(sender) && (this.enableSub || !this.disableSub);

        }

    }
}

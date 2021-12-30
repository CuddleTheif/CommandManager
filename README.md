# CommandManager
A Spigot plugin for managing commands on a minecraft spigot server

Hide/Disabled commands from players!
Add new commands that run other commands!

#Config

Look at the default config.yml for an example.
Each command can have the following fields.

name (Required)
  The actual command used (Place a | between names for different alias)

permission
  The permission that these command changes apply to. If a player has this permission than everything in these command settings will affect them

disabled
  If this command's default functionality is disabled (NOTE: This does NOT hide the command)

disable-sub
  If all sub commands (meaning commands that start with this command and have other arguments) are disabled

enabled
  If this command's default functionality should be enabled (This is only needed if you want to override the disabled setting on a lower permission)

enable-sub
  If all sub commands of this command are enabled

hidden
  If this command should be hidden from the player's tab list

hide-sub
  If all sub commands of this command should be hidden from the player's tab list
  
unhidden
  If this command should not be hidden from the player's tab list (This is only needed if you want to override the hidden setting on a lower permission)

unhide-sub
  If all commands of this command should not be hidden from the player's tab list
  
new-commands
  A list of the new commands to run when this command is run
  These commands are by default run by the sender running the command (see 'server' to change that)
  Use %sender% to put the sender's name in the command

server
  If the new commands run should be run as the server instead of as the sender of the command
  
description
  Description of the command to use for /help (Only used if new commands are set)
  
subcommands
  All the sub commands of this command to have settings for. each of these follows the same format as a command and can have their own sub commands

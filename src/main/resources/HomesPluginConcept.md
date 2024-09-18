# Homes Plugin

## Concepts

### Groups:

Multiple worlds can be organized into groups in the config. Worlds that aren't assigned to a specific group are automatically placed in a 'global' group. These groups are used to manage homes separately on the server. Additionally, each group can have different maximum home limits, which can be set through permissions.

A group defines the following:

- Worlds included in the group
- Experience required to use /sethome
- Experience required to use /home
- Teleportation delay
- Damage sources that prevent teleportation
- Time period after which damage taken is ignored
- Requirement for standing on solid ground for teleportation
- MaxHomes (Can be overridden using the permission `<plname>.maxhomes.<groupname>.<amount>`)

## Commands

### /sethome (player) \<name>

Sets a home at your current location with the specified name. To set a home, you need at least x experience levels, as defined in the world group settings. No experience is required if you have the bypass flag or are not in survival mode. If you have the sethome.other flag, you can set a home for another player. In this case, no experience is required, but it will count as if the player for whom you're setting the home has paid the required experience.

Experience cost is calculated based on a formula defined in the config. You can also configure whether experience is charged for each home set or only for new, non-deleted homes.
The success/fail messages will be sent to the players chat.
There will be a worldguard flag to decide whether you can place homes in a region. This will be default true

### /delhome (player) \<name> (--group \<group>)

Deletes the home with the specified name. With the `delhome.other.<groupnames>` permission, you can also remove homes belonging to other players.
If a group flag is specified, and you have the valid other permission, you can delete homes in other groups as well.
The success/fail messages will be sent to the players chat.

### /homes (player) (--group \<group>)

Displays your homes in the current group. If you have the `homes.other` permission, you can also view homes of other players.
Specifying a group flag allows you to view homes of players in that specific group, if you have the valid .other. permission.

The success/fail messages will be sent to the players chat.
The list shown in the chat will be clickable, and selecting a home will automatically use the /home command to teleport you there.

List is sorted after

### /home (player) \<name> \(--group \<group>)

Teleports you to the home with the specified name. This requires the amount of experience defined by the group settings. No experience is needed if you're not in survival mode or if you have the `home.bypass` flag.

With the `home.other` permission, you can teleport to other players' homes without needing experience. Additionally, if you specify a group and have the valid other permission, you can teleport to homes in that specific group.

In the config, you can define a delay for teleportation. You can also specify damage sources that prevent teleportation and set the time needed before recent damage is ignored. Additionally, you can require players to be standing on solid ground for teleportation to be allowed.
These checks are ignored if you have the `home.bypass` permission, or you are teleporting to another players homes.

Attempting to teleport to a home in a world that does not exist, will prompt the used with the option to remove this home.

Success (You have been teleported) messages will be displayed in the Action bar.
Fail Messages will be displayed in the chat.

If the location of the home is obstructed the user should be prompted if he wants to continue the teleportation.

### /renamehome (player) \<old_name> \<new_name> \(--group \<group>)

Rename the home with the old_name to the new_name. You can configure if this action uses experience.
Experience requirements will be bypassed using the `renamehome.bypass` permission, or you are not in survival mode.

### /homesearch \<radius>

Lists all homes in the specified radius. (If I'm cool also display the blocks as invisible slimes for the spawned player)

### /homeadmin (purge/reload/cleanup/remove/export/import)

Admin commands:

- Purge:
    - Clears all homes on the server
    - Prompts the user again to confirm
- Reload:
    - Reloads the configs/groups from the disk
- Cleanup:
    - Deletes all homes in worlds that don't exist anymore
- Remove (world):
    - Removes all Homes in the specified world if no world with the specified name is found a group with that name is searched.
- Export:
    - Exports the Database to a yaml file for easy debugging
- Import:
    - Imports a yaml file to the database

## Migration

Old homes from the [BasicHomes](https://github.com/Ulfu/BasicHomes) plugin will be migrated.

## Permissions:

Prefix: <plname>.

- MaxHomes Per Group: maxhomes.<group>.<amount>
- Set Home for a player in other groups (Player p1 in group a creates home for player p2 in group b): sethome.other.<groupname> (Spieler kann wenn er in der Gruppe skyblock ist ein Home für einen anderen Spieler erstellen wenn er die Permission sethome.other.skyblock)
- Deletion from a player in other groups (Player p in group a deletes home in group b): delhome.other.<groupname>
- Viewing Homes from a player in other groups (Player p in group a views homes in group b): listhomes.other.<groupname>
- Teleporting to Homes from a player in another groups (Player p in group a teleports to home in group b): home.other.<groupname>
- Experience Bypass Permissions: sethome.bypass; tphome.bypass; renamehome.bypass;


- sethome.other.* ALLOW
- sethome.other.skyblock DENY

- maxhomes.skyblock.10
- maxhomes.survival.5
- delhome
- listhome
- home
- sethome
- renamehome

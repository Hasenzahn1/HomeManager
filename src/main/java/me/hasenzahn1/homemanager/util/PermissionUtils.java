package me.hasenzahn1.homemanager.util;

import me.hasenzahn1.homemanager.HomeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that provides permission-related methods for the HomeManager plugin.
 */
public class PermissionUtils {

    /**
     * Retrieves the maximum number of homes allowed based on the permissions assigned to a sender.
     * <p>
     * The permission structure is as follows:
     * - homemanager.maxhomes.*.(amount)
     * - homemanager.maxhomes.global.*
     *
     * @param sender the permissible entity (usually a player or command sender)
     * @param group  the group for which the max homes permission is checked
     * @return the maximum number of homes allowed for the sender, or -1 if no valid permission is found
     */
    public static int getMaxHomesFromPermission(Permissible sender, String group) {
        if (sender == null) return -1;
        int maxHomes = -1;
        for (PermissionAttachmentInfo info : sender.getEffectivePermissions()) {
            // Checks for the wildcard permission (amount)
            if (info.getPermission().startsWith("homemanager.maxhomes.*.")) {
                String amount = info.getPermission().replace("homemanager.maxhomes.*.", "");
                if (amount.equalsIgnoreCase("*")) return Integer.MAX_VALUE;
                if (!isInt(amount)) continue;
                maxHomes = Math.max(maxHomes, Integer.parseInt(amount));
            }

            // Checks for global group-specific max homes permission
            if (info.getPermission().equalsIgnoreCase("homemanager.maxhomes." + group + ".*")) {
                return Integer.MAX_VALUE;
            }

            // Checks for group-specific max homes permission
            if (info.getPermission().startsWith("homemanager.maxhomes." + group)) {
                maxHomes = Math.max(maxHomes, Integer.parseInt(info.getPermission().replaceAll("homemanager.maxhomes." + group + ".", "")));
            }
        }
        return maxHomes;
    }


    /**
     * Retrieves the list of groups from the "other" permission based on a permission stub.
     * <p>
     * The permission structure is as follows:
     * - homemanager.delhome.other.*
     * - homemanager.delhome.other.survival
     *
     * @param sender         the command sender (usually a player or console)
     * @param permissionStub the permission stub to check for the "other" permissions
     * @return a list of groups that the sender has permission for
     */
    public static List<String> getGroupsFromOtherPermission(CommandSender sender, String permissionStub) {
        if (sender == null) return List.of();
        List<String> groups = new ArrayList<>();

        // Iterate over world group names and check if the sender has the specific permission
        for (String group : HomeManager.getInstance().getWorldGroupManager().getWorldGroupNames()) {
            if (sender.hasPermission(permissionStub + "." + group))
                groups.add(group);
        }

        return groups; //TODO: Consider returning the default group max homes
    }

    /**
     * Checks if the given input string can be parsed as an integer.
     *
     * @param input the string to check
     * @return true if the string can be parsed as an integer, false otherwise
     */
    public static boolean isInt(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

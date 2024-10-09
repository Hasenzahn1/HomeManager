package me.hasenzahn1.homemanager.util;

import me.hasenzahn1.homemanager.HomeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    /**
     * Bsp. homemanager.maxhomes.*.(amount)<p>
     * homemanager.maxhomes.world.*
     * homemanager
     *
     * @param sender
     * @param group
     * @return
     */
    public static int getMaxHomesFromPermission(CommandSender sender, String group) {
        if (sender == null) return 0;
        int maxhomes = 0;
        for (PermissionAttachmentInfo info : sender.getEffectivePermissions()) {
            if (info.getPermission().startsWith("homemanager.maxhomes.*.")) {
                String amount = info.getPermission().replace("homemanager.maxhomes.*.", "");
                if (amount.equalsIgnoreCase("*")) return Integer.MAX_VALUE;
                if (!isInt(amount)) continue;
                maxhomes = Math.max(maxhomes, Integer.parseInt(amount));
            }

            if (info.getPermission().equalsIgnoreCase("homemanager.maxhomes." + group + ".*")) {
                return Integer.MAX_VALUE;
            }

            if (info.getPermission().startsWith("homemanager.maxhomes." + group)) {
                maxhomes = Math.max(maxhomes, Integer.parseInt(info.getPermission().replaceAll("homemanager.maxhomes." + group + ".", "")));
            }
        }
        return maxhomes; //TODO return default group max homes
    }


    /**
     * Bsp. homemanager.delhome.other.*<p>
     * homemanager.delhome.other.survival
     *
     * @param sender
     * @param permissionStub
     * @return
     */
    public static List<String> getGroupsFromOtherPermission(CommandSender sender, String permissionStub) {
        if (sender == null) return List.of();
        List<String> groups = new ArrayList<>();
        for (PermissionAttachmentInfo info : sender.getEffectivePermissions()) {
            if (info.getPermission().equalsIgnoreCase(permissionStub + ".*")) {
                return HomeManager.getInstance().getWorldGroupManager().getWorldGroupNames();
            }

            if (info.getPermission().startsWith(permissionStub + ".")) {
                String groupName = info.getPermission().replace(permissionStub + ".", "");
                if (HomeManager.getInstance().getWorldGroupManager().groupExists(groupName))
                    groups.add(groupName);
            }
        }
        return groups; //TODO return default group max homes
    }

    public static boolean isInt(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}

package me.hasenzahn1.homemanager.util;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class PermissionUtils {

    public static int getMaxHomesFromPermission(CommandSender sender, String group) {
        System.out.println("Sender: " + sender + ", group: " + group);
        if (sender == null) return 0;
        int maxhomes = 0;
        for (PermissionAttachmentInfo info : sender.getEffectivePermissions()) {
            if (info.getPermission().startsWith("homemanager.maxhomes.*.")) {
                System.out.println(info.getPermission());
                maxhomes = Math.max(maxhomes, Integer.parseInt(info.getPermission().replace("homemanager.maxhomes.*.", "")));
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

}

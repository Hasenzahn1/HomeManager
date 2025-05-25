package me.hasenzahn1.homemanager.commands.checks;

import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;
import me.hasenzahn1.homemanager.homes.Home;
import me.hasenzahn1.homemanager.permission.PermissionValidator;

public abstract class HomeExperienceCheck {

    public abstract int getRequiredExperience(PlayerNameArguments arguments, int currentHomes, Home requestedHome);

    public boolean checkForInvalidExperience(PlayerNameArguments arguments, int currentHomes, Home requestedHome) {
        int requiredLevels = getRequiredExperience(arguments, currentHomes, requestedHome);
        boolean hasToPayExperience = hasToPayExperience(arguments);

        return hasToPayExperience && arguments.getCmdSender().getLevel() < requiredLevels;
    }

    public boolean hasToPayExperience(PlayerNameArguments arguments) {
        return arguments.isSelf() && !arguments.getCmdSender().getGameMode().isInvulnerable() && !PermissionValidator.hasBypassPermission(arguments.getCmdSender(), arguments.getWorldGroup());
    }

}

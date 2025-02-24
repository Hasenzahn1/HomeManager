package me.hasenzahn1.homemanager.commands.checks;

import me.hasenzahn1.homemanager.commands.args.PlayerNameArguments;

public abstract class HomeExperienceCheck {

    public abstract int getRequiredExperience(PlayerNameArguments arguments, int currentHomes);

    public boolean checkForInvalidExperience(PlayerNameArguments arguments, int currentHomes) {
        int requiredLevels = getRequiredExperience(arguments, currentHomes);
        boolean hasToPayExperience = hasToPayExperience(arguments);

        return hasToPayExperience && arguments.getCmdSender().getLevel() < requiredLevels;
    }

    public boolean hasToPayExperience(PlayerNameArguments arguments) {
        return arguments.isSelf() && !arguments.getCmdSender().getGameMode().isInvulnerable();
    }

}

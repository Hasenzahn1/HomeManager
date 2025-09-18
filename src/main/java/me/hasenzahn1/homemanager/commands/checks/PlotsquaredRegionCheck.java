package me.hasenzahn1.homemanager.commands.checks;


import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import me.hasenzahn1.homemanager.integration.PlotsquaredIntegration;
import org.bukkit.entity.Player;

/**
 * Checks whether a player is allowed to use home-related features within a PlotSquared plot,
 * based on a custom boolean flag and plot membership.
 */
public class PlotsquaredRegionCheck {

    private final BooleanFlag<?> flag;

    public PlotsquaredRegionCheck(BooleanFlag<?> flag) {
        this.flag = flag;
    }

    /**
     * Determines whether the given player is allowed to use home-related features at their current location.
     * <p>
     * Evaluation steps:
     * <ul>
     *     <li>If PlotSquared is not available, permission is granted.</li>
     *     <li>If the player is not in a plot or the area is null, permission is granted.</li>
     *     <li>If the plot has the specified flag set to {@code true}, permission is granted.</li>
     *     <li>If the flag is {@code false}, permission is granted only if the player is explicitly added to the plot.</li>
     * </ul>
     *
     * @param player the player to evaluate
     * @return {@code true} if the player is allowed to use homes in their current plot, otherwise {@code false}
     */
    public boolean canUseHomes(Player player) {
        if (PlotsquaredIntegration.PLOT_API_INSTANCE == null) return true;

        Location location = Location.at(
                player.getLocation().getWorld().getName(),
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ()
        );

        //Check plotsquared bypass permission
        if (player.hasPermission("plots.admin")) return true;

        PlotArea area = PlotsquaredIntegration.PLOT_API_INSTANCE.getPlotSquared()
                .getPlotAreaManager().getPlotArea(location);
        if (area == null) return true;

        Plot plot = area.getPlot(location);
        if (plot == null) return true;

        boolean allowed = plot.getFlag(flag);
        if (allowed) return true;

        return plot.isAdded(player.getUniqueId());
    }
}

package me.hasenzahn1.homemanager.commands.checks;


import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import me.hasenzahn1.homemanager.integration.PlotsquaredIntegration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlotsquaredRegionCheck {

    private BooleanFlag<?> flag;

    public PlotsquaredRegionCheck(BooleanFlag<?> flag) {
        this.flag = flag;
    }

    public boolean canUseHomes(Player player) {
        if (PlotsquaredIntegration.PLOT_API_INSTANCE == null) return true;

        Location location = Location.at(player.getLocation().getWorld().getName(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());

        PlotArea area = PlotsquaredIntegration.PLOT_API_INSTANCE.getPlotSquared().getPlotAreaManager().getPlotArea(location);
        Bukkit.broadcastMessage("Area: " + area);
        if (area == null) return true;

        Plot plot = area.getPlot(location);
        Bukkit.broadcastMessage("Plot: " + plot);
        if (plot == null) return true;

        boolean allowed = (Boolean) plot.getFlag(flag);
        Bukkit.broadcastMessage("allowed: " + allowed);
        if (allowed) return true;

        return plot.isAdded(player.getUniqueId());
    }/**/

}

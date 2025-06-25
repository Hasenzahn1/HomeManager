package me.hasenzahn1.homemanager.integration;

import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.integration.pflags.CreateHomesFlag;
import me.hasenzahn1.homemanager.integration.pflags.TeleportHomesFlag;

/**
 * Integration class for PlotSquared plugin to add custom flags related to home functionality.
 * This class is only loaded when the PlotSquared plugin is loaded on the server
 * <p>
 * This class allows registering flags such as {@code CREATE_HOMES} and {@code TELEPORT_HOMES}
 * that control whether players can set or teleport to homes on specific plots.
 */
public class PlotsquaredIntegration {

    public static BooleanFlag<?> CREATE_HOMES;
    public static BooleanFlag<?> TELEPORT_HOMES;


    private final HomeManager plugin;
    public static PlotAPI PLOT_API_INSTANCE;


    public PlotsquaredIntegration(HomeManager plugin) {
        this.plugin = plugin;
        PLOT_API_INSTANCE = new PlotAPI();
    }

    /**
     * Registers the custom home-related flags with the PlotSquared {@link GlobalFlagContainer}.
     * <p>
     * After registration, the flags {@link #CREATE_HOMES} and {@link #TELEPORT_HOMES}
     * can be used in PlotSquared plot settings.
     */
    public void register() {
        GlobalFlagContainer.getInstance().addFlag(CreateHomesFlag.FLAG_INSTANCE);
        GlobalFlagContainer.getInstance().addFlag(TeleportHomesFlag.FLAG_INSTANCE);

        CREATE_HOMES = CreateHomesFlag.FLAG_INSTANCE;
        TELEPORT_HOMES = TeleportHomesFlag.FLAG_INSTANCE;
    }
}

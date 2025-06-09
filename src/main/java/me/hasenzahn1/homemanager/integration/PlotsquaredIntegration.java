package me.hasenzahn1.homemanager.integration;

import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import me.hasenzahn1.homemanager.HomeManager;
import me.hasenzahn1.homemanager.integration.pflags.CreateHomesPFlag;
import me.hasenzahn1.homemanager.integration.pflags.TeleportHomesPFlag;

public class PlotsquaredIntegration {


    public static BooleanFlag<?> CREATE_HOMES;
    public static BooleanFlag<?> TELEPORT_HOMES;

    private final HomeManager plugin;

    public static PlotAPI PLOT_API_INSTANCE;

    public PlotsquaredIntegration(HomeManager plugin) {
        System.out.println("Register PlotsquaredIntegration");
        this.plugin = plugin;
        PLOT_API_INSTANCE = new PlotAPI();

    }

    public void register() {
        GlobalFlagContainer.getInstance().addFlag(CreateHomesPFlag.FLAG_INSTANCE);
        GlobalFlagContainer.getInstance().addFlag(TeleportHomesPFlag.FLAG_INSTANCE);

        CREATE_HOMES = CreateHomesPFlag.FLAG_INSTANCE;
        TELEPORT_HOMES = TeleportHomesPFlag.FLAG_INSTANCE;
    }

}

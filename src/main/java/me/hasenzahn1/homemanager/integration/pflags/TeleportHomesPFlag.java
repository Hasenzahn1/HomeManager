package me.hasenzahn1.homemanager.integration.pflags;

import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import me.hasenzahn1.homemanager.Language;
import org.checkerframework.checker.nullness.qual.NonNull;

public class TeleportHomesPFlag extends BooleanFlag<TeleportHomesPFlag> {

    public static TeleportHomesPFlag FLAG_INSTANCE;

    private static final TeleportHomesPFlag TELEPORT_HOMES_P_FLAG_TRUE = new TeleportHomesPFlag(true);
    private static final TeleportHomesPFlag TELEPORT_HOMES_P_FLAG_FALSE = new TeleportHomesPFlag(false);

    protected TeleportHomesPFlag(boolean value) {
        super(value, StaticCaption.of(Language.getLang(Language.PLOTSQUARED_TELEPORT_HOMES_CAPTION)));

        FLAG_INSTANCE = TELEPORT_HOMES_P_FLAG_TRUE;
    }

    @Override
    protected TeleportHomesPFlag flagOf(@NonNull Boolean value) {
        return value ? TELEPORT_HOMES_P_FLAG_TRUE : TELEPORT_HOMES_P_FLAG_FALSE;
    }
}

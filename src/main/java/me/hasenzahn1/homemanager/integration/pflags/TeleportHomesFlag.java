package me.hasenzahn1.homemanager.integration.pflags;

import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import me.hasenzahn1.homemanager.Language;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Standard plotsquared boolean flag definition.
 * Flags will be checked in the command classes.
 */
public class TeleportHomesFlag extends BooleanFlag<TeleportHomesFlag> {

    public static TeleportHomesFlag FLAG_INSTANCE;

    private static final TeleportHomesFlag TELEPORT_HOMES_P_FLAG_TRUE = new TeleportHomesFlag(true);
    private static final TeleportHomesFlag TELEPORT_HOMES_P_FLAG_FALSE = new TeleportHomesFlag(false);

    protected TeleportHomesFlag(boolean value) {
        super(value, StaticCaption.of(Language.getLang(Language.PLOTSQUARED_TELEPORT_HOMES_CAPTION)));

        FLAG_INSTANCE = TELEPORT_HOMES_P_FLAG_TRUE;
    }

    @Override
    protected TeleportHomesFlag flagOf(@NonNull Boolean value) {
        return value ? TELEPORT_HOMES_P_FLAG_TRUE : TELEPORT_HOMES_P_FLAG_FALSE;
    }
}

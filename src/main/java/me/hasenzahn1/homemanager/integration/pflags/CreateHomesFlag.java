package me.hasenzahn1.homemanager.integration.pflags;

import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import me.hasenzahn1.homemanager.Language;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Standard plotsquared boolean flag definition.
 * Flags will be checked in the command classes.
 */
public class CreateHomesFlag extends BooleanFlag<CreateHomesFlag> {

    public static CreateHomesFlag FLAG_INSTANCE;

    private static final CreateHomesFlag CREATE_HOMES_FLAG_TRUE = new CreateHomesFlag(true);
    private static final CreateHomesFlag CREATE_HOMES_FLAG_FALSE = new CreateHomesFlag(false);

    protected CreateHomesFlag(boolean value) {
        super(value, StaticCaption.of(Language.getLang(Language.PLOTSQUARED_CREATION_HOMES_CAPTION)));

        FLAG_INSTANCE = CREATE_HOMES_FLAG_TRUE;
    }

    @Override
    protected CreateHomesFlag flagOf(@NonNull Boolean value) {
        return value ? CREATE_HOMES_FLAG_TRUE : CREATE_HOMES_FLAG_FALSE;
    }
}
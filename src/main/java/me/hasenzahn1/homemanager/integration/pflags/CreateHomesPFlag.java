package me.hasenzahn1.homemanager.integration.pflags;

import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import me.hasenzahn1.homemanager.Language;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CreateHomesPFlag extends BooleanFlag<CreateHomesPFlag> {

    public static CreateHomesPFlag FLAG_INSTANCE;

    private static final CreateHomesPFlag CREATE_HOMES_P_FLAG_TRUE = new CreateHomesPFlag(true);
    private static final CreateHomesPFlag CREATE_HOMES_P_FLAG_FALSE = new CreateHomesPFlag(false);


    protected CreateHomesPFlag(boolean value) {
        super(value, StaticCaption.of(Language.getLang(Language.PLOTSQUARED_CREATION_HOMES_CAPTION)));

        FLAG_INSTANCE = CREATE_HOMES_P_FLAG_TRUE;
    }

    @Override
    protected CreateHomesPFlag flagOf(@NonNull Boolean value) {
        return value ? CREATE_HOMES_P_FLAG_TRUE : CREATE_HOMES_P_FLAG_FALSE;
    }
}

package me.hasenzahn1.homemanager.homes;

import me.hasenzahn1.homemanager.HomeManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/**
 * Represents a single home entry displayed as part of the {@code /homesearch} command.
 */
public class HomeDisplay_entity extends HomeDisplay {

    public static NamespacedKey DISPLAY_KEY = new NamespacedKey(HomeManager.getInstance(), "home_display");

    private Shulker glowingMarker;
    private TextDisplay nameDisplay;

    public HomeDisplay_entity(Player initiator, Home home) {
        super(initiator, home);
    }

    @Override
    public void display() {
        if (!home.location().getChunk().isLoaded()) return;

        glowingMarker = home.location().getWorld().spawn(new Location(home.location().getWorld(), home.location().getBlockX() + 0.5, home.location().getBlockY(), home.location().getBlockZ() + 0.5), Shulker.class, s -> {
            s.setAI(false);
            s.setGlowing(true);
            s.setGravity(false);
            s.setInvulnerable(true);
            s.setInvisible(true);
            s.setRotation(0, 0);
            s.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(1024);
            s.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(1024);
            s.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(1024);
            s.setHealth(1024);
            s.addPotionEffect(PotionEffectType.REGENERATION.createEffect(255 * 20, 255 * 20).withAmbient(false).withParticles(false));
            s.getPersistentDataContainer().set(DISPLAY_KEY, PersistentDataType.BYTE, (byte) 1);

            s.setVisibleByDefault(false);
            initiator.showEntity(HomeManager.getInstance(), s);
        });

        nameDisplay = home.location().getWorld().spawn(new Location(home.location().getWorld(), home.location().getBlockX() + 0.5, home.location().getBlockY() + 1.5, home.location().getBlockZ() + 0.5), TextDisplay.class, t -> {
            t.setSeeThrough(true);
            t.text(Component.text(home.name() + "\n" + home.getOwnersName())); // .color(TextColor.color(getColorFromUUID(home.uuid())))
            t.setTextOpacity((byte) 255);
            t.setBillboard(Display.Billboard.VERTICAL);
            t.setLineWidth(t.getLineWidth() + 30);
            t.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(1.5f, 1.5f, 1.5f), new AxisAngle4f()));
            t.setBackgroundColor(Color.fromRGB(getColorFromUUID(home.uuid())));
            t.setBrightness(new Display.Brightness(15, 15));
            t.getPersistentDataContainer().set(DISPLAY_KEY, PersistentDataType.BYTE, (byte) 1);

            t.setVisibleByDefault(false);
            initiator.showEntity(HomeManager.getInstance(), t);
        });
    }

    @Override
    public void destroy() {
        if (glowingMarker != null) glowingMarker.remove();
        if (nameDisplay != null) nameDisplay.remove();
    }

    @Override
    public boolean hasBeenSpawned() {
        return nameDisplay != null;
    }
}

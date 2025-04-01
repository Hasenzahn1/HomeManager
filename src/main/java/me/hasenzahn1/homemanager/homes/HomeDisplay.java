package me.hasenzahn1.homemanager.homes;

import me.hasenzahn1.homemanager.HomeManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

public class HomeDisplay {

    public static NamespacedKey DISPLAY_KEY = new NamespacedKey(HomeManager.getInstance(), "home_display");

    private final Player initiator;
    private final Home home;

    private Slime slime;
    private TextDisplay textDisplay;

    public HomeDisplay(Player initiator, Home home) {
        this.initiator = initiator;
        this.home = home;

        display();
    }

    private void display() {
        if (!home.location().getChunk().isLoaded()) return;

        //
        slime = home.location().getWorld().spawn(new Location(home.location().getWorld(), home.location().getBlockX() + 0.5, home.location().getBlockY(), home.location().getBlockZ() + 0.5), Slime.class, s -> {
            s.setSize(2);
            s.setAI(false);
            s.setGlowing(true);
            s.setGravity(false);
            s.setInvulnerable(true);
            s.setInvisible(true);
            s.setRotation(0, 0);
            s.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100000000);
            s.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(100000000);
            s.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(100000000);
            s.setHealth(2048);
            s.addPotionEffect(PotionEffectType.REGENERATION.createEffect(255 * 20, 255 * 20).withAmbient(false).withParticles(false));
            s.getPersistentDataContainer().set(DISPLAY_KEY, PersistentDataType.BYTE, (byte) 1);

            s.setVisibleByDefault(false);
            initiator.showEntity(HomeManager.getInstance(), s);
        });

        //
        textDisplay = home.location().getWorld().spawn(new Location(home.location().getWorld(), home.location().getBlockX() + 0.5, home.location().getBlockY() + 1.5, home.location().getBlockZ() + 0.5), TextDisplay.class, t -> {
            t.setSeeThrough(true);
            //t.setRotation(0, 0);
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

    private int getColorFromUUID(UUID uuid) {
        int hash = uuid.hashCode();

        int r = (hash >> 16) & 0xFF;
        int g = (hash >> 8) & 0xFF;
        int b = hash & 0xFF;

        return (r << 16) | (g << 8) | b;
    }

    public void destroy() {
        if (slime != null) {
            //slime.teleport(slime.getLocation().subtract(0, -1000, 0));
            slime.remove();
        }
        if (textDisplay != null) textDisplay.remove();
    }

    public boolean hasBeenSpawned() {
        return textDisplay != null;
    }
}

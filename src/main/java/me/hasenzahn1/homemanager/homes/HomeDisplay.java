package me.hasenzahn1.homemanager.homes;

import me.hasenzahn1.homemanager.HomeManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.TextDisplay;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class HomeDisplay {

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

            s.setVisibleByDefault(false);
            initiator.showEntity(HomeManager.getInstance(), s);
        });

        textDisplay = home.location().getWorld().spawn(new Location(home.location().getWorld(), home.location().getBlockX() + 0.5, home.location().getBlockY() + 1.5, home.location().getBlockZ() + 0.5), TextDisplay.class, t -> {
            t.setSeeThrough(true);
            t.text(Component.text(home.name() + "\n" + home.getOwnersName()));
            t.setTextOpacity((byte) 255);
            t.setBillboard(Display.Billboard.VERTICAL);
            t.setLineWidth(t.getLineWidth() + 30);
            t.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(1.5f, 1.5f, 1.5f), new AxisAngle4f()));

            t.setVisibleByDefault(false);
            initiator.showEntity(HomeManager.getInstance(), t);
        });
    }

    public void destroy() {
        if (slime != null) slime.teleport(slime.getLocation().subtract(0, -1000, 0));
        if (textDisplay != null) textDisplay.remove();
    }
}

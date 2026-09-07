package me.hasenzahn1.homemanager.homes;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

/**
 * Represents a single home entry displayed as part of the {@code /homesearch} command.
 * <p>
 * Unlike {@link HomeDisplay_entity}, this implementation never adds a real entity to the
 * world. It builds the marker/name-tag entities via {@link org.bukkit.RegionAccessor#createEntity},
 * which constructs them without registering them in the world's entity list, reads their real
 * metadata back out via PacketEvents' {@code SpigotConversionUtil.getEntityMetadata}, and sends
 * spawn/metadata packets to the initiator only. This avoids hand-rolling protocol metadata
 * indices (which drift between game versions) - the entity itself is the source of truth.
 */
public class HomeDisplay_packet extends HomeDisplay {

    private int markerEntityId;
    private int nameDisplayEntityId;
    private boolean spawned;

    public HomeDisplay_packet(Player initiator, Home home) {
        super(initiator, home);
    }

    @Override
    public void display() {
        if (!home.location().getChunk().isLoaded()) return;

        Shulker marker = createMarker();
        TextDisplay nameDisplay = createNameDisplay();

        markerEntityId = marker.getEntityId();
        nameDisplayEntityId = nameDisplay.getEntityId();

        sendSpawnAndMetadata(marker);
        sendSpawnAndMetadata(nameDisplay);

        spawned = true;
    }

    private Shulker createMarker() {
        Location location = new Location(home.location().getWorld(), home.location().getBlockX() + 0.5, home.location().getBlockY(), home.location().getBlockZ() + 0.5);
        Shulker shulker = home.location().getWorld().createEntity(location, Shulker.class);

        //slime.setSize(2);
        shulker.setAI(false);
        shulker.setGlowing(true);
        shulker.setGravity(false);
        shulker.setInvulnerable(true);
        shulker.setInvisible(true);
        shulker.setRotation(0, 0);
        shulker.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(1024);
        shulker.setHealth(1024);
        shulker.setCollidable(false);

        return shulker;
    }

    private TextDisplay createNameDisplay() {
        Location location = new Location(home.location().getWorld(), home.location().getBlockX() + 0.5, home.location().getBlockY() + 1.5, home.location().getBlockZ() + 0.5);
        TextDisplay textDisplay = home.location().getWorld().createEntity(location, TextDisplay.class);

        textDisplay.setSeeThrough(true);
        textDisplay.text(Component.text(home.name() + "\n" + home.getOwnersName()));
        textDisplay.setTextOpacity((byte) 255);
        textDisplay.setBillboard(Display.Billboard.VERTICAL);
        textDisplay.setLineWidth(textDisplay.getLineWidth() + 30);
        textDisplay.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(1.5f, 1.5f, 1.5f), new AxisAngle4f()));
        textDisplay.setBackgroundColor(Color.fromRGB(getColorFromUUID(home.uuid())));
        textDisplay.setBrightness(new Display.Brightness(15, 15));

        return textDisplay;
    }

    private void sendSpawnAndMetadata(Entity entity) {
        Location location = entity.getLocation();
        Vector3d position = new Vector3d(location.getX(), location.getY(), location.getZ());

        sendPacket(new WrapperPlayServerSpawnEntity(
                entity.getEntityId(), Optional.of(entity.getUniqueId()), SpigotConversionUtil.fromBukkitEntityType(entity.getType()),
                position, location.getPitch(), location.getYaw(), location.getYaw(), 0, Optional.empty()
        ));

        List<EntityData<?>> metadata = SpigotConversionUtil.getEntityMetadata(entity);
        sendPacket(new WrapperPlayServerEntityMetadata(entity.getEntityId(), metadata));
    }

    private void sendPacket(PacketWrapper<?> wrapper) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(initiator);
        user.sendPacket(wrapper);
    }

    @Override
    public void destroy() {
        if (!spawned) return;
        sendPacket(new WrapperPlayServerDestroyEntities(markerEntityId, nameDisplayEntityId));
    }

    @Override
    public boolean hasBeenSpawned() {
        return spawned;
    }
}

package com.lauriethefish.betterportals.bukkit.entity.faking;

import com.lauriethefish.betterportals.bukkit.math.Matrix;
import com.lauriethefish.betterportals.bukkit.math.PortalTransformations;
import com.lauriethefish.betterportals.bukkit.nms.EntityUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * We wrap the actual entity to make sending the packets more ergonomic.
 * Since we have to do transformation of the position and ID.
 */
@Getter
public class EntityInfo {
    private static final Random entityIdGenerator = new Random();

    private final Entity entity;
    // This is separate from the actual ID, since otherwise we will have issues when two entity IDs collide if an entity is visible and a copy of it is visible
    private final int entityId;
    private final UUID entityUniqueId;

    private final Matrix translation;
    private final Matrix rotation;

    /**
     * Creates an instance suitable for viewing an entity from the origin of a portal.
     * @param transformations The portals matrices, used for moving/rotating the entity
     * @param entity The entity to wrap
     */
    public EntityInfo(@NotNull PortalTransformations transformations, @NotNull Entity entity) {
        this.entity = entity;
        this.entityId = entityIdGenerator.nextInt() & Integer.MAX_VALUE;

        // If the entity is a player, then we need to use the actual UUID in order to get the skin correct
        // This means that the same player cannot be on screen multiple times, i.e. mirrors will not work for players
        // TODO: Implement this in non-NMS tracking method
        this.entityUniqueId = UUID.randomUUID();
        this.translation = transformations.getDestinationToOrigin();
        this.rotation = transformations.getRotateToOrigin();
    }

    /**
     * Creates an instance suitable for hiding or reshowing a hidden entity
     * @param entity The entity to wrap
     */
    public EntityInfo(@NotNull Entity entity) {
        this.entity = entity;
        this.entityId = entity.getEntityId();
        this.entityUniqueId = entity.getUniqueId();

        this.translation = Matrix.makeIdentity();
        this.rotation = Matrix.makeIdentity();
    }

    /**
     * Finds the position that the entity should be shown at
     * @return The entity's rendered position
     */
    public Location findRenderedLocation() {
        Location actualPos = entity.getLocation();
        Location atOrigin = translation.transform(actualPos.toVector()).toLocation(Objects.requireNonNull(actualPos.getWorld()));

        atOrigin.setDirection(rotation.transform(EntityUtil.getActualEntityDirection(entity)));
        return atOrigin;
    }
}

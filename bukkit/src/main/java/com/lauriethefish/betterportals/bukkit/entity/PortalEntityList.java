package com.lauriethefish.betterportals.bukkit.entity;

import com.google.inject.assistedinject.Assisted;
import com.lauriethefish.betterportals.api.PortalPosition;
import com.lauriethefish.betterportals.bukkit.config.MiscConfig;
import com.lauriethefish.betterportals.bukkit.config.RenderConfig;
import com.lauriethefish.betterportals.bukkit.portal.IPortal;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import javax.inject.Inject;
import java.util.Collection;

// Stores the two lists of entities at the origin and destination of a portal
// (or only 1 if specified)
public class PortalEntityList implements IPortalEntityList {
    private final IPortal portal;
    private final MiscConfig miscConfig;
    private final RenderConfig renderConfig;

    private final boolean requireDestination;

    @Getter private Collection<Entity> originEntities = null;
    @Getter private Collection<Entity> destinationEntities = null;

    @Inject
    public PortalEntityList(@Assisted IPortal portal, @Assisted boolean requireDestination, MiscConfig miscConfig, RenderConfig renderConfig) {
        this.portal = portal;
        this.requireDestination = requireDestination;
        this.miscConfig = miscConfig;
        this.renderConfig = renderConfig;
    }

    @Override
    public void update(int ticksSinceActivated) {
        // Only update the entity lists when it's time to via the entity check interval
        if(ticksSinceActivated % miscConfig.getEntityCheckInterval() != 0) {return;}

        originEntities = getNearbyEntities(portal.getOriginPos());
        if(requireDestination) {
            destinationEntities = getNearbyEntities(portal.getDestPos());
        }
    }

    private Collection<Entity> getNearbyEntities(PortalPosition position) {
        World world = position.getWorld();
        return world.getNearbyEntities(position.getLocation(), renderConfig.getMaxXZ(), renderConfig.getMaxY(), renderConfig.getMaxXZ());
    }
}

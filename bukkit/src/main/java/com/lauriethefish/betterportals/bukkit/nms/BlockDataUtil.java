package com.lauriethefish.betterportals.bukkit.nms;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.lauriethefish.betterportals.api.IntVector;
import com.lauriethefish.betterportals.bukkit.util.VersionUtil;
import com.lauriethefish.betterportals.shared.util.ReflectionUtil;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Objects;

public class BlockDataUtil {
    private static final Method GET_HANDLE;
    private static final Method GET_COMBINED_ID;
    private static final Method GET_FROM_COMBINED_ID;
    private static final Method FROM_HANDLE;
    private static final Method GET_TILE_ENTITY;
    private static final Method GET_UPDATE_PACKET;

    static {
        boolean packageNamesMapped = VersionUtil.isMcVersionAtLeast("1.17.0");

        if(VersionUtil.isMcVersionAtLeast("1.13.0")) {
            Class<?> nmsBlock = packageNamesMapped ? ReflectionUtil.findClass("net.minecraft.world.level.block.Block") : MinecraftReflectionUtil.findVersionedNMSClass("Block");
            Class<?> craftBlockData = MinecraftReflectionUtil.findCraftBukkitClass("block.data.CraftBlockData");
            Class<?> nmsBlockData = packageNamesMapped ? ReflectionUtil.findClass("net.minecraft.world.level.block.state.IBlockData") : MinecraftReflectionUtil.findVersionedNMSClass("IBlockData");

            GET_HANDLE = ReflectionUtil.findMethod(craftBlockData, "getState");
            GET_COMBINED_ID = ReflectionUtil.findMethod(nmsBlock, VersionUtil.isMcVersionAtLeast("1.18.0") ? "i" : "getCombinedId", nmsBlockData);

            GET_FROM_COMBINED_ID = ReflectionUtil.findMethod(nmsBlock, VersionUtil.isMcVersionAtLeast("1.18.0") ? "a" : "getByCombinedId", int.class);
            FROM_HANDLE = ReflectionUtil.findMethod(craftBlockData, "fromData", nmsBlockData);
        }   else    {
            GET_HANDLE = null;
            GET_COMBINED_ID = null;
            GET_FROM_COMBINED_ID = null;
            FROM_HANDLE = null;
        }

        Class<?> blockEntityState = MinecraftReflectionUtil.findCraftBukkitClass("block.CraftBlockEntityState");
        Class<?> nmsTileEntity = packageNamesMapped ? ReflectionUtil.findClass("net.minecraft.world.level.block.entity.TileEntity") : MinecraftReflectionUtil.findVersionedNMSClass("TileEntity");
        GET_TILE_ENTITY = ReflectionUtil.findMethod(blockEntityState, "getTileEntity");
        GET_UPDATE_PACKET = ReflectionUtil.findMethod(nmsTileEntity, VersionUtil.isMcVersionAtLeast("1.18.0") ? "h" : "getUpdatePacket");
    }

    /**
     * Converts <code>blockData</code> into a combined ID that stores all info about the block.
     * @param blockData The data to convert
     * @return The combined ID of the data
     */
    public static int getCombinedId(@NotNull BlockData blockData) {
        Object nmsData = ReflectionUtil.invokeMethod(blockData, GET_HANDLE);
        return (int) ReflectionUtil.invokeMethod(null, GET_COMBINED_ID, nmsData);
    }

    /**
     * Converts <code>combinedId</code> as created in {@link BlockDataUtil#getCombinedId(BlockData)} back into a {@link BlockData}.
     * @param combinedId The ID to convert
     * @return The bukkit block data
     */
    public static BlockData getByCombinedId(int combinedId) {
        Object nmsData = ReflectionUtil.invokeMethod(null, GET_FROM_COMBINED_ID, combinedId);
        return (BlockData) ReflectionUtil.invokeMethod(null, FROM_HANDLE, nmsData);
    }

    /**
     * Finds the ProtocolLib wrapper around the <code>PacketPlayOutTileEntityData</code> which updates the tile entity data for <code>tileState</code>.
     * @param tileState The tile entity to get the packet of (Not a TileState since that doesn't exist on 1.12)
     * @return The ProtocolLib wrapper
     */
    public static @Nullable PacketContainer getUpdatePacket(@NotNull BlockState tileState) {
        Object nmsTileEntity = ReflectionUtil.invokeMethod(tileState, GET_TILE_ENTITY);
        Object unwrappedPacket = ReflectionUtil.invokeMethod(nmsTileEntity, GET_UPDATE_PACKET);

        if(unwrappedPacket == null) {
            return null;
        }

        return PacketContainer.fromPacket(unwrappedPacket);
    }

    /**
     * Sets the position of a <code>PacketPlayOutTileEntityData</code> in both the NBT and packet itself
     * @param packet The packet to modify the position of
     * @param position The new position
     */
    public static void setTileEntityPosition(@NotNull PacketContainer packet, @NotNull IntVector position) {
        BlockPosition blockPosition = new BlockPosition(position.getX(), position.getY(), position.getZ());
        packet.getBlockPositionModifier().write(0, blockPosition);

        // The NBT Data also stores the position
        NbtCompound compound = (NbtCompound) packet.getNbtModifier().read(0);
        if (Objects.nonNull(compound)) {
            compound.put("x", blockPosition.getX());
            compound.put("y", blockPosition.getY());
            compound.put("z", blockPosition.getZ());
        }
    }

}

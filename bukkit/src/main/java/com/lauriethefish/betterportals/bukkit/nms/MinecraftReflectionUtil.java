package com.lauriethefish.betterportals.bukkit.nms;

import com.lauriethefish.betterportals.shared.util.NewReflectionUtil;
import org.bukkit.Bukkit;

/**
 * Bukkit is strange and puts the version number in the NMS package names.
 * This means that we have to use reflection to access them if we want to work across versions.
 */
public class MinecraftReflectionUtil {
    private static final String packageVersion; // Name of the NMS/craftbukkit packages, e.g. 1_12_R1
    private static final String minecraftClassPrefix; // E.g. net.minecraft.server.1_12_R1
    private static final String craftBukkitClassPrefix; // E.g. org.bukkit.craftbukkit.1_12_R1

    static {
        packageVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        minecraftClassPrefix = String.format("net.minecraft.server.%s.", packageVersion);
        craftBukkitClassPrefix = String.format("org.bukkit.craftbukkit.%s.", packageVersion);
    }

    /**
     * Finds a class in the <code>net.minecraft.server.version</code> package.
     * Should ONLY be used for pre-1.17 classes, since post-1.17 classes are no longer versioned, and follow minecraft class naming.
     * @param name Name of the class relative to this package, with no dot at the start.
     * @return The located class
     */
    public static Class<?> findVersionedNMSClass(String name) {
        return NewReflectionUtil.findClass(minecraftClassPrefix + name);
    }

    /**
     * Finds a class in the <code>org.bukkit.craftbukkit.version</code> package
     * @param name Name of the class relative to this package, with no dot at the start.
     * @return The located class
     */
    public static Class<?> findCraftBukkitClass(String name) {
        return NewReflectionUtil.findClass(craftBukkitClassPrefix + name);
    }
}

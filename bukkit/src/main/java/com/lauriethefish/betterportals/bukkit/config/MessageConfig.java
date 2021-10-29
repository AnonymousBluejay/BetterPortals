package com.lauriethefish.betterportals.bukkit.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lauriethefish.betterportals.bukkit.command.framework.CommandException;
import com.lauriethefish.betterportals.bukkit.util.VersionUtil;
import com.lauriethefish.betterportals.bukkit.nms.NBTTagUtil;
import com.lauriethefish.betterportals.shared.logging.Logger;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Handles formatting text based on what's in the messages section of the config.
 */
@Singleton
public class MessageConfig {
    private static final boolean HEX_COLOR_CODES_AVAILABLE;

    static {
        boolean isAvailable = false;

        if(VersionUtil.isMcVersionAtLeast("1.16.0")) {
            try {
                Class.forName("net.md_5.bungee.api.ChatColor");
                isAvailable = true;
            }   catch(ClassNotFoundException ignored) {}
        }

        HEX_COLOR_CODES_AVAILABLE = isAvailable;
    }

    private static final String PORTAL_WAND_TAG = "portalWand";

    private final Logger logger;
    private final Map<String, String> messageMap = new HashMap<>();

    private String portalWandName;
    @Getter private String prefix;
    @Getter private String messageColor;

    private ItemStack portalWand = null;

    @Inject
    public MessageConfig(Logger logger) {
        this.logger = logger;
    }

    public void load(FileConfiguration file) {
        ConfigurationSection messagesSection = Objects.requireNonNull(file.getConfigurationSection("chatMessages"), "Missing chat messages section");

        for(String key : messagesSection.getKeys(false)) {
            messageMap.put(key, translateColorCodes(messagesSection.getString(key)));
        }

        portalWandName = translateColorCodes(Objects.requireNonNull(file.getString("portalWandName"), "Missing portalWandName"));
        prefix = getRawMessage("prefix");
        messageColor = translateColorCodes(Objects.requireNonNull(messagesSection.getString("messageColor"), "Missing messageColor"));
    }

    /**
     * Translates both the <code>&</code> color codes, and hex colours if on 1.16 spigot.
     * @param message The message to translate
     * @return The translated message with the colours
     */
    private @NotNull String translateColorCodes(@NotNull String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (HEX_COLOR_CODES_AVAILABLE) {
            return translateHexColors(message);
        }   else    {
            return message;
        }
    }

    /**
     * Translates hex colour codes in <code>message</code>, e.g. <code>{(#000000)}</code> is black.
     * Invalid colour codes print a warning and are removed.
     * @param message The message to translate
     * @return The translated message with the colours
     */
    private @NotNull String translateHexColors(@NotNull String message) {
        StringBuilder result = new StringBuilder();
        StringBuilder currentSegment = null;

        for(char c : message.toCharArray()) {
            // Start a new segment if we reach an opening curly bracket
            if(c == '{' && currentSegment == null) {
                currentSegment = new StringBuilder();
            }

            // Add to the current {...} segment if we are in one, otherwise we just add to the resultant string
            if(currentSegment == null) {
                result.append(c);
            }   else    {
                currentSegment.append(c);
            }

            // If we reach a closing curly bracket, we have reached the end of the current segment
            if(c == '}' && currentSegment != null) {
                String segment = currentSegment.toString();
                boolean parsingFailed = true;
                // Hex colours should be {(#000000)}, so the 2nd and 2nd to last character of the segment should be ( and ) respectively (segments include the curly brackets)
                if(segment.charAt(1) == '(' && segment.charAt(segment.length() - 2) == ')') {
                    String hexString = segment.substring(2, segment.length() - 2); // Get the #000000 part of the segment

                    try {
                        result.append(net.md_5.bungee.api.ChatColor.of(hexString));
                        parsingFailed = false;
                    }   catch(IllegalArgumentException ex) {
                        logger.warning("Failed to parse hex colour: %s", hexString);
                    }
                }

                // Just add the segment as it was if parsing fails
                if(parsingFailed) {
                    result.append(segment);
                }

                currentSegment = null;
            }
        }

        return result.toString();
    }

    /**
     * @return The wand with the NBT tags for creating portals
     */
    public @NotNull ItemStack getPortalWand() {
        if(portalWand == null) {
            portalWand = new ItemStack(Material.BLAZE_ROD);

            ItemMeta meta = portalWand.getItemMeta();
            assert meta != null;
            meta.setDisplayName(portalWandName);

            portalWand.setItemMeta(meta);
            // Portal wand checking is done with an NBT tag
            portalWand = NBTTagUtil.addMarkerTag(portalWand, PORTAL_WAND_TAG);
        }

        return portalWand;
    }

    /**
     * Checks if <code>item</code> is a portal wand
     * @param item The item to test
     * @return true if it is a valid portal wand, false otherwise
     */
    public boolean isPortalWand(ItemStack item) {
        return NBTTagUtil.hasMarkerTag(item, PORTAL_WAND_TAG);
    }

    /**
     * Finds a chat message with the plugin prefix.
     * @param name The name in the config
     * @return A chat message with the configured plugin prefix
     */
    public String getChatMessage(String name) {
        return prefix + getRawMessage(name);
    }

    /**
     * Finds a chat message without the prefix, for boxing in a {@link CommandException}
     * @param name The name in the config
     * @return A chat message without the prefix.
     */
    public String getErrorMessage(String name) {
        return getRawMessage(name);
    }

    /**
     * Returns a yellow message for warnings in chat.
     * @param name The name in the config
     * @return The yellow formatted message
     */
    public String getWarningMessage(String name) {
        String rawMessage = getRawMessage(name);
        if(rawMessage.isEmpty()) {return "";} // Avoid returning the extra character so that we can use a simple String#isEmpty check to see whether to send the warning

        return ChatColor.YELLOW + rawMessage;
    }

    /**
     * Finds a chat message without the prefix.
     * @param name The name in the config
     * @return A chat message without the prefix.
     */
    public String getRawMessage(String name) {
        return messageMap.get(name);
    }
}

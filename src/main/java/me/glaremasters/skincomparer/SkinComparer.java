package me.glaremasters.skincomparer;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class SkinComparer extends JavaPlugin implements Listener {
    private final List<String> blocked = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        blocked.addAll(getConfig().getStringList("blocked"));
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onLogin(final PlayerLoginEvent event) {
        final Player player = event.getPlayer();
        final String texture = getPlayerSkin(player);

        if (blocked.contains(texture)) {
            getServer().getScheduler().runTaskLater(this, () -> player.kick(Component.text("Bad Skin")), 10L);
            return;
        }

        getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
            try {
                for (final String skin : blocked) {
                    if (similar(skin, texture)) {
                        getServer().getScheduler().runTask(this, () -> player.kick(Component.text("Bad Skin")));
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 10L);
    }

    /**
     * Compares images from 2 URLs to see if they are similar or not
     *
     * @param expectedSkin the skin in the blocked list being checked
     * @param playerSkin   the skin of the player
     * @return if the result different was less than 20%
     * @throws IOException when ImageIO fails to read in the image
     */
    private boolean similar(final String expectedSkin, final String playerSkin) throws IOException {
        final BufferedImage expected = ImageIO.read(new URL(expectedSkin));
        final BufferedImage actual = ImageIO.read(new URL(playerSkin));

        final ImageComparisonResult result = new ImageComparison(expected, actual).compareImages();

        return result.getDifferencePercent() < 0.2f;
    }

    /**
     * Obtains the skin of a player
     *
     * @param player the player obtaining skin from
     * @return skin of player
     */
    private String getPlayerSkin(final Player player) {
        final PlayerProfile profile = player.getPlayerProfile();
        if (!profile.hasTextures()) {
            return null;
        }
        final ProfileProperty profileProperty = profile.getProperties().stream().filter(property -> property.getName().equals("textures")).findFirst().get();
        final JsonObject obj = JsonParser.parseString(new String(Base64.getDecoder().decode(profileProperty.getValue()))).getAsJsonObject();
        return obj.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
    }
}

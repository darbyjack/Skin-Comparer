package me.glaremasters.skincomparer;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
        final PlayerProfile profile = player.getPlayerProfile();
        final ProfileProperty profileProperty = profile.getProperties().stream().filter(property -> property.getName().equals("textures")).findFirst().get();
        final JsonObject obj = JsonParser.parseString(new String(Base64.getDecoder().decode(profileProperty.getValue()))).getAsJsonObject();
        final String texture = obj.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
        if (blocked.contains(texture)) {
            getServer().getScheduler().runTaskLater(this, () -> player.kick(Component.text("Bad Skin")), 10L);
        }
    }
}

package net.matteo.fakename;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings({"resource", "DataFlowIssue"})
public class Utils {
    public static Map<UUID, ServerEntity> trackers = new HashMap<>();
    public static Map<UUID, Set<ServerPlayer>> viewers = new HashMap<>();

    public static Map<UUID, String> memoryCache = new HashMap<>();
    public static Path fileCache = null;

    public static boolean fakeProfile = false;   // true only while building the "fake name" info packet

    public static boolean hasFake(ServerPlayer target) {
        String fake = name(target);
        return fake != null && !profileName(fake).equals(target.getGameProfile().name());
    }

    public static void sendInfo(ServerPlayer viewer, ServerPlayer target, boolean fake) {
        viewer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(target.getUUID())));
        fakeProfile = fake;
        try {
            viewer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(target)));
        } finally {
            fakeProfile = false;
        }
    }

    public static String name(Player player) {
        if (!memoryCache.containsKey(player.getUUID())) {
            String name = profileName(player.getName().getString());

            try {
                if (fileCache == null && player.level().getServer() != null) {
                    fileCache = player.level().getServer().getWorldPath(LevelResource.ROOT).resolve("fakename");
                    Files.createDirectories(fileCache);
                }


                if (!fileCache.resolve(name + ".txt").toFile().exists()) {
                    Files.writeString(fileCache.resolve(name + ".txt").toFile().toPath(), name);
                }

                memoryCache.put(player.getUUID(), profileName(Files.readString(fileCache.resolve(name + ".txt"))));

            } catch (Throwable exception) {
                exception.fillInStackTrace();
            }

        }

        return memoryCache.get(player.getUUID());
    }

    public static String profileName(String string) {
        if (string.endsWith("§r")) string = string.substring(0, string.length() - 2);
        if (string.length() > 16) string = string.substring(0, 16);
        if (string.endsWith("§")) string = string.substring(0, string.length() - 1); // don't leave a dangling §
        return string;
    }

    public static void refresh(ServerPlayer target) {
        PlayerList list = target.level().getServer().getPlayerList();

        // tab list: real profile, new display name (ADD_PLAYER is ignored for known UUIDs)
        list.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(target)));

        // nametag: respawn the entity for current viewers, which runs the hooks above
        ServerEntity tracker = trackers.get(target.getUUID());
        Set<ServerPlayer> seen = viewers.get(target.getUUID());
        if (tracker == null || seen == null) return;

        for (ServerPlayer viewer : new ArrayList<>(seen)) {   // copy: the hooks edit the set
            tracker.removePairing(viewer);
            tracker.addPairing(viewer);
        }
    }
}

package net.matteo.fakename;

import static net.matteo.fakename.Variables.*;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.util.*;

@SuppressWarnings({"resource", "DataFlowIssue"})
public class Utils {

    public static boolean hasFake(ServerPlayer target) {
        String fake = name(target);
        return fake != null && !profileName(fake).equals(target.getGameProfile().name());
    }

    public static void sendInfo(ServerPlayer viewer, ServerPlayer target, boolean fake) {
        viewer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(target.getUUID())));
        Variables.fakeProfile = fake;

        try {
            viewer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(target)));
        } finally {
            Variables.fakeProfile = false;
        }
    }

    public static String name(Player player) {
        if (!Variables.memoryCache.containsKey(player.getUUID())) {
            String name = profileName(player.getName().getString());

            try {
                if (Variables.fileCache == null && player.level().getServer() != null) {
                    Variables.fileCache = player.level().getServer().getWorldPath(LevelResource.ROOT).resolve("fakename");
                    Files.createDirectories(Variables.fileCache);
                }


                if (!Variables.fileCache.resolve(name + ".txt").toFile().exists()) {
                    Files.writeString(Variables.fileCache.resolve(name + ".txt").toFile().toPath(), name);
                }

                Variables.memoryCache.put(player.getUUID(), profileName(Files.readString(Variables.fileCache.resolve(name + ".txt"))));

            } catch (Throwable exception) {
                logger.error("Fakename exception, please report using the github or the discord: https://github.com/matteo335/fakename https://discord.gg/2EyYeFPqRK" + exception);
            }
        }

        return Variables.memoryCache.get(player.getUUID());
    }

    public static String profileName(String string) {
        if (string.endsWith("§r")) string = string.substring(0, string.length() - 2);
        if (string.length() > 16) string = string.substring(0, 16);
        if (string.endsWith("§")) string = string.substring(0, string.length() - 1); //remove §
        return string;
    }

    public static void refresh(ServerPlayer target) {
        PlayerList list = target.level().getServer().getPlayerList();

        list.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(target)));

        ServerEntity tracker = Variables.trackers.get(target.getUUID());
        Set<ServerPlayer> seen = Variables.viewers.get(target.getUUID());
        if (tracker == null || seen == null) return;

        for (ServerPlayer viewer : new ArrayList<>(seen)) {
            tracker.removePairing(viewer);
            tracker.addPairing(viewer);
        }
    }
}

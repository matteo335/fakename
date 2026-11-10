package net.matteo.custom_name.utils;

import net.matteo.custom_name.mixins.DisplayInvoker;
import net.matteo.custom_name.mixins.TextDisplayInvoker;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import com.mojang.math.Transformation;

import org.joml.Vector3f;

import io.netty.buffer.Unpooled;

import java.util.*;

public final class DisplayName {
    private static final Map<UUID, Display.TextDisplay> DISPLAYS = new HashMap<>();
    private static final Map<UUID, Set<ServerPlayer>> VIEWERS = new HashMap<>();

    public static String desired(ServerPlayer target) {
        String name = Utils.name(target);
        return (name == null || name.equals(target.getGameProfile().name())) ? null : name;
    }

    public static Display.TextDisplay get(ServerPlayer target) {
        return DISPLAYS.get(target.getUUID());
    }

    private static Display.TextDisplay create(ServerPlayer target, String raw) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("text_display"));
        Display.TextDisplay display = new Display.TextDisplay(type, target.level());
        ((DisplayInvoker) display).invokeSetBillboardConstraints(Display.BillboardConstraints.CENTER);
        ((DisplayInvoker) display).invokeSetTransformation(new Transformation(new Vector3f(0F, 0.25F, 0F), null, null, null));
        ((TextDisplayInvoker) display).invokeSetText(Component.literal(raw.replace('&', '§')));
        return display;
    }

    public static void set(ServerPlayer target, String raw) {
        remove(target);
        DISPLAYS.put(target.getUUID(), create(target, raw));
        for (Packet<?> packet : spawnPackets(target)) broadcast(target, packet);
    }

    public static void remove(ServerPlayer target) {
        Display.TextDisplay display = DISPLAYS.remove(target.getUUID());
        if (display != null) broadcast(target, new ClientboundRemoveEntitiesPacket(display.getId()));
    }

    public static List<Packet<?>> spawnPackets(ServerPlayer target) {
        if (DISPLAYS.get(target.getUUID()) == null) {
            String fake = desired(target);
            if (fake == null) return List.of();
            DISPLAYS.put(target.getUUID(), create(target, fake));
        }

        Display.TextDisplay display = DISPLAYS.get(target.getUUID());

        List<SynchedEntityData.DataValue<?>> data = display.getEntityData().getNonDefaultValues();
        return List.of(
                new ClientboundAddEntityPacket(display.getId(), display.getUUID(), target.getX(), target.getY(), target.getZ(), 0f, 0f, display.getType(), 0, Vec3.ZERO, 0.0),
                new ClientboundSetEntityDataPacket(display.getId(), data != null ? data : List.of()),
                passengersPacket(target, display)
        );
    }

    public static Packet<?> passengersPacket(ServerPlayer target, Display.TextDisplay display) {
        List<Entity> passengers = target.getPassengers();
        int[] ids = new int[passengers.size() + 1];
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        for (int i = 0; i < passengers.size(); i++) ids[i] = passengers.get(i).getId();
        ids[passengers.size()] = display.getId();

        buf.writeVarInt(target.getId());
        buf.writeVarIntArray(ids);

        return ClientboundSetPassengersPacket.STREAM_CODEC.decode(buf);
    }

    public static void track(ServerPlayer target, ServerPlayer viewer) {
        VIEWERS.computeIfAbsent(target.getUUID(), uuid -> new HashSet<>()).add(viewer);
    }

    public static void untrack(ServerPlayer target, ServerPlayer viewer) {
        Set<ServerPlayer> set = VIEWERS.get(target.getUUID());
        if (set == null) return;
        set.remove(viewer);
        if (set.isEmpty()) VIEWERS.remove(target.getUUID());
    }

    private static void broadcast(ServerPlayer target, Packet<?> packet) {
        Set<ServerPlayer> set = VIEWERS.get(target.getUUID());
        if (set == null) return;
        for (ServerPlayer viewer : new ArrayList<>(set)) viewer.connection.send(packet);
    }
}

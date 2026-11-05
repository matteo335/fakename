package net.matteo.fakename.mixins;

import net.matteo.fakename.utils.DisplayName;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {

    @Shadow @Final private Entity entity;

    @Inject(method = "addPairing", at = @At("TAIL"))
    private void addPairing(ServerPlayer viewer, CallbackInfo ci) {
        if (entity instanceof ServerPlayer target && target != viewer) {
            DisplayName.track(target, viewer);
            for (Packet<?> packet : DisplayName.spawnPackets(target)) viewer.connection.send(packet);
        }
    }

    @Inject(method = "removePairing", at = @At("HEAD"))
    private void removePairing(ServerPlayer viewer, CallbackInfo ci) {
        if (entity instanceof ServerPlayer target) {
            Display.TextDisplay display = DisplayName.get(target);
            if (display != null) viewer.connection.send(new ClientboundRemoveEntitiesPacket(display.getId()));
            DisplayName.untrack(target, viewer);
        }
    }
}


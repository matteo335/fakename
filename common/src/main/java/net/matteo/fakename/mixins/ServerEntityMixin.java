package net.matteo.fakename.mixins;

import static net.matteo.fakename.Utils.*;
import static net.matteo.fakename.Variables.*;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {

    @Shadow @Final private Entity entity;

    @Inject(method = "addPairing", at = @At("HEAD"))
    private void addPairing$head$fakename(ServerPlayer viewer, CallbackInfo ci) {
        if (entity instanceof ServerPlayer target && target != viewer && hasFake(target)) {
            sendInfo(viewer, target, true);
        }
    }

    @Inject(method = "addPairing", at = @At("TAIL"))
    private void addPairing$tail$fakename(ServerPlayer viewer, CallbackInfo ci) {
        if (entity instanceof ServerPlayer target && target != viewer) {
            trackers.put(target.getUUID(), (ServerEntity) (Object) this);
            viewers.computeIfAbsent(target.getUUID(), k -> new HashSet<>()).add(viewer);
            if (hasFake(target)) sendInfo(viewer, target, false);
        }
    }

    @Inject(method = "removePairing", at = @At("HEAD"))
    private void removePairing$fakename(ServerPlayer viewer, CallbackInfo ci) {
        if (entity instanceof ServerPlayer target) {
            Set<ServerPlayer> seen = viewers.get(target.getUUID());
            if (seen != null) {
                seen.remove(viewer);
                if (seen.isEmpty()) {
                    viewers.remove(target.getUUID());
                    trackers.remove(target.getUUID());
                }
            }
        }
    }
}


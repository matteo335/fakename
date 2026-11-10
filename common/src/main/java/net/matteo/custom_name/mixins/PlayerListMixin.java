package net.matteo.custom_name.mixins;

import static net.matteo.custom_name.utils.Variables.*;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Shadow @Final
    private List<ServerPlayer> players;

    @Inject(method = "getPlayerByName", at = @At("RETURN"), cancellable = true)
    private void byName$fakename(String name, CallbackInfoReturnable<ServerPlayer> cir) {
        if (cir.getReturnValue() != null) return;

        for (ServerPlayer player : players) {
            String fake = memoryCache.get(player.getUUID());
            if (fake != null && fake.equalsIgnoreCase(name)) {
                cir.setReturnValue(player);
                return;
            }
        }
    }
}

package net.matteo.fakename.mixins;

import static net.matteo.fakename.Utils.*;

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

    // lets selectors and commands resolve the name clients now see
    @Inject(method = "getPlayerByName", at = @At("RETURN"), cancellable = true)
    private void byName$fakename(String name, CallbackInfoReturnable<ServerPlayer> cir) {
        if (cir.getReturnValue() != null) return;   // real names still win
        for (ServerPlayer p : players) {
            String fake = memoryCache.get(p.getUUID());
            if (fake != null && profileName(fake).equalsIgnoreCase(name)) {
                cir.setReturnValue(p);
                return;
            }
        }
    }
}

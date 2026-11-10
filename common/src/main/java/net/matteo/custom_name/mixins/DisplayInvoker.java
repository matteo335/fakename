package net.matteo.custom_name.mixins;

import net.minecraft.world.entity.Display;

import com.mojang.math.Transformation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface DisplayInvoker {

    @Invoker("setBillboardConstraints") void invokeSetBillboardConstraints(Display.BillboardConstraints constraints);
    @Invoker("setTransformation") void invokeSetTransformation(Transformation transformation);
}


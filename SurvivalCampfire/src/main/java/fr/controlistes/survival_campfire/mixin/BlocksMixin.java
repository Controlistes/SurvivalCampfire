package fr.controlistes.survival_campfire.mixin;

import net.minecraft.block.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import java.util.function.ToIntFunction;
import fr.controlistes.survival_campfire.mixinaccess.ICampfireBlockMixin;

@Mixin(Blocks.class)
public class BlocksMixin {

    @Redirect(
            method = "<clinit>",
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=campfire")),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Blocks;createLightLevelFromLitBlockState (I)Ljava/util/function/ToIntFunction;",
                    ordinal = 0
            )
    )
    private static ToIntFunction<BlockState> createLightLevelFromIntensity(int a) {
        return state -> state.get(ICampfireBlockMixin.FIRE_INTENSITY) != null ? state.get(ICampfireBlockMixin.FIRE_INTENSITY) : 1;
    }

}
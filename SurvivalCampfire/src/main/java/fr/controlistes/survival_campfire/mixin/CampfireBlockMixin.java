package fr.controlistes.survival_campfire.mixin;

import fr.controlistes.survival_campfire.SurvivalCampfire;
import fr.controlistes.survival_campfire.mixinaccess.ICampfireBlockEntityMixin;
import fr.controlistes.survival_campfire.mixinaccess.ICampfireBlockMixin;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.block.CampfireBlock;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin extends BlockWithEntity implements ICampfireBlockMixin {

    protected CampfireBlockMixin(Settings builder) {
        super(builder);
    }

    @Inject(at = @At("RETURN"), method = "<init>*")
    protected void initProxy(CallbackInfo info) {
        this.setDefaultState(this.getDefaultState().with(CampfireBlock.LIT, false));
    }

    @Inject(at = @At("RETURN"), method = "getPlacementState", cancellable = true)
    protected void getPlacementStateProxy(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() != null) {
            cir.setReturnValue(cir.getReturnValue().with(CampfireBlock.LIT, false));
            cir.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "appendProperties")
    protected void appendPropertiesProxy(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(FIRE_INTENSITY);
    }

    @Inject(at = @At("RETURN"), method = "onUse", cancellable = true)
    protected final void onUseProxy(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue() == ActionResult.CONSUME) {
            return;
        }
        ItemStack stack = player.getStackInHand(hand);
        // I initially wanted to write this in Kotlin and it was way prettier
        // We should be able to write Mixins in Kotlin
        Integer v = (Integer) FuelRegistryImpl.INSTANCE.get((ItemConvertible) stack.getItem());
        int fuelTime = v != null ? v : 0;
        if (fuelTime < 1) {
            return;
        }
        BlockEntity blockentity = world.getBlockEntity(pos);
        if (blockentity instanceof ICampfireBlockEntityMixin) {
            if (((ICampfireBlockEntityMixin) blockentity).addFuel((int) (fuelTime * SurvivalCampfire.INSTANCE.getConfig().getCampfireFuelMultiplier()))) {
                Random random = new Random();
                if (state.get(CampfireBlock.LIT)) {
                    world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5f, (float) (0.8f * (2 + (fuelTime - 50) * (0.1 - 2) / (20000 - 50))));
                    for (int i = 1; i <= ((2 + (fuelTime - 50) * (15 - 2) / (20000 - 50))); i++) {
                        world.addParticle(ParticleTypes.LAVA, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, random.nextFloat() / 2.0f, 5.0E-5, random.nextFloat() / 2.0f);
                    }
                } else {
                    world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 0.5f, 1.2f);
                }

                if (!player.getAbilities().creativeMode) {
                    stack.setCount(stack.getCount() - 1);
                }

                cir.setReturnValue(ActionResult.SUCCESS);
                cir.cancel();
            }

        }

    }
}


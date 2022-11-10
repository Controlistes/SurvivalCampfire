package fr.controlistes.survival_campfire.mixin;

import fr.controlistes.survival_campfire.SurvivalCampfire;
import fr.controlistes.survival_campfire.mixinaccess.ICampfireBlockEntityMixin;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static fr.controlistes.survival_campfire.mixinaccess.ICampfireBlockMixin.FIRE_INTENSITY;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin extends BlockEntity implements ICampfireBlockEntityMixin {

    int burnTime = 0;

    public CampfireBlockEntityMixin(BlockPos pos, BlockState state) {
        super(BlockEntityType.CAMPFIRE, pos, state);
    }

    public boolean addFuel(int fuelTime){
        if (burnTime + fuelTime < SurvivalCampfire.INSTANCE.getConfig().getCampfireMaxFuel()){
            burnTime += fuelTime;
            return true;
        }
        else{
            return false;
        }
    }

    @Inject(at = @At("RETURN"), method = "writeNbt")
    private void writeNbtProxy(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("BurnTime", this.burnTime);
    }

    @Inject(at = @At("RETURN"), method = "writeNbt")
    private void readNbtProxy(NbtCompound nbt,CallbackInfo ci) {
        burnTime = (int) nbt.getShort("BurnTime");
    }

    @Inject(at = @At("HEAD"), method = "litServerTick")
    private static void litServerTickProxy(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        if (world.getBlockState(pos).getBlock() != Blocks.SOUL_CAMPFIRE){
            CampfireBlockEntityMixin mixinEntity = (CampfireBlockEntityMixin) (BlockEntity) campfire;
            // It shouldn't work but it does
            if (!world.getBlockState(pos).get(Properties.LIT)){
                world.setBlockState(pos,(BlockState)state.with(FIRE_INTENSITY, 0));
            }
            --mixinEntity.burnTime;
            if (mixinEntity.burnTime >= 24000){
                world.setBlockState(pos,(BlockState)state.with(FIRE_INTENSITY, 15));
            }
            if (mixinEntity.burnTime < 24000){
                world.setBlockState(pos,(BlockState)state.with(FIRE_INTENSITY, 12));
            }
            if (mixinEntity.burnTime < 3600){
                world.setBlockState(pos,(BlockState)state.with(FIRE_INTENSITY, 7));
            }
            if (mixinEntity.burnTime < 300){
                world.setBlockState(pos,(BlockState)state.with(FIRE_INTENSITY, 3));
            }
            if (mixinEntity.burnTime < 1){
                world.setBlockState(pos,(BlockState)state.with(FIRE_INTENSITY, 0));
                world.setBlockState(pos,(BlockState)state.with(Properties.LIT, false));
                world.playSound(null,pos,SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,SoundCategory.BLOCKS,1.0f,1.0f);
            }
        }
    }
}

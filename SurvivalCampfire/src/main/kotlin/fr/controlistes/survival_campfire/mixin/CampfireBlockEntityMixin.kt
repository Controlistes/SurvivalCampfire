@file:JvmName("CampfireBlockEntityMixin")
@file:Mixin(CampfireBlockEntity::class)

package fr.controlistes.survival_campfire.mixin

import fr.controlistes.survival_campfire.SurvivalCampfire
import fr.controlistes.survival_campfire.mixinaccess.ICampfireBlockEntityMixin
import fr.controlistes.survival_campfire.mixinaccess.ICampfireBlockMixin.FIRE_INTENSITY
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.CampfireBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo


@Mixin(CampfireBlockEntity::class)
abstract class CampfireBlockEntityMixin(pos: BlockPos?, state: BlockState?) : BlockEntity(BlockEntityType.CAMPFIRE, pos, state), ICampfireBlockEntityMixin {

    private var burnTime = 0

    override fun addFuel(fuelTime: Int) : Boolean{
        if (burnTime + fuelTime < SurvivalCampfire.getConfig().campfireMaxFuel){
            burnTime += fuelTime
            return true
        }
        else{
            return false
        }

    }

    private companion object {
        @JvmStatic
        @Inject(at = [At("HEAD")], method = ["litServerTick"])
        private fun litServerTickProxy(
            world: World,
            pos: BlockPos,
            state: BlockState,
            campfire: CampfireBlockEntity,
            ci: CallbackInfo
        ) {

            if (world.getBlockState(pos).block != Blocks.SOUL_CAMPFIRE) {
                val mixinEntity = campfire as CampfireBlockEntityMixin

                if (world.getBlockState(pos).get(Properties.LIT) == false) {
                    world.setBlockState(pos, state.with(FIRE_INTENSITY, 0) as BlockState)
                }
                --mixinEntity.burnTime
                if (mixinEntity.burnTime >= 24000) {
                    world.setBlockState(pos, state.with(FIRE_INTENSITY, 15) as BlockState)
                }
                if (mixinEntity.burnTime < 24000) {
                    world.setBlockState(pos, state.with(FIRE_INTENSITY, 12) as BlockState)
                }
                if (mixinEntity.burnTime < 3600) {
                    world.setBlockState(pos, state.with(FIRE_INTENSITY, 7) as BlockState)
                }
                if (mixinEntity.burnTime < 300) {
                    world.setBlockState(pos, state.with(FIRE_INTENSITY, 3) as BlockState)
                }
                if (mixinEntity.burnTime < 1) {
                    world.setBlockState(pos, state.with(FIRE_INTENSITY, 0) as BlockState)
                    world.setBlockState(pos, state.with(Properties.LIT, false) as BlockState)
                    world.playSound(
                        null as PlayerEntity?,
                        pos,
                        SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
                        SoundCategory.BLOCKS,
                        1.0f,
                        1.0f
                    )
                }
            }

        }
    }

    @Inject(at = [At("RETURN")], method = ["writeNbt"])
    protected fun writeNbtProxy(nbt: NbtCompound,ci: CallbackInfo) {
        nbt.putInt("BurnTime", this.burnTime)
    }

    @Inject(at = [At("RETURN")], method = ["readNbt"])
    protected fun readNbtProxy(nbt: NbtCompound,ci: CallbackInfo) {
        burnTime = nbt.getShort("BurnTime").toInt()
    }

}


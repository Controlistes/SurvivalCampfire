package fr.controlistes.survival_campfire.mixin

import fr.controlistes.survival_campfire.SurvivalCampfire
import fr.controlistes.survival_campfire.mixinaccess.ICampfireBlockEntityMixin
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.CampfireBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import fr.controlistes.survival_campfire.mixinaccess.ICampfireBlockMixin.FIRE_INTENSITY
import kotlin.random.Random


@Mixin(CampfireBlock::class)
abstract class CampfireBlockMixin protected constructor(builder: Settings?) : BlockWithEntity(builder){

    @Inject(at = [At("RETURN")], method = ["<init>*"])
    protected fun initProxy(info: CallbackInfo?) {
        defaultState = defaultState.with(CampfireBlock.LIT, false)
    }

    @Inject(at = [At("RETURN")], method = ["getPlacementState"], cancellable = true)
    protected fun getPlacementStateProxy(context: ItemPlacementContext?, cir: CallbackInfoReturnable<BlockState?>) {
        if (cir.returnValue != null) {
            cir.returnValue = cir.returnValue!!.with(CampfireBlock.LIT, false)
            cir.cancel()
        }
    }

    @Inject(at = [At("RETURN")], method = ["onUse"], cancellable = true)
    protected fun onUseProxy(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult, cir: CallbackInfoReturnable<ActionResult?>){
        if (cir.getReturnValue() == ActionResult.CONSUME) {
            return
        }
        val stack : ItemStack = player.getStackInHand(hand)
        val fuelTime : Int = FuelRegistryImpl.INSTANCE.get(stack.getItem()) ?: 0
        if (fuelTime < 1) {
            return
        }
        val blockentity : BlockEntity? = world.getBlockEntity(pos)
        if (blockentity is ICampfireBlockEntityMixin){
            if (blockentity.addFuel((fuelTime * SurvivalCampfire.getConfig().campfireFuelMultiplier).toInt())){
                if (state.get(CampfireBlock.LIT) as Boolean) {
                    world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5f, 0.8f * (2 + (fuelTime - 50) * (0.1 - 2) / (20000 - 50)).toFloat())
                    val random = Random
                    for (i in 1..((2 + (fuelTime - 50) * (15 - 2) / (20000 - 50)))) {
                        world.addParticle(
                            ParticleTypes.LAVA,
                            pos.x.toDouble() + 0.5,
                            pos.y.toDouble() + 0.5,
                            pos.z.toDouble() + 0.5,
                            (random.nextFloat() / 2.0f).toDouble(),
                            5.0E-5,
                            (random.nextFloat() / 2.0f).toDouble()
                        )
                    }
                }
                else {
                    world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 0.5f, 1.2f)
                }
                if (!player.getAbilities().creativeMode) {
                    stack.setCount(stack.getCount() - 1)
                }
                cir.setReturnValue(ActionResult.SUCCESS)
                cir.cancel()
            }
        }
    }

    @Inject(at = [At("RETURN")], method = ["appendProperties"])
    protected open fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>, ci: CallbackInfo) {
        builder.add(FIRE_INTENSITY)
    }

}
package fr.controlistes.survival_campfire
import net.fabricmc.api.ModInitializer
import fr.controlistes.survival_campfire.config.SurvivalCampfireConfig

@Suppress("UNUSED")
object SurvivalCampfire: ModInitializer {
    private const val MOD_ID = "survival_campfire"
    override fun onInitialize() {
        getConfig().load()
    }
    fun getConfig(): SurvivalCampfireConfig {
        return SurvivalCampfireConfig.INSTANCE
    }
}
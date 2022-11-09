package fr.controlistes.survival_campfire.integrations

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import com.terraformersmc.modmenu.gui.ModsScreen

import fr.controlistes.survival_campfire.config.SurvivalCampfireConfig

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory {
            SurvivalCampfireConfig.INSTANCE.createGui(ModsScreen(null))
        }
    }
}
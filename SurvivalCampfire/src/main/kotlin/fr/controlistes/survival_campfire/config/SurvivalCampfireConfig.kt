package fr.controlistes.survival_campfire.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dev.isxander.yacl.api.ConfigCategory
import dev.isxander.yacl.api.Option
import dev.isxander.yacl.api.YetAnotherConfigLib
import dev.isxander.yacl.gui.controllers.slider.FloatSliderController
import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

open class SurvivalCampfireConfig {

    val configFile: Path = FabricLoader.getInstance().getConfigDir().resolve("survival-campfire.json")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    var campfireFuelMultiplier : Float = 1.0f
    var campfireMaxFuel : Int = 72000

    open fun save() {
        try {
            Files.deleteIfExists(configFile)
            val json = JsonObject()
            json.addProperty("campfireFuelMultiplier", campfireFuelMultiplier)
            json.addProperty("campfireMaxFuel", campfireMaxFuel)
            Files.writeString(configFile, gson.toJson(json))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun load() {
        try {
            if (Files.notExists(configFile)) {
                save()
                return
            }
            val json = gson.fromJson(
                Files.readString(configFile),
                JsonObject::class.java
            )
            if (json.has("campfireFuelMultiplier")) campfireFuelMultiplier = json.getAsJsonPrimitive("campfireFuelMultiplier").asFloat
            if (json.has("campfireMaxFuel")) campfireMaxFuel = json.getAsJsonPrimitive("campfireMaxFuel").asInt
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    open fun createGui(parent: Screen?) : Screen{
        val builder = YetAnotherConfigLib.createBuilder()
            .title(Text.of("Survival Campfire"))
            .save(this::save)
            .category(
                ConfigCategory.createBuilder()
                .name(Text.of("Options"))
                    .option(
                        Option.createBuilder(Float::class.javaPrimitiveType)
                            .name(Text.translatable("Campfire : Fuel Multiplier"))
                            .tooltip(Text.translatable("By how much vanilla fuel furnace values should be multiplied when applied on a campfire."))
                            .binding(
                                1.0f,
                                { campfireFuelMultiplier },
                                { value: Float -> campfireFuelMultiplier = value }
                            )
                            .controller { yacl: Option<Float>? ->
                                FloatSliderController(
                                    yacl,
                                    0.1f,
                                    5.0f,
                                    0.1f
                                )
                            }
                            .build())
                    .option(
                        Option.createBuilder(Int::class.javaPrimitiveType)
                            .name(Text.translatable("Campfire : Max Fuel"))
                            .tooltip(Text.translatable("How many units of fuel you can stack in a campfire in minecraft ticks."))
                            .binding(
                                72000,
                                { campfireMaxFuel },
                                { value: Int -> campfireMaxFuel = value }
                            )
                            .controller { yacl: Option<Int>? ->
                                IntegerSliderController(
                                    yacl,
                                    24000,
                                    288000,
                                    1200
                                )
                            }
                            .build())
                .build())
        return builder.build().generateScreen(parent)
    }

    companion object {
        var INSTANCE: SurvivalCampfireConfig = SurvivalCampfireConfig()
    }
}

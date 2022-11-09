package fr.controlistes.survival_campfire.mixinaccess;

import net.minecraft.state.property.IntProperty;

public interface ICampfireBlockMixin {
    IntProperty FIRE_INTENSITY = IntProperty.of("fire_intensity", 0, 15);
}

package fr.controlistes.survival_campfire.mixinaccess

interface ICampfireBlockEntityMixin {
    fun addFuel(fuelTime: Int) : Boolean
}
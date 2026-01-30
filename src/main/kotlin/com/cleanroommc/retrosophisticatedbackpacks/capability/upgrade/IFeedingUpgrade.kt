package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.IItemHandler

sealed interface IFeedingUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {

    /**
     * Returns the first available food that matches the upgrade's criteria
     *
     * @return the slot number of where the food is in the item handler, -1 if not available
     */
    fun getFoodSlot(handler: IItemHandler, foodLevel: Int, health: Float, maxHealth: Float): Int

    /**
     * Feeds the player, with the handler being the inventory that the upgrade will search through
     *
     * @return if the player was successfully fed
     */
    fun feed(entity: EntityPlayer, handler: IItemHandler): Boolean {
        if (!entity.canEat(false))
            return false

        val slot = getFoodSlot(handler, entity.foodStats.foodLevel, entity.health, entity.maxHealth)

        if (slot > -1) {
            var food = handler.extractItem(slot, Int.MAX_VALUE, false)

            if (!food.isEmpty) {
                food = food.onItemUseFinish(entity.world, entity)
                handler.insertItem(slot, food, false)
            }
        }

        return false
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.IFEEDING_UPGRADE_CAPABILITY
}

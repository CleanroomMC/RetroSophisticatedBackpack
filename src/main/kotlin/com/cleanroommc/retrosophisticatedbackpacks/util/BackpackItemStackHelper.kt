package com.cleanroommc.retrosophisticatedbackpacks.util

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.NonNullList
import net.minecraftforge.items.IItemHandler
import squeek.applecore.api.AppleCoreAPI

object BackpackItemStackHelper {
    fun saveAllSlotsExtended(nbt: NBTTagCompound, inventory: NonNullList<ItemStack>): NBTTagCompound {
        val list = NBTTagList()

        for ((i, stack) in inventory.withIndex()) {
            if (!stack.isEmpty) {
                val tag = NBTTagCompound()
                tag.setByte("Slot", i.toByte())
                stack.writeToNBTExtended(tag)
                list.appendTag(tag)
            }
        }

        nbt.setTag("Items", list)
        return nbt
    }

    fun ItemStack.writeToNBTExtended(nbt: NBTTagCompound): NBTTagCompound {
        val nbt = writeToNBT(nbt)
        nbt.setInteger("Count", count)
        return nbt
    }

    fun loadAllItemsExtended(nbt: NBTTagCompound, inventory: NonNullList<ItemStack>) {
        val list: NBTTagList = nbt.getTagList("Items", 10)

        for (i in 0..<list.tagCount()) {
            val tag = list.getCompoundTagAt(i)
            val j = tag.getByte("Slot").toInt() and 255

            if (j < inventory.size) {
                inventory[j] = loadItemStackExtended(tag)
            }
        }
    }

    fun loadItemStackExtended(nbt: NBTTagCompound): ItemStack {
        val stack = ItemStack(nbt)
        stack.count = nbt.getInteger("Count")
        return stack
    }

    /**
     * Returns the hunger value of the food in the slot, if it is a food item
     *
     * @return the hunger value of the food in the slot, or null if the slot is not a food item
     */
    fun getHungerFromSlot(handler: IItemHandler, slot: Int, predicate: (ItemStack) -> Boolean): Int? {
        val stack = handler.getStackInSlot(slot)

        if (!predicate(stack))
            return null

        return if (RetroSophisticatedBackpacks.appleCoreLoaded) {
            val foodValues = AppleCoreAPI.accessor.getFoodValues(stack)

            foodValues.hunger
        } else {
            val item = stack.item as? ItemFood

            item?.getHealAmount(stack)
        }
    }
}

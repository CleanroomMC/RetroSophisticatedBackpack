package com.cleanroommc.retrosophisticatedbackpack.utils

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.NonNullList

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
}
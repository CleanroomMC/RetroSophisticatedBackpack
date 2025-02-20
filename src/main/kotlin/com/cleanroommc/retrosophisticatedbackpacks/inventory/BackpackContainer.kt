package com.cleanroommc.retrosophisticatedbackpacks.inventory

import com.cleanroommc.modularui.screen.ContainerCustomizer
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.inventory.slot.BackpackSlot
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import kotlin.math.min

class BackpackContainer() : ContainerCustomizer() {
    companion object {
        private val DROP_TO_WORLD: Int = -999
        private const val LEFT_MOUSE: Int = 0
        private const val RIGHT_MOUSE: Int = 1
    }

    override fun slotClick(slotId: Int, mouseButton: Int, clickTypeIn: ClickType, player: EntityPlayer): ItemStack {
        val playerInventory = player.inventory
        val heldStack = playerInventory.itemStack

        if (clickTypeIn == ClickType.PICKUP &&
            (mouseButton == LEFT_MOUSE || mouseButton == RIGHT_MOUSE) &&
            (slotId != DROP_TO_WORLD || slotId >= 0)
        ) {
            val clickedSlot = container.getSlot(slotId)
            val slotStack = clickedSlot.stack

            if (clickedSlot is BackpackSlot && !slotStack.isEmpty && heldStack.isEmpty) {
                val s = min(slotStack.count, clickedSlot.getItemStackLimit(slotStack))
                val toRemove = if (mouseButton == LEFT_MOUSE) s else (s + 1) / 2
                playerInventory.itemStack = slotStack.splitStack(toRemove)
                clickedSlot.putStack(slotStack)
                clickedSlot.onTake(player, playerInventory.itemStack)
                clickedSlot.onSlotChanged()
                container.detectAndSendChanges()
                return ItemStack.EMPTY
            }
        } else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
            val clickedSlot = container.getSlot(slotId)
            val slotStack = clickedSlot.stack
            val maxStackSize = clickedSlot.getItemStackLimit(slotStack)

            if (!heldStack.isEmpty &&
                (clickedSlot == null || !clickedSlot.hasStack || !clickedSlot.canTakeStack(player))
            ) {
                val i = if (mouseButton == 0) 0 else container.inventorySlots.size - 1
                val j = if (mouseButton == 0) 1 else -1

                for (k in 0..1) {
                    var l = i

                    while (l >= 0 && l < container.inventorySlots.size && heldStack.count < maxStackSize) {
                        val slot1 = container.inventorySlots[l]

                        if (slot1 is ModularSlot && slot1.isPhantom) {
                            l += j
                            continue
                        }

                        if (slot1.hasStack && Container.canAddItemToSlot(slot1, heldStack, true) &&
                            slot1.canTakeStack(player) && canMergeSlot(heldStack, slot1)
                        ) {
                            val itemstack2 = slot1.stack

                            if (k != 0 || itemstack2.count != maxStackSize) {
                                val i1 = min((maxStackSize - heldStack.count), itemstack2.count)
                                val itemstack3 = slot1.decrStackSize(i1)
                                heldStack.grow(i1)

                                if (itemstack3.isEmpty) {
                                    slot1.putStack(ItemStack.EMPTY)
                                }

                                slot1.onTake(player, itemstack3)
                            }
                        }
                        l += j
                    }
                }
            }

            container.detectAndSendChanges()
            return ItemStack.EMPTY
        }

        return super.slotClick(slotId, mouseButton, clickTypeIn, player)
    }
}

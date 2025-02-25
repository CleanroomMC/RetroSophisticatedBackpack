package com.cleanroommc.retrosophisticatedbackpacks.common.gui

import com.cleanroommc.modularui.screen.ContainerCustomizer
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlot
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min

class BackpackContainer(private val wrapper: BackpackWrapper) : ContainerCustomizer() {
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
            (slotId != DROP_TO_WORLD && slotId >= 0)
        ) {
            val clickedSlot = container.getSlot(slotId)
            val slotStack = clickedSlot.stack

            if (clickedSlot is ModularBackpackSlot && !slotStack.isEmpty && heldStack.isEmpty) {
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
        } else if (clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode &&
            playerInventory.itemStack.isEmpty && slotId >= 0
        ) {
            val slot = container.getSlot(slotId)

            if (slot != null && slot.hasStack)
                playerInventory.itemStack = slot.stack.copy()

            return ItemStack.EMPTY
        }

        return super.slotClick(slotId, mouseButton, clickTypeIn, player)
    }

    override fun transferItem(fromSlot: ModularSlot, fromStack: ItemStack): ItemStack {
        if (fromSlot.slotGroupName == "player_inventory") {
            val fromSlotGroup = fromSlot.slotGroup
            val memorizedSlots = container.shiftClickSlots.filter {
                if (it !is ModularBackpackSlot) false
                else wrapper.isSlotMemorized(it.slotIndex)
            }

            for (toSlot in memorizedSlots) {
                val slotGroup = toSlot.slotGroup
                if (slotGroup !== fromSlotGroup && toSlot.isEnabled && toSlot.isItemValid(fromStack)) {
                    val toStack = toSlot.stack.copy()
                    if (toSlot.isPhantom) {
                        if (toStack.isEmpty || (ItemHandlerHelper.canItemStacksStack(
                                fromStack,
                                toStack
                            ) && toStack.count < toSlot.getItemStackLimit(toStack))
                        ) {
                            toSlot.putStack(fromStack.copy())
                            return fromStack
                        }
                    } else if (ItemHandlerHelper.canItemStacksStack(fromStack, toStack)) {
                        val j = toStack.count + fromStack.count
                        val maxSize: Int =
                            toSlot.getItemStackLimit(fromStack) //Math.min(toSlot.getSlotStackLimit(), fromStack.getMaxStackSize());

                        if (j <= maxSize) {
                            fromStack.setCount(0)
                            toStack.setCount(j)
                            toSlot.putStack(toStack)
                        } else if (toStack.count < maxSize) {
                            fromStack.shrink(maxSize - toStack.count)
                            toStack.setCount(maxSize)
                            toSlot.putStack(toStack)
                        }

                        if (fromStack.isEmpty)
                            return fromStack
                    }
                }
            }

            for (emptySlot in memorizedSlots) {
                val stack = emptySlot.stack
                val slotGroup = emptySlot.slotGroup
                if (slotGroup !== fromSlotGroup && emptySlot.isEnabled && stack.isEmpty && emptySlot.isItemValid(
                        fromStack
                    )
                ) {
                    if (fromStack.count > emptySlot.getItemStackLimit(fromStack)) {
                        emptySlot.putStack(fromStack.splitStack(emptySlot.getItemStackLimit(fromStack)))
                    } else {
                        emptySlot.putStack(fromStack.splitStack(fromStack.count))
                    }
                    if (fromStack.count < 1) {
                        return fromStack
                    }
                }
            }
        }

        return super.transferItem(fromSlot, fromStack)
    }
}

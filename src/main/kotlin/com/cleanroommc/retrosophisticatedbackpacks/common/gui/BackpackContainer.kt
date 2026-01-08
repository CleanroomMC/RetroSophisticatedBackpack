package com.cleanroommc.retrosophisticatedbackpacks.common.gui

import com.cleanroommc.bogosorter.api.ISlot
import com.cleanroommc.bogosorter.api.ISortingContextBuilder
import com.cleanroommc.modularui.ModularUI
import com.cleanroommc.modularui.screen.ModularContainer
import com.cleanroommc.modularui.utils.Platform
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CraftingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.IndexedInventoryCraftingWrapper
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.IndexedModularCraftingSlot
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlot
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlotWrapper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.CraftingManager
import net.minecraftforge.fml.common.Optional
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min

class BackpackContainer(private val wrapper: BackpackWrapper, private val backpackSlotIndex: Int?) :
    ModularContainer() {
    companion object {
        private val DROP_TO_WORLD: Int = -999
        private const val LEFT_MOUSE: Int = 0
        private const val RIGHT_MOUSE: Int = 1
    }

    /**
     * Internal hash table used to store wrapped crafting matrices. Intended for use alongside craftingSlotInstances, to connect an instance of wrapper and slot together.
     */
    private val inventoryCraftingInstances: HashMap<Int, IndexedInventoryCraftingWrapper> = HashMap()

    /**
     * Internal hash table used to store crafting output slots. Intended for use alongside inventoryCraftingInstances, to connect an instance of wrapper and slot together.
     */
    private val craftingSlotInstances: HashMap<Int, IndexedModularCraftingSlot> = HashMap()

    override fun registerSlot(panelName: String?, slot: ModularSlot) {
        super.registerSlot(panelName, slot)
        if (slot is IndexedModularCraftingSlot) {
            registerCraftingSlot(slot.upgradeSlotIndex, slot)
        }
    }

    override fun onCraftMatrixChanged(inventoryIn: IInventory) {
        if (!guiData.isClient && inventoryIn is IndexedInventoryCraftingWrapper) {
            val playerMP = player as EntityPlayerMP
            val inventoryCrafting: IndexedInventoryCraftingWrapper = inventoryIn
            var stack: ItemStack = Platform.EMPTY_STACK
            val recipe = CraftingManager.findMatchingRecipe(inventoryCrafting, player.world)

            if (recipe != null && (recipe.isDynamic || !player.world.gameRules.getBoolean("doLimitedCrafting") || playerMP.recipeBook.isUnlocked(
                    recipe
                ))
            ) {
                craftingSlotInstances[inventoryCrafting.upgradeSlotIndex]?.setRecipeUsed(recipe)
                stack = recipe.getCraftingResult(inventoryCrafting)
            }
            inventoryIn.setSlot(9, stack, false)
        }
    }

    override fun slotClick(slotId: Int, mouseButton: Int, clickTypeIn: ClickType, player: EntityPlayer): ItemStack {
        val playerInventory = player.inventory
        val heldStack = playerInventory.itemStack

        if (clickTypeIn == ClickType.PICKUP &&
            (mouseButton == LEFT_MOUSE || mouseButton == RIGHT_MOUSE) &&
            (slotId != DROP_TO_WORLD && slotId >= 0)
        ) {
            val clickedSlot = getSlot(slotId)
            val slotStack = clickedSlot.stack

            if (clickedSlot is ModularBackpackSlot && !slotStack.isEmpty && heldStack.isEmpty) {
                val s = min(slotStack.count, clickedSlot.getItemStackLimit(slotStack))
                val toRemove = if (mouseButton == LEFT_MOUSE) s else (s + 1) / 2
                playerInventory.itemStack = slotStack.splitStack(toRemove)
                clickedSlot.putStack(slotStack)
                clickedSlot.onTake(player, playerInventory.itemStack)
                clickedSlot.onSlotChanged()
                detectAndSendChanges()
                return ItemStack.EMPTY
            }
        } else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
            val clickedSlot = getSlot(slotId)
            val slotStack = clickedSlot.stack
            val maxStackSize = clickedSlot.getItemStackLimit(slotStack)

            if (!heldStack.isEmpty &&
                (clickedSlot == null || !clickedSlot.hasStack || !clickedSlot.canTakeStack(player))
            ) {
                val i = if (mouseButton == 0) 0 else inventorySlots.size - 1
                val j = if (mouseButton == 0) 1 else -1

                for (k in 0..1) {
                    var l = i

                    while (l >= 0 && l < inventorySlots.size && heldStack.count < maxStackSize) {
                        val slot1 = inventorySlots[l]

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

            detectAndSendChanges()
            return ItemStack.EMPTY
        } else if (clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode &&
            playerInventory.itemStack.isEmpty && slotId >= 0
        ) {
            val slot = getSlot(slotId)

            if (slot != null && slot.hasStack)
                playerInventory.itemStack = slot.stack.copy()

            return ItemStack.EMPTY
        } else if (clickTypeIn == ClickType.SWAP && mouseButton >= 0 && mouseButton < 9 && backpackSlotIndex == mouseButton) {
            // Prevents swapping opened backpack when backpack is in player inventory
            return ItemStack.EMPTY
        }

        return super.slotClick(slotId, mouseButton, clickTypeIn, player)
    }

    override fun transferItem(fromSlot: ModularSlot, fromStack: ItemStack): ItemStack {
        if (fromSlot is IndexedModularCraftingSlot) {
            val inventoryCrafting = inventoryCraftingInstances[fromSlot.upgradeSlotIndex]

            if (inventoryCrafting == null) {
                // Shouldn't normally happen, but just in case...
                return transferItemFiltered(fromSlot, fromStack) {
                    it.slotGroupName == "player_inventory"
                }
            } else if (inventoryCrafting.craftingDestination == CraftingUpgradeWrapper.CraftingDestination.BACKPACK) {
                // Force transfer to backpack at all costs
                return transferItemFiltered(fromSlot, fromStack, {
                    it is ModularBackpackSlot && wrapper.isSlotMemorized(it.slotIndex)
                }, {
                    it is ModularBackpackSlot
                })
            } else {
                // Force transfer to player inventory at all costs
                return transferItemFiltered(fromSlot, fromStack) {
                    it.slotGroupName == "player_inventory"
                }
            }
        } else if (fromSlot.slotGroupName == "player_inventory") {
            return transferItemFiltered(fromSlot, fromStack) {
                if (it !is ModularBackpackSlot) false
                else wrapper.isSlotMemorized(it.slotIndex)
            }
        }

        return super.transferItem(fromSlot, fromStack)
    }

    /**
     * Modified version of transferToBackpack that allows for multiple filters to be "tried" in succession before delegating to vanilla behavior.
     * For instance, this can be used to prioritize memorized slots within a subset of all slots, then prioritize that subset if no memorized slots are available.
     */
    fun transferItemFiltered(
        fromSlot: ModularSlot,
        fromStack: ItemStack,
        vararg slotFilters: (ModularSlot) -> Boolean
    ): ItemStack {
        val fromSlotGroup = fromSlot.slotGroup
        for (slotFilter in slotFilters) {
            val memorizedSlots = shiftClickSlots.filter(slotFilter)

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

    /**
     * Attempts to transfer the provided stack from the provided slot to another available slot, based on shift priority and the provided filter.
     * @param fromSlot The slot being transferred from.
     * @param fromStack The stack being transferred.
     * @param slotFilter The filter to choose what slots are available as destinations.
     */
    fun transferItemFiltered(
        fromSlot: ModularSlot,
        fromStack: ItemStack,
        slotFilter: (ModularSlot) -> Boolean
    ): ItemStack {
        val fromSlotGroup = fromSlot.slotGroup
        val memorizedSlots = shiftClickSlots.filter(slotFilter)

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
        return super.transferItem(fromSlot, fromStack)
    }

    /**
     * Registers the provided ModularCraftingSlot to the internal hash table.
     * This is used to later connect it to its corresponding InventoryCraftingWrapper, once both are present.
     * @see registerInventoryCrafting
     * @param slotIndex The slot index for the upgrade this crafting slot belongs to.
     * @param craftingSlot The output slot in question.
     */
    private fun registerCraftingSlot(slotIndex: Int, craftingSlot: IndexedModularCraftingSlot) {
        craftingSlotInstances[slotIndex] = craftingSlot
        inventoryCraftingInstances[slotIndex]?.let(craftingSlot::setCraftMatrix)
    }

    /**
     * Registers the provided InventoryCraftingWrapper to the internal hash table.
     * This is used to later connect it to its corresponding ModularCraftingSlot, once both are present.
     * @see registerInventoryCrafting
     * @param slotIndex The slot index for the upgrade this wrapper belongs to.
     * @param inventoryCrafting The wrapper in question
     */
    fun registerInventoryCrafting(slotIndex: Int, inventoryCrafting: IndexedInventoryCraftingWrapper) {
        inventoryCraftingInstances[slotIndex] = inventoryCrafting
        craftingSlotInstances[slotIndex]?.setCraftMatrix(inventoryCrafting)
    }

    @Optional.Method(modid = ModularUI.BOGO_SORT)
    override fun buildSortingContext(builder: ISortingContextBuilder) {
        if (syncManager != null) {
            val backpackWrapper = wrapper
            val sortableSlots = backpackWrapper.getSortableSlotIndexes()
            val slots = mutableListOf<ISlot>()

            for (slot in inventorySlots) {
                if (slot is ModularBackpackSlot && slot.slotIndex in sortableSlots) {
                    slots.add(ModularBackpackSlotWrapper(slot))
                }
            }

            builder.addSlotGroup(slots, if (backpackWrapper.backpackInventorySize() > 81) 12 else 9)
        }
    }
}

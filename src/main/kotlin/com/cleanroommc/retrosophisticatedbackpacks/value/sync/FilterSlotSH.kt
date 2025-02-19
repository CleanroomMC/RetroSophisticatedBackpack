package com.cleanroommc.retrosophisticatedbackpacks.value.sync

import com.cleanroommc.modularui.utils.MouseData
import com.cleanroommc.modularui.value.sync.ItemSlotSH
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import net.minecraft.item.ItemStack

/**
 * Used to prevent phantom items (or ingredients) from overflowing, which has undefined meaning for filter action.
 */
class FilterSlotSH(slot: ModularSlot) : ItemSlotSH(slot) {
    override fun phantomClick(mouseData: MouseData, cursorStack: ItemStack) {
        super.phantomClick(mouseData, cursorStack)
        clampStackCount()
    }

    override fun phantomScroll(mouseData: MouseData) {
        super.phantomScroll(mouseData)
        clampStackCount()
    }

    private fun clampStackCount() {
        val stack = slot.stack

        if (stack.count > 1)
            stack.count = 1
    }
}
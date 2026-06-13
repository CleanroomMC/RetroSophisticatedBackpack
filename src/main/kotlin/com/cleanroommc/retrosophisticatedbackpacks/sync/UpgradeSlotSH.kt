package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.modularui.value.sync.ItemSlotSH
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedFeedingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CraftingUpgradeWrapper.CraftingDestination
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvancedFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAnvilUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IContentsFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IFilterUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IPumpUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.JukeboxUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.RefillUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.RepeatMode
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.ToolSwapMode
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.VoidType
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.server.SPacketSetSlot
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.items.IItemHandlerModifiable
import net.minecraftforge.items.ItemHandlerHelper

/**
 * Used to synchronize upgrade item's capability, this is only fired from client to reflect client's action to server
 * side.
 */
class UpgradeSlotSH(
    slot: ModularSlot,
    private val onClientSlotUpdate: () -> Unit = {}
) : ItemSlotSH(slot) {
    companion object {
        const val UPDATE_UPGRADE_TAB_STATE = 6
        const val UPDATE_UPGRADE_TOGGLE = 7
        const val UPDATE_BASIC_FILTERABLE = 8
        const val UPDATE_ADVANCED_FILTERABLE = 9
        const val UPDATE_ADVANCED_FEEDING = 10
        const val UPDATE_FILTER_WAY = 11
        const val UPDATE_CRAFTING_DESTINATION = 12
        const val UPDATE_VOID_TYPE = 13
        const val UPDATE_COMPACT_NON_UNCRAFTABLE = 14
        const val UPDATE_REFILL_TARGET_SLOT = 15
        const val UPDATE_VOID_WORK_IN_GUI = 16
        const val UPDATE_COMPACT_WORK_IN_GUI = 17
        const val UPDATE_JUKEBOX_PLAY = 18
        const val UPDATE_JUKEBOX_STOP = 19
        const val UPDATE_JUKEBOX_NEXT = 20
        const val UPDATE_JUKEBOX_PREVIOUS = 21
        const val UPDATE_JUKEBOX_SHUFFLE = 22
        const val UPDATE_JUKEBOX_REPEAT_MODE = 23
        const val UPDATE_TOOL_SWAPPER_SWAP_WEAPON = 24
        const val UPDATE_TOOL_SWAPPER_MODE = 25
        const val UPDATE_TANK_CLICK = 26
        const val UPDATE_PUMP_INPUT = 27
        const val UPDATE_PUMP_HAND = 28
        const val UPDATE_PUMP_WORLD = 29
        const val UPDATE_PUMP_FLUID_HANDLERS = 30
        const val UPDATE_PUMP_FLUID_FILTER = 31
        const val UPDATE_ANVIL_ITEM_NAME = 32
        const val UPDATE_ANVIL_SHIFT_CLICK = 33
        const val UPDATE_ANVIL_TAKE_RESULT = 34
        const val UPDATE_REOPEN_BACKPACK = 35
        const val UPDATE_MAGNET_PICKUP_ITEMS = 36
        const val UPDATE_CONTENTS_FILTERABLE = 37
    }

    private var lastCapabilityNbt = NBTTagCompound()

    override fun onSlotUpdate(stack: ItemStack, onlyAmountChanged: Boolean, client: Boolean, init: Boolean) {
        if (client && !onlyAmountChanged) {
            (slot.itemHandler as? IItemHandlerModifiable)?.setStackInSlot(slot.slotIndex, stack)
        }
        super.onSlotUpdate(stack, onlyAmountChanged, client, init)
        if (client) {
            onClientSlotUpdate()
        }
    }

    override fun detectAndSendChanges(init: Boolean) {
        super.detectAndSendChanges(init)
        val wrapper = slot.stack.getCapability(Capabilities.UPGRADE_CAPABILITY, null) ?: return
        val capabilityNbt = wrapper.serializeNBT()
        if (init || capabilityNbt != lastCapabilityNbt) {
            lastCapabilityNbt = capabilityNbt.copy()
            forceSyncItem()
        }
    }

    override fun readOnServer(id: Int, buf: PacketBuffer) {
        super.readOnServer(id, buf)

        when (id) {
            UPDATE_UPGRADE_TAB_STATE -> updateTabState(buf)
            UPDATE_UPGRADE_TOGGLE -> updateToggleable()
            UPDATE_BASIC_FILTERABLE -> updateBasicFilterable(buf)
            UPDATE_ADVANCED_FILTERABLE -> updateAdvancedFilterable(buf)
            UPDATE_ADVANCED_FEEDING -> updateAdvanceFeedingUpgrade(buf)
            UPDATE_FILTER_WAY -> updateFilterUpgrade(buf)
            UPDATE_CRAFTING_DESTINATION -> updateCraftingDestination(buf)
            UPDATE_VOID_TYPE -> updateVoidType(buf)
            UPDATE_COMPACT_NON_UNCRAFTABLE -> updateCompactNonUncraftable()
            UPDATE_REFILL_TARGET_SLOT -> updateRefillTargetSlot(buf)
            UPDATE_VOID_WORK_IN_GUI -> updateVoidWorkInGui()
            UPDATE_COMPACT_WORK_IN_GUI -> updateCompactWorkInGui()
            UPDATE_JUKEBOX_PLAY -> updateJukeboxPlay()
            UPDATE_JUKEBOX_STOP -> updateJukeboxStop()
            UPDATE_JUKEBOX_NEXT -> updateJukeboxNext()
            UPDATE_JUKEBOX_PREVIOUS -> updateJukeboxPrevious()
            UPDATE_JUKEBOX_SHUFFLE -> updateJukeboxShuffle()
            UPDATE_JUKEBOX_REPEAT_MODE -> updateJukeboxRepeatMode(buf)
            UPDATE_TOOL_SWAPPER_SWAP_WEAPON -> updateToolSwapperSwapWeapon()
            UPDATE_TOOL_SWAPPER_MODE -> updateToolSwapperMode(buf)
            UPDATE_TANK_CLICK -> updateTankClick()
            UPDATE_PUMP_INPUT -> updatePumpInput(buf)
            UPDATE_PUMP_HAND -> updatePumpHand()
            UPDATE_PUMP_WORLD -> updatePumpWorld()
            UPDATE_PUMP_FLUID_HANDLERS -> updatePumpFluidHandlers()
            UPDATE_PUMP_FLUID_FILTER -> updatePumpFluidFilter(buf)
            UPDATE_ANVIL_ITEM_NAME -> updateAnvilItemName(buf)
            UPDATE_ANVIL_SHIFT_CLICK -> updateAnvilShiftClick()
            UPDATE_ANVIL_TAKE_RESULT -> updateAnvilTakeResult()
            UPDATE_REOPEN_BACKPACK -> reopenBackpackGui()
            UPDATE_MAGNET_PICKUP_ITEMS -> updateMagnetPickupItems()
            UPDATE_CONTENTS_FILTERABLE -> updateContentsFilterable(buf)
        }
    }

    private fun reopenBackpackGui() {
        val player = syncManager.player as? EntityPlayerMP ?: return
        val container = player.openContainer as? BackpackContainer ?: return
        container.reopenBackpackGui(player)
    }

    private fun updateTabState(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.UPGRADE_CAPABILITY, null) ?: return
        wrapper.isTabOpened = buf.readBoolean()
    }

    private fun updateToggleable() {
        val wrapper = slot.stack.getCapability(Capabilities.TOGGLEABLE_CAPABILITY, null) ?: return
        wrapper.toggle()
    }

    private fun updateBasicFilterable(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.BASIC_FILTERABLE_CAPABILITY, null) ?: return

        wrapper.filterType = buf.readEnumValue(IBasicFilterable.FilterType::class.java)
    }

    private fun updateAdvancedFilterable(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.ADVANCED_FILTERABLE_CAPABILITY, null) ?: return

        wrapper.filterType = buf.readEnumValue(IBasicFilterable.FilterType::class.java)
        wrapper.matchType = buf.readEnumValue(IAdvancedFilterable.MatchType::class.java)
        wrapper.ignoreDurability = buf.readBoolean()
        wrapper.ignoreNBT = buf.readBoolean()

        val size = buf.readInt()

        wrapper.oreDictEntries.clear()

        for (i in 0 until size) {
            wrapper.oreDictEntries.add(buf.readString(100))
        }
    }

    private fun updateAdvanceFeedingUpgrade(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.ADVANCED_FEEDING_UPGRADE_CAPABILITY, null) ?: return

        wrapper.hungerFeedingStrategy =
            buf.readEnumValue(AdvancedFeedingUpgradeWrapper.FeedingStrategy.Hunger::class.java)
        wrapper.healthFeedingStrategy =
            buf.readEnumValue(AdvancedFeedingUpgradeWrapper.FeedingStrategy.Health::class.java)
    }

    private fun updateFilterUpgrade(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.IFILTER_UPGRADE_CAPABILITY, null) ?: return

        wrapper.filterWay = buf.readEnumValue(IFilterUpgrade.FilterWayType::class.java)
    }

    private fun updateCraftingDestination(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.CRAFTING_ITEM_HANDLER_CAPABILITY, null) ?: return

        wrapper.craftingDestination = buf.readEnumValue(CraftingDestination::class.java)
    }

    private fun updateVoidType(buf: PacketBuffer) {
        val voidType = buf.readEnumValue(VoidType::class.java)
        slot.stack.getCapability(Capabilities.VOID_UPGRADE_CAPABILITY, null)?.voidType = voidType
        slot.stack.getCapability(Capabilities.ADVANCED_VOID_UPGRADE_CAPABILITY, null)?.voidType = voidType
    }

    private fun updateCompactNonUncraftable() {
        slot.stack.getCapability(Capabilities.COMPACTING_UPGRADE_CAPABILITY, null)?.toggleCompactNonUncraftable()
        slot.stack.getCapability(Capabilities.ADVANCED_COMPACTING_UPGRADE_CAPABILITY, null)?.toggleCompactNonUncraftable()
    }

    private fun updateVoidWorkInGui() {
        slot.stack.getCapability(Capabilities.VOID_UPGRADE_CAPABILITY, null)?.toggleWorkInGui()
        slot.stack.getCapability(Capabilities.ADVANCED_VOID_UPGRADE_CAPABILITY, null)?.toggleWorkInGui()
    }

    private fun updateCompactWorkInGui() {
        slot.stack.getCapability(Capabilities.COMPACTING_UPGRADE_CAPABILITY, null)?.toggleWorkInGui()
        slot.stack.getCapability(Capabilities.ADVANCED_COMPACTING_UPGRADE_CAPABILITY, null)?.toggleWorkInGui()
    }

    private fun updateMagnetPickupItems() {
        slot.stack.getCapability(Capabilities.MAGNET_UPGRADE_CAPABILITY, null)?.togglePickupItems()
        slot.stack.getCapability(Capabilities.ADVANCED_MAGNET_UPGRADE_CAPABILITY, null)?.togglePickupItems()
    }

    private fun updateContentsFilterable(buf: PacketBuffer) {
        val filterType = buf.readEnumValue(IContentsFilterable.ContentsFilterType::class.java)
        (slot.stack.getCapability(Capabilities.UPGRADE_CAPABILITY, null) as? IContentsFilterable)?.contentsFilterType = filterType
    }

    private fun updateRefillTargetSlot(buf: PacketBuffer) {
        val filterSlot = buf.readInt()
        val targetSlot = buf.readEnumValue(RefillUpgradeWrapper.TargetSlot::class.java)
        slot.stack.getCapability(Capabilities.ADVANCED_REFILL_UPGRADE_CAPABILITY, null)?.setTargetSlot(filterSlot, targetSlot)
    }

    private fun jukebox(): JukeboxUpgradeWrapper? =
        slot.stack.getCapability(Capabilities.JUKEBOX_UPGRADE_CAPABILITY, null)
            ?: slot.stack.getCapability(Capabilities.ADVANCED_JUKEBOX_UPGRADE_CAPABILITY, null)

    private fun updateJukeboxPlay() {
        jukebox()?.requestPlay()
    }

    private fun updateJukeboxStop() {
        jukebox()?.requestStop()
    }

    private fun updateJukeboxNext() {
        jukebox()?.next()
    }

    private fun updateJukeboxPrevious() {
        jukebox()?.previous()
    }

    private fun updateJukeboxShuffle() {
        jukebox()?.toggleShuffle()
    }

    private fun updateJukeboxRepeatMode(buf: PacketBuffer) {
        jukebox()?.repeatMode = buf.readEnumValue(RepeatMode::class.java)
    }

    private fun updateToolSwapperSwapWeapon() {
        val wrapper = slot.stack.getCapability(Capabilities.ADVANCED_TOOL_SWAPPER_UPGRADE_CAPABILITY, null) ?: return
        wrapper.shouldSwapWeapon = !wrapper.shouldSwapWeapon
    }

    private fun updateToolSwapperMode(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.ADVANCED_TOOL_SWAPPER_UPGRADE_CAPABILITY, null) ?: return
        wrapper.toolSwapMode = buf.readEnumValue(ToolSwapMode::class.java)
    }

    private fun updateTankClick() {
        val wrapper = slot.stack.getCapability(Capabilities.TANK_UPGRADE_CAPABILITY, null) ?: return
        val player = slot.getSyncHandler().syncManager.player
        val backpackWrapper = (player.openContainer as? BackpackContainer)?.backpackWrapper ?: return
        if (player.inventory.itemStack.count <= 1) {
            wrapper.interactWithCursorStack(player, backpackWrapper)
        }
    }

    private fun pump(): IPumpUpgrade? =
        slot.stack.getCapability(Capabilities.IPUMP_UPGRADE_CAPABILITY, null)

    private fun updatePumpInput(buf: PacketBuffer) {
        pump()?.isInput = buf.readBoolean()
    }

    private fun updatePumpHand() {
        pump()?.let { it.interactWithHand = !it.interactWithHand }
    }

    private fun updatePumpWorld() {
        pump()?.let { it.interactWithWorld = !it.interactWithWorld }
    }

    private fun updatePumpFluidHandlers() {
        pump()?.let { it.interactWithFluidHandlers = !it.interactWithFluidHandlers }
    }

    private fun updatePumpFluidFilter(buf: PacketBuffer) {
        val wrapper = pump() ?: return
        val slotIndex = buf.readInt()
        val carried = slot.getSyncHandler().syncManager.player.inventory.itemStack
        val fluid = if (carried.isEmpty) null else FluidUtil.getFluidContained(carried.copy())
        wrapper.setFluidFilter(slotIndex, fluid)
    }

    private fun anvil(): IAnvilUpgrade? =
        slot.stack.getCapability(Capabilities.IANVIL_UPGRADE_CAPABILITY, null)

    private fun updateAnvilItemName(buf: PacketBuffer) {
        anvil()?.itemName = buf.readString(64)
    }

    private fun updateAnvilShiftClick() {
        anvil()?.let { it.shouldShiftClickIntoStorage = !it.shouldShiftClickIntoStorage }
    }

    private fun updateAnvilTakeResult() {
        val wrapper = anvil() ?: return
        val player = slot.getSyncHandler().syncManager.player as? EntityPlayerMP ?: return
        val preview = wrapper.updateRepairOutput(player, player.world)
        val carried = player.inventory.itemStack
        if (preview.isEmpty || !wrapper.canTakeResult(player) || !canPlaceAnvilResultOnCursor(carried, preview)) {
            return
        }

        val taken = wrapper.takeResult(player, player.world)
        if (taken.isEmpty) {
            return
        }
        if (carried.isEmpty) {
            player.inventory.itemStack = taken
        } else {
            carried.grow(taken.count)
            player.inventory.itemStack = carried
        }
        player.connection.sendPacket(SPacketSetSlot(-1, -1, player.inventory.itemStack))
        player.openContainer.detectAndSendChanges()
    }

    private fun canPlaceAnvilResultOnCursor(carried: ItemStack, result: ItemStack): Boolean =
        carried.isEmpty || ItemHandlerHelper.canItemStacksStack(carried, result) &&
                carried.count + result.count <= carried.maxStackSize
}

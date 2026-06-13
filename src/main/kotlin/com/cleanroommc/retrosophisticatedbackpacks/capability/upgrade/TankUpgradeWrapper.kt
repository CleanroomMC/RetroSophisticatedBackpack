package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.TankUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.IFluidHandlerItem
import net.minecraftforge.fluids.capability.IFluidTankProperties
import net.minecraftforge.fluids.capability.FluidTankProperties
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.ItemHandlerHelper

class TankUpgradeWrapper : UpgradeWrapper<TankUpgradeItem>(), ITankUpgrade {
    companion object {
        private const val FLUID_TAG = "Fluid"
        private const val INVENTORY_TAG = "Inventory"
        private const val INPUT_SLOT = 0
        private const val OUTPUT_SLOT = 1
        private const val INPUT_RESULT_SLOT = 2
        private const val OUTPUT_RESULT_SLOT = 3
        private const val BUCKET = 1000
    }

    override val settingsLangKey = "gui.tank_settings".asTranslationKey()
    override val tankCapacity = Config.tankUpgrade.capacityPerSlotRow * 3
    private var fluid: FluidStack? = null
    private var nextContainerActionTime = 0L
    private val inventory = object : ExposedItemStackHandler(4) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
            slot == INPUT_RESULT_SLOT || slot == OUTPUT_RESULT_SLOT || stack.isEmpty || FluidUtil.getFluidHandler(stack) != null

        override fun onContentsChanged(slot: Int) {
            super.onContentsChanged(slot)
        }
    }

    override fun getFluid(): FluidStack? =
        fluid?.copy()

    override fun getTankCapacity(wrapper: BackpackWrapper): Int =
        maxOf(BUCKET, (getSlotRows(wrapper) * Config.tankUpgrade.capacityPerSlotRow * getAdjustedStackMultiplier(wrapper)).toInt())

    private fun getMaxInOut(wrapper: BackpackWrapper): Int =
        maxOf(BUCKET, (getSlotRows(wrapper) * Config.tankUpgrade.maxInputOutput * getAdjustedStackMultiplier(wrapper)).toInt())

    private fun getSlotRows(wrapper: BackpackWrapper): Int =
        maxOf(1, (wrapper.backpackInventorySize() + 8) / 9)

    private fun getAdjustedStackMultiplier(wrapper: BackpackWrapper): Double =
        1.0 + Config.tankUpgrade.stackMultiplierRatio * (wrapper.getTotalStackMultiplier() - 1)

    override fun fill(wrapper: BackpackWrapper, resource: FluidStack, doFill: Boolean, ignoreInOutLimit: Boolean): Int {
        val current = fluid
        if (resource.amount <= 0 || current != null && !current.isFluidEqual(resource)) {
            return 0
        }

        val accepted = minOf(resource.amount, getTankCapacity(wrapper) - (current?.amount ?: 0), if (ignoreInOutLimit) Int.MAX_VALUE else getMaxInOut(wrapper))
        if (doFill && accepted > 0) {
            fluid = if (current == null) FluidStack(resource.fluid, accepted, resource.tag?.copy())
            else current.also { it.amount += accepted }
        }
        return accepted
    }

    override fun drain(wrapper: BackpackWrapper, maxDrain: Int, doDrain: Boolean, ignoreInOutLimit: Boolean): FluidStack? {
        val current = fluid ?: return null
        val drained = minOf(maxDrain, current.amount, if (ignoreInOutLimit) Int.MAX_VALUE else getMaxInOut(wrapper))
        if (drained <= 0) {
            return null
        }

        val result = FluidStack(current.fluid, drained, current.tag?.copy())
        if (doDrain) {
            current.amount -= drained
            if (current.amount <= 0) {
                fluid = null
            }
        }
        return result
    }

    override fun drain(wrapper: BackpackWrapper, resource: FluidStack, doDrain: Boolean, ignoreInOutLimit: Boolean): FluidStack? {
        val current = fluid ?: return null
        if (!current.isFluidEqual(resource)) {
            return null
        }
        return drain(wrapper, resource.amount, doDrain, ignoreInOutLimit)
    }

    override fun tick(wrapper: BackpackWrapper, world: World) {
        if (world.totalWorldTime < nextContainerActionTime) {
            return
        }
        var didSomething = tryDrainInput(wrapper)
        didSomething = tryFillOutput(wrapper) || didSomething
        if (didSomething) {
            nextContainerActionTime =
                world.totalWorldTime + Config.tankUpgrade.autoFillDrainContainerCooldown.coerceAtLeast(1)
        }
    }

    override fun getInventory(): IItemHandler =
        inventory

    override fun interactWithCursorStack(player: EntityPlayer, wrapper: BackpackWrapper) {
        val cursor = player.inventory.itemStack
        if (cursor.isEmpty) {
            return
        }
        val handler = FluidUtil.getFluidHandler(cursor.copy()) ?: return
        val current = fluid
        if (current != null && fillHandler(wrapper, handler, false)) {
            player.inventory.itemStack = handler.container
        } else if (drainHandler(wrapper, handler, false)) {
            player.inventory.itemStack = handler.container
        }
    }

    private fun tryDrainInput(wrapper: BackpackWrapper): Boolean {
        val stack = inventory.getStackInSlot(INPUT_SLOT)
        if (stack.isEmpty) {
            return false
        }
        val single = ItemHandlerHelper.copyStackWithSize(stack, 1)
        if (stack.count > 1 && !drainStack(wrapper, single, true, true, {})) {
            return false
        }
        return drainStack(wrapper, single, true, false) { container ->
            if (stack.count > 1) {
                stack.shrink(1)
                if (stack.isEmpty) {
                    inventory.setStackInSlot(INPUT_SLOT, ItemStack.EMPTY)
                }
            } else {
                inventory.setStackInSlot(INPUT_SLOT, container)
            }
        }
    }

    private fun tryFillOutput(wrapper: BackpackWrapper): Boolean {
        val stack = inventory.getStackInSlot(OUTPUT_SLOT)
        if (stack.isEmpty) {
            return false
        }
        val single = ItemHandlerHelper.copyStackWithSize(stack, 1)
        if (stack.count > 1 && !fillStack(wrapper, single, true, true, {})) {
            return false
        }
        return fillStack(wrapper, single, true, false) { container ->
            if (stack.count > 1) {
                stack.shrink(1)
                if (stack.isEmpty) {
                    inventory.setStackInSlot(OUTPUT_SLOT, ItemStack.EMPTY)
                }
            } else {
                inventory.setStackInSlot(OUTPUT_SLOT, container)
            }
        }
    }

    private fun drainStack(
        wrapper: BackpackWrapper,
        stack: ItemStack,
        moveResult: Boolean,
        requireFullDrain: Boolean,
        updateContainerStack: (ItemStack) -> Unit
    ): Boolean =
        FluidUtil.getFluidHandler(stack)?.let { drainHandler(wrapper, it, moveResult, requireFullDrain, updateContainerStack) } ?: false

    private fun fillStack(
        wrapper: BackpackWrapper,
        stack: ItemStack,
        moveResult: Boolean,
        requireFullFill: Boolean,
        updateContainerStack: (ItemStack) -> Unit
    ): Boolean =
        FluidUtil.getFluidHandler(stack)?.let { fillHandler(wrapper, it, moveResult, requireFullFill, updateContainerStack) } ?: false

    private fun drainHandler(
        wrapper: BackpackWrapper,
        handler: IFluidHandlerItem,
        moveResult: Boolean,
        requireFullDrain: Boolean = false,
        updateContainerStack: (ItemStack) -> Unit = {}
    ): Boolean {
        val current = fluid
        val toDrain = if (current == null) BUCKET else minOf(BUCKET, getTankCapacity(wrapper) - current.amount)
        val extracted = if (current == null) handler.drain(toDrain, false) else handler.drain(FluidStack(current.fluid, toDrain, current.tag?.copy()), false)
        if (extracted == null || extracted.amount <= 0 || fill(wrapper, extracted, false) <= 0) {
            return false
        }
        if (requireFullDrain && fill(wrapper, extracted, false) != firstTankCapacity(handler)) {
            return false
        }
        if (moveResult) {
            val preview = FluidUtil.getFluidHandler(handler.container.copy()) ?: return false
            val filled = fill(wrapper, extracted, false)
            preview.drain(FluidStack(extracted.fluid, filled, extracted.tag?.copy()), true)
            val movesToResult = hasNoMatchingFluid(preview)
            if (requireFullDrain && !movesToResult) {
                return false
            }
            if (movesToResult && !inventory.insertItem(INPUT_RESULT_SLOT, preview.container, true).isEmpty) {
                return false
            }
        }
        if (requireFullDrain) {
            return true
        }
        val filled = fill(wrapper, extracted, true)
        handler.drain(FluidStack(extracted.fluid, filled, extracted.tag?.copy()), true)
        val resultHandler = FluidUtil.getFluidHandler(handler.container.copy())
        if (moveResult && (resultHandler?.let(::hasNoMatchingFluid) ?: true)) {
            updateContainerStack(ItemStack.EMPTY)
            inventory.insertItem(INPUT_RESULT_SLOT, handler.container, false)
        } else {
            updateContainerStack(handler.container)
        }
        return true
    }

    private fun fillHandler(
        wrapper: BackpackWrapper,
        handler: IFluidHandlerItem,
        moveResult: Boolean,
        requireFullFill: Boolean = false,
        updateContainerStack: (ItemStack) -> Unit = {}
    ): Boolean {
        val current = fluid ?: return false
        val filled = handler.fill(FluidStack(current.fluid, minOf(BUCKET, current.amount), current.tag?.copy()), false)
        if (filled <= 0 || drain(wrapper, filled, false) == null) {
            return false
        }
        if (requireFullFill && drain(wrapper, filled, false)?.amount != firstTankCapacity(handler)) {
            return false
        }
        if (moveResult) {
            val preview = FluidUtil.getFluidHandler(handler.container.copy()) ?: return false
            val drained = drain(wrapper, filled, false) ?: return false
            preview.fill(drained, true)
            val movesToResult = matchingTankIsFull(preview)
            if (requireFullFill && !movesToResult) {
                return false
            }
            if (movesToResult && !inventory.insertItem(OUTPUT_RESULT_SLOT, preview.container, true).isEmpty) {
                return false
            }
        }
        if (requireFullFill) {
            return true
        }
        val drained = drain(wrapper, filled, true) ?: return false
        handler.fill(drained, true)
        val resultHandler = FluidUtil.getFluidHandler(handler.container.copy())
        if (moveResult && resultHandler?.let(::matchingTankIsFull) == true) {
            updateContainerStack(ItemStack.EMPTY)
            inventory.insertItem(OUTPUT_RESULT_SLOT, handler.container, false)
        } else {
            updateContainerStack(handler.container)
        }
        return true
    }

    private fun hasNoMatchingFluid(handler: IFluidHandlerItem): Boolean =
        handler.tankProperties.all { it.contents?.amount ?: 0 <= 0 }

    private fun firstTankCapacity(handler: IFluidHandlerItem): Int =
        handler.tankProperties.firstOrNull()?.capacity ?: 0

    private fun matchingTankIsFull(handler: IFluidHandlerItem): Boolean {
        val current = fluid
        if (current == null) {
            return handler.tankProperties.all { property ->
                val contents = property.contents
                contents != null && contents.amount >= property.capacity
            }
        }
        return handler.tankProperties.all { property ->
            val contents = property.contents
            contents == null || !contents.isFluidEqual(current) || contents.amount >= property.capacity
        }
    }

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        fluid?.let { nbt.setTag(FLUID_TAG, it.writeToNBT(NBTTagCompound())) }
        nbt.setTag(INVENTORY_TAG, inventory.serializeNBT())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        if (nbt.hasKey(FLUID_TAG))
            fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(FLUID_TAG))
        else if (nbt.hasKey(INVENTORY_TAG))
            fluid = null
        if (nbt.hasKey(INVENTORY_TAG))
            inventory.deserializeNBT(nbt.getCompoundTag(INVENTORY_TAG))
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.TANK_UPGRADE_CAPABILITY ||
                capability == Capabilities.ITANK_UPGRADE_CAPABILITY ||
                super<UpgradeWrapper>.hasCapability(capability, facing)

    fun getTankProperties(wrapper: BackpackWrapper): Array<IFluidTankProperties> =
        arrayOf(FluidTankProperties(getFluid(), getTankCapacity(wrapper), true, true))
}

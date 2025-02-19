package com.cleanroommc.retrosophisticatedbackpacks.inventory.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.InventoryCraftResult
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.CraftingManager
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.items.wrapper.InvWrapper
import kotlin.math.min

class CraftingOutputSlot(
    private val player: EntityPlayer,
    private val craftingMatrix: InventoryCrafting,
    private val craftResultInventory: InventoryCraftResult,
    index: Int
) : ModularSlot(InvWrapper(craftResultInventory), index) {
    private var amountCrafted = 0

    init {
        changeListener { stack, onlyAmountChanged, isClient, init ->
            println(stack)
        }
    }

    override fun isItemValid(stack: ItemStack): Boolean =
        false

    override fun onSlotChangedReal(itemStack: ItemStack, onlyChangedAmount: Boolean, client: Boolean, init: Boolean) {
        super.onSlotChangedReal(itemStack, onlyChangedAmount, client, init)
        val wrapper = this.itemHandler as InvWrapper
        wrapper.setStackInSlot(0, itemStack)
    }

    override fun decrStackSize(amount: Int): ItemStack {
        if (hasStack) {
            amountCrafted += min(amount, stack.count)
        }

        return super.decrStackSize(amount)
    }

    override fun onCrafting(stack: ItemStack, amount: Int) {
        amountCrafted += amount
        onCrafting(stack)
    }

    override fun onSwapCraft(amount: Int) {
        amountCrafted += amount
    }

    override fun onSlotChange(p_75220_1_: ItemStack, p_75220_2_: ItemStack) {
        val countDiff = p_75220_2_.count - p_75220_1_.count

        if (countDiff > 0) {
            onCrafting(p_75220_2_, countDiff)
        }
    }

    override fun onCrafting(stack: ItemStack) {
        if (amountCrafted > 0) {
            stack.onCrafting(player.world, player, amountCrafted)
            FMLCommonHandler.instance().firePlayerCraftingEvent(player, stack, craftingMatrix)
        }

        amountCrafted = 0
        val recipe = craftResultInventory.recipeUsed ?: return

        if (!recipe.isDynamic) {
            player.unlockRecipes(listOf(recipe))
            craftResultInventory.recipeUsed = null
        }
    }

    override fun onTake(player: EntityPlayer, stack: ItemStack): ItemStack {
        onCrafting(stack)
        ForgeHooks.setCraftingPlayer(player)

        val craftingMatrix = craftingMatrix
        val remainingStacks = CraftingManager.getRemainingItems(craftingMatrix, player.world)

        ForgeHooks.setCraftingPlayer(null)

        for ((i, remainingStack) in remainingStacks.withIndex()) {
            var currentStack = craftingMatrix.getStackInSlot(i)

            if (!currentStack.isEmpty) {
                craftingMatrix.decrStackSize(i, 1)
                currentStack = craftingMatrix.getStackInSlot(i)
            }

            if (!remainingStack.isEmpty) {
                if (currentStack.isEmpty) {
                    craftingMatrix.setInventorySlotContents(i, remainingStack)
                } else if (ItemStack.areItemsEqual(currentStack, remainingStack) && ItemStack.areItemStackTagsEqual(
                        currentStack,
                        remainingStack
                    )
                ) {
                    remainingStack.grow(currentStack.count)
                    craftingMatrix.setInventorySlotContents(i, remainingStack)
                } else if (!player.inventory.addItemStackToInventory(remainingStack)) {
                    player.dropItem(remainingStack, false)
                }
            }
        }

        return stack
    }
}
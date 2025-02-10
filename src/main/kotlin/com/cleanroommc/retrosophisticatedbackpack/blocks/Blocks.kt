package com.cleanroommc.retrosophisticatedbackpack.blocks

import com.cleanroommc.retrosophisticatedbackpack.backpack.BackpackTier
import net.minecraft.block.Block

object Blocks {
    @JvmField
    val BLOCKS = mutableListOf<Block>()

    @JvmField
    val BACKPACK_BLOCKS = mutableListOf<BackpackBlock>()

    @JvmField
    val leatherBackpack = BackpackBlock("backpack_leather", BackpackTier.LEATHER)

    @JvmField
    val ironBackpack = BackpackBlock("backpack_iron", BackpackTier.IRON)

    val goldBackpack = BackpackBlock("backpack_gold", BackpackTier.GOLD)

    val diamondBackpack = BackpackBlock("backpack_diamond", BackpackTier.DIAMOND)

    val obsidianBackpack = BackpackBlock("backpack_obsidian", BackpackTier.OBSIDIAN)
}
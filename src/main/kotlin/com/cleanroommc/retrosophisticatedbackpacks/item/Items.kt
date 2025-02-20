package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.Config
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackTier
import com.cleanroommc.retrosophisticatedbackpacks.block.Blocks
import net.minecraft.item.Item

object Items {
    @JvmField
    val ITEMS = mutableListOf<Item>()

    @JvmField
    val BACKPACK_ITEMS = mutableListOf<BackpackItem>()

    // Backpacks
    @JvmField
    val backpackLeather = BackpackItem(
        "backpack_leather",
        Blocks.leatherBackpack,
        Config.leatherBackpack::slots,
        Config.leatherBackpack::upgradeSlots,
        BackpackTier.LEATHER
    )

    @JvmField
    val backpackIron = BackpackItem(
        "backpack_iron",
        Blocks.ironBackpack,
        Config.ironBackpack::slots,
        Config.ironBackpack::upgradeSlots,
        BackpackTier.IRON
    )

    @JvmField
    val backpackGold = BackpackItem(
        "backpack_gold",
        Blocks.goldBackpack,
        Config.goldBackpack::slots,
        Config.goldBackpack::upgradeSlots,
        BackpackTier.GOLD
    )

    @JvmField
    val backpackDiamond = BackpackItem(
        "backpack_diamond",
        Blocks.diamondBackpack,
        Config.diamondBackpack::slots,
        Config.diamondBackpack::upgradeSlots,
        BackpackTier.DIAMOND
    )

    @JvmField
    val backpackObsidian = BackpackItem(
        "backpack_obsidian",
        Blocks.obsidianBackpack,
        Config.obsidianBackpack::slots,
        Config.obsidianBackpack::upgradeSlots,
        BackpackTier.OBSIDIAN
    )

    // Upgrades
    @JvmField
    val upgradeBase = UpgradeBaseItem("upgrade_base")

    @JvmField
    val stackUpgradeTierStarter = StackUpgradeItem("stack_upgrade_starter_tier", Config.stackUpgrade::leatherMultiplier)

    @JvmField
    val stackUpgradeTier1 = StackUpgradeItem("stack_upgrade_tier_1", Config.stackUpgrade::ironMultiplier)

    @JvmField
    val stackUpgradeTier2 = StackUpgradeItem("stack_upgrade_tier_2", Config.stackUpgrade::goldMultiplier)

    @JvmField
    val stackUpgradeTier3 = StackUpgradeItem("stack_upgrade_tier_3", Config.stackUpgrade::diamondMultiplier)

    @JvmField
    val stackUpgradeTier4 = StackUpgradeItem("stack_upgrade_tier_4", Config.stackUpgrade::obsidianMultiplier)

    @JvmField
    val craftingUpgrade = CraftingUpgradeItem("crafting_upgrade")

    @JvmField
    val inceptionUpgrade = InceptionUpgradeItem("inception_upgrade")

    @JvmField
    val pickupUpgrade = PickupUpgradeItem("pickup_upgrade")

    @JvmField
    val advancedPickupUpgrade = PickupUpgradeItem("advanced_pickup_upgrade", true)

    @JvmField
    val feedingUpgrade = FeedingUpgradeItem("feeding_upgrade")

    @JvmField
    val advancedFeedingUpgrade = FeedingUpgradeItem("advanced_feeding_upgrade", true)
}
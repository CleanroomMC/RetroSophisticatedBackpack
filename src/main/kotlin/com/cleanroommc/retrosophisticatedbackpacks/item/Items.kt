package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackTier
import com.cleanroommc.retrosophisticatedbackpacks.block.Blocks
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.*
import net.minecraft.item.Item

@Suppress("UNUSED")
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
    val pickupUpgrade = PickupUpgradeItem("pickup_upgrade", ::PickupUpgradeWrapper)

    @JvmField
    val advancedPickupUpgrade = PickupUpgradeItem("advanced_pickup_upgrade", ::AdvancedPickupUpgradeWrapper)

    @JvmField
    val feedingUpgrade = FeedingUpgradeItem("feeding_upgrade", ::FeedingUpgradeWrapper)

    @JvmField
    val advancedFeedingUpgrade = FeedingUpgradeItem("advanced_feeding_upgrade", ::AdvancedFeedingUpgradeWrapper)

    @JvmField
    val depositUpgrade = DepositUpgradeItem("deposit_upgrade", ::DepositUpgradeWrapper)

    @JvmField
    val advancedDepositUpgrade = DepositUpgradeItem("advanced_deposit_upgrade", ::AdvancedDepositUpgradeWrapper)

    @JvmField
    val restockUpgrade = RestockUpgradeItem("restock_upgrade", ::RestockUpgradeWrapper)

    @JvmField
    val advancedRestockUpgrade = RestockUpgradeItem("advanced_restock_upgrade", ::AdvancedRestockUpgradeWrapper)

    @JvmField
    val filterUpgrade = FilterUpgradeItem("filter_upgrade", ::FilterUpgradeWrapper)

    @JvmField
    val advancedFilterUpgrade = FilterUpgradeItem("advanced_filter_upgrade", ::AdvancedFilterUpgradeWrapper)
}
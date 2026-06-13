package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackTier
import com.cleanroommc.retrosophisticatedbackpacks.block.Blocks
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.*
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher.MobCatcherUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
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
        Config.leatherBackpack::inventorySlotCount,
        Config.leatherBackpack::upgradeSlotCount,
        BackpackTier.LEATHER
    )

    @JvmField
    val backpackIron = BackpackItem(
        "backpack_iron",
        Blocks.ironBackpack,
        Config.ironBackpack::inventorySlotCount,
        Config.ironBackpack::upgradeSlotCount,
        BackpackTier.IRON
    )

    @JvmField
    val backpackGold = BackpackItem(
        "backpack_gold",
        Blocks.goldBackpack,
        Config.goldBackpack::inventorySlotCount,
        Config.goldBackpack::upgradeSlotCount,
        BackpackTier.GOLD
    )

    @JvmField
    val backpackDiamond = BackpackItem(
        "backpack_diamond",
        Blocks.diamondBackpack,
        Config.diamondBackpack::inventorySlotCount,
        Config.diamondBackpack::upgradeSlotCount,
        BackpackTier.DIAMOND
    )

    @JvmField
    val backpackObsidian = BackpackItem(
        "backpack_obsidian",
        Blocks.obsidianBackpack,
        Config.obsidianBackpack::inventorySlotCount,
        Config.obsidianBackpack::upgradeSlotCount,
        BackpackTier.OBSIDIAN
    )

    // Upgrades
    @JvmField
    val upgradeBase = UpgradeBaseItem("upgrade_base")

    @JvmField
    val stackUpgradeTierStarter = StackUpgradeItem("stack_upgrade_starter_tier", Config.stackUpgrade::leatherMultiplier)

    @JvmField
    val exponentialStackUpgrade = ExponentialStackUpgradeItem("exponential_stack_upgrade")

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

    @JvmField
    val magnetUpgrade = MagnetUpgradeItem("magnet_upgrade", ::MagnetUpgradeWrapper)

    @JvmField
    val advancedMagnetUpgrade = MagnetUpgradeItem("advanced_magnet_upgrade", ::AdvancedMagnetUpgradeWrapper)

    @JvmField
    val voidUpgrade = VoidUpgradeItem("void_upgrade", ::VoidUpgradeWrapper)

    @JvmField
    val advancedVoidUpgrade = VoidUpgradeItem("advanced_void_upgrade", ::AdvancedVoidUpgradeWrapper)

    @JvmField
    val refillUpgrade = RefillUpgradeItem("refill_upgrade", ::RefillUpgradeWrapper)

    @JvmField
    val advancedRefillUpgrade = RefillUpgradeItem("advanced_refill_upgrade", ::AdvancedRefillUpgradeWrapper)

    @JvmField
    val compactingUpgrade = CompactingUpgradeItem("compacting_upgrade", ::CompactingUpgradeWrapper)

    @JvmField
    val advancedCompactingUpgrade = CompactingUpgradeItem("advanced_compacting_upgrade", ::AdvancedCompactingUpgradeWrapper)

    @JvmField
    val everlastingUpgrade = EverlastingUpgradeItem("everlasting_upgrade", ::EverlastingUpgradeWrapper)

    @JvmField
    val jukeboxUpgrade = JukeboxUpgradeItem("jukebox_upgrade", ::JukeboxUpgradeWrapper)

    @JvmField
    val advancedJukeboxUpgrade = JukeboxUpgradeItem("advanced_jukebox_upgrade", ::AdvancedJukeboxUpgradeWrapper)

    @JvmField
    val toolSwapperUpgrade = ToolSwapperUpgradeItem("tool_swapper_upgrade", ::ToolSwapperUpgradeWrapper)

    @JvmField
    val advancedToolSwapperUpgrade = ToolSwapperUpgradeItem("advanced_tool_swapper_upgrade", ::AdvancedToolSwapperUpgradeWrapper, hasTab = true)

    @JvmField
    val tankUpgrade = TankUpgradeItem("tank_upgrade", ::TankUpgradeWrapper)

    @JvmField
    val pumpUpgrade = PumpUpgradeItem("pump_upgrade", ::PumpUpgradeWrapper)

    @JvmField
    val advancedPumpUpgrade = PumpUpgradeItem("advanced_pump_upgrade", ::AdvancedPumpUpgradeWrapper)

    @JvmField
    val batteryUpgrade = BatteryUpgradeItem("battery_upgrade", ::BatteryUpgradeWrapper)

    @JvmField
    val anvilUpgrade = AnvilUpgradeItem("anvil_upgrade", ::AnvilUpgradeWrapper)

    @JvmField
    val mobCatcherUpgrade = MobCatcherUpgradeItem("mob_catcher_upgrade", false, ::MobCatcherUpgradeWrapper)

    @JvmField
    val advancedMobCatcherUpgrade = MobCatcherUpgradeItem("advanced_mob_catcher_upgrade", true, ::MobCatcherUpgradeWrapper)
}

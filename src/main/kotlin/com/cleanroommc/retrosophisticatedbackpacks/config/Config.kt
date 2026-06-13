package com.cleanroommc.retrosophisticatedbackpacks.config

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraftforge.common.config.Config
import net.minecraftforge.items.CapabilityItemHandler

@Config(modid = Tags.MOD_ID, name = "${Tags.MOD_ID}_general")
object Config {
    @JvmField
    @Config.Comment("List of items that are not allowed to be put in backpacks - e.g. \"minecraft:shulker_box\"")
    var disallowedItems = arrayOf("minecraft:shulker_box")

    @JvmField
    @Config.Comment("Determines if container items are able to fit in backpacks")
    var containerItemsDisallowed = false

    @JvmField
    @Config.Comment("List of blocks that inventory interaction upgrades can't interact with - e.g. \"minecraft:shulker_box\"")
    var noInteractionBlocks = arrayOf("minecraft:shulker_box")

    @JvmField
    @Config.Comment("List of blocks that are not allowed to connect to backpacks - e.g. \"refinedstorage:external_storage\"")
    var noConnectionBlocks = arrayOf<String>()

    @JvmField
    @Config.Comment("If true, disallows all blocks from connecting to backpacks")
    var allBlockConnectionsDisallowed = false

    @JvmField
    @Config.Comment("Turns on/off item fluid handler of backpack in its item form")
    var itemFluidHandlerEnabled = true

    @JvmField
    @Config.Comment("Determines whether player can right click on backpack that another player is wearing to open it")
    var allowOpeningOtherPlayerBackpacks = true

    @JvmField
    @Config.Comment("Allows disabling item display settings")
    @Config.RequiresMcRestart
    var itemDisplayDisabled = false

    @JvmField
    @Config.Comment("Allows disabling logic that dedupes backpacks with the same UUID in players' inventory")
    var tickDedupeLogicDisabled = false

    var blacklistedItems = arrayOf<String>()

    @JvmField
    @Config.Comment(
        "Entities that spawn with backpack equipped.",
        "Only entities that have chest armor slot will be able to spawn with a backpack equipped.",
    )
    var backpackEntitySpawnList = arrayOf(
        "minecraft:zombie",
        "minecraft:skeleton",
        "minecraft:husk",
        "minecraft:stray",
        "minecraft:wither_skeleton",
        "minecraft:zombie_pigman",
        "minecraft:wither_skeleton",
    )

    @JvmField
    var baseBackpackSpawnChance = 0.03f

    @JvmField
    val leatherBackpack = LeatherBackpackConfig()

    @JvmField
    val ironBackpack = IronBackpackConfig()

    @JvmField
    val goldBackpack = GoldBackpackConfig()

    @JvmField
    val diamondBackpack = DiamondBackpackConfig()

    @JvmField
    val obsidianBackpack = ObsidianBackpackConfig()

    @JvmField
    val stackUpgrade = StackUpgradeConfig()

    @JvmField
    val compactingUpgrade = FilteredUpgradeConfig(9, 3)

    @JvmField
    val advancedCompactingUpgrade = FilteredUpgradeConfig(16, 4)

    @JvmField
    val depositUpgrade = FilteredUpgradeConfig(9, 3)

    @JvmField
    val advancedDepositUpgrade = FilteredUpgradeConfig(16, 4)

    @JvmField
    val feedingUpgrade = FilteredUpgradeConfig(9, 3)

    @JvmField
    val advancedFeedingUpgrade = FilteredUpgradeConfig(16, 4)

    @JvmField
    val filterUpgrade = FilteredUpgradeConfig(9, 3)

    @JvmField
    val advancedFilterUpgrade = FilteredUpgradeConfig(16, 4)

    @JvmField
    val magnetUpgrade = MagnetUpgradeConfig(9, 3, 3)

    @JvmField
    val advancedMagnetUpgrade = MagnetUpgradeConfig(16, 4, 5)

    @JvmField
    val pickupUpgrade = FilteredUpgradeConfig(9, 3)

    @JvmField
    val advancedPickupUpgrade = FilteredUpgradeConfig(16, 4)

    @JvmField
    val refillUpgrade = FilteredUpgradeConfig(6, 3)

    @JvmField
    val advancedRefillUpgrade = FilteredUpgradeConfig(12, 4)

    @JvmField
    val restockUpgrade = FilteredUpgradeConfig(9, 3)

    @JvmField
    val advancedRestockUpgrade = FilteredUpgradeConfig(16, 4)

    @JvmField
    val voidUpgrade = VoidUpgradeConfig(9, 3)

    @JvmField
    val advancedVoidUpgrade = VoidUpgradeConfig(16, 4)

    @JvmField
    val toolSwapperUpgrade = FilteredUpgradeConfig(8, 4)

    @JvmField
    val tankUpgrade = TankUpgradeConfig()

    @JvmField
    val batteryUpgrade = BatteryUpgradeConfig()

    @JvmField
    val pumpUpgrade = PumpUpgradeConfig()

    @JvmField
    val advancedJukeboxUpgrade = JukeboxUpgradeConfig(12)

    @JvmField
    val mobCatcherUpgrade = MobCatcherUpgradeConfig()

    @JvmField
    val maxUpgradesPerStorage = MaxUpgradesPerStorageConfig()

    fun isItemDisallowed(stack: ItemStack): Boolean {
        val registryName = stack.item.registryName?.toString() ?: return false
        if (registryName in disallowedItems) {
            return true
        }
        return containerItemsDisallowed &&
                stack.item !is BackpackItem &&
                stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
    }

    fun canStackWithStackUpgrade(stack: ItemStack): Boolean {
        val registryName = stack.item.registryName?.toString() ?: return true
        return registryName !in stackUpgrade.nonStackableItems
    }

    fun isInteractionBlockDisallowed(block: Block): Boolean =
        block.registryName?.toString() in noInteractionBlocks

    fun isConnectionBlockDisallowed(block: Block): Boolean =
        allBlockConnectionsDisallowed || block.registryName?.toString() in noConnectionBlocks

    fun getUpgradeLimit(upgradeItem: UpgradeItem): Pair<String, Int>? {
        val limits = maxUpgradesPerStorage.maxUpgradesPerStorage.mapNotNull {
            val parts = it.split("|", limit = 2)
            val max = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
            parts[0] to max
        }.toMap()
        val registryPath = upgradeItem.registryName?.path
        if (registryPath != null && registryPath in limits) {
            return registryPath to limits.getValue(registryPath)
        }
        val group = upgradeItem.upgradeGroup
        return if (group != null && group in limits) group to limits.getValue(group) else null
    }

    fun matchesUpgradeLimit(upgradeItem: UpgradeItem, limitKey: String): Boolean =
        upgradeItem.registryName?.path == limitKey || upgradeItem.upgradeGroup == limitKey

    class LeatherBackpackConfig {
        @JvmField
        @Config.RequiresMcRestart
        var inventorySlotCount = 27

        @JvmField
        @Config.RequiresMcRestart
        var upgradeSlotCount = 1

        @JvmField
        var spawnChanceOnMob = 0.05f

        @JvmField
        val dropChance = 1f
    }

    class IronBackpackConfig {
        @JvmField
        @Config.RequiresMcRestart
        var inventorySlotCount = 54

        @JvmField
        @Config.RequiresMcRestart
        var upgradeSlotCount = 2

        @JvmField
        var spawnChanceOnMob = 0.04f

        @JvmField
        val dropChance = 1f
    }

    class GoldBackpackConfig {
        @JvmField
        @Config.RequiresMcRestart
        var inventorySlotCount = 81

        @JvmField
        @Config.RequiresMcRestart
        var upgradeSlotCount = 3

        @JvmField
        var spawnChanceOnMob = 0.03f

        @JvmField
        val dropChance = 1f
    }

    class DiamondBackpackConfig {
        @JvmField
        @Config.RequiresMcRestart
        var inventorySlotCount = 108

        @JvmField
        @Config.RequiresMcRestart
        var upgradeSlotCount = 5

        @JvmField
        var spawnChanceOnMob = 0.02f

        @JvmField
        val dropChance = 1f
    }

    class ObsidianBackpackConfig {
        @JvmField
        @Config.RequiresMcRestart
        var inventorySlotCount = 120

        @JvmField
        @Config.RequiresMcRestart
        var upgradeSlotCount = 7

        @JvmField
        var spawnChanceOnMob = 0.01f

        @JvmField
        val dropChance = 1f
    }

    class StackUpgradeConfig {
        @JvmField
        @Config.RequiresMcRestart
        var leatherMultiplier = 2

        @JvmField
        @Config.RequiresMcRestart
        var ironMultiplier = 4

        @JvmField
        @Config.RequiresMcRestart
        var goldMultiplier = 8

        @JvmField
        @Config.RequiresMcRestart
        var diamondMultiplier = 16

        @JvmField
        @Config.RequiresMcRestart
        var obsidianMultiplier = 32

        @JvmField
        var nonStackableItems = arrayOf(
            "minecraft:shulker_box",
            "minecraft:white_shulker_box",
            "minecraft:orange_shulker_box",
            "minecraft:magenta_shulker_box",
            "minecraft:light_blue_shulker_box",
            "minecraft:yellow_shulker_box",
            "minecraft:lime_shulker_box",
            "minecraft:pink_shulker_box",
            "minecraft:gray_shulker_box",
            "minecraft:silver_shulker_box",
            "minecraft:cyan_shulker_box",
            "minecraft:purple_shulker_box",
            "minecraft:blue_shulker_box",
            "minecraft:brown_shulker_box",
            "minecraft:green_shulker_box",
            "minecraft:red_shulker_box",
            "minecraft:black_shulker_box"
        )
    }

    open class FilteredUpgradeConfig(defaultFilterSlots: Int, defaultSlotsInRow: Int) {
        @JvmField
        var filterSlots = defaultFilterSlots

        @JvmField
        var slotsInRow = defaultSlotsInRow
    }

    class MagnetUpgradeConfig(defaultFilterSlots: Int, defaultSlotsInRow: Int, defaultMagnetRange: Int) {
        @JvmField
        var filterSlots = defaultFilterSlots

        @JvmField
        var slotsInRow = defaultSlotsInRow

        @JvmField
        var magnetRange = defaultMagnetRange
    }

    class VoidUpgradeConfig(defaultFilterSlots: Int, defaultSlotsInRow: Int) {
        @JvmField
        var filterSlots = defaultFilterSlots

        @JvmField
        var slotsInRow = defaultSlotsInRow

        @JvmField
        var voidAlwaysEnabled = true
    }

    class TankUpgradeConfig {
        @JvmField
        var capacityPerSlotRow = 4000

        @JvmField
        var stackMultiplierRatio = 1.0

        @JvmField
        var autoFillDrainContainerCooldown = 20

        @JvmField
        var maxInputOutput = 20
    }

    class BatteryUpgradeConfig {
        @JvmField
        var energyPerSlotRow = 10000

        @JvmField
        var stackMultiplierRatio = 1.0

        @JvmField
        var maxInputOutput = 20
    }

    class PumpUpgradeConfig {
        @JvmField
        var filterSlots = 4

        @JvmField
        var maxInputOutput = 20

        @JvmField
        var stackMultiplierRatio = 1.0
    }

    class JukeboxUpgradeConfig(defaultNumberOfSlots: Int) {
        @JvmField
        var numberOfSlots = defaultNumberOfSlots

        @JvmField
        var slotsInRow = 4
    }

    class MobCatcherUpgradeConfig {
        @JvmField
        var basicMaxSlotCost = 18

        @JvmField
        var advancedMaxSlotCost = 72

        @JvmField
        var animalMultiplier = 1.0

        @JvmField
        var hostileMultiplier = 2.0

        @JvmField
        var disallowInventoryEntities = false

        @JvmField
        var entityBlockList = arrayOf("minecraft:wither")

        @JvmField
        var hostileOverrides = arrayOf("minecraft:enderman")

        @JvmField
        var passiveOverrides = arrayOf("minecraft:villager")
    }

    class MaxUpgradesPerStorageConfig {
        @JvmField
        var maxUpgradesPerStorage = arrayOf(
            "stack|3",
            "cooking|1",
            "jukebox|1"
        )
    }
}

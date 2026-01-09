package com.cleanroommc.retrosophisticatedbackpacks.config

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import net.minecraftforge.common.config.Config

@Config(modid = Tags.MOD_ID, name = "${Tags.MOD_ID}_general")
object Config {
    @JvmField
    @Config.Comment("Items that cannot be stored in backpack")
    @Config.RequiresMcRestart
    var blacklistedItems = arrayOf<String>()
    
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

    class LeatherBackpackConfig {
        @JvmField
        @Config.RequiresMcRestart
        var slots = 27

        @JvmField
        @Config.RequiresMcRestart
        var upgradeSlots = 1
    }

    class IronBackpackConfig {
        @JvmField
        @Config.RequiresMcRestart
        var slots = 54

        @JvmField
        @Config.RequiresMcRestart
        var upgradeSlots = 2
    }

    class GoldBackpackConfig {
        @JvmField
        @Config.RequiresMcRestart
        var slots = 81

        @JvmField
        @Config.RequiresMcRestart
        var upgradeSlots = 3
    }

    class DiamondBackpackConfig {
        @JvmField
        @Config.RequiresMcRestart
        var slots = 108

        @JvmField
        @Config.RequiresMcRestart
        var upgradeSlots = 5
    }

    class ObsidianBackpackConfig {
        @JvmField
        @Config.RequiresMcRestart
        var slots = 120

        @JvmField
        @Config.RequiresMcRestart
        var upgradeSlots = 7
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
    }
}

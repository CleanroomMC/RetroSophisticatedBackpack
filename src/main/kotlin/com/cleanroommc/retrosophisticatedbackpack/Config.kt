package com.cleanroommc.retrosophisticatedbackpack

import net.minecraftforge.common.config.Config
import net.minecraftforge.common.config.Config.RequiresMcRestart

@Config(modid = Tags.MOD_ID)
object Config {
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
        @RequiresMcRestart
        var slots = 27

        @JvmField
        @RequiresMcRestart
        var upgradeSlots = 1
    }
    
    class IronBackpackConfig {
        @JvmField
        @RequiresMcRestart
        var slots = 54

        @JvmField
        @RequiresMcRestart
        var upgradeSlots = 2
    }
    
    class GoldBackpackConfig {
        @JvmField
        @RequiresMcRestart
        var slots = 81
        
        @JvmField
        @RequiresMcRestart
        var upgradeSlots = 3
    }
    
    class DiamondBackpackConfig {
        @JvmField
        @RequiresMcRestart
        var slots = 108

        @JvmField
        @RequiresMcRestart
        var upgradeSlots = 5
    }
    
    class ObsidianBackpackConfig {
        @JvmField
        @RequiresMcRestart
        var slots = 120

        @JvmField
        @RequiresMcRestart
        var upgradeSlots = 7
    }

    class StackUpgradeConfig {
        @JvmField
        @RequiresMcRestart
        var leatherMultiplier = 2

        @JvmField
        @RequiresMcRestart
        var ironMultiplier = 4

        @JvmField
        @RequiresMcRestart
        var goldMultiplier = 8

        @JvmField
        @RequiresMcRestart
        var diamondMultiplier = 16

        @JvmField
        @RequiresMcRestart
        var obsidianMultiplier = 32
    }
}
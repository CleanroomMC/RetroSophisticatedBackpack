package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

sealed class UpgradeWrapper<T> : INBTSerializable<NBTTagCompound>, ISidelessCapabilityProvider where T : UpgradeItem

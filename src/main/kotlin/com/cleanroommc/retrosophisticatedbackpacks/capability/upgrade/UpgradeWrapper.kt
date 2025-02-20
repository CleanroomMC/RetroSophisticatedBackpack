package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

sealed class UpgradeWrapper<T> : INBTSerializable<NBTTagCompound> where T : UpgradeItem

package com.cleanroommc.retrosophisticatedbackpack

import com.cleanroommc.retrosophisticatedbackpack.handlers.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpack.items.Items
import com.cleanroommc.retrosophisticatedbackpack.proxy.RSBProxy
import com.cleanroommc.retrosophisticatedbackpack.utils.Utils.asTranslationKey
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent
import org.apache.logging.log4j.LogManager

@Mod(
    modid = Tags.MOD_ID,
    name = Tags.MOD_NAME,
    version = Tags.MOD_ID,
    modLanguageAdapter = "io.github.chaosunity.forgelin.KotlinAdapter",
    dependencies = "required-after:mixinbooter@[8.0,);required-after:forgelin_continuous@[2.0.0.0,)"
)
object RetroSophisticatedBackpacks {
    val LOGGER = LogManager.getLogger(Tags.MOD_NAME)

    @SidedProxy(
        modId = Tags.MOD_ID,
        serverSide = $$"com.cleanroommc.retrosophisticatedbackpack.proxy.RSBProxy$ServerProxy",
        clientSide = $$"com.cleanroommc.retrosophisticatedbackpack.proxy.RSBProxy$ClientProxy"
    )
    lateinit var proxy: RSBProxy

    @Mod.Instance
    lateinit var instance: RetroSophisticatedBackpacks

    val CREATIVE_TAB = object : CreativeTabs("creative_tab".asTranslationKey()) {
        override fun createIcon(): ItemStack =
            ItemStack(Items.backpackLeather)
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        CapabilityHandler.register()
        proxy.preInit(event)
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        proxy.postInit(event)
    }

    @Mod.EventHandler
    fun onShutdown(event: FMLServerStoppedEvent) {
        CapabilityHandler.BACKPACK_INVENTORY_CACHE.clear()
        LOGGER.info("Backpack UUID cache has been cleared")
    }
}
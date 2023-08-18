package tkworld.tools.mythicitemstyrke.itemdata.impl

import ink.ptms.um.Mythic
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.nms.getName
import tkworld.tools.mythicitemstyrke.itemdata.ItemData
import tkworld.tools.mythicitemstyrke.name

object MythicItemData : ItemData {

    override val source: String = "MM"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            registerItem()
        }
    }

    override fun getItemList(): MutableList<String> {
        return Mythic.API.getItemList().map { it.internalName }.toMutableList()
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        return Mythic.API.getItemStack(id)
    }

    override fun getItemId(itemStack: ItemStack): String? {
        return Mythic.API.getItemList().firstOrNull { it.displayName == itemStack.getName() }?.internalName
    }

    override fun getConfig(key: String): taboolib.library.configuration.ConfigurationSection? {
        return Mythic.API.getItem(key)?.config
    }
}

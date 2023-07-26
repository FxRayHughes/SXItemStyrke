package tkworld.tools.mythicitemstyrke.itemdata.impl

import ink.ptms.um.Mythic
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import tkworld.tools.mythicitemstyrke.itemdata.ItemData

object MythicItemData : ItemData {

    override val source: String = "MM"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            registerItem()
        }
    }

    override fun getItemList(): MutableList<ItemStack> {
        return Mythic.API.getItemList().map { it.generateItemStack(1) }.toMutableList()
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        return Mythic.API.getItemStack(id)
    }

    override fun getItemId(itemStack: ItemStack): String? {
        return Mythic.API.getItemId(itemStack)
    }

    override fun getConfig(key: String): taboolib.library.configuration.ConfigurationSection? {
        return Mythic.API.getItem(key)?.config
    }
}

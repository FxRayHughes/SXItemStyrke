package tkworld.tools.mythicitemstyrke.itemdata.impl

import github.saukiya.sxitem.SXItem
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.asMap
import tkworld.tools.mythicitemstyrke.itemdata.ItemData

object SXItemData : ItemData {

    override val source: String = "SI"

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (Bukkit.getPluginManager().getPlugin("SX-Item") != null) {
            registerItem()
        }
    }

    private val itemManager by lazy {
        SXItem.getItemManager()
    }

    override fun getItemList(): MutableList<String> {
        return itemManager.itemList.toMutableList()
    }

    override fun getItem(player: Player, id: String): ItemStack? {
        if (!id.contains(":")) {
            return itemManager.getItem(id, player)
        }
        val args = id.split(":")
        val idz = args[0]
        val arg = args.toList().drop(1)
        return itemManager.getItem(idz, player, *arg.toTypedArray())
    }

    override fun getItemId(itemStack: ItemStack): String? {
        return itemManager.getGenerator(itemStack)?.key
    }

    override fun getConfig(key: String): ConfigurationSection? {
        return Configuration.loadFromString(itemManager.getGenerator(key)?.configString ?: return null)
    }
}

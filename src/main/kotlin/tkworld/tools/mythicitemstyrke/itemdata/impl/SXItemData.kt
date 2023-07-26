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

    override fun getItemList(): MutableList<ItemStack> {
        val player = Bukkit.getOnlinePlayers().first() ?: return mutableListOf()
        return itemManager.itemList.mapNotNull { getItem(player, it) }.toMutableList()
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
        val yamlConfiguration = itemManager.getGenerator(key)?.config as? YamlConfiguration ?: return null
        return Configuration.loadFromOther(yamlConfiguration)
    }
}

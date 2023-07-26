package tkworld.tools.mythicitemstyrke

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import tkworld.tools.mythicitemstyrke.itemdata.ItemData
import tkworld.tools.mythicitemstyrke.itemdata.ItemDataManager

object MythicItemNatur {

    fun getItemData(itemStack: ItemStack): ItemData? {
        if (itemStack.type == Material.AIR || !itemStack.hasItemMeta()) {
            return null
        }
        return ItemDataManager.getData(itemStack)
    }

    fun getItemStack(mythicItem: ItemData, id: String, player: Player): ItemStack {
        return mythicItem.getItem(player, id) ?: error("物品不存在")
    }

    @Config(value = "drops.yml")
    lateinit var dropConfig: ConfigFile
        private set
}

package tkworld.tools.mythicitemstyrke.itemdata

import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.info
import java.util.concurrent.ConcurrentHashMap

object ItemDataManager {

    val items = ConcurrentHashMap<String, ItemData>()

    fun registerItem(item: ItemData) {
        items[item.source] = item
        info("注册物品来源 ${item.source}")
    }

    fun getData(itemStack: ItemStack): ItemData? {
        var itemData: ItemData? = null
        items.forEach { (_, u) ->
            u.getItemId(itemStack)?.let {
                itemData = u
            }
        }
        return itemData
    }

}

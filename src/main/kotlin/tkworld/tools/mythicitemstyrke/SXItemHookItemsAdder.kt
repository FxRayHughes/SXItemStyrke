package tkworld.tools.mythicitemstyrke

import dev.lone.itemsadder.api.CustomStack
import github.saukiya.sxitem.event.SXItemSpawnEvent
import github.saukiya.sxitem.event.SXItemUpdateEvent
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.modifyMeta

object SXItemHookItemsAdder {


    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun itemsAdder(event: SXItemSpawnEvent) {
        val items = event.ig.config?.getString("ItemsAdder") ?: return
        val ia = CustomStack.getInstance(items)?.itemStack ?: return
        val customData = ia.itemMeta?.customModelData ?: return
        val item = event.item ?: return
        item.modifyMeta<ItemMeta> {
            setCustomModelData(customData)
        }
        val old = item.itemMeta?.clone() ?: return
        item.type = ia.type
        item.itemMeta = old
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun itemsAdder(event: SXItemUpdateEvent) {
        val items = event.ig.config?.getString("ItemsAdder") ?: return
        val ia = CustomStack.getInstance(items)?.itemStack ?: return
        val customData = ia.itemMeta?.customModelData ?: return
        val item = event.item ?: return
        item.modifyMeta<ItemMeta> {
            setCustomModelData(customData)
        }
        val old = item.itemMeta?.clone() ?: return
        item.type = ia.type
        item.itemMeta = old
    }

}

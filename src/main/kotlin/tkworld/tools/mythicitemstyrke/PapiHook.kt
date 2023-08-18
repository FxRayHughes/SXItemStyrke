package tkworld.tools.mythicitemstyrke

import org.bukkit.entity.Player
import taboolib.module.nms.getName
import taboolib.platform.compat.PlaceholderExpansion
import tkworld.tools.mythicitemstyrke.itemdata.ItemDataManager

object PapiHook : PlaceholderExpansion {

    override val identifier: String = "mxi"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        val target = player ?: return "null"
        val info = args.split("::")
        // mxi_hasItem_type_物品ID_数量 => boolen
        // mxi_getItem_type_物品ID => amount
        // mxi_getItemName_物品ID => displayname
        val type = info.getOrNull(1) ?: return "null"
        val id = info.getOrNull(2) ?: return "null"
        when (info[0]) {
            "hasItem" -> {
                val amount = info.getOrNull(3)?.toIntOrNull() ?: return "false"
                return ItemDataManager.items[type]?.isMeet(target, id, amount)?.toString() ?: "false"
            }

            "getItem" -> {
                return ItemDataManager.items[type]?.getNumber(target, id)?.toString() ?: "0"
            }

            "getItemName" -> {
                return ItemDataManager.items[type]?.getItem(target, id)?.getName() ?: "null"
            }
        }
        return super.onPlaceholderRequest(player, args)
    }
}

package tkworld.tools.mythicitemstyrke

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.platform.util.takeItem
import tkworld.tools.mythicitemstyrke.itemdata.ItemDataManager

@CommandHeader(name = "itemcommand", aliases = ["ic"], permission = "*")
object MythicItemCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val take = subCommand {
        dynamic("type") {
            suggestion<CommandSender> { sender, context ->
                ItemDataManager.items.keys().toList()
            }
            dynamic("id") {
                suggestion<CommandSender> { sender, context ->
                    ItemDataManager.items[context["type"]]!!.getItemList()
                }
                int("amount") {
                    player("target") {
                        execute<CommandSender> { sender, context, argument ->
                            val player = Bukkit.getPlayer(context.player("target").uniqueId) ?: return@execute
                            val data = ItemDataManager.items[context["type"]] ?: return@execute
                            val id = context["id"]
                            val amount = context.int("amount")
                            player.inventory.takeItem(amount) {
                                data.getItemId(it) == id
                            }
                        }
                    }
                    execute<Player> { sender, context, argument ->
                        val data = ItemDataManager.items[context["type"]] ?: return@execute
                        val id = context["id"]
                        val amount = context.int("amount")
                        sender.inventory.takeItem(amount) {
                            data.getItemId(it) == id
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val run = subCommand {
        dynamic("type") {
            dynamic("id") {
                player("target") {
                    execute<CommandSender> { sender, context, argument ->
                        val data = ItemDataManager.items[context["type"]] ?: return@execute
                        val action =
                            data.getConfig(context["id"])?.getStringList("Styrke.action.onCommand") ?: return@execute
                        val player = Bukkit.getPlayer(context.player("target").uniqueId) ?: return@execute
                        action.ketherEval(player)
                    }
                }
                execute<Player> { sender, context, argument ->
                    val data = ItemDataManager.items[context["type"]] ?: return@execute
                    val action =
                        data.getConfig(context["id"])?.getStringList("Styrke.action.onCommand") ?: return@execute
                    action.ketherEval(sender)
                }
            }
        }
    }

}

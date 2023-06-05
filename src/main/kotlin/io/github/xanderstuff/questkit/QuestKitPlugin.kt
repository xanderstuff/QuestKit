package io.github.xanderstuff.questkit

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import org.bukkit.plugin.java.JavaPlugin

class QuestKitPlugin : JavaPlugin() {

    companion object {
        lateinit var instance: QuestKitPlugin
    }

    override fun onEnable() {
        instance = this

        // ensure config file exists
        saveDefaultConfig()

        // setup commands
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).verboseOutput(true))
        registerCommands()

        // load scripts
        reloadAllScripts()

        @Suppress("DEPRECATION")
        log("${this.name} version ${description.version} enabled!")
    }

    override fun onDisable() {
        log("${this.name} disabled.")
    }

    private fun registerCommands() {
        CommandAPICommand("reloadScripts").executes(
            CommandExecutor { sender, _ ->
                val (numScriptsLoaded, numScriptsAvailable) = reloadAllScripts()
                if (numScriptsLoaded == numScriptsAvailable) {
                    sender.sendMessage(
                        Component.text(
                            "$numScriptsLoaded scripts successfully loaded!",
                            Style.style(NamedTextColor.GREEN)
                        )
                    )
                } else {
                    sender.sendMessage(
                        Component.text(
                            "$numScriptsLoaded scripts loaded, ${numScriptsAvailable - numScriptsLoaded} scripts failed to load. Check the log for details.",
                            Style.style(NamedTextColor.RED)
                        )
                    )
                }
            }
        ).register()
    }
}

package io.github.xanderstuff.questkit

import org.bukkit.plugin.java.JavaPlugin

class QuestKitPlugin : JavaPlugin() {

    companion object {
        lateinit var instance: QuestKitPlugin
    }

    override fun onEnable() {
        instance = this
        // ensure config file exists
        saveDefaultConfig()

        logger.info("${description.name} version ${description.version} enabled!")
        reloadAllScripts()
    }

    override fun onDisable() {
        logger.info("${description.name} disabled.")
    }
}

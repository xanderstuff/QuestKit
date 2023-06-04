package io.github.xanderstuff.questkit

import java.util.logging.Level

fun <T> log(message: T) {
    QuestKitPlugin.instance.logger.info(message.toString())
}

fun <T> error(message: T) {
    QuestKitPlugin.instance.logger.log(Level.SEVERE, message.toString())
}
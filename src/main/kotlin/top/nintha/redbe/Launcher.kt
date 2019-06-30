package top.nintha.redbe

import io.vertx.core.Vertx
import javafx.application.Application
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.nintha.redbe.view.MainScreen
import tornadofx.*

fun main(args: Array<String>) {
    Application.launch(MainApp::class.java)
}

class MainApp : App(MainScreen::class)

fun Any.logger(): Logger {
    return LoggerFactory.getLogger(this::class.java)
}

object REDBE {
    const val PROJECT_NAME = "redbe"
    val vertx: Vertx = Vertx.vertx()
}
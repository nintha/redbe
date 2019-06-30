package top.nintha.redbe.view

import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.scene.control.Alert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import top.nintha.redbe.REDBE
import top.nintha.redbe.controller.ConnectionController
import top.nintha.redbe.logger
import tornadofx.*
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

class ConnectorModel : View(REDBE.PROJECT_NAME.toUpperCase()), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = REDBE.vertx.dispatcher()

    private val controller: ConnectionController by inject()
    private val hostname by SimpleStringProperty("192.168.189.128")
    private val password by SimpleStringProperty("")

    init {
        primaryStage.onCloseRequest = EventHandler {
            this.logger().info("${REDBE.PROJECT_NAME} exit.")
            exitProcess(0)
        }
        primaryStage.isResizable = true
    }

    override val root = borderpane {
        center = vbox {
            form {
                fieldset {
                    field("hostname") { textfield(hostname) }
                    field("password") { textfield(password) }

                }
            }

            hbox {
                button("connect") {
                    action {
                        if (hostname.isBlank()) {
                            alert(Alert.AlertType.ERROR, "Hostname must not be blank")
                            return@action
                        }
                    }
                }

                button("disconnect") {
                    action {
                        controller.disconnect(hostname)
                    }
                }
                button("Test connection") {
                    action {
                        checkConnection(hostname, password)
                    }
                }
            }
        }
    }



    private fun checkConnection(hostname: String, password: String) {
        if (hostname.isBlank()) {
            alert(Alert.AlertType.ERROR, "Hostname must not be blank")
            return
        }

        val options = RedisOptions().setHost(hostname).setSelect(1)
        if (password.isNotBlank()) {
            options.auth = password
        }

        val client = RedisClient.create(REDBE.vertx, options)
        val future = Future.future<JsonObject>()
        try {
            client.info(future)
            val info: JsonObject = runBlocking { future.await() }
            logger().info("[checkConnection] info > $info")
            alert(Alert.AlertType.INFORMATION, "Test pass")
        } catch (e: Exception) {
            logger().error("[checkConnection] info error", e)
            alert(Alert.AlertType.ERROR, "Test failed", e.message)
        } finally {
            client.close {}
        }
    }

}



package top.nintha.redbe.view

import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.kotlin.coroutines.await
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.scene.control.TreeItem
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import kotlinx.coroutines.runBlocking
import top.nintha.redbe.REDBE
import top.nintha.redbe.logger
import tornadofx.*
import kotlin.system.exitProcess

class MainScreen : View(REDBE.PROJECT_NAME.toUpperCase()) {
    private val client: RedisClient

    private val keys: MutableMap<String, MutableList<KeyNode>> = mutableMapOf()
    private val keyRoot = KeyNode("", "", "", listOf(), KeyKind.Normal)

    private val selectedKeyProp = SimpleStringProperty("")

    init {
        primaryStage.onCloseRequest = EventHandler {
            this.logger().info("${REDBE.PROJECT_NAME} exit.")
            exitProcess(0)
        }
        primaryStage.isResizable = true

        val options = RedisOptions().setHost("192.168.189.128").setSelect(1)
        client = RedisClient.create(REDBE.vertx, options)
        loadKeys()
    }

    override val root = borderpane {
        left = vbox {
            paddingTop = 10
            button("reload key") {
                action { loadKeys() }

                vboxConstraints {
                    marginLeft = 10.0
                    marginBottom = 10.0
                    vGrow = Priority.ALWAYS
                }
            }



            treeview<KeyNode>(TreeItem(keyRoot)) {
                root.isExpanded = true

                cellFormat { text = if (it.fullName.isBlank()) "database" else it.name }

                onUserSelect {
                    if (it.kind == KeyKind.Normal) return@onUserSelect

                    selectedKeyProp.value = it.fullName
                    logger().info("select key > ${it.fullName}")
                }

                populate { parent ->
                    when (parent.value.kind) {
                        KeyKind.Data -> listOf()
                        KeyKind.Normal -> keys[parent.value.fullName]
                    }
                }
            }
        }

        center = vbox {
            minWidth = 500.0
            paddingLeft = 10

            text(selectedKeyProp) {
                font = Font(20.0)

                vboxConstraints {
                    marginLeft = 10.0
                    marginTop = 10.0
                    vGrow = Priority.ALWAYS
                }
            }

        }
    }

    private fun buildKeyNode(key: String): Set<KeyNode> {
        val nodes = mutableSetOf<KeyNode>()

        fun build(fullName: String) {
            val fullPath = fullName.split(":").filter { it.isNotBlank() }
            val name = fullPath.last()
            val prefix = fullPath.subList(0, fullPath.size - 1).joinToString(":")
            nodes.add(KeyNode(name, prefix, fullName, fullPath, if (fullName == key) KeyKind.Data else KeyKind.Normal))
            if (prefix.isNotBlank()) build(prefix)
        }
        build(key)
        return nodes
    }

    private fun loadKeys() {
        keys.values.forEach { it.clear() }

        val future = Future.future<JsonArray>()
        client.keys("*", future)
        runBlocking { future.await() }.toList().flatMap { buildKeyNode(it.toString()) }.toSet().forEach { node ->
            keys.putIfAbsent(node.prefix, if (node.prefix == keyRoot.prefix) observableList() else mutableListOf())
            keys[node.prefix]?.add(node)
        }
        keys[keyRoot.prefix]?.sortBy { it.name }
        logger().info("load keys > ${keys.values.flatten().size}")
    }

    private fun stringValue(key: String): String {
        val future = Future.future<String>()
        client.get(key, future)
        return runBlocking { future.await() }
    }


}

enum class KeyKind {
    Data,
    Normal
}

data class KeyNode(val name: String, val prefix: String, val fullName: String, val fullPath: List<String>, val kind: KeyKind)





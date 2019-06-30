package top.nintha.redbe.controller

import io.vertx.core.Future
import io.vertx.kotlin.coroutines.await
import top.nintha.redbe.REDBE
import top.nintha.redbe.logger
import top.nintha.redbe.verticle.RedisVerticle
import tornadofx.*

class ConnectionController : Controller() {
    private val connectionMap = mutableMapOf<String, String>()

    init {
        logger().info("ConnectionController init, hashcode=${this.hashCode()}")
    }

    suspend fun connect(hostname: String, password: String): String {
        if (connectionMap.containsKey(hostname)){
            logger().warn("the hostname=$hostname has already connected")
            return ""
        }

        val future = Future.future<String>()
        REDBE.vertx.deployVerticle(RedisVerticle(hostname, password), future)
        val verticleId = future.await()
        connectionMap[hostname] = verticleId
        return verticleId
    }

    fun disconnect(hostname: String) {
        if (!connectionMap.containsKey(hostname)){
            logger().warn("not found connection of hostname=$hostname")
            return
        }

        REDBE.vertx.undeploy(connectionMap[hostname]){
            if(it.succeeded()){
                connectionMap.remove(hostname)
            }else{
                logger().error("disconnect error", it.cause())
            }
        }
    }
}
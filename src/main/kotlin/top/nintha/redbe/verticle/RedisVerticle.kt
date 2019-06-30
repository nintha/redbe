package top.nintha.redbe.verticle

import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions
import top.nintha.redbe.logger

class RedisVerticle(private val hostname: String, private val password: String) : CoroutineVerticle() {

    override suspend fun start() {
        val options = RedisOptions().setHost(hostname).setSelect(1)
        if(password.isNotBlank()){
            options.auth = password
        }

        val client = RedisClient.create(vertx, options)
        try {
            val future = Future.future<JsonArray>()
            client.keys("*", future)
            val keys = future.await().toList().map { it.toString() }
            logger().info("$hostname > fetch keys: $keys")
        } catch (e: Exception) {
            logger().error("$hostname > fetch keys error", e)
        }

        logger().info("[RedisVerticle] starting, hostname=$hostname, password=$password, verticleId=$deploymentID")
    }

    override suspend fun stop() {
        logger().info("[RedisVerticle] stopping, hostname=$hostname, verticleId=$deploymentID")
    }
}
package org.edla.netty.example.uptime

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.channel.{ChannelPipeline, ChannelPipelineFactory, Channels}
import org.jboss.netty.handler.timeout.ReadTimeoutHandler
import org.jboss.netty.util.{HashedWheelTimer, Timer}

/**
  * Connects to a server periodically to measure and print the uptime of the
  * server.  This example demonstrates how to implement reliable reconnection
  * mechanism in Netty.
  */
object UptimeClient {

  // Sleep 5 seconds before a reconnection attempt.
  val RECONNECT_DELAY = 5

  // Reconnect when the server sends nothing for 10 seconds.
  private val READ_TIMEOUT = 10

  def main(args: Array[String]): Unit = {
    // Print usage if no argument is specified.
    if (args.length != 2) {
      System.err.println(
        "Usage: " + UptimeClient.getClass.getSimpleName +
          " <host> <port>"
      )
      return
    }

    // Parse options.
    val host = args(0)
    val port = args(1).toInt

    // Initialize the timer that schedules subsequent reconnection attempts.
    val timer: Timer = new HashedWheelTimer

    // Configure the client.
    val bootstrap = new ClientBootstrap(
      new NioClientSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool)
    )

    // Set up the pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory {

      private val timeoutHandler = new ReadTimeoutHandler(timer, READ_TIMEOUT)
      private val uptimeHandler  = new UptimeClientHandler(bootstrap, timer)

      //@throws(classOf[java.lang.Exception])
      override def getPipeline: ChannelPipeline = Channels.pipeline(timeoutHandler, uptimeHandler)
    })

    bootstrap.setOption("remoteAddress", new InetSocketAddress(host, port))

    // Initiate the first connection attempt - the rest is handled by
    // UptimeClientHandler.
    bootstrap.connect
  }
}

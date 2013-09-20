package org.edla.netty.example.factorial

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory

/**
 * Receives a sequence of integers from a FactorialClient to calculate
 * the factorial of the specified integer.
 */
object FactorialServer {

  def main(args: Array[String]) {
    // Configure the server.
    val bootstrap = new ServerBootstrap(
      new NioServerSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool))

    // Set up the event pipeline factory.
    bootstrap.setPipelineFactory(new FactorialServerPipelineFactory)

    // Bind and start to accept incoming connections.
    bootstrap.bind(new InetSocketAddress(8080))
  }
}

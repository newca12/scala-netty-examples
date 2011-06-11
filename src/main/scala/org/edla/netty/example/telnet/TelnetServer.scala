package org.edla.netty.example.telnet

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory

/**
 * Simplistic telnet server.
 */
object TelnetServer {

  def main(args: Array[String]) {
    // Configure the server.
    val bootstrap = new ServerBootstrap(
      new NioServerSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool))

    // Configure the pipeline factory.
    bootstrap.setPipelineFactory(new TelnetServerPipelineFactory)

    // Bind and start to accept incoming connections.
    bootstrap.bind(new InetSocketAddress(8080))
  }
}

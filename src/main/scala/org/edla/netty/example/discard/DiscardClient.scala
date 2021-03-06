package org.edla.netty.example.discard

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.channel.{ChannelPipeline, ChannelPipelineFactory, Channels}

/**
  * Keeps sending random data to the specified address.
  */
object DiscardClient {

  def main(args: Array[String]): Unit = {
    // Print usage if no argument is specified.
    if (args.length < 2 || args.length > 3) {
      System.err.println(
        "Usage: " + DiscardClient.getClass.getSimpleName +
          " <host> <port> [<first message size>]"
      )
      return
    }

    // Parse options.
    val host                  = args(0)
    val port                  = args(1).toInt
    var firstMessageSize: Int = 0
    if (args.length == 3) firstMessageSize = args(2).toInt
    else firstMessageSize = 256

    // Configure the server.
    val bootstrap = new ClientBootstrap(
      new NioClientSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool)
    )

    // Set up the pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory {
      override def getPipeline: ChannelPipeline = Channels.pipeline(new DiscardClientHandler(firstMessageSize))
    })

    // Start the connection attempt.
    val future = bootstrap.connect(new InetSocketAddress(host, port))

    // Wait until the connection is closed or the connection attempt fails.
    future.getChannel.getCloseFuture.awaitUninterruptibly

    // Shut down thread pools to exit.
    bootstrap.releaseExternalResources()
  }
}

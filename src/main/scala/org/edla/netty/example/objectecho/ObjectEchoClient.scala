package org.edla.netty.example.objectecho

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.channel.{ ChannelFuture, ChannelPipeline, ChannelPipelineFactory, Channels }
import org.jboss.netty.handler.codec.serialization.{ ObjectDecoder, ObjectEncoder }

/**
 * Modification of EchoClient which utilizes Java object serialization.
 */
object ObjectEchoClient {

  def main(args: Array[String]) {
    // Print usage if no argument is specified.
    if (args.length < 2 || args.length > 3) {
      System.err.println(
        "Usage: " + ObjectEchoClient.getClass.getSimpleName +
          " <host> <port> [<first message size>]")
      return
    }

    // Parse options.
    val host = args(0)
    val port = args(1).toInt
    var firstMessageSize: Int = 0
    if (args.length == 3) firstMessageSize = args(2).toInt
    else firstMessageSize = 256

    // Configure the client.
    val bootstrap = new ClientBootstrap(
      new NioClientSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool))

    // Set up the pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory {
      override def getPipeline = Channels.pipeline(
        new ObjectEncoder,
        new ObjectDecoder,
        new ObjectEchoClientHandler(firstMessageSize))
    })

    // Start the connection attempt.
    bootstrap.connect(new InetSocketAddress(host, port))
  }
}

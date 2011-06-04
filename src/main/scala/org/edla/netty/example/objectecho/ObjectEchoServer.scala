package org.edla.netty.example.objectecho

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.jboss.netty.channel.{ChannelPipeline, ChannelPipelineFactory, Channels}
import org.jboss.netty.handler.codec.serialization.{ObjectDecoder, ObjectEncoder}

/**
 * Modification of EchoServer which utilizes Java object serialization.
 */
object ObjectEchoServer {

  def main(args: Array[String]): Unit = {
    // Configure the server.
    val bootstrap = new ServerBootstrap(
      new NioServerSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool))

    // Configure the pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      override def getPipeline: ChannelPipeline = {
        Channels.pipeline(
          new ObjectEncoder,
          new ObjectDecoder,
          new ObjectEchoServerHandler)
      }
    })
    // Bind and start to accept incoming connections.
    bootstrap.bind(new InetSocketAddress(8080))
  }
}

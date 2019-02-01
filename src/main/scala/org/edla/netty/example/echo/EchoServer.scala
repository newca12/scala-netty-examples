package org.edla.netty.example.echo

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.{ChannelPipeline, ChannelPipelineFactory, Channels}
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory

/**
  * Echoes back any received data from a client.
  */
object EchoServer {

  def main(args: Array[String]): Unit = {
    // Configure the server.
    val bootstrap = new ServerBootstrap(
      new NioServerSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool)
    )
    // Set up the pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory {
      override def getPipeline: ChannelPipeline = Channels.pipeline(new EchoServerHandler)
    })

    // Bind and start to accept incoming connections.
    bootstrap.bind(new InetSocketAddress(8080))
  }
}

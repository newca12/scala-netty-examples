package org.edla.netty.example.discard

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

/**
 * Discards any incoming data.
 */
class DiscardServer(port: Int) {

  //@throws 
  def run() {
    val bossGroup = new NioEventLoopGroup
    val workerGroup = new NioEventLoopGroup
    try {
      val b = new ServerBootstrap
      b.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .childHandler(new ChannelInitializer[SocketChannel]() {
          @throws override def initChannel(ch: SocketChannel) {
            ch.pipeline.addLast(new DiscardServerHandler);
          }
        })
      // Bind and start to accept incoming connections.
      val f = b.bind(port).sync

      // Wait until the server socket is closed.
      // In this example, this does not happen, but you can do that to gracefully
      // shut down your server.
      f.channel.closeFuture.sync
    } finally {
      workerGroup.shutdownGracefully()
      bossGroup.shutdownGracefully()
    }
  }
}

object DiscardServer {
  //@throws 
  def main(args: Array[String]) {
    println("start")
    var port: Int = 0
    if (args.length > 0) {
      port = Integer.parseInt(args(0))
    } else {
      port = 8080
    }
    new DiscardServer(port).run
  }

}

package org.edla.netty.example.discard

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel

/**
 * Keeps sending random data to the specified address.
 */
class DiscardClient(host: String, port: Int, firstMessageSize: Int) {

  //@throws 
  def run {
    val group = new NioEventLoopGroup
    try {
      val b = new Bootstrap
      b.group(group)
        .channel(classOf[NioSocketChannel])
        .handler(new DiscardClientHandler(firstMessageSize))

      // Make the connection attempt.
      val f = b.connect(host, port).sync;

      // Wait until the connection is closed.
      f.channel.closeFuture.sync
    } finally {
      group.shutdownGracefully()
    }
  }
}

object DiscardClient {
  //@throws 
  def main(args: Array[String]) {
    require(!(args.length < 2 || args.length > 3),
      s"Usage:  ${DiscardClient.getClass.getSimpleName} <host> <port> [<first message size>]")

    // Parse options.
    val host = args(0)
    val port = args(1).toInt
    var firstMessageSize: Int = 0
    if (args.length == 3) firstMessageSize = args(2).toInt
    else firstMessageSize = 256

    new DiscardClient(host, port, firstMessageSize).run
  }

}

package org.edla.netty.example.discard

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Handles a client-side channel.
 */
class DiscardClientHandler(messageSize: Int) extends SimpleChannelInboundHandler[Object] {

  require(messageSize > 0)

  private val logger = Logger.getLogger(getClass.getName)

  private var content: ByteBuf = _

  private var ctx: ChannelHandlerContext = _

  @throws override def channelActive(ctx: ChannelHandlerContext) {
    this.ctx = ctx

    // Initialize the message.
    content = ctx.alloc.directBuffer(messageSize).writeZero(messageSize)

    // Send the initial messages.
    generateTraffic
  }

  @throws override def channelInactive(ctx: ChannelHandlerContext) = content.release

  @throws override def channelRead0(ctx: ChannelHandlerContext, msg: Object) = ()

  @throws override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    // Close the connection when an exception is raised.
    logger.log(
      Level.WARNING,
      "Unexpected exception from downstream.",
      cause)
    ctx.close()

  }

  private def generateTraffic {
    // Flush the outbound buffer to the socket.
    // Once flushed, generate the same amount of traffic again.
    ctx.writeAndFlush(content.duplicate.retain).addListener(trafficGenerator);
  }

  private final def trafficGenerator: ChannelFutureListener = new ChannelFutureListener() {
    @throws override def operationComplete(future: ChannelFuture) {
      if (future.isSuccess) {
        generateTraffic
      }
    }
  }

}

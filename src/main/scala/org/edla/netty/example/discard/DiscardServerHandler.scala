package org.edla.netty.example.discard

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles a server-side channel.
 */
class DiscardServerHandler extends SimpleChannelInboundHandler[Object] {

  val logger = Logger.getLogger(getClass.getName)

  @throws override def channelRead0(ctx: ChannelHandlerContext, msg: Object) = ()

  @throws override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    // Close the connection when an exception is raised.
    logger.log(
      Level.WARNING,
      "Unexpected exception from downstream.",
      cause)
    ctx.close()

  }

}

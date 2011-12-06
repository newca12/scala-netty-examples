package org.edla.netty.example.discard

import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.channel.{
  ChannelEvent,
  ChannelHandlerContext,
  ChannelStateEvent,
  ExceptionEvent,
  MessageEvent,
  SimpleChannelUpstreamHandler
}
import java.util.logging.Logger

/**
 * Handles a server-side channel.
 */
class DiscardServerHandler extends SimpleChannelUpstreamHandler {

  val logger = Logger.getLogger(getClass.getName)

  var transferredBytes = 0L

  def getTransferredBytes = transferredBytes

  override def handleUpstream(ctx: ChannelHandlerContext, e: ChannelEvent) {
    e match {
      case c: ChannelStateEvent => logger.info(e.toString)
      case _ => None
    }
    super.handleUpstream(ctx, e)
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    // Discard received data silently by doing nothing.
    transferredBytes += ((e.getMessage match {
      case c: ChannelBuffer => c
      case _ => throw new ClassCastException
    }) readableBytes)
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent) {
    // Close the connection when an exception is raised.
    logger.warning("Unexpected exception from downstream." + e.getCause)
    e.getChannel.close
  }

}

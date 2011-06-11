package org.edla.netty.example.echo

import java.util.concurrent.atomic.AtomicLong
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
 * Handler implementation for the echo server.
 */
class EchoServerHandler extends SimpleChannelUpstreamHandler {

  val logger = Logger.getLogger(getClass.getName)

  val transferredBytes = new AtomicLong

  def getTransferredBytes = transferredBytes.get

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    // Send back the received message to the remote peer.
    transferredBytes.addAndGet((e.getMessage match {
      case c: ChannelBuffer => c
      case _ => throw new ClassCastException
    }) readableBytes)
    e.getChannel.write(e.getMessage)
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent) {
    // Close the connection when an exception is raised.
    logger.warning("Unexpected exception from downstream." + e.getCause)
    e.getChannel.close
  }

}

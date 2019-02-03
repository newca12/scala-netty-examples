package org.edla.netty.example.telnet

import java.util.logging.Logger

import org.jboss.netty.channel._

/**
  * Handles a client-side channel.
  */
class TelnetClientHandler extends SimpleChannelUpstreamHandler {

  private val logger = Logger.getLogger(getClass.getName)

  override def handleUpstream(ctx: ChannelHandlerContext, e: ChannelEvent): Unit = {
    e match {
      case _: ChannelStateEvent => logger.info(e.toString)
      case _                    => None
    }
    super.handleUpstream(ctx, e)
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent): Unit = {
    System.err.println(e.getMessage)
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent): Unit = {
    // Close the connection when an exception is raised.
    logger.warning("Unexpected exception from downstream." + e.getCause)
    e.getChannel.close()
  }
}

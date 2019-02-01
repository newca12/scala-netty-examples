package org.edla.netty.example.objectecho

import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Logger

import org.jboss.netty.channel._

/**
  * Handles both client-side and server-side handler depending on which
  * constructor was called.
  */
class ObjectEchoServerHandler extends SimpleChannelUpstreamHandler {

  val logger: Logger = Logger.getLogger(getClass.getName)

  private val transferredMessages = new AtomicLong

  def getTransferredMessages: Long = transferredMessages.get

  override def handleUpstream(ctx: ChannelHandlerContext, e: ChannelEvent): Unit = {
    e match {
      case c: ChannelStateEvent ⇒ if (c.getState != ChannelState.INTEREST_OPS) logger.info(e.toString)
      case _                    ⇒ None
    }
    super.handleUpstream(ctx, e)
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent): Unit = {
    // Echo back the received object to the client.
    transferredMessages.incrementAndGet
    e.getChannel.write(e.getMessage)
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent): Unit = {
    // Close the connection when an exception is raised.
    logger.warning("Unexpected exception from downstream." + e.getCause)
    e.getChannel.close()
  }

}

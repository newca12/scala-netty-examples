package org.edla.netty.example.echo

import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Logger

import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.channel.{ChannelHandlerContext, ExceptionEvent, MessageEvent, SimpleChannelUpstreamHandler}

/**
  * Handler implementation for the echo server.
  */
class EchoServerHandler extends SimpleChannelUpstreamHandler {

  val logger: Logger = Logger.getLogger(getClass.getName)

  val transferredBytes = new AtomicLong

  def getTransferredBytes: Long = transferredBytes.get

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent): Unit = {
    // Send back the received message to the remote peer.
    transferredBytes.addAndGet((e.getMessage match {
      case c: ChannelBuffer ⇒ c
      case _                ⇒ throw new ClassCastException
    }) readableBytes)
    e.getChannel.write(e.getMessage)
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent): Unit = {
    // Close the connection when an exception is raised.
    logger.warning("Unexpected exception from downstream." + e.getCause)
    e.getChannel.close()
  }

}

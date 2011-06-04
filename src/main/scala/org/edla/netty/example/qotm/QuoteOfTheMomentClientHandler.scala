package org.edla.netty.example.qotm

import org.jboss.netty.channel.{ ChannelHandlerContext, ExceptionEvent, MessageEvent, SimpleChannelUpstreamHandler }

/**
 * Handles a client-side channel.
 */
class QuoteOfTheMomentClientHandler extends SimpleChannelUpstreamHandler {

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent): Unit = {
    val msg = e.getMessage.toString
    if (msg.startsWith("QOTM: ")) {
      System.out.println("Quote of the Moment: " + msg.substring(6))
      e.getChannel.close
    }
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent): Unit = {
    e.getCause.printStackTrace
    e.getChannel.close
  }
}

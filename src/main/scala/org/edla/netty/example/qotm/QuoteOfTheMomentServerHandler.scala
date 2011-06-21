package org.edla.netty.example.qotm

import java.util.Random
import org.jboss.netty.channel.{ ChannelHandlerContext, ExceptionEvent, MessageEvent, SimpleChannelUpstreamHandler }

/**
 * Handles a server-side channel.
 */
class QuoteOfTheMomentServerHandler extends SimpleChannelUpstreamHandler {

  private val random = new Random

  private val quotes = Array(
    "Where there is love there is life.",
    "First they ignore you, then they laugh at you, then they fight you, then you win.",
    "Be the change you want to see in the world.",
    "The weak can never forgive. Forgiveness is the attribute of the strong.")

  def nextQuote(): String = {
    var quoteId: Int = 0
    synchronized {
      quoteId = random.nextInt(quotes.length)
    }
    quotes(quoteId)
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    val msg = e.getMessage.toString
    if (msg.equals("QOTM?")) e.getChannel.write("QOTM: " + nextQuote, e.getRemoteAddress)
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent) =
    e.getCause.printStackTrace
  // We don't close the channel because we can keep serving requests.
}

package org.edla.netty.example.factorial

import org.jboss.netty.channel.{
  ChannelEvent,
  ChannelHandlerContext,
  ChannelStateEvent,
  ExceptionEvent,
  MessageEvent,
  SimpleChannelUpstreamHandler
}
import java.util.logging.Logger
import java.math.BigInteger
import java.util.Formatter

/**
 * Handler for a server-side channel.  This handler maintains stateful
 * information which is specific to a certain channel using member variables.
 * Therefore, an instance of this handler can cover only one channel.  You have
 * to create a new handler instance whenever you create a new channel and insert
 * this handler  to avoid a race condition.
 */
class FactorialServerHandler extends SimpleChannelUpstreamHandler {

  val logger = Logger.getLogger(getClass.getName)

  private var lastMultiplier = 1
  //var factorial = new BigInteger(new Array[Byte](1))
  private var factorial = BigInteger.ONE

  override def handleUpstream(ctx: ChannelHandlerContext, e: ChannelEvent) {
    e match {
      case c: ChannelStateEvent => logger.info(e.toString)
      case _ => None
    }
    super.handleUpstream(ctx, e)
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {

    // Calculate the cumulative factorial and send it to the client.
    var number = BigInteger.ONE
    e.getMessage match {
      case m: BigInteger => number = (e.getMessage).asInstanceOf[BigInteger]
      case _ => number = new BigInteger(e.getMessage.toString)
    }
    lastMultiplier = number.intValue
    factorial = factorial.multiply(number)
    e.getChannel.write(factorial)
  }

  override def channelDisconnected(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
    System.err.format(
      "Factorial of %s is: %s", lastMultiplier.toString, factorial.toString)
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent) {
    // Close the connection when an exception is raised.
    logger.warning("Unexpected exception from downstream." + e.getCause)
    e.getChannel.close
  }

}

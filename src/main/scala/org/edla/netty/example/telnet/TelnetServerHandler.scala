package org.edla.netty.example.telnet

import java.util.concurrent.atomic.AtomicLong
import org.jboss.netty.buffer.{ ChannelBuffer, ChannelBuffers }
import org.jboss.netty.channel.{
  Channel,
  ChannelEvent,
  ChannelHandlerContext,
  ChannelState,
  ChannelStateEvent,
  ExceptionEvent,
  MessageEvent,
  SimpleChannelUpstreamHandler
}
import java.util.logging.Logger
import java.net.InetAddress
import java.util.Date
import org.jboss.netty.channel.ChannelFutureListener

/**
 * Handles a server-side channel.
 */
class TelnetServerHandler extends SimpleChannelUpstreamHandler {

  private val logger = Logger.getLogger(getClass.getName)

  override def handleUpstream(ctx: ChannelHandlerContext, e: ChannelEvent) {
    e match {
      case c: ChannelStateEvent => logger.info(e.toString)
    }
    super.handleUpstream(ctx, e)
  }

  override def channelConnected(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
    // Send greeting for a new connection.
    e.getChannel.write(
      "Welcome to " + InetAddress.getLocalHost.getHostName + "!\r\n")
    e.getChannel.write("It is " + new Date + " now.\r\n")
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {

        // Cast to a String first.
        // We know it is a String because we put some codec in TelnetPipelineFactory.
        val request = e.getMessage.toString

        // Generate and write a response.
        var response: String = ""
        var close: Boolean = false
        if (request.length == 0) {
            response = "Please type something.\r\n"
        } else if (request.toLowerCase.equals("bye")) {
            response = "Have a good day!\r\n"
            close = true
        } else {
            response = "Did you say '" + request + "'?\r\n"
        }

        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
        val future = e.getChannel.write(response)

        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'bye'.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE)
        }
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent) {
    // Close the connection when an exception is raised.
    logger.warning("Unexpected exception from downstream." + e.getCause)
    e.getChannel.close
  }
}

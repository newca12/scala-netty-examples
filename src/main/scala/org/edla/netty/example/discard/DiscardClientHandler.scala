package org.edla.netty.example.discard

import java.util.logging.Logger

import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.channel.ChannelEvent
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ChannelState
import org.jboss.netty.channel.ChannelStateEvent
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.jboss.netty.channel.WriteCompletionEvent

/**
  * Handles a client-side channel.
  */
class DiscardClientHandler(messageSize: Int) extends SimpleChannelUpstreamHandler {

  require(messageSize > 0)

  private val logger = Logger.getLogger(getClass.getName)

  val content = new Array[Byte](messageSize)

  private var transferredBytes = 0L

  def getTransferredBytes: Long = transferredBytes

  override def handleUpstream(ctx: ChannelHandlerContext, e: ChannelEvent): Unit = {
    e match {
      case c: ChannelStateEvent => if (c.getState != ChannelState.INTEREST_OPS) logger.info(e.toString)
      case _                    =>
    }

    // Let SimpleChannelHandler call actual event handler methods below.
    super.handleUpstream(ctx, e)
  }

  // Send the initial messages.
  override def channelConnected(ctx: ChannelHandlerContext, e: ChannelStateEvent): Unit = { generateTraffic(e) }

  // Keep sending messages whenever the current socket buffer has room.
  override def channelInterestChanged(ctx: ChannelHandlerContext, e: ChannelStateEvent): Unit = { generateTraffic(e) }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent): Unit = {
    // Server is supposed to send nothing.  Therefore, do nothing.
  }

  override def writeComplete(ctx: ChannelHandlerContext, e: WriteCompletionEvent): Unit = {
    transferredBytes += e.getWrittenAmount
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent): Unit = {
    // Close the connection when an exception is raised.
    logger.warning("Unexpected exception from downstream." + e.getCause)
    e.getChannel.close()
  }

  private def generateTraffic(e: ChannelStateEvent): Unit = {
    // Keep generating traffic until the channel is unwritable.
    // A channel becomes unwritable when its internal buffer is full.
    // If you keep writing messages ignoring this property,
    // you will end up with an OutOfMemoryError.
    val channel = e.getChannel
    //TODO rewrite the loop as a recursive function. (Refer to Programming in Scala, 7.6 Living without break and continue)
    breakable {
      while (channel.isWritable) {
        val m: ChannelBuffer = nextMessage()
        if (m == null) {
          break
        }
        channel.write(m)
      }
    }
  }

  private def nextMessage(): ChannelBuffer = ChannelBuffers.wrappedBuffer(content)

}

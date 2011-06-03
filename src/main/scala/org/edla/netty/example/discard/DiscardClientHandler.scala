package org.edla.netty.example.discard

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
  SimpleChannelUpstreamHandler,
  WriteCompletionEvent
}
import java.util.logging.Logger
import util.control.Breaks._

class DiscardClientHandler(messageSize: Int) extends SimpleChannelUpstreamHandler {

  require(messageSize > 0)

  private val logger = Logger.getLogger(getClass.getName)

  var content = new Array[Byte](messageSize);

  private val transferredBytes = new AtomicLong

  def getTransferredBytes: Long = {
    transferredBytes.get
  }

  override def handleUpstream(ctx: ChannelHandlerContext, e: ChannelEvent): Unit = {
    e match {
      case c: ChannelStateEvent => if (c.getState != ChannelState.INTEREST_OPS) logger.info(e.toString)
      case _ => None
    }

    // Let SimpleChannelHandler call actual event handler methods below.
    super.handleUpstream(ctx, e)
  }

  override def channelConnected(ctx: ChannelHandlerContext, e: ChannelStateEvent): Unit = {
    // Send the initial messages.
    generateTraffic(e)
  }

  override def channelInterestChanged(ctx: ChannelHandlerContext, e: ChannelStateEvent): Unit = {
    // Keep sending messages whenever the current socket buffer has room.
    generateTraffic(e)
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent): Unit = {
    // Server is supposed to send nothing.  Therefore, do nothing.
  }

  override def writeComplete(ctx: ChannelHandlerContext, e: WriteCompletionEvent): Unit = {
    transferredBytes.addAndGet(e.getWrittenAmount)
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent): Unit = {
    // Close the connection when an exception is raised.
    logger.warning("Unexpected exception from downstream." + e.getCause)
    e.getChannel.close
  }

  private def generateTraffic(e: ChannelStateEvent) {
    // Keep generating traffic until the channel is unwritable.
    // A channel becomes unwritable when its internal buffer is full.
    // If you keep writing messages ignoring this property,
    // you will end up with an OutOfMemoryError.
    val channel = e.getChannel
    breakable {
      while (channel.isWritable) {
        val m: ChannelBuffer = nextMessage
        if (m == null) {
          break
        }
        channel.write(m)
      }
    }
  }

  private def nextMessage(): ChannelBuffer = {
    ChannelBuffers.wrappedBuffer(content)
  }

}

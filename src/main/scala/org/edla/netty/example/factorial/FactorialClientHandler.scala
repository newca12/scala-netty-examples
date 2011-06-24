package org.edla.netty.example.factorial

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
import java.util.ArrayList
import java.math.BigInteger
import org.jboss.netty.channel.ChannelFutureListener
import org.jboss.netty.channel.ChannelFuture
import java.util.concurrent.LinkedBlockingQueue

/**
 * Handler for a client-side channel.  This handler maintains stateful
 * information which is specific to a certain channel using member variables.
 * Therefore, an instance of this handler can cover only one channel.  You have
 * to create a new handler instance whenever you create a new channel and insert
 * this handler to avoid a race condition.
 */
class FactorialClientHandler(count: Int) extends SimpleChannelUpstreamHandler {

  private val logger = Logger.getLogger(getClass.getName)

  private var i: Int = 1
  private var receivedMessages: Int = 0
  private val answer = new LinkedBlockingQueue[BigInteger]

  logger.info("FactorialClientHandler:count:"+count)
  
  def getFactorial(): BigInteger = {
    logger.info("FactorialClientHandler.getFactorial")
    var interrupted = false
    while (true) {
      try {
        val factorial = answer.take
        if (interrupted) {
          Thread.currentThread.interrupt
        }
        return factorial
      } catch {
        case e: InterruptedException =>
          interrupted = true
      }
    }
    BigInteger.ONE
  }

  override def handleUpstream(ctx: ChannelHandlerContext, e: ChannelEvent) {
    logger.info("FactorialClientHandler.handleStream")
    e match {
      case c: ChannelStateEvent => logger.info(e.toString)
      case _ =>
    }
    super.handleUpstream(ctx, e)
  }

  override def channelConnected(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
    sendNumbers(e)
  }

  override def channelInterestChanged(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
    sendNumbers(e)
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    receivedMessages += 1
    if (receivedMessages == count) {
      // Offer the answer after closing the connection.
      e.getChannel.close.addListener(new ChannelFutureListener {
        override def operationComplete(future: ChannelFuture) {
          val offered: Boolean = answer.offer(((e.getMessage).asInstanceOf[BigInteger]))
          assert(offered)
        }
      })
    }
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent) {
    // Close the connection when an exception is raised.
    logger.warning("Unexpected exception from downstream." + e.getCause)
    e.getChannel.close
  }

  def sendNumbers(e: ChannelStateEvent) {
    val channel = e.getChannel
    breakable {
      while (channel.isWritable) {
        if (i <= count) {
          channel.write(Integer.valueOf(i))
          i += 1
        } else {
          break
        }
      }
    }
  }
}

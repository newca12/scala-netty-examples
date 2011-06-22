package org.edla.netty.example.proxy

import org.jboss.netty.channel.{ ChannelHandlerContext, ExceptionEvent, MessageEvent, SimpleChannelUpstreamHandler }
import org.jboss.netty.channel.socket.ClientSocketChannelFactory
import org.jboss.netty.channel.ChannelStateEvent
import org.jboss.netty.bootstrap.ClientBootstrap
import java.net.InetSocketAddress
import org.jboss.netty.channel.ChannelFutureListener
import org.jboss.netty.channel.ChannelFuture
import org.jboss.netty.buffer.{ ChannelBuffer, ChannelBuffers }
import org.jboss.netty.channel.Channel

class HexDumpProxyInboundHandler(cf: ClientSocketChannelFactory, remoteHost: String, remotePort: Int) extends SimpleChannelUpstreamHandler {

  // This lock guards against the race condition that overrides the
  // OP_READ flag incorrectly.
  // See the related discussion: http://markmail.org/message/x7jc6mqx6ripynqf
  val trafficLock = new Object

  @volatile
  private var outboundChannel: Channel = null

  override def channelOpen(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
    // Suspend incoming traffic until connected to the remote host.
    val inboundChannel = e.getChannel
    inboundChannel.setReadable(false)

    // Start the connection attempt.
    val cb = new ClientBootstrap(cf)
    cb.getPipeline.addLast("handler", new OutboundHandler(e.getChannel))
    val f = cb.connect(new InetSocketAddress(remoteHost, remotePort))

    outboundChannel = f.getChannel
    f.addListener(new ChannelFutureListener {
      override def operationComplete(future: ChannelFuture) {
        if (future.isSuccess) {
          // Connection attempt succeeded:
          // Begin to accept incoming traffic.
          inboundChannel.setReadable(true)
        } else {
          // Close the connection if the connection attempt has failed.
          inboundChannel.close
        }
      }
    })
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    val msg = e.getMessage.asInstanceOf[ChannelBuffer]
    //System.out.println(">>> " + ChannelBuffers.hexDump(msg));
    trafficLock.synchronized {
      outboundChannel.write(msg)
      // If outboundChannel is saturated, do not read until notified in
      // OutboundHandler.channelInterestChanged().
      if (!outboundChannel.isWritable) {
        e.getChannel.setReadable(false)
      }
    }
  }

  override def channelInterestChanged(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
    // If inboundChannel is not saturated anymore, continue accepting
    // the incoming traffic from the outboundChannel.
    trafficLock.synchronized {
      if (e.getChannel.isWritable) {
        outboundChannel.setReadable(true)
      }
    }
  }

  override def channelClosed(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
    if (outboundChannel != null) {
      closeOnFlush(outboundChannel)
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    e.getCause.printStackTrace
    closeOnFlush(e.getChannel)
  }

  private class OutboundHandler(inboundChannel: Channel) extends SimpleChannelUpstreamHandler {

    override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
      val msg = e.getMessage.asInstanceOf[ChannelBuffer]
      //System.out.println("<<< " + ChannelBuffers.hexDump(msg))
      trafficLock.synchronized {
        inboundChannel.write(msg)
        // If inboundChannel is saturated, do not read until notified in
        // HexDumpProxyInboundHandler.channelInterestChanged().
        if (!inboundChannel.isWritable) {
          e.getChannel.setReadable(false)
        }
      }
    }

    override def channelInterestChanged(ctx: ChannelHandlerContext,
      e: ChannelStateEvent) {
      // If outboundChannel is not saturated anymore, continue accepting
      // the incoming traffic from the inboundChannel.
      trafficLock.synchronized {
        if (e.getChannel.isWritable) {
          inboundChannel.setReadable(true)
        }
      }
    }

    override def channelClosed(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
      closeOnFlush(inboundChannel)
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
      e.getCause.printStackTrace;
      closeOnFlush(e.getChannel())
    }
  }

  /**
   * Closes the specified channel after all queued write requests are flushed.
   */
  def closeOnFlush(ch: Channel) {
    if (ch.isConnected) {
      ch.write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
  }

}

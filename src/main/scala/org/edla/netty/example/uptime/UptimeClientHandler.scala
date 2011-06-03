package org.edla.netty.example.uptime
import java.net.{ ConnectException, InetSocketAddress }
import java.util.concurrent.TimeUnit
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.{ ChannelHandlerContext, ChannelStateEvent, ExceptionEvent, SimpleChannelUpstreamHandler }
import org.jboss.netty.handler.timeout.ReadTimeoutException
import org.jboss.netty.util.{ Timeout, Timer, TimerTask }

/**
 * Keep reconnecting to the server while printing out the current uptime and
 * connection attempt status.
 */
class UptimeClientHandler(bootstrap: ClientBootstrap, timer: Timer) extends SimpleChannelUpstreamHandler {

  var startTime: Long = -1

  def getRemoteAddress() = (bootstrap.getOption("remoteAddress")).asInstanceOf[InetSocketAddress]

  override def channelDisconnected(ctx: ChannelHandlerContext, e: ChannelStateEvent): Unit = {
    println("Disconnected from: " + getRemoteAddress())
  }

  override def channelClosed(ctx: ChannelHandlerContext, e: ChannelStateEvent): Unit = {
    println("Sleeping for: " + UptimeClient.RECONNECT_DELAY + "s");
    timer.newTimeout(new TimerTask() {
      override def run(timeout: Timeout): Unit = {
        println("Reconnecting to: " + getRemoteAddress());
        bootstrap.connect();
      }
    }, UptimeClient.RECONNECT_DELAY, TimeUnit.SECONDS)
  }

  override def channelConnected(ctx: ChannelHandlerContext, e: ChannelStateEvent): Unit = {
    if (startTime < 0) {
      startTime = System.currentTimeMillis()
    }
    println("Connected to: " + getRemoteAddress())
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent): Unit = {
    val cause = e.getCause()
    cause match {
      case e: ConnectException => {
        startTime = -1;
        println("Failed to connect: " + cause.getMessage());
      }
      case e: ReadTimeoutException => {
        // The connection was OK but there was no traffic for last period.
        println("Disconnecting due to no inbound traffic")
      }
      case _ => cause.printStackTrace();
    }

    ctx.getChannel().close()
  }
  
    def println(msg :String) :Unit = {
        if (startTime < 0) {
            System.err.format("[SERVER IS DOWN] %s%n", msg)
        } else {
            System.err.format("[UPTIME: %s] %s%n", ((System.currentTimeMillis() - startTime) / 1000).toString, msg)
        }
    }  
}
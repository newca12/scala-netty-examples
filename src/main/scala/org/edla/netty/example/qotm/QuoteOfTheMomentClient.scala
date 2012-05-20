package org.edla.netty.example.qotm

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory
import org.jboss.netty.channel.{ ChannelPipeline, ChannelPipelineFactory, Channels, FixedReceiveBufferSizePredictorFactory }
import org.jboss.netty.handler.codec.string.{ StringDecoder, StringEncoder }
import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.channel.socket.DatagramChannel

/**
 * A UDP broadcast client that asks for a quote of the moment (QOTM) to QuoteOfTheMomentServer
 */
object QuoteOfTheMomentClient {

  def main(args: Array[String]) {
    val f = new NioDatagramChannelFactory(Executors.newCachedThreadPool)

    val b = new ConnectionlessBootstrap(f)

    // Configure the pipeline factory.
    b.setPipelineFactory(new ChannelPipelineFactory {
      override def getPipeline = Channels.pipeline(
        new StringEncoder(CharsetUtil.ISO_8859_1),
        new StringDecoder(CharsetUtil.ISO_8859_1),
        new QuoteOfTheMomentClientHandler)
    })

    // Enable broadcast
    b.setOption("broadcast", "true")

    // Allow packets as large as up to 1024 bytes (default is 768).
    // You could increase or decrease this value to avoid truncated packets
    // or to improve memory footprint respectively.
    //
    // Please also note that a large UDP packet might be truncated or
    // dropped by your router no matter how you configured this option.
    // In UDP, a packet is truncated or dropped if it is larger than a
    // certain size, depending on router configuration.  IPv4 routers
    // truncate and IPv6 routers drop a large packet.  That's why it is
    // safe to send small packets in UDP.
    b.setOption(
      "receiveBufferSizePredictorFactory",
      new FixedReceiveBufferSizePredictorFactory(1024))

    val c: DatagramChannel = b.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]

    // Broadcast the QOTM request to port 8080.
    c.write("QOTM?", new InetSocketAddress("255.255.255.255", 8080))

    // QuoteOfTheMomentClientHandler will close the DatagramChannel when a
    // response is received.  If the channel is not closed within 5 seconds,
    // print an error message and quit.
    if (!c.getCloseFuture.awaitUninterruptibly(5000)) {
      System.err.println("QOTM request timed out.")
      c.close().awaitUninterruptibly
    }

    f.releaseExternalResources()
  }
}

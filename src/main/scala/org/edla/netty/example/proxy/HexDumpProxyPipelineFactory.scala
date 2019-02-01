package org.edla.netty.example.proxy

import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels.pipeline
import org.jboss.netty.channel.socket.ClientSocketChannelFactory
import org.jboss.netty.handler.logging.LoggingHandler
import org.jboss.netty.logging.InternalLogLevel

class HexDumpProxyPipelineFactory(cf: ClientSocketChannelFactory, remoteHost: String, remotePort: Int)
    extends ChannelPipelineFactory {

  override def getPipeline: ChannelPipeline = {
    val p = pipeline
    p.addLast("logger", new LoggingHandler(InternalLogLevel.INFO))
    p.addLast("handler", new HexDumpProxyInboundHandler(cf, remoteHost, remotePort))
    p
  }

}

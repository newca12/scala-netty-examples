package org.edla.netty.example.proxy

import org.jboss.netty.channel.Channels._

import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.socket.ClientSocketChannelFactory

class HexDumpProxyPipelineFactory(cf: ClientSocketChannelFactory, remoteHost: String, remotePort: Int) extends ChannelPipelineFactory {

  override def getPipeline = {
    val p = pipeline
    p.addLast("handler", new HexDumpProxyInboundHandler(cf, remoteHost, remotePort))
    p
  }

}
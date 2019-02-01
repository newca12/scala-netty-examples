package org.edla.netty.example.telnet
import org.jboss.netty.channel.{ChannelPipeline, ChannelPipelineFactory}
import org.jboss.netty.handler.codec.frame.{DelimiterBasedFrameDecoder, Delimiters}
import org.jboss.netty.handler.codec.string.{StringDecoder, StringEncoder}

/**
  * Creates a newly configured ChannelPipeline for a new channel.
  */
class TelnetServerPipelineFactory extends ChannelPipelineFactory {

  override def getPipeline: ChannelPipeline = {
    // Create a default pipeline implementation.
    val pipeline = org.jboss.netty.channel.Channels.pipeline

    // Add the text line codec combination first,
    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter: _*))
    pipeline.addLast("decoder", new StringDecoder)
    pipeline.addLast("encoder", new StringEncoder)

    // and then business logic.
    pipeline.addLast("handler", new TelnetServerHandler)

    pipeline
  }

}

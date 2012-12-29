package org.edla.netty.example.factorial
import org.jboss.netty.channel.{ChannelPipeline, ChannelPipelineFactory}
import org.jboss.netty.handler.codec.compression.{ZlibDecoder, ZlibEncoder, ZlibWrapper}

/**
 * Creates a newly configured ChannelPipeline for a client-side channel.
 */
class FactorialClientPipelineFactory(count :Int) extends ChannelPipelineFactory {

  override def getPipeline: ChannelPipeline = {

    val pipeline = org.jboss.netty.channel.Channels.pipeline

        // Enable stream compression (you can remove these two if unnecessary)
        //pipeline.addLast("deflater", new ZlibEncoder(ZlibWrapper.GZIP))
        //pipeline.addLast("inflater", new ZlibDecoder(ZlibWrapper.GZIP))

        // Add the number codec first,
        pipeline.addLast("decoder", new BigIntegerDecoder)
        pipeline.addLast("encoder", new NumberEncoder)

        // and then business logic.
        pipeline.addLast("handler", new FactorialClientHandler(count))

    pipeline
  }

}

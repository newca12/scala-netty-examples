package org.edla.netty.example.factorial
import java.math.BigInteger
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.channel.{Channel, ChannelHandlerContext}
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder

/**
  * Encodes a Number into the binary representation prepended with
  * a magic number ('F' or 0x46) and a 32-bit length prefix.  For example, 42
  * will be encoded to { 'F', 0, 0, 0, 1, 42 }.
  */
class NumberEncoder extends OneToOneEncoder {

  override def encode(ctx: ChannelHandlerContext, channel: Channel, msg: Object): Object = {
    msg match {
      case _: Number =>
      // Ignore what this encoder can't encode.
      case _ => msg
    }

    // Convert to a BigInteger first for easier implementation.
    val v = msg match {
      case m: BigInteger => m
      // Ignore what this encoder can't encode.
      case _ => new BigInteger(String.valueOf(msg))
    }

    // Convert the number into a byte array.
    val data       = v.toByteArray
    val dataLength = data.length

    // Construct a message.
    val buf = ChannelBuffers.dynamicBuffer
    buf.writeByte('F'.toByte) // magic number
    buf.writeInt(dataLength)  // data length
    buf.writeBytes(data)      // data

    // Return the constructed message.
    buf
  }
}

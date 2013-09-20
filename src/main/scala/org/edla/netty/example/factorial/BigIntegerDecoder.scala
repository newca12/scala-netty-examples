package org.edla.netty.example.factorial

import java.math.BigInteger
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.channel.{ Channel, ChannelHandlerContext }
import org.jboss.netty.handler.codec.frame.{ CorruptedFrameException, FrameDecoder }

/**
 * Decodes the binary representation of a {@link BigInteger} prepended
 * with a magic number ('F' or 0x46) and a 32-bit integer length prefix into a
 * BigInteger instance.  For example, { 'F', 0, 0, 0, 1, 42 } will be
 * decoded into new BigInteger("42").
 */
class BigIntegerDecoder extends FrameDecoder {

  override def decode(ctx: ChannelHandlerContext, channel: Channel, buffer: ChannelBuffer): Object = {
    // Wait until the length prefix is available.
    if (buffer.readableBytes < 5) {
      null
    }

    buffer.markReaderIndex()

    // Check the magic number.
    val magicNumber: Int = buffer.readUnsignedByte
    if (magicNumber != 'F') {
      buffer.resetReaderIndex()
      throw new CorruptedFrameException(
        "Invalid magic number: " + magicNumber)
    }

    // Wait until the whole data is available.
    val dataLength = buffer.readInt
    if (buffer.readableBytes < dataLength) {
      buffer.resetReaderIndex()
      null
    }

    // Convert the received data into a new BigInteger.
    val decoded = new Array[Byte](dataLength)
    buffer.readBytes(decoded)

    new BigInteger(decoded)
  }
}

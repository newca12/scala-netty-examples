package org.edla.netty.example.telnet
import java.io.{ BufferedReader, InputStreamReader }
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.channel.ChannelFuture
import scala.util.control.Breaks._

/**
 * Simplistic telnet client.
 */
object TelnetClient {
  def main(args: Array[String]) {
    // Print usage if no argument is specified.
    if (args.length != 2) {
      System.err.println(
        "Usage: " + TelnetClient.getClass.getSimpleName +
          " <host> <port>")
      return
    }

    // Parse options.
    val host = args(0)
    val port = args(1).toInt

    // Configure the client.
    val bootstrap = new ClientBootstrap(
      new NioClientSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool))

    // Set up the pipeline factory.
    bootstrap.setPipelineFactory(new TelnetClientPipelineFactory)

    // Start the connection attempt.
    val future: ChannelFuture = bootstrap.connect(new InetSocketAddress(host, port))

    // Wait until the connection attempt succeeds or fails.
    val channel = future.awaitUninterruptibly.getChannel
    if (!future.isSuccess) {
      future.getCause.printStackTrace
      bootstrap.releaseExternalResources
      return
    }

    // Read commands from the stdin.
    var lastWriteFuture: ChannelFuture = null
    val in = new BufferedReader(new InputStreamReader(System.in))
    breakable {
      while (true) {
        val line = in.readLine
        if (line == null) break

        // Sends the received line to the server.
        lastWriteFuture = channel.write(line + "\r\n")

        // If user typed the 'bye' command, wait until the server closes
        // the connection.
        if (line.toLowerCase.equals("bye")) {
          channel.getCloseFuture.awaitUninterruptibly
          break
        }
      }
    }

    // Wait until all messages are flushed before closing the channel.
    if (lastWriteFuture != null) lastWriteFuture.awaitUninterruptibly

    // Close the connection.  Make sure the close operation ends because
    // all I/O operations are asynchronous in Netty.
    channel.close.awaitUninterruptibly

    // Shut down all thread pools to exit.
    bootstrap.releaseExternalResources
  }
}
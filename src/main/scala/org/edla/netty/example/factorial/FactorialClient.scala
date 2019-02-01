package org.edla.netty.example.factorial

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory

/**
  * Sends a sequence of integers to a FactorialServer to calculate
  * the factorial of the specified integer.
  */
object FactorialClient {

  def main(args: Array[String]): Unit = {
    // Print usage if no argument is specified.
    if (args.length != 3) {
      System.err.println(
        "Usage: " + FactorialClient.getClass.getSimpleName +
          " <host> <port> <count>"
      )
      return
    }

    // Parse options.
    val host  = args(0)
    val port  = args(1).toInt
    val count = args(2).toInt
    if (count <= 0) {
      throw new IllegalArgumentException("count must be a positive integer.")
    }

    // Configure the client.
    val bootstrap = new ClientBootstrap(
      new NioClientSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool)
    )

    // Set up the event pipeline factory.
    bootstrap.setPipelineFactory(new FactorialClientPipelineFactory(count))

    // Make a new connection.
    val connectFuture = bootstrap.connect(new InetSocketAddress(host, port))

    // Wait until the connection is made successfully.
    val channel = connectFuture.awaitUninterruptibly.getChannel

    // Get the handler instance to retrieve the answer.
    val handler: FactorialClientHandler = channel.getPipeline.getLast.asInstanceOf[FactorialClientHandler]

    // Print out the answer.
    System.err.println(s"Factorial of ${count} is: ${handler.getFactorial}")

    // Shut down all thread pools to exit.
    bootstrap.releaseExternalResources()
  }
}

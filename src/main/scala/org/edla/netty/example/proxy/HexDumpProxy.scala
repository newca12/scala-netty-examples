package org.edla.netty.example.proxy

import java.net.InetSocketAddress
import java.util.concurrent.{Executor, Executors}
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.ClientSocketChannelFactory
import org.jboss.netty.channel.socket.nio.{NioClientSocketChannelFactory, NioServerSocketChannelFactory}

object HexDumpProxy {

  def main(args: Array[String]): Unit = {
    // Validate command line options.
    if (args.length != 3) {
      System.err.println(
        "Usage: " + HexDumpProxy.getClass.getSimpleName +
          " <local port> <remote host> <remote port>"
      )
      return
    }

    // Parse command line options.
    val localPort  = args(0).toInt
    val remoteHost = args(1)
    val remotePort = args(2).toInt

    System.err.println(
      "Proxying *:" + localPort + " to " +
        remoteHost + ':' + remotePort + " ..."
    )

    // Configure the bootstrap.
    val executor: Executor = Executors.newCachedThreadPool
    val sb                 = new ServerBootstrap(new NioServerSocketChannelFactory(executor, executor))

    // Set up the event pipeline factory.
    val cf: ClientSocketChannelFactory = new NioClientSocketChannelFactory(executor, executor)

    sb.setPipelineFactory(new HexDumpProxyPipelineFactory(cf, remoteHost, remotePort))

    // Start up the server.
    sb.bind(new InetSocketAddress(localPort))
  }
}

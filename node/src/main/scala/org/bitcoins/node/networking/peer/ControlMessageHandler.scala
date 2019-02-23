package org.bitcoins.node.networking.peer

import org.bitcoins.core.util.BitcoinSLogger
import org.bitcoins.node.db.DbConfig
import org.bitcoins.node.messages._

import scala.concurrent.ExecutionContext

class ControlMessageHandler(dbConfig: DbConfig)(implicit ec: ExecutionContext)
    extends BitcoinSLogger {

  def handleControlPayload(
      controlMsg: ControlPayload,
      peerMsgSender: PeerMessageSender): Unit = {
    controlMsg match {
      case _: PingMessage =>
        ()

      case SendHeadersMessage =>
        //not implemented as of now
        ()
      case _: AddrMessage =>
        ()
      case _ @(_: FilterAddMessage | _: FilterLoadMessage |
          FilterClearMessage) =>
        ()
      case _ @(GetAddrMessage | VerAckMessage | _: VersionMessage |
          _: PongMessage) =>
        ()
      case _: RejectMessage =>
        ()

      case _: FeeFilterMessage =>
        ()
    }
  }
}
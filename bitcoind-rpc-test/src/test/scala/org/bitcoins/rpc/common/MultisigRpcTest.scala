package org.bitcoins.rpc.common

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.bitcoins.core.config.NetworkParameters
import org.bitcoins.core.crypto.ECPrivateKey
import org.bitcoins.core.protocol.P2PKHAddress
import org.bitcoins.rpc.{BitcoindRpcTestConfig, BitcoindRpcTestUtil}
import org.bitcoins.rpc.client.common.BitcoindRpcClient
import org.bitcoins.rpc.client.common.RpcOpts.AddressType
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll}

import scala.concurrent.{Await, ExecutionContext}

class MultisigRpcTest extends AsyncFlatSpec with BeforeAndAfterAll {
  implicit val system: ActorSystem = ActorSystem("MultisigRpcTest")
  implicit val m: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = m.executionContext
  implicit val networkParam: NetworkParameters = BitcoindRpcTestUtil.network

  val client: BitcoindRpcClient = new BitcoindRpcClient(
    BitcoindRpcTestUtil.instance())

  override def beforeAll(): Unit = {
    import BitcoindRpcTestConfig.DEFAULT_TIMEOUT
    Await.result(BitcoindRpcTestUtil.startServers(Vector(client)),
                 DEFAULT_TIMEOUT)

    Await.result(client.generate(200), DEFAULT_TIMEOUT)
  }

  override protected def afterAll(): Unit = {
    BitcoindRpcTestUtil.stopServers(Vector(client))
    TestKit.shutdownActorSystem(system)
  }

  behavior of "MultisigRpc"

  it should "be able to create a multi sig address" in {
    val ecPrivKey1 = ECPrivateKey.freshPrivateKey
    val ecPrivKey2 = ECPrivateKey.freshPrivateKey

    val pubKey1 = ecPrivKey1.publicKey
    val pubKey2 = ecPrivKey2.publicKey

    client.createMultiSig(2, Vector(pubKey1, pubKey2)).map { _ =>
      succeed
    }
    succeed
  }

  it should "be able to add a multi sig address to the wallet" in {
    val ecPrivKey1 = ECPrivateKey.freshPrivateKey
    val pubKey1 = ecPrivKey1.publicKey

    client.getNewAddress(addressType = AddressType.Legacy).flatMap { address =>
      client
        .addMultiSigAddress(2,
                            Vector(Left(pubKey1),
                                   Right(address.asInstanceOf[P2PKHAddress])))
        .map { _ =>
          succeed
        }
    }
  }

}
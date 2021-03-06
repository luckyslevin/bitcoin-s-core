package org.bitcoins.core.crypto

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import org.bitcoins.core.protocol.NetworkElement
import org.bitcoins.core.util.Factory
import scodec.bits.ByteVector

sealed abstract class BIP39Seed extends NetworkElement {
  require(
    bytes.length <= MAX_SEED_LENGTH_BYTES && bytes.length >= MIN_SEED_LENGTH_BYTES,
    s"Seed must be between $MIN_SEED_LENGTH_BYTES and $MAX_SEED_LENGTH_BYTES bytes, got ${bytes.length}"
  )

  private def MIN_SEED_LENGTH_BYTES = 16
  private def MAX_SEED_LENGTH_BYTES = 64

  /** Generates an extended private key given a version */
  def toExtPrivateKey(keyVersion: ExtKeyVersion): ExtPrivateKey =
    ExtPrivateKey.fromBIP39Seed(keyVersion, this)
}

/**
  * @see [[https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki# BIP32]]
  */
object BIP39Seed extends Factory[BIP39Seed] {
  private case class BIP39SeedImpl(bytes: ByteVector) extends BIP39Seed

  /**
    * Generates a [[https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki BIP32]]
    * seed from a sequence of bytes. Must be between 16 and 64 bytes.
    */
  override def fromBytes(bytes: ByteVector): BIP39Seed =
    BIP39SeedImpl(bytes)

  private val PSEUDO_RANDOM_FUNCTION = "PBKDF2WithHmacSHA512"
  private val ITERATION_COUNT = 2048
  private val DERIVED_KEY_LENGTH = 512

  private val secretKeyFactory =
    SecretKeyFactory.getInstance(PSEUDO_RANDOM_FUNCTION)

  /**
    * Generates a [[https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki BIP32]]
    * seed from a mnemonic code. An optional password can be supplied.
    * @param password Defaults to the empty string
    */
  def fromMnemonic(mnemonic: MnemonicCode, password: String = ""): BIP39Seed = {
    val salt = s"mnemonic$password"

    val words = mnemonic.words.mkString(" ")

    val keySpec = new PBEKeySpec(
      words.toCharArray,
      salt.getBytes,
      ITERATION_COUNT,
      DERIVED_KEY_LENGTH
    )

    val encodedBytes = secretKeyFactory.generateSecret(keySpec).getEncoded

    BIP39Seed.fromBytes(ByteVector(encodedBytes))
  }

}

package org.sh.utils.crypto
import org.sh.utils.common.encoding.Base64
import org.sh.utils.common.Util._
import org.sh.utils.common.encoding.Hex
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import org.bouncycastle.jce.provider.BouncyCastleProvider
import sun.security.rsa.RSAPrivateCrtKeyImpl

object Util {
  case class CryptoException(m:String) extends Exception(m)
  val secureRandom = new java.security.SecureRandom()
  Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
  
  ///////////////////////////////////////////////////////
  // for public key crypto
  ///////////////////////////////////////////////////////
  class BetterPrvKey(prvKey:PrivateKey) {
    val hex = Hex.encodeBytes(prvKey.getEncoded)
    def getFingerPrint = shaSmall(hex)
  }
  
  implicit def prvKeyToBetterPrvKey(prvKey:PrivateKey) = new BetterPrvKey(prvKey)
  
  class BetterPubKey(pubKey:PublicKey) {
    val hex = Hex.encodeBytes(pubKey.getEncoded)
    def getFingerPrint = shaSmall(hex)
  }
  
  implicit def pubKeyToBetterPubKey(pubKey:PublicKey) = new BetterPubKey(pubKey)
  
  
  val cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding","BC");
  
  /** Used for generating random bytes */
  def strongRandomBytes(numBytes:Int)= {
    var bytes = new Array[Byte](numBytes)
    secureRandom.nextBytes(bytes)
    bytes
  }

  /**
   * Used for computing SHA256 of b
   * @author Adapted from http://stackoverflow.com/questions/3103652/hash-string-via-sha-256-in-java
   */
  def SHA256(b:Array[Byte]):Array[Byte] = {
    import java.security.MessageDigest
    val md = MessageDigest.getInstance("SHA-256")
    md.update(b)
    md.digest()
  }

  /**
   * Generates a X509 certificate object from an inputstream containing the DER encoded certificate
   */
  private def certFromDERInputStream(is:InputStream):java.security.cert.X509Certificate = {
    val cf = CertificateFactory.getInstance("X509", new BouncyCastleProvider())
    // alternate: val cf = CertificateFactory.getInstance("X.509","BC")
    cf.generateCertificate(is).asInstanceOf[X509Certificate]
  }

  /**
   * Generates a X509 certificate object from a base64-encoded byte array containing a DER-encoded
   * X509 certificate
   */
  private def certFromBase64DERCertString(base64DERCertString:String):java.security.cert.X509Certificate = {
    val is = new ByteArrayInputStream(Base64.decode(base64DERCertString))
    certFromDERInputStream(is)
  }

  /**
   * Generates a X509 certificate object from a binary file containing a DER-encoded
   * X509 certificate. Usually, the extension of the file is .cert, .cer, .crt, .der
   */
  private def certFromBinaryDERCertFileName(certFileName:String):java.security.cert.X509Certificate = {
    using (new FileInputStream(certFileName)){
      fis => certFromDERInputStream(fis)
    }
  }

  /**
   * Generates a X509 certificate object from a binary file containing a DER-encoded
   * X509 certificate. Usually, the extension of the file is .cert, .cer, .crt, .der
   */
  private def certFromBinaryDERCertFile(certFile:File):java.security.cert.X509Certificate = {
    using (new FileInputStream(certFile)){
      fis => certFromDERInputStream(fis)
    }
  }

  /**
   * Generates a Public Key object from a PEM string of a X509 certificate
   * See http://en.wikipedia.org/wiki/X.509#Certificate_filename_extensions
   *
   * A PEM (Privacy Enhanced Mail) string is the Base64 encoded DER certificate, enclosed between
   * "-----BEGIN CERTIFICATE-----" and "-----END CERTIFICATE-----"
   */
  def pubKeyFromPEMCertString(PEMCertString:String):PublicKey =
    javax.security.cert.X509Certificate.getInstance(PEMCertString.getBytes).getPublicKey

  /**
   * Generates a PublicKey object from a binary file containing a DER-encoded
   * X509 certificate. Usually, the extension of the file is .cert, .cer, .crt, .der
   */
  def pubKeyFromBinaryDERCertFileName(certFileName:String):PublicKey = try {
    certFromBinaryDERCertFileName(certFileName).getPublicKey
  } catch {
    case _:Throwable =>
      //throw new Exception("unable to load certificate from file: "+certFileName)
      null
  }

  /**
   * Generates a PublicKey object from a binary file containing a DER-encoded
   * X509 certificate. Usually, the extension of the file is .cert, .cer, .crt, .der
   */
  def pubKeyFromBinaryDERCertFile(certFile:File):PublicKey = {
    certFromBinaryDERCertFile(certFile).getPublicKey
  }
  /**
   * Generates a PublicKey object from a the base64 representation of a DER encoded
   * X509 certificate.
   */
  def pubKeyFromBase64DERCertString(base64DERCertString:String):PublicKey =
    certFromBase64DERCertString(base64DERCertString).getPublicKey

  /**
   * Generates a PublicKey object from a the base64 representation of a DER encoded
   * public key.
   */
  def pubKeyFromBase64DERString(base64DERString:String):PublicKey =
    pubKeyFromDERBytes(Base64.decode(base64DERString))

  /**
   * Generates a PublicKey object from a byte array representing a DER encoded
   * public key. 
   */
  def pubKeyFromDERBytes(bytes:Array[Byte]):PublicKey =
    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
  
  def prvKeyFromDERBytes(bytes:Array[Byte]):PrivateKey = {
    // from http://stackoverflow.com/questions/4600106/create-privatekey-from-byte-array/6377471#6377471
    //create a keyfactory - use whichever algorithm and provider
    KeyFactory.getInstance("RSA", "BC").generatePrivate(new PKCS8EncodedKeySpec(bytes))
    // KeyFactory.getInstance("RSA").generatePrivate(new X509EncodedKeySpec(bytes));
  }
    
  //  def getPubFromPrvIfAvailable(prvKey:PrivateKey) = {
  //    /// https://gist.github.com/manzke/1068441
  //    val rsaPrivateKey = prvKey.asInstanceOf[RSAPrivateCrtKeyImpl]; 
  //    //create a KeySpec and let the Factory due the Rest. You could also create the KeyImpl by your own.
  //    val pubKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(rsaPrivateKey.getModulus, rsaPrivateKey.getPublicExponent));
  //    pubKey
  //  }
  def getPubFromPrvIfAvailable(prvKey:PrivateKey) = {
    /// https://gist.github.com/manzke/1068441
    val rsaPrivateKey = prvKey.asInstanceOf[org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey]; 
    //create a KeySpec and let the Factory due the Rest. You could also create the KeyImpl by your own.
    val pubKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(rsaPrivateKey.getModulus, rsaPrivateKey.getPublicExponent));
    pubKey
  }
  
  def privKeyFromStore(keyStoreFile:String, alias:String, keyStorePass:String, privateKeyPass:String) = {
    //Load the key store to memory.
    val privateKS = KeyStore.getInstance("JKS");
    var fis = new FileInputStream(keyStoreFile);
    privateKS.load(fis, keyStorePass.toCharArray());
    fis.close
    //Extract key from store
    privateKS.getKey(alias, privateKeyPass.toCharArray)
  }

  //  def decryptRSA(privateKey:Key, ciphertext:Array[Byte]) = {
  def decryptRSA(privateKey:PrivateKey, ciphertext:Array[Byte]) = {
    cipherRSA.init(Cipher.DECRYPT_MODE, privateKey, new SecureRandom());
    cipherRSA.doFinal(ciphertext)
  }
  def encryptRSA(pubKey:PublicKey, b:Array[Byte])= {
    // Initialize the cipher for encryption
    cipherRSA.init(Cipher.ENCRYPT_MODE, pubKey, secureRandom)
    // Encrypt the message
    cipherRSA.doFinal(b)
  }
  def encrypt(pubKey:PublicKey, s:String) = Base64.encodeBytes(encryptRSA(pubKey, s.getBytes("UTF-8")))
  def decrypt(privKey:PrivateKey, s:String) = new String(decryptRSA(privKey, Base64.decode(s)), "UTF-8")

  def sign(privateKey:PrivateKey, message:Array[Byte]) = {
    //val signer = Signature.getInstance("SHA1withRSA")
    val signer = Signature.getInstance(privateKey.getAlgorithm)
    signer.initSign(privateKey);
    signer.update(message);
    signer.sign
  }
  def generateRSAKeyPair = {
    import java.security.KeyPairGenerator
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(1024);
    val KPair = keyPairGenerator.generateKeyPair
    (KPair.getPublic, KPair.getPrivate)
  }

  def verify(publicKey:PublicKey, message:Array[Byte], sig:Array[Byte]) = {
    val verifier = Signature.getInstance(publicKey.getAlgorithm)
    verifier.initVerify(publicKey)
    verifier.update(message)
    verifier.verify(sig)
  }
  
}

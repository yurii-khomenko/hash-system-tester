package system.hash.tester.core.model

import system.hash.tester.core.conf.Conf
import system.hash.tester.core.service.HashSystemService.{getHashBlocked, getMsisdnBlocked}
import system.hash.tester.core.service.MetricService.{successCounter, _}
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory.getLogger

object HashSystemLoadTask {
  val log = getLogger(getClass)
}

class HashSystemLoadTask(msisdn: String) extends Runnable {

  def run() = withMetric("hash+dehash") {

    val hash = generateHash(msisdn)

    getHash(msisdn).zip(getMsisdn(hash)).foreach {

      case (actualHash, actualMsisdn) if actualHash == hash && actualMsisdn == msisdn =>
        successCounter.labels("hash+dehash").inc()

      case (actualHash, _) if actualHash != hash =>
        errorCounter.labels("hash+dehash").inc()
        log.warn(s"[$msisdn] hashes mismatch, expectedHash: $hash, actualHash: $actualHash")

      case (_, actualMsisdn) if actualMsisdn != msisdn =>
        errorCounter.labels("hash+dehash").inc()
        log.warn(s"[$msisdn] msisdns mismatch, expectedMsisdn: $msisdn, actualMsisdn: $actualMsisdn")
    }
  }

  private def generateHash(msisdn: String) = {
    val digest = DigestUtils.getDigest(Conf.algorithm)
    digest.update((msisdn + Conf.salt).getBytes)
    Hex.encodeHexString(digest.digest)
  }

  private def getHash(msisdn: String) = withMetric("hash", {

    val result = getHashBlocked(msisdn, msisdn)

    if (result.errorId.isEmpty && result.errorMsg.isEmpty) {
      successCounter.labels("hash").inc()
      Some(result.value)
    } else {
      errorCounter.labels("hash").inc()
      None
    }
  }, { t =>
    log.warn(s"[$msisdn] unable to get hash, ex: ${t.getMessage}")
    errorCounter.labels("hash").inc()
    None
  })

  private def getMsisdn(hash: String) = withMetric("dehash", {

    val result = getMsisdnBlocked(hash, msisdn)

    if (result.errorId.isEmpty && result.errorMsg.isEmpty) {
      successCounter.labels("dehash").inc()
      Some(result.value)
    } else {
      errorCounter.labels("dehash").inc()
      None
    }
  }, { t =>

    log.warn(s"[$msisdn] unable to dehash, ex: ${t.getMessage}")
    errorCounter.labels("dehash").inc()
    None
  })
}
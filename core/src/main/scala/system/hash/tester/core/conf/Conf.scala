package system.hash.tester.core.conf

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory.getLogger

import scala.collection.JavaConverters._

object Conf {

  private val log = getLogger(getClass)

  private val gc = ConfigFactory.load
  private val profile = gc.getString("profiles.active")

  log.info(s"active profile: $profile")

  private val c = gc.getConfig("profiles." + profile)

  val numberMask = 380000000000L
  val numbersInNDC = 10000000

  val samplesCount = gc.getInt("samplesCount")
  val treadsCount = gc.getIntList("treadsCounts").asScala.toStream
  val ndcs = gc.getIntList("ndcs").asScala.toArray

  val name = c.getString("name")
  val hashUrls = c.getStringList("hashUrls").asScala.toArray
  val dehashUrls = c.getStringList("dehashUrls").asScala.toArray
  val user = c.getString("user")
  val password = c.getString("password")
  val algorithm = c.getString("algorithm")
  val salt = c.getString("salt")
}
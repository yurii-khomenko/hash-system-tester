package system.hash.tester.core.service

import java.lang.Math.abs
import java.util.Base64
import java.util.Objects.isNull
import java.util.concurrent.atomic.AtomicInteger

import system.hash.tester.core.conf.Conf
import system.hash.tester.core.conf.Conf.{dehashUrls, hashUrls}
import system.hash.tester.core.model.HashSystemResponse
import org.asynchttpclient.Dsl.asyncHttpClient
import org.asynchttpclient.{DefaultAsyncHttpClientConfig, Response}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.slf4j.LoggerFactory.getLogger

import scala.compat.java8.FutureConverters._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object HashSystemService {

  implicit val formats = DefaultFormats

  private val log = getLogger(getClass)
  private val roundRobinIndex = new AtomicInteger

  private val authHeader = "Basic " + new String(
    Base64.getEncoder.encode((Conf.user + ":" + Conf.password).getBytes))

  private val client = asyncHttpClient(
    new DefaultAsyncHttpClientConfig.Builder()
      .setKeepAlive(true)
      .setMaxConnections(2050)
      .setPooledConnectionIdleTimeout(10000)
      .setConnectTimeout(1000)
      .setReadTimeout(10000)
      .setRequestTimeout(11000)
      .build
  )

  def getHashBlocked(msisdn: String, cid: String) =
    Await.result(HashSystemService.getHash(msisdn, cid), 10 seconds)

  private def getHash(msisdn: String, cid: String) = {

    val uri = hashUrls(abs(roundRobinIndex.getAndIncrement % hashUrls.length)) + "/" + msisdn

    client
      .prepareGet(uri)
      .addHeader("Authorization", authHeader)
      .execute
      .toCompletableFuture
      .toScala
      .map(response =>
        if (isOk(response)) {
          log.debug(
            s"[$cid] OK, getHash is success, uri: $uri, code: ${response.getStatusCode}, msg: ${response.getStatusText}")
          toResponse(response)
        } else {
          log.warn(
            s"[$cid] unable to getHash, uri: $uri, code: ${response.getStatusCode}, msg: ${response.getStatusText}")
          toResponse(response)
        })
  }

  private def isOk(response: Response) = 200 <= response.getStatusCode && response.getStatusCode <= 204

  private def toResponse(response: Response): HashSystemResponse = try {

    val statusCode = response.getStatusCode
    val responseBody = response.getResponseBody

    val resp = parse(
      if (isNull(responseBody) || responseBody.isEmpty) "{}"
      else responseBody
    ).extract[HashSystemResponse]

    if (!isOk(response))
      resp.copy(errorId = if (resp.errorId == null || resp.errorId.isEmpty) Some(statusCode) else resp.errorId)
    else resp

  } catch {
    case t: Throwable => toResponse(t)
  }

  private def toResponse(throwable: Throwable): HashSystemResponse =
    HashSystemResponse("", Some(-1), Some(throwable.getMessage))

  def getMsisdnBlocked(hash: String, cid: String) =
    Await.result(HashSystemService.getMsisdn(hash, cid), 10 seconds)

  private def getMsisdn(hash: String, cid: String) = {

    val uri = dehashUrls(abs(roundRobinIndex.getAndIncrement % dehashUrls.length)) + "/" + hash

    client
      .prepareGet(uri)
      .addHeader("Authorization", authHeader)
      .execute
      .toCompletableFuture
      .toScala
      .map(response =>
        if (isOk(response)) {

          log.debug(s"[$cid] OK, getHash is success, " +
            s"uri: $uri, code: ${response.getStatusCode}, msg: ${response.getStatusText}")

          toResponse(response)
        } else {
          log.warn(s"[$cid] unable to getMsisdn, " +
            s"uri: $uri, code: ${response.getStatusCode}, msg: ${response.getStatusText}")
          toResponse(response)
        })
  }

  def close() = {
    try asyncHttpClient.close()
    catch {
      case t: Throwable => log.warn("unable to close, ex: ", t)
    }
  }
}
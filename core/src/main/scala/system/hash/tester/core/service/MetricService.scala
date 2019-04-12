package system.hash.tester.core.service

import io.prometheus.client.{CollectorRegistry, Counter, Summary}
import system.hash.tester.core.model.Metric
import org.slf4j.LoggerFactory.getLogger

import scala.collection.Iterable

object MetricService {

  val log = getLogger(getClass)

  val group = "hash_system"

  CollectorRegistry.defaultRegistry.clear()

  val requestLatency = Summary.build
    .namespace(group)
    .maxAgeSeconds(300)
    .quantile(0.90, 0.01)
    .quantile(0.99, 0.001)
    .name("request_latency_seconds")
    .labelNames("name")
    .help("request latency in seconds.")
    .register

  val successCounter = Counter.build
    .namespace(group)
    .name("success_total")
    .labelNames("name")
    .help("http success total")
    .register

  val errorCounter = Counter.build
    .namespace(group)
    .name("errors_total")
    .labelNames("name")
    .help("http error total")
    .register

  def clear() = {
    requestLatency.clear()
    successCounter.clear()
    errorCounter.clear()
  }

  def withMetric[T](name: String)(body: => T): T = {

    val timer = requestLatency.labels(name).startTimer()

    try {
      body
    } finally {
      timer.observeDuration()
    }
  }

  def withMetric[T](name: String, body: => T, bodyEx: Throwable => T): T = {

    val timer = requestLatency.labels(name).startTimer()

    try {
      body
    } catch {
      case t: Throwable => bodyEx(t)
    } finally {
      timer.observeDuration()
    }
  }

  def printMetrics(results: Iterable[Metric]) = {

    log.info("")
    log.info("results:")

    results.foreach(printMetric)

    results
  }

  private def printMetric(result: Metric) = {

    log.info("")
    log.info(s"${result.name} success =>\t${result.successesCount}")
    log.info(s"${result.name} errors =>\t${result.errorsCount}")

    log.info(f"${result.name} tps(1) =>\t${result.tpsOne}%.0f")
    log.info(f"${result.name} tps(all) =>\t${result.tpsAll}%.0f")

    log.info(f"${result.name} p90 =>\t\t${result.p90}%.3fms")
    log.info(f"${result.name} p99 =>\t\t${result.p99}%.3fms")

    result
  }

  def printTop(results: Iterable[Metric]) = {

    log.info("")
    log.info("top:")
    log.info("treads   name    tpsOne tpsAll    p90    p99")

    results
      .filter(_.name != "hash+dehash")
      .foreach(r => log.info(f"${r.treadsCount}\t${r.name}\t${r.tpsOne}%.0f   ${r.tpsAll}%.0f    ${r.p90}%.0fms    ${r.p99}%.0fms"))

    results
  }
}
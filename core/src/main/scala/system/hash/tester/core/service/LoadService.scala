package system.hash.tester.core.service

import java.util.concurrent.{Executors, TimeUnit}

import io.prometheus.client.Collector
import system.hash.tester.core.conf.Conf
import system.hash.tester.core.model.{HashSystemLoadTask, Metric}
import system.hash.tester.core.service.HashSystemService.getHashBlocked
import system.hash.tester.core.service.MetricService._
import org.slf4j.LoggerFactory.getLogger

import scala.collection.Iterable
import scala.collection.JavaConverters._
import scala.util.Random

object LoadService {

  val log = getLogger(getClass)
  val random = new Random

  def startLoadTest() = {

    val metrics = Conf.treadsCount.map(loadTest(Conf.samplesCount, _)).flatMap(printMetrics).toList

    printTop(metrics)
  }

  private def loadTest(sampleCount: Int, treadsCount: Int) = {

    val pool = Executors.newFixedThreadPool(treadsCount)

    log.info("")
    log.info(s"${Conf.name} starts load test for $sampleCount samples in $treadsCount threads...")

    warm()

    (1 to sampleCount).toStream.foreach(_ => pool.execute(new HashSystemLoadTask(nextRandomNumber.toString)))

    pool.shutdown()
    pool.awaitTermination(Long.MaxValue, TimeUnit.SECONDS)

    getMetrics(treadsCount)
  }

  private def nextRandomNumber =
    Conf.numberMask + Conf.ndcs(random.nextInt(Conf.ndcs.length)) * Conf.numbersInNDC + random.nextInt(Conf.numbersInNDC)

  private def warm() = (0 to 100)
    .map(i => nextRandomNumber.toString)
    .foreach(msisdn => getHashBlocked(msisdn, msisdn))

  private def getMetrics(treadsCount: Int): Iterable[Metric] = {

    val samples = requestLatency.collect().asScala.headOption

    val names = samples
      .map(mfs => mfs.samples.asScala
        .filter(!_.labelNames.asScala
          .exists(_.equals("quantile")))
        .flatMap(s => s.labelValues.asScala).toSet)

    val results = samples.zip(names).flatMap { case (s, n) => n.map(toMetric(_, treadsCount, s)) }

    MetricService.clear()

    results.toList.sortBy(_.name)
  }

  private def toMetric(name: String, threadsCount: Int, samples: Collector.MetricFamilySamples): Metric = {

    val successesCount = successCounter.labels(name).get.toInt
    val errorsCount = errorCounter.labels(name).get.toInt

    val sum = samples.samples.asScala.find(m => m.labelValues.contains(name) && m.name.endsWith("sum")).map(_.value).getOrElse(Double.NaN)
    val count = samples.samples.asScala.find(m => m.labelValues.contains(name) && m.name.endsWith("count")).map(_.value).getOrElse(Double.NaN)

    val tpsOne = count / sum
    val tpsAll = tpsOne * threadsCount

    val p90 = samples.samples.asScala.find(m => m.labelValues.contains(name) && m.labelValues.contains("0.9")).map(_.value * 1000).getOrElse(Double.NaN)
    val p99 = samples.samples.asScala.find(m => m.labelValues.contains(name) && m.labelValues.contains("0.99")).map(_.value * 1000).getOrElse(Double.NaN)

    Metric(name, threadsCount, successesCount, errorsCount, tpsOne, tpsAll, p90, p99)
  }
}
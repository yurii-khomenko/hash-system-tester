package system.hash.tester.core.model

case class Metric(
                   name: String,
                   treadsCount: Int,

                   successesCount: Long,
                   errorsCount: Long,

                   tpsOne: Double,
                   tpsAll: Double,

                   p90: Double,
                   p99: Double
                 )
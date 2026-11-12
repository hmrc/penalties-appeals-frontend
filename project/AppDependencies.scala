import sbt.*

object AppDependencies {

  val bootstrapVersion = "10.8.0"

  val mongoPlayVersion = "2.14.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30" % "13.16.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % mongoPlayVersion,
    "uk.gov.hmrc"       %% "crypto-json-play-30"        % "8.4.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % mongoPlayVersion % Test,
    "org.jsoup"          % "jsoup"                   % "1.24.1"         % Test,
    "org.mockito"       %% "mockito-scala-scalatest" % "2.2.3"          % Test
  )

  val itTest: Seq[ModuleID] = Seq.empty // Pulled in through test by build.sbt

}

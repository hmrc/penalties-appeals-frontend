import sbt._

object AppDependencies {

  val bootstrapVersion = "8.6.0"

  val mongoPlayVersion = "1.9.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30"     % bootstrapVersion,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30"     % "8.5.0",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"             % mongoPlayVersion,
    "uk.gov.hmrc"                  %% "crypto-json-play-30"            % "7.6.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapVersion    % "test, it",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"  % mongoPlayVersion    % "test, it",
    "org.jsoup"               %  "jsoup"                    % "1.17.1"            % "test, it",
    "org.mockito"             %% "mockito-scala-scalatest"  % "1.17.30"           % "it, test"
  )
}

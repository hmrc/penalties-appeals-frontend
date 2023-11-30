import sbt.*

object AppDependencies {

  val bootstrapVersion = "8.1.0"

  val mongoPlayVersion = "1.6.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28"     % bootstrapVersion,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"             % "7.29.0-play-28",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"             % mongoPlayVersion,
    "uk.gov.hmrc"                  %% "crypto-json-play-28"            % "7.6.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % bootstrapVersion    % "test, it",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"  % mongoPlayVersion    % "test, it",
    "org.jsoup"               %  "jsoup"                    % "1.16.1"            % "test, it",
    "org.mockito"             %% "mockito-scala-scalatest"  % "1.17.27"           % "it, test"
  )
}

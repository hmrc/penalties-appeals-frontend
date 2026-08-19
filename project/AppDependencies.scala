import sbt.*

object AppDependencies {

  val bootstrapVersion = "10.7.1"

  val mongoPlayVersion = "2.13.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30"     % bootstrapVersion,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30"     % "13.11.0",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"             % mongoPlayVersion,
    "uk.gov.hmrc"                  %% "crypto-json-play-30"            % "7.6.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapVersion    % "test, it",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"  % mongoPlayVersion    % "test, it",
    "org.jsoup"               %  "jsoup"                    % "1.23.1"            % "test, it",
    "org.mockito"             %% "mockito-scala-scalatest"  % "2.2.3"           % "it, test"
  )
}

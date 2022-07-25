import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-28"     % "5.24.0",
    "uk.gov.hmrc"             %% "play-frontend-hmrc"             % "3.21.0-play-28",
    "uk.gov.hmrc"             %% "play-frontend-govuk"            % "2.0.0-play-28",
    "uk.gov.hmrc"             %% "play-language"                  % "5.2.0-play-28",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"             % "0.68.0",
    "uk.gov.hmrc"             %% "play-conditional-form-mapping"  % "1.11.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % "5.24.0"            % "test, it",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"  % "0.68.0"           % "test, it",
    "org.scalatest"           %% "scalatest"                % "3.2.12"            % "test, it",
    "org.jsoup"               %  "jsoup"                    % "1.15.1"           % "test, it",
    "com.typesafe.play"       %% "play-test"                % PlayVersion.current  % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"            % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.26.3"           % "it",
    "uk.gov.hmrc"             %% "service-integration-test" % "1.3.0-play-28"    % "test, it",
    "org.mockito"             % "mockito-core"              % "4.6.0"            % "test, it",
    "com.vladsch.flexmark"   % "flexmark-all"               % "0.62.2"           % "test, it"
  )
}

import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-27" % "4.2.0",
    "uk.gov.hmrc"             %% "play-frontend-hmrc"         % "0.57.0-play-27",
    "uk.gov.hmrc"             %% "play-frontend-govuk"        % "0.69.0-play-27",
    "uk.gov.hmrc"             %% "play-language"              % "4.12.0-play-27"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-27"   % "4.2.0"            % "test, it",
    "org.scalatest"           %% "scalatest"                % "3.0.8"            % "test, it",
    "org.jsoup"               %  "jsoup"                    % "1.13.1"           % "test, it",
    "com.typesafe.play"       %% "play-test"                % PlayVersion.current  % "test, it",
    "org.mockito"             %  "mockito-all"              % "1.10.19"          % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "4.0.3"            % "test, it",
    "uk.gov.hmrc"             %% "hmrctest"                 % "3.10.0-play-26"   % "test, it",
    "com.vladsch.flexmark"     %  "flexmark-all"              % "0.36.8"           % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.27.2"           % "it",
    "uk.gov.hmrc"             %% "service-integration-test" % "1.1.0-play-27"    % "test, it"
  )
}

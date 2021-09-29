import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-28"     % "5.3.0",
    "uk.gov.hmrc"             %% "play-frontend-hmrc"             % "0.64.0-play-28",
    "uk.gov.hmrc"             %% "play-frontend-govuk"            % "0.71.0-play-28",
    "uk.gov.hmrc"             %% "play-language"                  % "5.0.0-play-28",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"             % "0.53.0",
    "uk.gov.hmrc"             %% "play-conditional-form-mapping"  % "1.9.0-play-28",
    "uk.gov.hmrc"             %% "emailaddress"                   % "3.5.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % "5.3.0"            % "test, it",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"  % "0.50.0"           % "test, it",
    "org.scalatest"           %% "scalatest"                % "3.0.9"            % "test, it",
    "org.jsoup"               %  "jsoup"                    % "1.13.1"           % "test, it",
    "com.typesafe.play"       %% "play-test"                % PlayVersion.current  % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"            % "test, it",
    "com.vladsch.flexmark"     %  "flexmark-all"              % "0.36.8"           % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.26.3"           % "it",
    "uk.gov.hmrc"             %% "service-integration-test" % "1.1.0-play-28"    % "test, it",
    "org.mockito"             % "mockito-core"              % "3.4.6"            % "test, it"
  )
}

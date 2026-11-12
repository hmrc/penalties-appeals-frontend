import play.sbt.routes.RoutesKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

val appName = "penalties-appeals-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.18"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, SbtWeb)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "config.AppConfig",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "models.Mode",
      "views.html.layouts.{PageLayout => Layout}",
      "controllers.routes._",
      "utils.MessageRenderer._",
      "models.pages._"
    ),
    RoutesKeys.routesImport ++= Seq("models._", "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"),
    // ***************
    // Use the silencer plugin to suppress warnings
    // You may turn it on for `views` too to suppress warnings from unused imports in compiled twirl templates, but this will hide other warnings.
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    WebpackKeys.webpack / WebpackKeys.outputFileName := "javascripts/multiFileUpload.min.js",
    WebpackKeys.webpack / WebpackKeys.entries := Seq(
      "assets:javascripts/upload/multiFileUpload.js"
    ),
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*(BuildInfo|Routes).*",
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;" +
      ".*BuildInfo.*;.*javascript.*;.*FrontendAuditConnector.*;.*Routes.*;.*GuiceInjector;" +
      ".*ControllerConfiguration;.*LanguageSwitchController;.*testonly.*;.*layouts.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum    := false,
    ScoverageKeys.coverageHighlighting     := true,
    PlayKeys.playDefaultPort               := 9181
  )

lazy val it = project
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    libraryDependencies ++= AppDependencies.itTest,
    Test / unmanagedSourceDirectories   := Seq(baseDirectory.value / "test"),
    Test / unmanagedResourceDirectories := Seq(baseDirectory.value / "test" / "resources")
  )

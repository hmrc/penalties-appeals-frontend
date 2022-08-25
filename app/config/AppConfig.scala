/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import config.featureSwitches.FeatureSwitching
import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration

@Singleton
class AppConfig @Inject()(val config: Configuration, servicesConfig: ServicesConfig) extends FeatureSwitching {
  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  val en: String            = "en"
  val cy: String            = "cy"
  val defaultLanguage: Lang = Lang(en)

  lazy val signInUrl: String = config.get[String]("signIn.url")

  lazy val mongoTTL: Duration = config.get[Duration]("mongodb.ttl")

  lazy val penaltiesServiceBaseUrl: String = servicesConfig.baseUrl("penalties")

  def appealLSPDataForPenaltyAndEnrolmentKey(penaltyId: String, enrolmentKey: String): String = {
    s"$penaltiesServiceBaseUrl/penalties/appeals-data/late-submissions?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"
  }

  def appealLPPDataForPenaltyAndEnrolmentKey(penaltyId: String, enrolmentKey: String, isAdditional: Boolean): String = {
    s"$penaltiesServiceBaseUrl/penalties/appeals-data/late-payments?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey&isAdditional=$isAdditional"
  }

  def submitAppealUrl(enrolmentKey: String, isLPP: Boolean, penaltyNumber: String, correlationId: String): String =
    penaltiesServiceBaseUrl + config.get[String]("reasonableExcuse.submitUrl") + s"?enrolmentKey=$enrolmentKey&isLPP=$isLPP&penaltyNumber=$penaltyNumber&correlationId=$correlationId"

  def multiplePenaltyDataUrl(penaltyId: String, enrolmentKey: String): String =
    s"$penaltiesServiceBaseUrl/penalties/appeals-data/multiple-penalties?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"

  lazy val reasonableExcuseFetchUrl: String = penaltiesServiceBaseUrl + config.get[String]("reasonableExcuse.fetchUrl")

  lazy val signInContinueBaseUrl: String = config.get[String]("signIn.continueBaseUrl")

  lazy val signInContinueUrl: String = SafeRedirectUrl(signInContinueBaseUrl + controllers.routes.AppealStartController.onPageLoad().url).encodedUrl

  lazy val signOutUrl: String = config.get[String]("signOut.url") + signInContinueUrl

  lazy val timeoutPeriod: Int = config.get[Int]("timeout.period")

  lazy val timeoutCountdown: Int = config.get[Int]("timeout.countDown")

  lazy val penaltiesFrontendUrl: String = config.get[String]("urls.penalties-frontend")

  lazy val vatOverviewUrl: String = config.get[String]("urls.vatOverview")

  lazy val whatYouOweUrl: String = config.get[String]("urls.whatYouOwe")

  lazy val contactFrontendServiceId: String = config.get[String]("contact-frontend.serviceId")

  lazy val csatFeedbackUrl: String = config.get[String]("urls.csatFeedbackUrl")

  def betaFeedbackBackUrl(url: String): String =  SafeRedirectUrl(url).encodedUrl

  def betaFeedbackUrl(redirectUrl: String): String = s"${config.get[String]("urls.betaFeedbackUrl")}?service=$contactFrontendServiceId&backURL=${betaFeedbackBackUrl(redirectUrl)}"

  lazy val daysRequiredForLateAppeal: Int = config.get[Int]("constants.daysRequiredForLateAppeal")

  lazy val numberOfCharsInTextArea: Int = config.get[Int]("constants.numberOfCharsInTextArea")

  val vatAgentClientLookupFrontendHost: String = "vat-agent-client-lookup-frontend.host"
  val vatAgentClientLookupFrontendStartUrl: String = "vat-agent-client-lookup-frontend.startUrl"

  private lazy val agentClientLookupHost = servicesConfig.getConfString(vatAgentClientLookupFrontendHost, "")

  private lazy val platformHost = servicesConfig.getString("penalties-frontend-host")

  val appName: String = servicesConfig.getString("appName")

  private lazy val agentClientLookupRedirectUrl: String => String = uri => SafeRedirectUrl(platformHost + uri).encodedUrl

  lazy val agentClientLookupStartUrl: String => String = (uri: String) =>
    agentClientLookupHost +
      servicesConfig.getConfString(vatAgentClientLookupFrontendStartUrl, "") +
      s"?redirectUrl=${agentClientLookupRedirectUrl(uri)}"

  lazy val upscanInitiateBaseUrl: String = servicesConfig.baseUrl("upscan-initiate")

  lazy val upscanCallbackBaseUrl: String = config.get[String]("upscan.callback.base")

  lazy val upscanBaseUrl: String = config.get[String]("upscan.base")

  lazy val upscanStatusCheckTimeout: Int = config.get[Int]("upscan.statusCheckTimeout")

  lazy val upscanStatusCheckDelay: Int = config.get[Int]("upscan.statusCheckDelay")

  lazy val upscanSuccessUrl: String = config.get[String]("upscan.successUrl")

  lazy val upscanFailureUrl: String = config.get[String]("upscan.failureUrl")

  lazy val acceptedFileTypes: String = config.get[String]("upscan.acceptedFileTypes")

  lazy val maxFileUploadSize: Int = config.get[Int]("upscan.maxFileSize")

}

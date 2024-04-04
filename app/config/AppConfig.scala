/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrlPolicy.Id
import uk.gov.hmrc.play.bootstrap.binders._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration

@Singleton
class AppConfig @Inject()(val config: Configuration, servicesConfig: ServicesConfig) extends FeatureSwitching {
  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  val en: String            = "en"
  val cy: String            = "cy"
  val defaultLanguage: Lang = Lang(en)

  private lazy val permitAllRedirectPolicy = config.get[Boolean]("urls.permitAllRedirectPolicy")

  private lazy val allowedHostname = config.get[String]("urls.allowedHostname")

  private lazy val relativeRedirectPolicy: RedirectUrlPolicy[Id] = if(!permitAllRedirectPolicy) OnlyRelative else UnsafePermitAll

  private lazy val absoluteRedirectPolicy: RedirectUrlPolicy[Id] = if(!permitAllRedirectPolicy) AbsoluteWithHostnameFromAllowlist(allowedHostname) else UnsafePermitAll

  lazy val signInUrl: String = config.get[String]("signIn.url")

  lazy val mongoTTL: Duration = config.get[Duration]("mongodb.ttl")

  lazy val penaltiesServiceBaseUrl: String = servicesConfig.baseUrl("penalties")

  lazy val webChatBaseUrl: String = config.get[String]("webChat.baseUrl")

  def appealLSPDataForPenaltyAndEnrolmentKey(penaltyId: String, enrolmentKey: String): String = {
    s"$penaltiesServiceBaseUrl/penalties/appeals-data/late-submissions?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"
  }

  def appealLPPDataForPenaltyAndEnrolmentKey(penaltyId: String, enrolmentKey: String, isAdditional: Boolean): String = {
    s"$penaltiesServiceBaseUrl/penalties/appeals-data/late-payments?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey&isAdditional=$isAdditional"
  }

  def submitAppealUrl(enrolmentKey: String, isLPP: Boolean, penaltyNumber: String, correlationId: String, isMultiAppeal: Boolean): String =
    penaltiesServiceBaseUrl + config.get[String]("reasonableExcuse.submitUrl") + s"?enrolmentKey=$enrolmentKey&isLPP=$isLPP&penaltyNumber=$penaltyNumber&correlationId=$correlationId&isMultiAppeal=$isMultiAppeal"

  def multiplePenaltyDataUrl(penaltyId: String, enrolmentKey: String): String =
    s"$penaltiesServiceBaseUrl/penalties/appeals-data/multiple-penalties?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"

  lazy val reasonableExcuseFetchUrl: String = penaltiesServiceBaseUrl + config.get[String]("reasonableExcuse.fetchUrl")

  lazy val signInContinueBaseUrl: String = config.get[String]("signIn.continueBaseUrl")

  lazy val signInContinueUrl: String = RedirectUrl(signInContinueBaseUrl + controllers.routes.AppealStartController.onPageLoad()).get(relativeRedirectPolicy).encodedUrl

  lazy val signOutUrlUnauthorised: String = config.get[String]("signOut.url") + signInContinueUrl

  lazy val feedbackUrl: String = config.get[String]("urls.feedback")

  lazy val signOutUrl: String = config.get[String]("signOut.url") + feedbackUrl

  lazy val timeoutPeriod: Int = config.get[Int]("timeout.period")

  lazy val timeoutCountdown: Int = config.get[Int]("timeout.countDown")

  lazy val penaltiesFrontendUrl: String = config.get[String]("urls.penalties-frontend")

  lazy val timeToPayUrl: String = config.get[String]("urls.timeToPay")

  lazy val payYourVAT: String = config.get[String]("urls.payYourVAT")

  lazy val vatOverviewUrl: String = config.get[String]("urls.vatOverview")

  lazy val contactFrontendServiceId: String = config.get[String]("contact-frontend.serviceId")

  lazy val reasonableExcusesGuidanceLinkUrl: String = config.get[String]("urls.reasonableExcusesGuidanceLinkUrl")
  lazy val externalWebChatUrl: String = webChatBaseUrl + config.get[String]("webChat.url")

  lazy val contactHMRCLinkUrl: String = config.get[String]("urls.externalHMRCLinkUrl")

  lazy val contactHMRCLinkWelshUrl: String = config.get[String]("urls.externalHMRCLinkWelshUrl")

  lazy val taxTribunalLink: String = config.get[String]("urls.externalTaxTribunalUrl")

  lazy val appealToTaxTribunalLink: String = config.get[String]("urls.externalAppealToTaxTribunalUrl")

  def betaFeedbackBackUrl(url: String): String = RedirectUrl(platformHost ++ url).get(absoluteRedirectPolicy).encodedUrl

  def betaFeedbackUrl(redirectUrl: String): String = s"${config.get[String]("urls.betaFeedbackUrl")}?service=$contactFrontendServiceId&backUrl=${betaFeedbackBackUrl(redirectUrl)}"

  lazy val daysRequiredForLateAppeal: Int = config.get[Int]("constants.daysRequiredForLateAppeal")

  lazy val numberOfCharsInTextArea: Int = config.get[Int]("constants.numberOfCharsInTextArea")

  val vatAgentClientLookupFrontendHost: String = "vat-agent-client-lookup-frontend.host"
  val vatAgentClientLookupFrontendStartUrl: String = "vat-agent-client-lookup-frontend.startUrl"

  private lazy val agentClientLookupHost = servicesConfig.getConfString(vatAgentClientLookupFrontendHost, "")

  private lazy val platformHost = servicesConfig.getString("penalties-appeals-frontend-host")

  val appName: String = servicesConfig.getString("appName")

  private lazy val agentClientLookupRedirectUrl: String => String = uri => RedirectUrl(platformHost + uri).get(absoluteRedirectPolicy).encodedUrl

  lazy val agentClientLookupStartUrl: String => String = (uri: String) =>
    agentClientLookupHost +
      servicesConfig.getConfString(vatAgentClientLookupFrontendStartUrl, "") +
      s"?redirectUrl=${agentClientLookupRedirectUrl(uri)}"

  lazy val upscanInitiateBaseUrl: String = servicesConfig.baseUrl("upscan-initiate")

  lazy val upscanCallbackBaseUrl: String = config.get[String]("upscan.callback.base")

  lazy val upscanBaseUrl: String = config.get[String]("upscan.base")

  lazy val upscanStatusCheckTimeout: Int = config.get[Int]("upscan.statusCheckTimeout")

  lazy val upscanStatusCheckDelay: Int = config.get[Int]("upscan.statusCheckDelay")

  lazy val upscanCallbackDelayEnabled: Boolean = config.get[Boolean]("upscan.delayCallbackUpdate")

  lazy val upscanCallbackUpdateDelay: Int = config.get[Int]("upscan.callbackUpdateDelay")

  lazy val upscanSuccessUrl: String = config.get[String]("upscan.successUrl")

  lazy val upscanFailureUrl: String = config.get[String]("upscan.failureUrl")

  lazy val acceptedFileTypes: String = config.get[String]("upscan.acceptedFileTypes")

  lazy val maxFileUploadSize: Int = config.get[Int]("upscan.maxFileSize")

  lazy val essttpBackendUrl = servicesConfig.baseUrl("essttp-backend")


  lazy val payApiUrl = servicesConfig.baseUrl("pay-api")
  lazy val platformPenaltiesFrontendHost = servicesConfig.getString("penalties-appeals-frontend-host")
}

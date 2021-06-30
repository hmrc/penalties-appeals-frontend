/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.i18n.Lang
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {
  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  val en: String            = "en"
  val cy: String            = "cy"
  val defaultLanguage: Lang = Lang(en)

  lazy val signInUrl: String = config.get[String]("signIn.url")

  lazy val penaltiesServiceBaseUrl: String = servicesConfig.baseUrl("penalties")

  def appealDataForPenaltyAndEnrolmentKey(penaltyId: String, enrolmentKey: String): String = {
    s"$penaltiesServiceBaseUrl/penalties/appeals-data/late-submissions?penaltyId=$penaltyId&enrolmentKey=$enrolmentKey"
  }

  def submitAppealUrl(enrolmentKey: String) = penaltiesServiceBaseUrl + config.get[String]("reasonableExcuse.submitUrl") + s"?enrolmentKey=$enrolmentKey"

  lazy val reasonableExcuseFetchUrl: String = penaltiesServiceBaseUrl + config.get[String]("reasonableExcuse.fetchUrl")

  lazy val signInContinueBaseUrl: String = config.get[String]("signIn.continueBaseUrl")

  lazy val signInContinueUrl: String = SafeRedirectUrl(signInContinueBaseUrl + controllers.routes.AppealStartController.onPageLoad().url).encodedUrl

  lazy val signOutUrl: String = config.get[String]("signOut.url") + signInContinueUrl

  lazy val timeoutPeriod: Int = config.get[Int]("timeout.period")

  lazy val timeoutCountdown: Int = config.get[Int]("timeout.countDown")

  lazy val penaltiesFrontendUrl: String = config.get[String]("urls.penalties-frontend")

  lazy val vatOverviewUrl: String = config.get[String]("urls.vatOverview")

  lazy val feedbackUrl: String = config.get[String]("urls.feedbackUrl")

  lazy val daysRequiredForLateAppeal: Int = config.get[Int]("constants.daysRequiredForLateAppeal")
}

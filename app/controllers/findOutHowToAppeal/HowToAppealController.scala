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

package controllers.findOutHowToAppeal

import config.featureSwitches.{FeatureSwitching, ShowFindOutHowToAppealJourney}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRetrievalAction}
import models.pages.{HowToAppealPage, PageMode}
import models.NormalMode
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{CurrencyFormatter, SessionKeys}
import views.html.findOutHowToAppeal.HowToAppealPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HowToAppealController @Inject()(howToAppealPage: HowToAppealPage, errorHandler: ErrorHandler)
                                     (implicit mcc: MessagesControllerComponents,
                                            appConfig: AppConfig,
                                            authorise: AuthPredicate,
                                            dataRetrieval: DataRetrievalAction,
                                            val config: Configuration,
                                            ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  val pageMode: PageMode = PageMode(HowToAppealPage, NormalMode)


  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval).async {
    implicit request => {
      if(appConfig.isEnabled(ShowFindOutHowToAppealJourney)) {
        val vatAmount: BigDecimal = request.answers.getAnswer[BigDecimal](SessionKeys.vatAmount).get
        Future(Ok(howToAppealPage(CurrencyFormatter.parseBigDecimalToFriendlyValue(vatAmount), pageMode)))
      } else {
        errorHandler.onClientError(request, NOT_FOUND, "")
      }
    }
  }
}

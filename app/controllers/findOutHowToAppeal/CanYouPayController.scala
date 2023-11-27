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
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import forms.CanYouPayForm.canYouPayForm
import helpers.FormProviderHelper
import javax.inject.Inject
import navigation.Navigation
import models.NormalMode
import models.pages.{CanYouPayPage, PageMode}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{CurrencyFormatter, SessionKeys}
import viewtils.RadioOptionHelper
import views.html.findOutHowToAppeal.CanYouPayPage

import scala.concurrent.{ExecutionContext, Future}

class CanYouPayController @Inject()(page: CanYouPayPage, errorHandler: ErrorHandler)
                                            (implicit mcc: MessagesControllerComponents,
                                             appConfig: AppConfig,
                                             authorise: AuthPredicate,
                                             dataRequired: DataRequiredAction,
                                             dataRetrieval: DataRetrievalAction,
                                             navigation: Navigation,
                                             sessionService: SessionService,
                                             val config: Configuration,
                                             ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {
  val pageMode: PageMode = PageMode(CanYouPayPage, NormalMode)

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit request => {
      if(appConfig.isEnabled(ShowFindOutHowToAppealJourney)) {
        val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(canYouPayForm,
          SessionKeys.canYouPay,
          request.answers)
        val vatAmount: BigDecimal = 123.45 //TODO Get from session
        val radioOptions = RadioOptionHelper.radioOptionsForCanYouPayPage(formProvider, CurrencyFormatter.parseBigDecimalToFriendlyValue(vatAmount))
        val postAction = controllers.findOutHowToAppeal.routes.CanYouPayController.onSubmit()
        val willUserPay = request.answers.setAnswer[String](SessionKeys.willUserPay, "yes")
        sessionService.updateAnswers(willUserPay).map { //TODO: This should be moved to the Can You Pay Your VAT Bill page when that is implemented
          _ => Ok(page(formProvider, radioOptions, postAction, pageMode))
        }
      } else {
        errorHandler.onClientError(request, NOT_FOUND, "")
      }
    }
  }
  def onSubmit(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async { implicit userRequest => {
    canYouPayForm
      .bindFromRequest()
      .fold(
        form => {
          val vatAmount: BigDecimal = 123.45 //TODO Get from session
          val radioOptions = RadioOptionHelper.radioOptionsForCanYouPayPage(form, CurrencyFormatter.parseBigDecimalToFriendlyValue(vatAmount))
          val postAction = controllers.findOutHowToAppeal.routes.CanYouPayController.onSubmit()
          Future(BadRequest(page(form, radioOptions, postAction, pageMode)))
        },
        ableToPay => {
          val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.canYouPay, ableToPay)
          sessionService.updateAnswers(updatedAnswers).map {
            _ => Redirect(navigation.nextPage(CanYouPayPage, NormalMode, Some(ableToPay)))
          }
        }
      )
  }
  }
}
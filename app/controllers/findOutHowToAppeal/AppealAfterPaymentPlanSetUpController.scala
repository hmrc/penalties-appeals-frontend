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
import forms.AppealAfterPaymentPlanSetUpForm.appealAfterPaymentPlanSetUpForm
import helpers.FormProviderHelper
import models._
import models.pages.{AppealAfterPaymentPlanSetUpPage, PageMode}
import navigation.Navigation
import play.api.Configuration
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.govukfrontend.views.Aliases.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.findOutHowToAppeal.AppealAfterPaymentPlanSetUpPage
import viewtils.RadioOptionHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AppealAfterPaymentPlanSetUpController @Inject()(appealAfterPaymentPlanSetUpPage: AppealAfterPaymentPlanSetUpPage, errorHandler: ErrorHandler)
                                                     (implicit mcc: MessagesControllerComponents,
                                                      appConfig: AppConfig,
                                                      authorise: AuthPredicate,
                                                      dataRequired: DataRequiredAction,
                                                      dataRetrieval: DataRetrievalAction,
                                                      navigation: Navigation,
                                                      sessionService: SessionService,
                                                      val config: Configuration, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {
  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(AppealAfterPaymentPlanSetUpPage, mode)


  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {

    implicit request =>
      if (appConfig.isEnabled(ShowFindOutHowToAppealJourney)) {
        val formProvider: Form[String] = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(
          appealAfterPaymentPlanSetUpForm,
          SessionKeys.appealAfterPaymentPlanSetUp,
          request.answers
        )
        val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(formProvider, noContent = "common.radioOption.no.2", noHint = Some("common.radioOption.no.hint"))
        val postAction = controllers.findOutHowToAppeal.routes.AppealAfterPaymentPlanSetUpController.onSubmit()
        val willUserPay = request.answers.setAnswer[String](SessionKeys.willUserPay, "no")
        sessionService.updateAnswers(willUserPay).map { //TODO: This should be moved to the Can You Pay Your VAT Bill page when that is implemented
          _ => Ok(appealAfterPaymentPlanSetUpPage(formProvider, radioOptionsToRender, postAction, pageMode(NormalMode)))
        }
      }
      else {
        Future(errorHandler.notFoundError(request))
      }

  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async { implicit userRequest => {
    appealAfterPaymentPlanSetUpForm
      .bindFromRequest()
      .fold(
        form => {
          val postAction = controllers.findOutHowToAppeal.routes.AppealAfterPaymentPlanSetUpController.onSubmit()
          val radioOptionsToRender: Seq[RadioItem] = RadioOptionHelper.yesNoRadioOptions(form, noContent = "common.radioOption.no.2", noHint = Some("common.radioOption.no.hint"))
          Future(BadRequest(appealAfterPaymentPlanSetUpPage(form, radioOptionsToRender, postAction, pageMode(NormalMode))))
        },
        setUpPaymentPlan => {
          val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.appealAfterPaymentPlanSetUp, setUpPaymentPlan)
          sessionService.updateAnswers(updatedAnswers).map {
            _ => Redirect(navigation.nextPage(AppealAfterPaymentPlanSetUpPage, NormalMode, Some(setUpPaymentPlan)))
          }
        }
      )
  }
  }
}

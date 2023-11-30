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

import config.featureSwitches.{FeatureSwitching, ShowFindOutHowToAppealLSPJourney}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import forms.HasHMRCConfirmedRegistrationCancellationForm.hasHMRCConfirmedRegistrationCancellationForm
import helpers.FormProviderHelper
import models.NormalMode
import models.pages.{HasHMRCConfirmedRegistrationCancellationPage, PageMode}
import navigation.Navigation
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.findOutHowToAppeal.HasHMRCConfirmedRegistrationCancellationPage
import viewtils.RadioOptionHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HasHMRCConfirmedRegistrationCancellationController @Inject()(
                                                                    page: HasHMRCConfirmedRegistrationCancellationPage,
                                                                    errorHandler: ErrorHandler
                                                                  )(implicit mcc: MessagesControllerComponents,
                                                                    appConfig: AppConfig,
                                                                    authorise: AuthPredicate,
                                                                    dataRequired: DataRequiredAction,
                                                                    dataRetrieval: DataRetrievalAction,
                                                                    navigation: Navigation,
                                                                    sessionService: SessionService,
                                                                    val config: Configuration,
                                                                    ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  val pageMode: PageMode = PageMode(HasHMRCConfirmedRegistrationCancellationPage, NormalMode)

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit request => {
      if (appConfig.isEnabled(ShowFindOutHowToAppealLSPJourney)) {
        val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(hasHMRCConfirmedRegistrationCancellationForm,
          SessionKeys.hasHMRCConfirmedRegistrationCancellation,
          request.answers)
        val radioOptions = RadioOptionHelper.yesNoRadioOptions(formProvider)
        val postAction = controllers.findOutHowToAppeal.routes.HasHMRCConfirmedRegistrationCancellationController.onSubmit()
        Ok(page(formProvider, radioOptions, postAction, pageMode))
      } else {
        errorHandler.notFoundError(request)
      }
    }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit userRequest => {
      hasHMRCConfirmedRegistrationCancellationForm.bindFromRequest().fold(
        form => {
          val radioOptions = RadioOptionHelper.yesNoRadioOptions(form)
          val postAction = controllers.findOutHowToAppeal.routes.HasHMRCConfirmedRegistrationCancellationController.onSubmit()
          Future(BadRequest(page(form, radioOptions, postAction, pageMode)))
        },
        answer => {
          val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.hasHMRCConfirmedRegistrationCancellation, answer)
          sessionService.updateAnswers(updatedAnswers).map {
            _ => Redirect(navigation.nextPage(HasHMRCConfirmedRegistrationCancellationPage, NormalMode, Some(answer)))
          }
        }
      )
    }
  }
}

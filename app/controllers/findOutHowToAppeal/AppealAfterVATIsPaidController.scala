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

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, DataRetrievalAction}
import forms.DoYouWantToPayNowForm.doYouWantToPayNowForm
import helpers.FormProviderHelper
import models.monitoring.PenaltyAppealDidYouGoOffToPayModel
import models.pages.{PageMode, YouCanAppealOnlinePage}
import models.{Mode, NormalMode, UserRequest}
import navigation.Navigation
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.findOutHowToAppeal.AppealAfterVATIsPaidPage
import viewtils.RadioOptionHelper
import services.monitoring.AuditService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AppealAfterVATIsPaidController @Inject()(page: AppealAfterVATIsPaidPage, auditService: AuditService, errorHandler: ErrorHandler)
                                              (implicit mcc: MessagesControllerComponents,
                                               appConfig: AppConfig,
                                               authorise: AuthPredicate,
                                               dataRetrieval: DataRetrievalAction,
                                               navigation: Navigation,
                                               sessionService: SessionService,
                                               val config: Configuration,
                                               ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  val pageMode: Mode => PageMode = (mode: Mode) => PageMode(YouCanAppealOnlinePage, mode)

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval).async {
    implicit request => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(doYouWantToPayNowForm,
        SessionKeys.doYouWantToPayNow,
        request.answers)
      val radioOptions = RadioOptionHelper.yesNoRadioOptions(formProvider, noContent = "common.radioOption.no.2", noHint = Some("common.radioOption.no.hint"))
      val postAction = controllers.findOutHowToAppeal.routes.AppealAfterVATIsPaidController.onSubmit()
      val willUserPay = request.answers.setAnswer[String](SessionKeys.willUserPay, "yes")
      sessionService.updateAnswers(willUserPay).map { //TODO: This should be moved to the Can You Pay Your VAT Bill page when that is implemented
        _ => Ok(page(formProvider, radioOptions, postAction, pageMode(NormalMode)))
      }
    }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRetrieval).async { implicit userRequest => {
    doYouWantToPayNowForm.bindFromRequest().fold(
      form => {
        val postAction = controllers.findOutHowToAppeal.routes.AppealAfterVATIsPaidController.onSubmit()
        val radioOptions = RadioOptionHelper.yesNoRadioOptions(form, noContent = "common.radioOption.no.2", noHint = Some("common.radioOption.no.hint"))
        Future(BadRequest(page(form, radioOptions, postAction, pageMode(NormalMode))))
      },
      payYourVAT => {
        val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.doYouWantToPayNow, payYourVAT)
        auditDidTheUserGoOffToPay(payYourVAT)
        sessionService.updateAnswers(updatedAnswers).map {
          _ => Redirect(navigation.nextPage(YouCanAppealOnlinePage, NormalMode, Some(payYourVAT)))
        }
      }
    )
  }
  }

  def auditDidTheUserGoOffToPay(didUserGoToPayNow: String)(implicit hc: HeaderCarrier, request: UserRequest[_]): Unit = {
    val vatAmount: BigDecimal = request.answers.getAnswer[BigDecimal](SessionKeys.vatAmount).getOrElse(0)
    val amountToBePaidInPence : String = (vatAmount * 100).toString
    val chargeReference : String = request.answers.getAnswer[String](SessionKeys.principalChargeReference).getOrElse("")
    val auditModel = PenaltyAppealDidYouGoOffToPayModel(amountToBePaidInPence, chargeReference, didUserGoToPayNow)
    auditService.audit(auditModel)
  }
}

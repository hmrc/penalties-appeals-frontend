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
import forms.CanYouPayForm.canYouPayForm
import helpers.FormProviderHelper
import models.monitoring.PenaltyAppealPaidInFullAudit

import javax.inject.Inject
import navigation.Navigation
import models.{NormalMode, UserRequest}
import models.pages.{CanYouPayPage, PageMode}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import services.monitoring.AuditService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{CurrencyFormatter, SessionKeys}
import viewtils.RadioOptionHelper
import views.html.findOutHowToAppeal.CanYouPayPage

import scala.concurrent.{ExecutionContext, Future}

class CanYouPayController @Inject()(page: CanYouPayPage, errorHandler: ErrorHandler)
                                   (implicit mcc: MessagesControllerComponents,
                                    appConfig: AppConfig,
                                    authorise: AuthPredicate,
                                    dataRetrieval: DataRetrievalAction,
                                    auditService: AuditService,
                                    navigation: Navigation,
                                    sessionService: SessionService,
                                    val config: Configuration,
                                    ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {
  val pageMode: PageMode = PageMode(CanYouPayPage, NormalMode)

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval).async {
    implicit request => {
      val formProvider = FormProviderHelper.getSessionKeyAndAttemptToFillAnswerAsString(canYouPayForm,
        SessionKeys.willUserPay,
        request.answers)
      val vatAmount: BigDecimal = request.answers.getAnswer[BigDecimal](SessionKeys.vatAmount).get
      val radioOptions = RadioOptionHelper.radioOptionsForCanYouPayPage(formProvider, CurrencyFormatter.parseBigDecimalToFriendlyValue(vatAmount))
      val postAction = controllers.findOutHowToAppeal.routes.CanYouPayController.onSubmit()
      val willUserPay = request.answers.setAnswer[String](SessionKeys.willUserPay, "yes")
      sessionService.updateAnswers(willUserPay).map {
        _ => Ok(page(formProvider, radioOptions, postAction, pageMode))
      }
    }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen dataRetrieval).async { implicit userRequest => {
    canYouPayForm
      .bindFromRequest()
      .fold(
        form => {
          val vatAmount: BigDecimal = userRequest.answers.getAnswer[BigDecimal](SessionKeys.vatAmount).get
          val radioOptions = RadioOptionHelper.radioOptionsForCanYouPayPage(form, CurrencyFormatter.parseBigDecimalToFriendlyValue(vatAmount))
          val postAction = controllers.findOutHowToAppeal.routes.CanYouPayController.onSubmit()
          Future(BadRequest(page(form, radioOptions, postAction, pageMode)))
        },
        ableToPay => {
          val updatedAnswers = userRequest.answers.setAnswer[String](SessionKeys.willUserPay, ableToPay)
          if (ableToPay == "paid") { // Audit only for paid status
            auditDidTheUserAlreadyPay()
          }
          sessionService.updateAnswers(updatedAnswers).map {
            _ => Redirect(navigation.nextPage(CanYouPayPage, NormalMode, Some(ableToPay)))
          }
        }
      )
  }
  }
  def auditDidTheUserAlreadyPay()(implicit hc: HeaderCarrier, request: UserRequest[_]): Unit = {
    val vatAmount: BigDecimal = request.answers.getAnswer[BigDecimal](SessionKeys.vatAmount).getOrElse(0)
    val amountTobePaidInPence: String = (vatAmount * 100).toString
    val chargeReference: String = request.answers.getAnswer[String](SessionKeys.principalChargeReference).getOrElse("")
    val auditModel = PenaltyAppealPaidInFullAudit(amountTobePaidInPence, chargeReference)
    auditService.audit(auditModel)
  }
}
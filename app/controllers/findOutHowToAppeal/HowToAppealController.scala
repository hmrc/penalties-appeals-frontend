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
import models.pages.{HowToAppealPage, PageMode}
import models.{NormalMode, UserRequest}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.{CurrencyFormatter, SessionKeys}
import views.html.findOutHowToAppeal.HowToAppealPage

import javax.inject.Inject
import scala.concurrent.Future

object HowToAppealController {
  sealed trait HowToAppealError {
    def message: String
  }

  final case class MissingSessionValue(sessionKey: String) extends HowToAppealError {
    override val message: String =
      s"[HowToAppealController][onPageLoad] - Missing required session value for $sessionKey"
  }
}

class HowToAppealController @Inject()(howToAppealPage: HowToAppealPage, errorHandler: ErrorHandler)
                                     (implicit mcc: MessagesControllerComponents,
                                      appConfig: AppConfig,
                                      authorise: AuthPredicate,
                                      dataRetrieval: DataRetrievalAction,
                                      val config: Configuration) extends FrontendController(mcc) with I18nSupport {

  val pageMode: PageMode = PageMode(HowToAppealPage, NormalMode)

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval).async {
    implicit request =>
      pageVatAmount match {
        case Right(vatAmount) =>
          Future.successful(Ok(howToAppealPage(vatAmount, pageMode)))
        case Left(error) =>
          renderMissingSessionValue(error)
      }
  }

  private def pageVatAmount(implicit request: UserRequest[_]): Either[HowToAppealController.HowToAppealError, String] = {
    request.answers
      .getAnswer[BigDecimal](SessionKeys.vatAmount)
      .toRight(HowToAppealController.MissingSessionValue(SessionKeys.vatAmount))
      .map(CurrencyFormatter.parseBigDecimalToFriendlyValue)
  }

  private def renderMissingSessionValue(error: HowToAppealController.HowToAppealError)
                                       (implicit request: UserRequest[_]): Future[Result] = {
    logger.warn(error.message)
    Future.successful(errorHandler.showInternalServerError(Some(request)))
  }
}

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
import models.NormalMode
import models.pages.{PageMode, WaitForPaymentToClearPage}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.findOutHowToAppeal.WaitForPaymentToClearPage

import javax.inject.Inject

class WaitForPaymentToClearController @Inject()(page: WaitForPaymentToClearPage,
                                                errorHandler: ErrorHandler
                                               )(implicit mcc: MessagesControllerComponents,
                                                 appConfig: AppConfig,
                                                 authorise: AuthPredicate,
                                                 dataRetrieval: DataRetrievalAction,
                                                 val config: Configuration) extends FrontendController(mcc) with I18nSupport {

  val pageMode: PageMode = PageMode(WaitForPaymentToClearPage, NormalMode)

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval) {
    implicit request => {
      val isCA = request.answers.getAnswer[Boolean](SessionKeys.isCaLpp).getOrElse(false)

      if (!isCA) {
        Ok(page(pageMode))
      } else {
        errorHandler.notFoundError(request)
      }
    }
  }


}

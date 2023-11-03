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

package controllers

import config.{AppConfig, ErrorHandler}
import config.featureSwitches.{FeatureSwitching, ShowCanYouPayYourVATBillPages}
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.IfYouvePaidYourVATPage

import scala.concurrent.{ExecutionContext, Future}

class IfYouvePaidYourVATController @Inject()(ifYouvePaidYourVATPage: IfYouvePaidYourVATPage, errorHandler: ErrorHandler)
                                            (implicit mcc: MessagesControllerComponents,
                                             appConfig: AppConfig,
                                             authorise: AuthPredicate,
                                             dataRequired: DataRequiredAction,
                                             dataRetrieval: DataRetrievalAction,
                                             val config: Configuration, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired).async {
    implicit request => {
      if(isEnabled(ShowCanYouPayYourVATBillPages)) {
        Future(Ok(ifYouvePaidYourVATPage()))
      } else {
        errorHandler.onClientError(request, NOT_FOUND, "")
      }
    }
  }

}

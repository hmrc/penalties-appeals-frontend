/*
 * Copyright 2022 HM Revenue & Customs
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

import config.AppConfig
import controllers.predicates.{AuthPredicate, DataRetrievalAction}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.errors.ServiceUnavailablePage

import javax.inject.Inject

class ServiceUnavailableController @Inject()(serviceUnavailablePage: ServiceUnavailablePage)
                                            (implicit mcc: MessagesControllerComponents,
                                             appConfig: AppConfig,
                                             authorise: AuthPredicate,
                                             dataRetrieval: DataRetrievalAction) extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval) {
    implicit request => InternalServerError(serviceUnavailablePage())
  }
}

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

import controllers.predicates.{AuthPredicate, DataRetrievalAction}
import models.Mode
import models.pages.Page
import navigation.Navigation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject

class PreviousPageController @Inject()(navigation: Navigation)
                                      (implicit mcc: MessagesControllerComponents,
                                       authorise: AuthPredicate,
                                       dataRetrieval: DataRetrievalAction) extends FrontendController(mcc) with I18nSupport {
  def previousPage(pageName: String, mode: Mode, isJsEnabled: Boolean): Action[AnyContent] = (authorise andThen dataRetrieval) {
    implicit request => {
      val page = Page.find(pageName)
      Redirect(navigation.previousPage(page, mode, isJsEnabled))
    }
  }
}

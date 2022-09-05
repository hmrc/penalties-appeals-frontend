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
import controllers.predicates.{AuthPredicate, DataRequiredAction, DataRetrievalAction}
import models.PenaltyTypeEnum
import models.PenaltyTypeEnum.{Additional, Late_Payment}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionKeys
import views.html.errors.IncompleteSessionDataPage

import javax.inject.Inject

class IncompleteSessionDataController @Inject()(incompleteSessionDataPage: IncompleteSessionDataPage)
                                               (implicit mcc: MessagesControllerComponents,
                                                appConfig: AppConfig,
                                                authorise: AuthPredicate,
                                                dataRequired: DataRequiredAction,
                                                dataRetrieval: DataRetrievalAction
                                                ) extends FrontendController(mcc) with I18nSupport {

  //Assuming that the page before this has completed all the basic session data checks
  def onPageLoad(): Action[AnyContent] = (authorise andThen dataRetrieval andThen dataRequired) {
    implicit request => {
      val penaltyId = request.answers.getAnswer[String](SessionKeys.penaltyNumber).get
      val isLPP = request.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).contains(Late_Payment)
      val isAdditional = request.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).contains(Additional)
      Ok(incompleteSessionDataPage(penaltyId, isLPP, isAdditional))
    }
  }
}

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

import models.PenaltyTypeEnum
import models.session.UserAnswers
import org.mongodb.scala.Document
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, _}
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.LocalDate

class FindOutHowToAppealStartControllerISpec extends IntegrationSpecCommonBase {
  val controller: FindOutHowToAppealStartController = injector.instanceOf[FindOutHowToAppealStartController]
  def lppUserAnswers (answers: JsObject = Json.obj(), journeyId: Option[String] = None): UserAnswers = UserAnswers(journeyId.getOrElse("1234"), Json.obj(
    SessionKeys.penaltyNumber -> "1234",
    SessionKeys.appealType -> PenaltyTypeEnum.Late_Payment,
    SessionKeys.startDateOfPeriod -> LocalDate.parse("2023-01-01"),
    SessionKeys.endDateOfPeriod -> LocalDate.parse("2023-01-31"),
    SessionKeys.dueDateOfPeriod -> LocalDate.parse("2023-03-07"),
    SessionKeys.dateCommunicationSent -> LocalDate.parse("2023-03-12")
  ) ++ answers)
  class Setup {
    await(userAnswersRepository.collection.deleteMany(Document()).toFuture())
  }

  "GET /start-find-out-how-to-appeal" should {
    "redirect to can you pay page when wanting to appeal a no Central Assesment LPP as a trader" in new UserAnswersSetup(lppUserAnswers(Json.obj(
      SessionKeys.vatAmount -> BigDecimal(123.45),
      SessionKeys.principalChargeReference -> "123456789",
      SessionKeys.isCaLpp -> false
    ))) {

      val result = controller.startFindOutHowToAppeal()(fakeRequest)
      await(result).header.status shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.findOutHowToAppeal.routes.CanYouPayController.onPageLoad().url
    }
  }
}

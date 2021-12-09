/*
 * Copyright 2021 HM Revenue & Customs
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

import config.featureSwitches.{FeatureSwitching, NonJSRouting}
import models.NormalMode
import models.upload.{FailureDetails, FailureReasonEnum, UploadDetails, UploadJourney, UploadStatusEnum}
import org.jsoup.Jsoup
import org.mongodb.scala.Document
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Cookie, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UploadJourneyRepository
import stubs.AuthStub
import stubs.UpscanStub.successfulInitiateCall
import utils.{IntegrationSpecCommonBase, SessionKeys}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class OtherReasonControllerISpec extends IntegrationSpecCommonBase {
  val controller: OtherReasonController = injector.instanceOf[OtherReasonController]
  val featureSwitching: FeatureSwitching = injector.instanceOf[FeatureSwitching]
  val repository: UploadJourneyRepository = injector.instanceOf[UploadJourneyRepository]

  class Setup {
    await(repository.collection.deleteMany(Document()).toFuture())
  }

  "GET /when-inability-to-manage-account-happened" should {
    "return 200 (OK) when the user is authorised" in new Setup {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-inability-to-manage-account-happened").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-inability-to-manage-account-happened")
      val request = await(controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/when-inability-to-manage-account-happened").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onPageLoadForWhenDidBecomeUnable(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-inability-to-manage-account-happened").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "POST /when-inability-to-manage-account-happened" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new Setup {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      ).withJsonBody(
        Json.parse(
          """
            |{
            | "date.day": "08",
            | "date.month": "02",
            | "date.year": "2021"
            |}
            |""".stripMargin)
      )
      val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.OtherReasonController.onPageLoadForWhyReturnSubmittedLate(NormalMode).url
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.whenDidBecomeUnable).get shouldBe LocalDate.parse("2021-02-08").toString
    }

    "return 400 (BAD_REQUEST)" when {
      "the date submitted is in the future" in new Setup {
        val fakeRequestWithCorrectKeysAndInvalidBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
          (SessionKeys.journeyId, "1234")
        ).withJsonBody(
          Json.parse(
            """
              |{
              | "date.day": "08",
              | "date.month": "02",
              | "date.year": "2025"
              |}
              |""".stripMargin)
        )
        val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeysAndInvalidBody))
        request.header.status shouldBe BAD_REQUEST
      }

      "no body is submitted" in new Setup {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
          (SessionKeys.journeyId, "1234")
        )
        val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe BAD_REQUEST
      }

      "the date submitted is missing a field" in new Setup {
        val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
          (SessionKeys.journeyId, "1234")
        )

        val noDayJsonBody: JsValue = Json.parse(
          """
            |{
            | "date.day": "",
            | "date.month": "02",
            | "date.year": "2025"
            |}
            |""".stripMargin
        )

        val noMonthJsonBody: JsValue = Json.parse(
          """
            |{
            | "date.day": "02",
            | "date.month": "",
            | "date.year": "2025"
            |}
            |""".stripMargin
        )

        val noYearJsonBody: JsValue = Json.parse(
          """
            |{
            | "date.day": "02",
            | "date.month": "02",
            | "date.year": ""
            |}
            |""".stripMargin
        )
        val requestNoDay = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeys.withJsonBody(noDayJsonBody)))
        requestNoDay.header.status shouldBe BAD_REQUEST

        val requestNoMonth = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeys.withJsonBody(noMonthJsonBody)))
        requestNoMonth.header.status shouldBe BAD_REQUEST

        val requestNoYear = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithCorrectKeys.withJsonBody(noYearJsonBody)))
        requestNoYear.header.status shouldBe BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened")
      val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/when-inability-to-manage-account-happened").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onSubmitForWhenDidBecomeUnable(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/when-inability-to-manage-account-happened").post(""))
      request.status shouldBe SEE_OTHER
    }
  }

  "GET /upload-evidence-for-the-appeal" should {
    "return 200 (OK) when the user is authorised" in new Setup {
      val fakeRequestWithCorrectKeysAndJS: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-evidence-for-the-appeal").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      ).withCookies(Cookie("jsenabled", "true"))
      val request = await(controller.onPageLoadForUploadEvidence(NormalMode)(fakeRequestWithCorrectKeysAndJS))
      request.header.status shouldBe OK
    }

    "return 303 (SEE_OTHER) when the user does not have JavaScript enabled" in new Setup {
      featureSwitching.disableFeatureSwitch(NonJSRouting)
      val fakeRequestWithCorrectKeys = FakeRequest("GET", "/upload-evidence-for-the-appeal").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForUploadEvidence(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe SEE_OTHER
      featureSwitching.enableFeatureSwitch(NonJSRouting)
    }

    "return 303 (SEE_OTHER) when the user does not have JavaScript enabled but the feature switch is enabled" in new Setup {
      featureSwitching.enableFeatureSwitch(NonJSRouting)
      val fakeRequestWithCorrectKeys = FakeRequest("GET", "/upload-evidence-for-the-appeal").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForUploadEvidence(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe SEE_OTHER
      featureSwitching.disableFeatureSwitch(NonJSRouting)
    }

    "return 200 (OK) when the user does have JavaScript enabled and the feature switch is disabled" in new Setup {
      featureSwitching.disableFeatureSwitch(NonJSRouting)
      val fakeRequestWithCorrectKeysAndJS: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-evidence-for-the-appeal").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      ).withCookies(Cookie("jsenabled", "true"))
      val request = await(controller.onPageLoadForUploadEvidence(NormalMode)(fakeRequestWithCorrectKeysAndJS))
      request.header.status shouldBe OK
      featureSwitching.enableFeatureSwitch(NonJSRouting)
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-evidence-for-the-appeal")
      val request = await(controller.onPageLoadForUploadEvidence(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-evidence-for-the-appeal").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onPageLoadForUploadEvidence(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/upload-evidence-for-the-appeal").get())
      request.status shouldBe SEE_OTHER
    }
  }


  "GET /why-was-the-vat-late" should {
    "return 200 (OK) when the user is authorised" in new Setup {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/why-was-the-vat-late").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe OK
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/why-was-the-vat-late")
      val request = await(controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/why-was-the-vat-late").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onPageLoadForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/why-was-the-vat-late").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "POST /why-was-the-vat-late" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct" in new Setup {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/why-was-the-vat-late").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      ).withJsonBody(Json.parse(
        """
          |{
          | "why-return-submitted-late-text": "Other Reason"
          |}
          |""".stripMargin)
      )
      val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody))
      request.header.status shouldBe SEE_OTHER
      request.header.headers("Location") shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadEvidence(NormalMode).url
      request.session(fakeRequestWithCorrectKeysAndCorrectBody).get(SessionKeys.whyReturnSubmittedLate).get shouldBe "Other Reason"
    }

    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in new Setup {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/why-was-the-vat-late").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
          (SessionKeys.journeyId, "1234")
        )
        val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/why-was-the-vat-late")
      val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/why-was-the-vat-late").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onSubmitForWhyReturnSubmittedLate(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/why-was-the-vat-late").post(""))
      request.status shouldBe SEE_OTHER
    }
  }

  "GET /upload-first-document" should {
    "return 200 (OK) when the user is authorised" in new Setup {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-first-document").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe OK
    }

    "return 303 (SEE_OTHER) when the users has JS active but the feature switch is disabled" in new Setup {
      featureSwitching.disableFeatureSwitch(NonJSRouting)
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-first-document").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      ).withCookies(Cookie("jsenabled", "true"))
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe SEE_OTHER
      featureSwitching.enableFeatureSwitch(NonJSRouting)
    }

    "return 200 (OK) when the users has JS active and the feature switch is enabled" in new Setup {
      featureSwitching.enableFeatureSwitch(NonJSRouting)
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-first-document").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      ).withCookies(Cookie("jsenabled", "true"))
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe OK
      featureSwitching.disableFeatureSwitch(NonJSRouting)
    }

    "return 400 (BAD_REQUEST) when the users upload has failed (preflight check)" in new Setup {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-first-document").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequestWithCorrectKeys.withSession(SessionKeys.errorCodeFromUpscan -> "EntityTooLarge")))
      request.header.status shouldBe BAD_REQUEST
    }

    "return 400 (BAD_REQUEST) when the users upload has failed (upscan check)" in new Setup {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-first-document").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequestWithCorrectKeys.withSession(SessionKeys.failureMessageFromUpscan -> "upscan.duplicateFile")))
      request.header.status shouldBe BAD_REQUEST
    }

    "return 500 (ISE) when the call to upscan fails for initiate call" in new Setup {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-first-document").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-first-document")
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-first-document").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onPageLoadForFirstFileUpload(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/upload-first-document").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "GET /uploaded-documents" should {
    "return OK and correct view" in new Setup {
      val callBackModel: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file/url"),
        uploadDetails = Some(UploadDetails(
          fileName = "file1.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
          checksum = "check1234",
          size = 2
        ))
      )
      await(repository.updateStateOfFileUpload("1234", callBackModel))
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/uploaded-documents").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequestWithCorrectKeys)
      status(request) shouldBe OK
    }

    "return OK and the correct view - showing the inset text for duplicate uploads" in new Setup {
      val callbackModel: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file/url"),
        uploadDetails = Some(UploadDetails(
          fileName = "file1.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
          checksum = "check1234",
          size = 2
        ))
      )
      await(repository.updateStateOfFileUpload("1234", callbackModel))
      await(repository.updateStateOfFileUpload("1234", callbackModel.copy(
        reference = "ref2",
        fileStatus = UploadStatusEnum.DUPLICATE))
      )
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/uploaded-documents").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequestWithCorrectKeys)
      status(request) shouldBe OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select(".govuk-inset-text").text() should startWith("File 1 has the same contents as File 2.")
    }

    "return OK and the correct view - showing the inset text for multiple duplicate uploads" in new Setup {
      val callbackModel: UploadJourney = UploadJourney(
        reference = "ref1",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file/url"),
        uploadDetails = Some(UploadDetails(
          fileName = "file1.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
          checksum = "check1234",
          size = 2
        ))
      )
      val callbackModel2: UploadJourney = UploadJourney(
        reference = "ref2",
        fileStatus = UploadStatusEnum.READY,
        downloadUrl = Some("download.file/url"),
        uploadDetails = Some(UploadDetails(
          fileName = "file2.txt",
          fileMimeType = "text/plain",
          uploadTimestamp = LocalDateTime.of(2018, 1, 1, 1, 1),
          checksum = "check1235",
          size = 3
        ))
      )
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/uploaded-documents").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      await(repository.updateStateOfFileUpload("1234", callbackModel))
      await(repository.updateStateOfFileUpload("1234", callbackModel2))
      await(repository.updateStateOfFileUpload("1234", callbackModel.copy(
        reference = "ref3",
        fileStatus = UploadStatusEnum.DUPLICATE))
      )
      await(repository.updateStateOfFileUpload("1234", callbackModel2.copy(
        reference = "ref4",
        fileStatus = UploadStatusEnum.DUPLICATE))
      )
      val request = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequestWithCorrectKeys)
      status(request) shouldBe OK
      val parsedBody = Jsoup.parse(contentAsString(request))
      parsedBody.select(".govuk-inset-text").text() should startWith("4 of the documents you have uploaded have the same contents.")
    }

    "return 303 (SEE_OTHER) when the user has no uploads" in new Setup {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/uploaded-documents").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequestWithCorrectKeys)
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
    }

    "return 303 (SEE_OTHER) when the user has no successful uploads" in new Setup {
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.WAITING)))
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/uploaded-documents").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = controller.onPageLoadForUploadComplete(NormalMode)(fakeRequestWithCorrectKeys)
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/uploaded-documents").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "GET /upload-another-document" should {

    "return 200 (OK) when the user is authorised" in new Setup {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-another-document").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe OK
    }

    "return 400 (BAD_REQUEST) when the users upload has failed (preflight check)" in new Setup {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-another-document").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequestWithCorrectKeys.withSession(SessionKeys.errorCodeFromUpscan -> "EntityTooLarge")))
      request.header.status shouldBe BAD_REQUEST
    }

    "return 400 (BAD_REQUEST) when the users upload has failed (upscan check)" in new Setup {
      successfulInitiateCall(
        """
          |{
          | "reference": "12345",
          | "uploadRequest": {
          |   "href": "12345",
          |   "fields": {}
          | }
          |}
          |""".stripMargin)
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-another-document").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequestWithCorrectKeys.withSession(SessionKeys.failureMessageFromUpscan -> "upscan.duplicateFile")))
      request.header.status shouldBe BAD_REQUEST
    }

    "return 500 (ISE) when the call to upscan fails for initiate call" in new Setup {
      val fakeRequestWithCorrectKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-another-document").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      )
      val request = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequestWithCorrectKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-another-document")
      val request = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("GET", "/upload-another-document").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onPageLoadForAnotherFileUpload(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/upload-another-document").get())
      request.status shouldBe SEE_OTHER
    }
  }

  "POST /remove-file-upload" should {
    "return an ISE when the form fails to bind" in new Setup {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/remove-file-upload").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234"))
      val request = controller.removeFileUpload(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody.withJsonBody(Json.parse(
        """
          |{
          | "file": "file1"
          |}
          |""".stripMargin)))
      status(request) shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to the first document upload page when there is no uploads left" in new Setup {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/remove-file-upload").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234"))
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.READY)))
      val request = controller.removeFileUpload(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody.withJsonBody(Json.parse(
        """
          |{
          | "fileReference": "file1"
          |}
          |""".stripMargin)))
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
    }

    "reload the upload list when there is more uploads" in new Setup {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/remove-file-upload").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234"))
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file1", UploadStatusEnum.READY)))
      await(repository.updateStateOfFileUpload("1234", UploadJourney("file2", UploadStatusEnum.READY)))
      val request = controller.removeFileUpload(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody.withJsonBody(Json.parse(
        """
          |{
          | "fileReference": "file1"
          |}
          |""".stripMargin)))
      status(request) shouldBe SEE_OTHER
      redirectLocation(request).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
    }

  }

  "POST /upload-taking-longer-than-expected" should {
    val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/upload-taking-longer-than-expected").withSession(
      (SessionKeys.penaltyId, "1234"),
      (SessionKeys.appealType, "Late_Submission"),
      (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
      (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
      (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
      (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
      (SessionKeys.journeyId, "J1234"))
    "show an ISE" when {
      "the repository doesn't have a status for this file under this journey" in new Setup {
        val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody.withSession(SessionKeys.fileReference -> "file1"))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect back to the 'upload taking longer than expected' page when the recursive call times out" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.WAITING)))
      val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody.withSession(SessionKeys.fileReference -> "file1"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadTakingLongerThanExpected(NormalMode).url
    }

    "redirect to the non-JS first file upload page when there is an error from Upscan" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.FAILED, failureDetails = Some(FailureDetails(FailureReasonEnum.REJECTED, "upscan.invalidMimeType")))))
      val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody.withSession(
        SessionKeys.fileReference -> "file1",
        SessionKeys.isAddingAnotherDocument -> "false"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForFirstFileUpload(NormalMode).url
      await(result).session(FakeRequest()).get(SessionKeys.failureMessageFromUpscan) -> "upscan.invalidMimeType"
    }

    "redirect to the non-JS upload another document page when there is an error from Upscan" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.FAILED, failureDetails = Some(FailureDetails(FailureReasonEnum.REJECTED, "upscan.invalidMimeType")))))
      val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody
        .withSession(SessionKeys.fileReference -> "file1",
          SessionKeys.isAddingAnotherDocument -> "true"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(NormalMode).url
      await(result).session(FakeRequest()).get(SessionKeys.failureMessageFromUpscan) -> "upscan.invalidMimeType"
    }

    "redirect to the successful upload page when there is no error from Upscan" in new Setup {
      await(repository.updateStateOfFileUpload("J1234", UploadJourney("file1", UploadStatusEnum.READY)))
      val result: Future[Result] = controller.onSubmitForUploadTakingLongerThanExpected(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody
        .withSession(SessionKeys.fileReference -> "file1"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForUploadComplete(NormalMode).url
    }
  }

  "POST /uploaded-documents" should {
    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - going to another file upload page when user specifies 'yes'" in new Setup {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/uploaded-documents").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      ).withJsonBody(Json.parse(
        """
          |{
          | "value": "yes"
          |}
          |""".stripMargin)
      )
      val result = controller.onSubmitForUploadComplete(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.OtherReasonController.onPageLoadForAnotherFileUpload(NormalMode).url
    }

    "return 303 (SEE_OTHER) and add the session key to the session when the body is correct - going to another file upload page when user specifies 'no'" in new Setup {
      val fakeRequestWithCorrectKeysAndCorrectBody: FakeRequest[AnyContent] = FakeRequest("POST", "/uploaded-documents").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
        (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
        (SessionKeys.journeyId, "1234")
      ).withJsonBody(Json.parse(
        """
          |{
          | "value": "no"
          |}
          |""".stripMargin)
      )
      val result = controller.onSubmitForUploadComplete(NormalMode)(fakeRequestWithCorrectKeysAndCorrectBody)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe controllers.routes.MakingALateAppealController.onPageLoad().url
    }

    "return 400 (BAD_REQUEST)" when {
      "no body is submitted" in new Setup {
        val fakeRequestWithCorrectKeysAndNoBody: FakeRequest[AnyContent] = FakeRequest("POST", "/uploaded-documents").withSession(
          (SessionKeys.penaltyId, "1234"),
          (SessionKeys.appealType, "Late_Submission"),
          (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00"),
          (SessionKeys.dueDateOfPeriod, "2020-02-07T12:00:00"),
          (SessionKeys.dateCommunicationSent, "2020-02-08T12:00:00"),
          (SessionKeys.journeyId, "1234")
        )
        val request = await(controller.onSubmitForUploadComplete(NormalMode)(fakeRequestWithCorrectKeysAndNoBody))
        request.header.status shouldBe BAD_REQUEST
      }
    }

    "return 500 (ISE) when the user is authorised but the session does not contain the correct keys" in new Setup {
      val fakeRequestWithNoKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/uploaded-documents")
      val request = await(controller.onSubmitForUploadComplete(NormalMode)(fakeRequestWithNoKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (ISE) when the user is authorised but the session does not contain ALL correct keys" in new Setup {
      val fakeRequestWithIncompleteKeys: FakeRequest[AnyContent] = FakeRequest("POST", "/uploaded-documents").withSession(
        (SessionKeys.penaltyId, "1234"),
        (SessionKeys.appealType, "Late_Submission"),
        (SessionKeys.startDateOfPeriod, "2020-01-01T12:00:00"),
        (SessionKeys.endDateOfPeriod, "2020-01-01T12:00:00")
      )
      val request = await(controller.onSubmitForUploadComplete(NormalMode)(fakeRequestWithIncompleteKeys))
      request.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return 303 (SEE_OTHER) when the user is not authorised" in new Setup {
      AuthStub.unauthorised()
      val request = await(buildClientForRequestToApp(uri = "/uploaded-documents").post(""))
      request.status shouldBe SEE_OTHER
    }
  }
}
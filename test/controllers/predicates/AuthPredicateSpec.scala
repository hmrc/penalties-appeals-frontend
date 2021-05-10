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

package controllers.predicates

import play.api.http.Status
import play.api.mvc.{Action, AnyContent}
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import testUtils.AuthMocks
import uk.gov.hmrc.play.test.LogCapturing

import scala.concurrent.Future

class AuthPredicateSpec extends AuthMocks with LogCapturing {
  def target: Action[AnyContent] = mockAuthPredicate.async {
    _ => Future.successful(Ok("test"))
  }

  "AuthPredicate" should {
    "run the block request when the user has an active HMRC-MTD-VAT enrolment" in {
      mockOrganisationAuthorised()
      val result = target(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "run the block request when the user is an agent" in {
      mockAgentAuthorised()
      val result = target(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "run the block request if the user has an active HMRC-MTD-VAT enrolment amongst other enrolments" in {
      mockOrganisationAuthorisedWithNonRelatedEnrolments()
      val result = target(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "do not run the block request" when {
      "the user has no HMRC-MTD-VAT enrolment" in {
        mockOrganisationNoEnrolments()
        val result = target(fakeRequest)
        status(result) shouldBe Status.FORBIDDEN
      }

      "the user has a non-active HMRC-MTD-VAT enrolment" in {
        mockOrganisationNonActivatedMTDVATEnrolment()
        val result = target(fakeRequest)
        status(result) shouldBe Status.FORBIDDEN
      }

      "the affinity group of the user can not be determined" in {
        mockNoAffinityGroup()
        val result = target(fakeRequest)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "the user has no active session" in {
        mockNoActiveSession()
        val result = target(fakeRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result).get shouldBe appConfig.signInUrl
      }

      "there has been some unknown authorisation issue" in {
        mockAuthorisationException()
        val result = target(fakeRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result).get shouldBe appConfig.signInUrl
      }
    }
  }
}
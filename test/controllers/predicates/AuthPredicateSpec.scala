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

import models.UserRequest
import play.api.http.Status
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.api.test.Helpers._
import testUtils.AuthMocks
import utils.SessionKeys

import scala.concurrent.Future

class AuthPredicateSpec extends AuthMocks {
  def target: Action[AnyContent] = mockAuthPredicate.async {
    _ => Future.successful(Ok("test"))
  }

  def targetAgent(request: Request[AnyContent]): Future[Result] = mockAuthPredicate.authoriseAsAgent({
    _: UserRequest[_] => Future.successful(Ok("welcome"))
  }, agentDetails = (None, None, None))(request)

  "AuthPredicate" should {
    "run the block request when the user has an active HMRC-MTD-VAT enrolment" in {
      mockOrganisationAuthorised()
      val result = target(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "run the block request when the user is an agent (having a principal HMRC-AS-AGENT enrolment and a delegated HMRC-MTD-VAT enrolment - with a session key)" in {
      mockAgentAuthorised()
      val result = targetAgent(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123"))
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

      "the user is an agent and doesn't have the CLIENT_VRN in the session" in {
        mockAgentAuthorised()
        val result = targetAgent(fakeRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result).get.contains("http://localhost:9152/vat-through-software/test-only/vaclf-stub") shouldBe true
      }

      "the user is an agent and has no active session" in {
        mockNoActiveSession()
        val result = targetAgent(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123"))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result).get shouldBe appConfig.signInUrl
      }

      "the user is an agent but does not have an ARN in the enrolments" in {
        mockAgentAuthorisedNoARN()
        val result = targetAgent(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123"))
        status(result) shouldBe Status.FORBIDDEN
      }

      "the user does not have authority to act on behalf of their client" in {
        mockAuthorisationException()
        val result = targetAgent(fakeRequest.withSession(SessionKeys.agentSessionVrn -> "123"))
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
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

package testUtils

import base.SpecBase
import controllers.predicates.AuthPredicate
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, EnrolmentIdentifier, Enrolments, InternalError, MissingBearerToken}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait AuthMocks extends SpecBase {
  def setupAuthResponse(authResult: Future[~[Option[AffinityGroup], Enrolments]]): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] = {
    when(mockAuthConnector.authorise[~[Option[AffinityGroup], Enrolments]](
      Matchers.any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      Matchers.any(), Matchers.any())
    ).thenReturn(authResult)
  }

  val mockAuthPredicate: AuthPredicate =
    new AuthPredicate(
      messagesApi,
      mcc,
      mockAuthService,
      errorHandler,
      unauthorised
    )

  def mockOrganisationAuthorised(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.successful(
      new ~(Some(AffinityGroup.Organisation),
        Enrolments(
          Set(
            Enrolment("HMRC-MTD-VAT",
              Seq(
                EnrolmentIdentifier("VRN", vrn)
              ),
              "Activated"
            )
          ))
      )
    ))

  def mockAgentAuthorised(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] = {
    //TODO: Need to change this response to a more 'Agent-like' setup
    setupAuthResponse(Future.successful(
      new ~(Some(AffinityGroup.Agent),
        Enrolments(
          Set(
            Enrolment("HMRC-MTD-VAT",
              Seq(
                EnrolmentIdentifier("VRN", vrn)
              ),
              "Activated"
            )
          ))
      )
    ))
  }

  def mockOrganisationNonActivatedMTDVATEnrolment(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.successful(
      new ~(Some(AffinityGroup.Organisation),
        Enrolments(
          Set(
            Enrolment("HMRC-MTD-VAT",
              Seq(
                EnrolmentIdentifier("VRN", vrn)
              ),
              "Not Activated"
            )
          )
        )
      )
    ))

  def mockNoAffinityGroup(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.successful(
      new ~(None,
        Enrolments(
          Set(
            Enrolment("HMRC-MTD-VAT",
              Seq(
                EnrolmentIdentifier("VRN", vrn)
              ),
              "Not Activated"
            )
          )
        )
      )
    ))

  def mockNoActiveSession(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.failed(
      MissingBearerToken("No token to be found here.")
    ))

  def mockAuthorisationException(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.failed(
      InternalError("There has been a mystical error.")
    ))

  def mockOrganisationNoEnrolments(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.successful(
      new ~(Some(AffinityGroup.Organisation),
        Enrolments(
          Set()
        )
      )
    ))

  def mockOrganisationAuthorisedWithNonRelatedEnrolments(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.successful(
      new ~(Some(AffinityGroup.Organisation),
        Enrolments(
          Set(
            Enrolment("IR-SA",
              Seq(
                EnrolmentIdentifier("UTR", "123456789")
              ),
              "Activated"),
            Enrolment("HMRC-MTD-VAT",
              Seq(
                EnrolmentIdentifier("VRN", vrn)
              ),
              "Activated"
            ),
            Enrolment("IR-CT",
              Seq(
                EnrolmentIdentifier("UTR", "123456789")
              ),
              "Activated")
          ))
      )
    ))
}
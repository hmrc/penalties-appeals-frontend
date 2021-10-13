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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import uk.gov.hmrc.auth.core.retrieve.{ItmpAddress, Name, Retrieval, ~}
import uk.gov.hmrc.auth.core._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AuthMocks extends SpecBase {
  def setupAuthResponse(authResult: Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]):
  OngoingStubbing[Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]] = {

    when(mockAuthConnector.authorise[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]](
      any(), any[Retrieval[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]]())(
      any(), any())
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

  def mockOrganisationAuthorised(): OngoingStubbing[Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]] =
    setupAuthResponse(Future.successful(
      new ~( new ~( new ~( new ~(
        Some(AffinityGroup.Organisation),
        Enrolments(
          Set(
            Enrolment(
              "HMRC-MTD-VAT",
              Seq(EnrolmentIdentifier("VRN", "123456789")),
              "Activated")
          )
        )),
        Some(Name(Some("test Name"), None))),
        Some("testEmail@test.com")),
        Some(
          ItmpAddress(
            line1 = Some("Flat 20"),
            line2 = Some("123 Jack street"),
            line3 = None,
            line4 = Some("Birmingham"),
            line5 = Some("UK"),
            postCode = Some("AAA AAA"),
            countryName = None,
            countryCode = None)
        )
      )
    ))

  def mockAgentAuthorised(): OngoingStubbing[Future[Enrolments]] = {
    when(mockAuthConnector.authorise[Enrolments](
      any(), any())(
      any(), any())
    ).thenReturn(Future.successful(
      Enrolments(
        Set(
          Enrolment(
            "HMRC-AS-AGENT",
            Seq(EnrolmentIdentifier("AgentReferenceNumber", "1234567")),
            "Activated"
          )
        ))
    ))
  }

  def mockAgentAuthorisedNoARN(): OngoingStubbing[Future[Enrolments]] = {
    when(mockAuthConnector.authorise[Enrolments](
      any(), any())(
      any(), any())
    ).thenReturn(Future.successful(
      Enrolments(
        Set(
          Enrolment(
            "HMRC-AS-AGENT",
            Seq(),
            "Activated"
          )
        ))))
  }

  def mockOrganisationNonActivatedMTDVATEnrolment():
  OngoingStubbing[Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]] =

    setupAuthResponse(Future.successful(
      new ~( new ~( new ~( new ~(
        Some(AffinityGroup.Organisation),
        Enrolments(
          Set(
            Enrolment("HMRC-MTD-VAT",
              Seq(
                EnrolmentIdentifier("VRN", vrn)
              ),
              "Not Activated"
            )
          )
        )),
        None),
        None),
        None)
      )
    )

  def mockNoAffinityGroup(): OngoingStubbing[Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]] =
    setupAuthResponse(Future.successful(
      new ~( new ~( new ~( new ~(
        None,
        Enrolments(
          Set(
            Enrolment("HMRC-MTD-VAT",
              Seq(
                EnrolmentIdentifier("VRN", vrn)
              ),
              "Not Activated"
            )
          )
        )),
        None),
        None),
        None)
      )
    )

  def mockNoActiveSession(): OngoingStubbing[Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]] =
    setupAuthResponse(Future.failed(
      MissingBearerToken("No token to be found here.")
    ))

  def mockAuthorisationException(): OngoingStubbing[Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]] =
    setupAuthResponse(Future.failed(
      InternalError("There has been a mystical error.")
    ))

  def mockOrganisationNoEnrolments():OngoingStubbing[Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]] =
    setupAuthResponse(Future.successful(
      new ~( new ~( new ~( new ~(
        Some(AffinityGroup.Organisation),
        Enrolments(
          Set()
        )),
        None),
        None),
        None)
      )
    )

  def mockOrganisationAuthorisedWithNonRelatedEnrolments():
  OngoingStubbing[Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]]] =

    setupAuthResponse(Future.successful(
      new ~( new ~( new ~( new ~(
        Some(AffinityGroup.Organisation),
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
          ))),
        Some(Name(Some("test Name"), None))),
        Some("testEmail@test.com")),
        Some(
          ItmpAddress(
            line1 = Some("Flat 20"),
            line2 = Some("123 Jack street"),
            line3 = None,
            line4 = Some("Birmingham"),
            line5 = Some("UK"),
            postCode = Some("AAA AAA"),
            countryName = None,
            countryCode = None)
        )
      )
    ))
}
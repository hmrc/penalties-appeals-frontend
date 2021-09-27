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

import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.retrieve.{ItmpAddress, Name, ~}
import uk.gov.hmrc.auth.core._

import scala.concurrent.Future

object AuthTestModels {
  val successfulAuthResult: Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]] = Future.successful(
    new ~( new ~( new ~( new ~(
    Some(Organisation),
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
  )

  val failedAuthResultNoEnrolments: Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]] =
    Future.successful(new ~(new ~(
      new ~(
        new ~(
          Some(Organisation),
          Enrolments(
            Set())
        ),
        None
      ),
      None
    ), None)
  )

  val failedAuthResultUnauthorised: Future[~[~[~[~[Option[AffinityGroup], Enrolments], Option[Name]], Option[String]], Option[ItmpAddress]]] = Future.failed(
    BearerTokenExpired()
  )
}
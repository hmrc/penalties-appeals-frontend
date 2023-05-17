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

package connectors.httpParsers

import play.api.http.Status.{BAD_REQUEST, NO_CONTENT}

sealed trait ErrorResponse {
  val status: Int
  val body: String
}

case object NoContent extends ErrorResponse {
  override val status: Int = NO_CONTENT
  override val body: String = "No content returned"
}

case object InvalidJson extends ErrorResponse {
  override val status: Int = BAD_REQUEST
  override val body: String = "Invalid JSON received"
}

case object BadRequest extends ErrorResponse {
  override val status: Int = BAD_REQUEST
  override val body: String = "incorrect Json body sent"
}

case class  UnexpectedFailure(
                              override val status: Int,
                              override val body: String
                            ) extends ErrorResponse
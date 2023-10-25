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

package config

import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.Results.{BadRequest, InternalServerError, NotFound, Redirect}
import play.api.mvc.{Request, RequestHeader, Result, Results}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.errors.ErrorTemplate

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(errorTemplate: ErrorTemplate, val messagesApi: MessagesApi)(implicit appConfig: AppConfig)
    extends FrontendErrorHandler {

  private implicit def rhToRequest(rh: RequestHeader): Request[_] = Request(rh, "")

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    statusCode match {
      case play.mvc.Http.Status.BAD_REQUEST => Future.successful(BadRequest(badRequestTemplate(request)))
      case play.mvc.Http.Status.NOT_FOUND   => Future.successful(NotFound(notFoundTemplate(request)))
      case play.mvc.Http.Status.FORBIDDEN   => Future(Redirect(controllers.routes.TechnicalErrorController.onPageLoad()))
      case _                                => Future.successful(Results.Status(statusCode)(fallbackClientErrorTemplate(request)))
    }
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html = {
    errorTemplate(pageTitle, heading, message)
  }

  def showInternalServerError(implicit request: Request[_]): Result = InternalServerError(internalServerErrorTemplate)
}

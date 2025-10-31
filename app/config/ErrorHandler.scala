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
import models.UserRequest
import play.api.i18n.MessagesApi
import play.api.mvc.Results.{BadRequest, InternalServerError, NotFound, Redirect}
import play.api.mvc.{Request, RequestHeader, Result, Results}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.errors.{ErrorTemplate, InternalServerErrorGenericPage, InternalServerErrorPage}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

@Singleton
class ErrorHandler @Inject()(errorTemplate: ErrorTemplate, val messagesApi: MessagesApi, iseCustom: InternalServerErrorPage, iseGeneric: InternalServerErrorGenericPage)(implicit appConfig: AppConfig, val ec: ExecutionContext)
    extends FrontendErrorHandler {

  private implicit def rhToRequest(rh: RequestHeader): Request[_] = Request(rh, "")

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    statusCode match {
      case play.mvc.Http.Status.BAD_REQUEST => badRequestTemplate(request).map(html => BadRequest(html))
      case play.mvc.Http.Status.NOT_FOUND   => notFoundTemplate(request).map(html => NotFound(html))
      case play.mvc.Http.Status.FORBIDDEN   => Future(Redirect(controllers.routes.TechnicalErrorController.onPageLoad()))
      case play.mvc.Http.Status.INTERNAL_SERVER_ERROR => Future(showInternalServerError()(request))
      case _                                => fallbackClientErrorTemplate(request).map(html => Results.Status(statusCode)(html))
    }
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: RequestHeader): Future[Html] = {
    implicit val req: Request[_] = request.asInstanceOf[Request[_]]
    Future.successful(errorTemplate(pageTitle, heading, message))
  }

  def showInternalServerError(userOptional: Option[UserRequest[_]] = None)(implicit request: Request[_]): Result = {
    if (userOptional.isDefined) {
      implicit val user: UserRequest[_] = userOptional.get
      InternalServerError(iseCustom())
    } else {
      InternalServerError(iseGeneric())
    }
  }

  def notFoundError(implicit request: Request[_]): Future[Result] = {
    notFoundTemplate(request).map(html => NotFound(html))
  }
}

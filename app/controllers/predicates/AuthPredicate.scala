/*
 * Copyright 2022 HM Revenue & Customs
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

import config.{AppConfig, ErrorHandler}
import models.UserRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.AuthService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Logger.logger
import utils.{EnrolmentKeys, SessionKeys}
import views.html.errors.Unauthorised

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthPredicate @Inject()(override val messagesApi: MessagesApi,
                              val mcc: MessagesControllerComponents,
                              val authService: AuthService,
                              errorHandler: ErrorHandler,
                              unauthorisedView: Unauthorised)
                             (implicit val appConfig: AppConfig,
                              implicit val executionContext: ExecutionContext) extends FrontendController(mcc) with
  I18nSupport with
  ActionBuilder[UserRequest, AnyContent] with
  ActionFunction[Request, UserRequest] {

  override def parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
    implicit val req: Request[A] = request
    val logMsgStart: String = "[AuthPredicate][invokeBlock]"
    authService.authorised().retrieve(
      Retrievals.affinityGroup and Retrievals.allEnrolments
    ) {
      case Some(affinityGroup) ~ allEnrolments =>
        logger.debug(s"$logMsgStart - User is $affinityGroup and has ${allEnrolments.enrolments.size} enrolments. " +
          s"Enrolments: ${allEnrolments.enrolments}")
        (isAgent(affinityGroup), allEnrolments) match {
          case (true, _) =>
            logger.debug(s"$logMsgStart - Authorising user as Agent")
            authoriseAsAgent(block)
          case (false, enrolments) =>
            logger.debug(s"$logMsgStart - Authorising user as Individual/Organisation")
            checkVatEnrolment(enrolments, block)
        }
      case _ =>
        logger.warn(s"$logMsgStart - Missing affinity group")
        Future.successful(errorHandler.showInternalServerError)
    } recover {
      case _: NoActiveSession =>
        logger.debug(s"$logMsgStart - No active session, redirect to GG sign in")
        Redirect(appConfig.signInUrl)
      case _: AuthorisationException =>
        logger.warn(s"$logMsgStart - Unauthorised exception, redirect to GG sign in")
        Redirect(appConfig.signInUrl)
    }
  }

  private[predicates] def checkVatEnrolment[A](allEnrolments: Enrolments, block: UserRequest[A] => Future[Result])(implicit request: Request[A]) = {
    val logMsgStart: String = "[AuthPredicate][checkVatEnrolment]"
    val extractedMTDVATEnrolment: Option[String] = UserRequest.extractFirstMTDVatEnrolment(allEnrolments)
    if (extractedMTDVATEnrolment.isDefined) {
      val user: UserRequest[A] = UserRequest(extractedMTDVATEnrolment.get)
      block(user)
    } else {
      logger.debug(s"$logMsgStart - User does not have an activated HMRC-MTD-VAT enrolment. User had these enrolments: ${allEnrolments.enrolments}")
      Future(Forbidden(unauthorisedView()))
    }
  }

  private[predicates] def authoriseAsAgent[A](block: UserRequest[A] => Future[Result])
                                             (implicit request: Request[A]): Future[Result] = {

    val agentDelegatedAuthorityRule: String => Enrolment = vrn =>
      Enrolment(EnrolmentKeys.mtdVATEnrolmentKey)
        .withIdentifier(EnrolmentKeys.vrnId, vrn)
        .withDelegatedAuthRule(EnrolmentKeys.agentDelegatedAuthRuleKey)
    request.session.get(SessionKeys.agentSessionVrn) match {
      case Some(vrn) =>
        authService
          .authorised(agentDelegatedAuthorityRule(vrn))
          .retrieve(allEnrolments) {
            enrolments =>
              enrolments.enrolments.collectFirst {
                case Enrolment(EnrolmentKeys.agentEnrolmentKey, Seq(EnrolmentIdentifier(_, arn)), EnrolmentKeys.activated, _) => arn
              } match {
                case Some(arn) => block(UserRequest(vrn, arn = Some(arn)))
                case None =>
                  logger.debug("[AuthPredicate][authoriseAsAgent] - Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
                  Future.successful(Forbidden(unauthorisedView()))
              }
          } recover {
          case _: NoActiveSession =>
            logger.debug(s"AgentPredicate][authoriseAsAgent] - No active session. Redirecting to ${appConfig.signInUrl}")
            Redirect(appConfig.signInUrl)
          case _: AuthorisationException =>
            logger.debug(s"[AgentPredicate][authoriseAsAgent] - Agent does not have delegated authority for client. " +
              s"Showing unauthorised")
            Forbidden(unauthorisedView())
        }
      case None =>
        logger.debug(s"[AuthPredicate][authoriseAsAgent] - No Client VRN in session. Redirecting to ${appConfig.agentClientLookupStartUrl(request.uri)}")
        Future.successful(Redirect(appConfig.agentClientLookupStartUrl(request.uri)))
    }
  }

  private[predicates] def isAgent(affinityGroup: AffinityGroup): Boolean = {
    affinityGroup == AffinityGroup.Agent
  }
}

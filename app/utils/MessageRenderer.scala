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

package utils

import models.UserRequest
import play.api.i18n.Messages
import play.api.mvc.Request

object MessageRenderer {

  def getMessageKey(msgKey: String)(implicit user: UserRequest[_]): String = {
    if (user.isAgent && (didClientCauseLateSubmission() || (user.answers.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn).isEmpty
      && user.answers.getAnswer[String](SessionKeys.whatCausedYouToMissTheDeadline).isEmpty))) {
      s"agent.$msgKey"
    } else {
      msgKey
    }
  }

  def getMessage(msgKey: String, msgArgs: Any*)(implicit messages: Messages, user: UserRequest[_]): String = {
    if (user.isAgent && (didClientCauseLateSubmission() ||
      (user.answers.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn).isEmpty && user.answers.getAnswer[String](SessionKeys.whatCausedYouToMissTheDeadline).isEmpty))) {
      messages.apply(s"agent.$msgKey", msgArgs: _*)
    } else {
      messages.apply(msgKey, msgArgs: _*)
    }
  }

  def getPageTitle(msgArgs: Any*)(implicit messages: Messages, request: Request[_]): String = {
    if(request.session.get(SessionKeys.agentSessionVrn).isDefined) {
      messages.apply("common.agent.pageTitle", msgArgs: _*)
    } else {
      messages.apply("common.pageTitle", msgArgs: _*)
    }
  }

  def didClientCauseLateSubmission()(implicit user: UserRequest[_]): Boolean = {
    val clientPlannedToSubmit = user.answers.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn).contains("client")
    val clientCausedLateSubmissionReturn = user.answers.getAnswer[String](SessionKeys.whatCausedYouToMissTheDeadline).contains("client")

    (clientPlannedToSubmit, clientCausedLateSubmissionReturn) match {
      case (true, _) => true
      case (false, true) => true
      case (false, false) => false
    }
  }

  def didClientPlanToSubmit()(implicit userRequest: UserRequest[_]): Boolean = {
    userRequest.answers.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn).contains("client")
  }

  def didAgentPlanToSubmitAndClientMissedDeadline()(implicit userRequest: UserRequest[_]): Boolean = {
    userRequest.answers.getAnswer[String](SessionKeys.whoPlannedToSubmitVATReturn).contains("agent") &&
      userRequest.answers.getAnswer[String](SessionKeys.whatCausedYouToMissTheDeadline).contains("client")
  }
}

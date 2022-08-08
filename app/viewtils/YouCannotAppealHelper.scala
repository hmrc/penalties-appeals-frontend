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

package viewtils

import config.AppConfig
import javax.inject.Inject
import models.{PenaltyTypeEnum, UserRequest}
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.MessageRenderer.getMessage
import utils.{SessionKeys, ViewUtils}

class YouCannotAppealHelper @Inject()(appConfig: AppConfig,
                                      p: views.html.components.p,
                                      link: views.html.components.link)
                                              extends ViewUtils {

  def getHeaderAndTitle(implicit user: UserRequest[_]): String = {
    val isLPP = user.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Payment) ||
      user.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).contains(PenaltyTypeEnum.Additional)
    (isLPP, user.isAgent) match {
      case (true, true) =>
        "agent.youCannotAppeal.headingAndTitle.lpp"
      case (true, false) =>
        "youCannotAppeal.headingAndTitle.lpp"
      case (false, true) =>
        "agent.youCannotAppeal.headingAndTitle.lsp"
      case (_, _) =>
        "youCannotAppeal.headingAndTitle.lsp"
    }
  }

  private def lspHtml(implicit messages: Messages, user: UserRequest[_]): Html = {
    html(
      p(content = html(stringAsHtml(messages("youCannotAppeal.p1")))),
      p(content = html(stringAsHtml(getMessage("youCannotAppeal.p2.lsp")))),
      p(content = html(stringAsHtml(getMessage("youCannotAppeal.p3.lsp")))),
      p(content = html(stringAsHtml(getMessage("youCannotAppeal.p4")))),
      p(link(link = appConfig.vatOverviewUrl, getMessage("youCannotAppeal.returnToVATAccount")))
    )
  }

  private def lppHtml(agent: Boolean)(implicit messages: Messages, user: UserRequest[_]): Html = {
    html(
      p(content = html(stringAsHtml(messages("youCannotAppeal.p1")))),
      p(content = html(stringAsHtml(getMessage("youCannotAppeal.p2.lpp")))),
      p(content = html(stringAsHtml(getMessage("youCannotAppeal.p3.lpp")))),
      p(content = html(stringAsHtml(getMessage("youCannotAppeal.p4")))),
      p(link(link = if(!agent) appConfig.whatYouOweUrl else appConfig.vatOverviewUrl,
             messages(if(!agent) s"youCannotAppeal.checkWhatYouOwe" else "agent.youCannotAppeal.returnToVATAccount"))
      )
    )
  }

  def getContent(implicit messages: Messages, user: UserRequest[_]): Html = {
    val isLPP = user.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Payment) ||
      user.answers.getAnswer[PenaltyTypeEnum.Value](SessionKeys.appealType).contains(PenaltyTypeEnum.Additional)
    val isAgent = user.isAgent
    if(isLPP) lppHtml(isAgent) else lspHtml
  }
}

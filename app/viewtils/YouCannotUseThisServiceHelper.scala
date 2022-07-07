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
import play.twirl.api.{Html, HtmlFormat}
import utils.MessageRenderer.getMessage
import utils.{SessionKeys, ViewUtils}

class YouCannotUseThisServiceHelper @Inject()(appConfig: AppConfig,
                                              p: views.html.components.p,
                                              link: views.html.components.link)
                                              extends ViewUtils {

  def getHeaderAndTitle(implicit messages: Messages, user: UserRequest[_]): String = {
    println(Console.GREEN + "GEtting Header and Title" + Console.RESET)
    val isLPP = user.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Payment.toString) ||
      user.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Additional.toString)
    println(Console.GREEN + s"isLPP is $isLPP" + Console.RESET)
    (isLPP, user.isAgent) match {
      case (true, true) =>
        "agent.youCannotUse.headingAndTitle.lpp"
      case (true, false) =>
        "youCannotUse.headingAndTitle.lpp"
      case (false, true) =>
        "agent.youCannotUse.headingAndTitle.lsp"
      case (_, _) =>
        "youCannotUse.headingAndTitle.lsp"
    }
  }

  private def lspHtml(implicit messages: Messages, user: UserRequest[_]): Html = {
    html(
      p(content = html(stringAsHtml(messages("youCannotUse.p1")))),
      p(content = html(stringAsHtml(getMessage(s"youCannotUse.p2.lsp")))),
      p(content = html(stringAsHtml(getMessage(s"youCannotUse.p3.lsp")))),
      p(link(link = appConfig.vatOverviewUrl, getMessage(s"youCannotUse.returnToVATAccount")))
    )
  }

  private def lppHtml(agent: Boolean)(implicit messages: Messages, user: UserRequest[_]): Html = {
    html(
      p(content = html(stringAsHtml(messages("youCannotUse.p1")))),
      p(content = html(stringAsHtml(getMessage(s"youCannotUse.p2.lpp")))),
      p(content = html(stringAsHtml(getMessage(s"youCannotUse.p3.lpp")))),
      p(content = html(stringAsHtml(getMessage(s"youCannotUse.p6")))),
      p(link(link = if(agent) appConfig.whatYouOweUrl else appConfig.vatOverviewUrl,
             messages(if(!agent) s"youCannotUse.checkWhatYouOwe" else "agent.youCannotUse.returnToVATAccount"))
      )
    )
  }

  def getContent(implicit messages: Messages, user: UserRequest[_]): Html = {
    val isLPP = user.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Late_Payment.toString) ||
      user.session.get(SessionKeys.appealType).contains(PenaltyTypeEnum.Additional.toString)
    val isAgent = user.isAgent
    if(isLPP) lppHtml(isAgent) else lspHtml
  }
}

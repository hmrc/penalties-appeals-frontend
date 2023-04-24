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
    user.isAgent match {
      case true =>
        "agent.youCannotAppeal.headingAndTitle.lsp"
      case false =>
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

  def getContent(implicit messages: Messages, user: UserRequest[_]): Html = {
    lspHtml
  }
}

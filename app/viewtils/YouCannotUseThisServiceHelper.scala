
package viewtils

import javax.inject.Inject
import models.UserRequest
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.MessageRenderer.getMessage
import utils.ViewUtils

class YouCannotUseThisServiceHelper @Inject()(
                                               p: views.html.components.p,
                                               link: views.html.components.link,
                                               isLPP: Boolean)
                                             (implicit messages: Messages, user: UserRequest[_]) extends ViewUtils {

  def getHeaderAndTitle: String = {
    (isLPP, user.isAgent) match {
      case (true, true) =>
        "youCannotUse.headingAndTitle.lpp.agent"
      case (true, false) =>
        "youCannotUse.headingAndTitle.lpp"
      case (false, true) =>
        "youCannotUse.headingAndTitle.lsp.agent"
      case (_, _) =>
        "youCannotUse.headingAndTitle.lsp"
    }
  }

  def getContent: Html = {
    (isLPP, user.isAgent) match {
      case (true, true) =>
        html(
          p(content = html(stringAsHtml(getMessage("youCannotUse.headingAndTitle.lpp.agent"))),
            classes = "")
        )
    }
  }
}

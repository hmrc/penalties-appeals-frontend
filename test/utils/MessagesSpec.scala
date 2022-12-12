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

package utils

import base.SpecBase
import play.api.i18n.Messages
import play.api.i18n.Messages.MessageSource

import scala.io.Source

class MessagesSpec extends SpecBase {
  private val excludedKeys = Seq(
    "service.homePageUrl",
    "serviceUnavailable.p5",
    "serviceUnavailable.p6",
    "common.pageTitle",
    "honestyDeclaration.other",
    "otherReason.uploadEvidence.details.li.2",
    "otherReason.uploadEvidence.details.li.3",
    "otherReason.uploadEvidence.details.li.5"
  )

  private val MatchIncorrectTwoSingleQuotes = """\w+'{2}\w+""".r
  private val MatchIncorrectSingleQuote = """\w+'{1}\w+""".r
  private val MatchBacktickQuoteOnly = """`+""".r

  private val englishMessages = parseMessages("conf/messages")
  private val welshMessages = parseMessages("conf/messages.cy")

  "All message files" should {
    "have the same set of keys" in {
      withClue(describeMismatch(englishMessages.keySet, welshMessages.keySet)) {
        welshMessages.keySet shouldBe englishMessages.keySet
      }
    }

    "the value for each English key must be different to the corresponding value of the Welsh key" in {
      val englishMessagesWithoutExcludedKeys= englishMessages.filterNot(kV => excludedKeys.contains(kV._1))
      val welshMessagesWithoutExcludedKeys = welshMessages.filterNot(kV => excludedKeys.contains(kV._1))
      withClue(describeValueMismatch(englishMessagesWithoutExcludedKeys, welshMessagesWithoutExcludedKeys)) {
        englishMessagesWithoutExcludedKeys.values.toSet.intersect(welshMessagesWithoutExcludedKeys.values.toSet).size shouldBe 0
      }
    }

    "have a non-empty message for each key" in {
      assertNonEmpty("English", englishMessages)
      assertNonEmpty("Welsh", welshMessages)
    }

    "have no unescaped single quotes or 2 single quotes in value" in {
      assertCorrectUseOfQuotes("English", englishMessages)
      assertCorrectUseOfQuotes("Welsh", welshMessages)
    }

    "have a resolvable message for keys which take args" in {
      countMessagesWithArgs(welshMessages).size shouldBe countMessagesWithArgs(englishMessages).size
    }
  }

  private def parseMessages(filename: String): Map[String, String] = {
    val source = Source.fromFile(filename)
    val sourceAsString = source.mkString
    source.close()
    Messages.parse(new MessageSource {override def read: String = sourceAsString}, filename).fold(throw _, identity)
  }

  private def countMessagesWithArgs(messages: Map[String, String]): Iterable[String] = messages.values.filter(_.contains("{0}"))

  private def assertNonEmpty(label: String, messages: Map[String, String]): Unit = {
    val messagesWithoutOtherReason = messages.filterNot(_._1 == "honestyDeclaration.other")
    messagesWithoutOtherReason.foreach { case (key: String, value: String) =>
      withClue(s"In $label, there is an empty value for the key:[$key][$value]") {
        value.trim.isEmpty shouldBe false
      }
    }
  }

  private def assertCorrectUseOfQuotes(label: String, messages: Map[String, String]): Unit = messages.foreach { case (key: String, value: String) =>
    withClue(s"In $label, there is an unescaped or invalid quote:[$key][$value]") {
      MatchIncorrectTwoSingleQuotes.findFirstIn(value).isDefined shouldBe false
      MatchBacktickQuoteOnly.findFirstIn(value).isDefined shouldBe false
      MatchIncorrectSingleQuote.findFirstIn(value).isDefined shouldBe false
    }
  }

  private def listMissingMessageKeys(header: String, missingKeys: Set[String]): String = {
    val displayLine = "\n" + ("-" * 42) + "\n"
    missingKeys.toList.sorted.mkString(header + displayLine, "\n", displayLine)
  }

  private def listMessageKeyValues(header: String, keyValues: Set[(String, String)]): String = {
    val headLine = "\n" + ("-" * 42) + "\n" + "Displayed as (key,value)" + "\n"
    val lastLine = "\n" + ("-" * 42) + "\n"
    keyValues.toList.sorted.mkString(header + headLine, "\n", lastLine)
  }

  private def describeMismatch(englishKeySet: Set[String], welshKeySet: Set[String]): String = {
    if (englishKeySet.size > welshKeySet.size) listMissingMessageKeys("The following message keys are missing from the Welsh Set:", englishKeySet -- welshKeySet)
    else listMissingMessageKeys("The following message keys are missing from the English Set:", welshKeySet -- englishKeySet)
  }

  private def describeValueMismatch(englishKeyValues: Map[String, String], welshKeyValues: Map[String, String]): String = {
    val englishAndWelshMessagesThatAreTheSame = englishKeyValues.values.toSet.intersect(welshKeyValues.values.toSet)
    val keyValuesThatAreTheSame = englishAndWelshMessagesThatAreTheSame.map(value => englishKeyValues.find(_._2 == value)).collect{ case Some(x) => x }
    listMessageKeyValues("The following message keys have the same value for English and Welsh (missed translation):", keyValuesThatAreTheSame)
  }
}

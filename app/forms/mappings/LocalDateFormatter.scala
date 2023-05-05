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

package forms.mappings

import config.AppConfig
import config.featureSwitches.FeatureSwitching
import play.api.Configuration

import java.time.LocalDate
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.data.validation.Constraints
import play.api.i18n.Messages

import scala.util.{Failure, Success, Try}


private[mappings] class LocalDateFormatter(invalidKey: String,
                                           allRequiredKey: String,
                                           twoRequiredKey: String,
                                           requiredKey: String,
                                           futureKey: Option[String] = None,
                                           dateNotEqualOrAfterKeyAndCompareDate: Option[(String, LocalDate)] = None,
                                           args: Seq[String] = Seq.empty)
                                          (implicit val messages: Messages, appConfig: AppConfig) extends Formatter[LocalDate] with Formatters
                                          with Constraints with FeatureSwitching {

  implicit val config: Configuration = appConfig.config

  private val fieldKeys: List[String] = List("day", "month", "year")

  //scalastyle:off
  private def toDate(key: String, day: Int, month: Int, year: Int): Either[Seq[FormError], LocalDate] = {
    Try(LocalDate.of(year, month, day)) match {
      case Success(date) =>
        Right(date)
      case Failure(_) =>
        Left(Seq(FormError(s"$key.day", invalidKey, args)))
    }
  }

  private def bindIntSubfield(key: String, subKey: String, blankErrorKey: String, invalidErrorKey: String, data: Map[String, String],
                              extraValidation: Int => Boolean): Either[Seq[FormError], Int] = {
    intFormatter(
      requiredKey = blankErrorKey,
      wholeNumberKey = invalidErrorKey,
      nonNumericKey = invalidErrorKey
    ).bind(s"$key.$subKey", data.view.mapValues(_.trim).toMap)
      .flatMap(int =>
        if (extraValidation(int)) Right(int) else Left(Seq(FormError(s"$key.$subKey", invalidErrorKey))))
  }

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {
    val localisedFieldKeys = fieldKeys.map(key => messages(s"date.$key").toLowerCase)
    val dayField = bindIntSubfield(key, "day", invalidKey, invalidKey, data, day => day >= 1 && day <= 31)
    val monthField = bindIntSubfield(key, "month", invalidKey, invalidKey, data, month => month >= 1 && month <= 12)
    val yearField = bindIntSubfield(key, "year", invalidKey, invalidKey, data, year => year >= 1 && year.toString.length == 4)
    val parseResult = (dayField, monthField, yearField) match {
      case (Right(day), Right(month), Right(year)) => toDate(key, day, month, year)
      case (day, month, year) => Left(day.left.toSeq.flatten ++ month.left.toSeq.flatten ++ year.left.toSeq.flatten)
    }
    parseResult.fold(
      {
        errors =>
          if (errors.size > 1) {
            val focusTarget = errors.find(_.key == s"$key.day").orElse(errors.find(_.key == s"$key.month")).orElse(errors.find(_.key == s"$key.year")).map(_.key).getOrElse(s"$key.day")
            Left(List(FormError(focusTarget, invalidKey, localisedFieldKeys ++ args)))
          } else {
            Left(errors)
          }
      },
      date => {
        if (futureKey.isDefined) {
          if (dateNotEqualOrAfterKeyAndCompareDate.isDefined && dateNotEqualOrAfterKeyAndCompareDate.get._2.isAfter(date)) {
            Left(Seq(FormError(s"$key.day", dateNotEqualOrAfterKeyAndCompareDate.get._1, localisedFieldKeys)))
          } else if (date.isEqual(getFeatureDate) || date.isBefore(getFeatureDate)) {
            Right(date)
          } else {
            Left(Seq(FormError(s"$key.day", futureKey.get, localisedFieldKeys)))
          }
        } else {
          Right(date)
        }
      }
    )
  }

  //scalastyle:off
  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {
    val fields = fieldKeys.map {
      field => field -> data.get(s"$key.$field").map(_.replaceAll(" ", "")).filter(_.nonEmpty)
    }.toMap
    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList
    val localisedFieldMessages = missingFields.map(field => messages(s"date.$field").toLowerCase)
    fields.count(_._2.isDefined) match {
      case 3 =>
        formatDate(key, data).left.map {
          _.map(err => err.copy(args = err.args ++ args))
        }
      case 2 =>
        Left(List(FormError(s"$key.${missingFields.head}", requiredKey, localisedFieldMessages ++ args)))
      case 1 =>
        Left(List(FormError(s"$key.${missingFields.head}", twoRequiredKey, localisedFieldMessages ++ args)))
      case _ =>
        Left(List(FormError(s"$key.${missingFields.head}", allRequiredKey, localisedFieldMessages ++ args)))
    }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.day" -> value.getDayOfMonth.toString,
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year" -> value.getYear.toString
    )
}

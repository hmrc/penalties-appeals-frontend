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

import utils.Logger.logger

object PagerDutyHelper {

  object PagerDutyKeys extends Enumeration {
    final val RECEIVED_4XX_FROM_PENALTIES = Value
    final val RECEIVED_5XX_FROM_PENALTIES = Value
    final val RECEIVED_4XX_FROM_UPSCAN = Value
    final val RECEIVED_5XX_FROM_UPSCAN = Value
    final val FILE_UPLOAD_STATUS_NOT_FOUND_UPSCAN = Value
    final val UNKNOWN_EXCEPTION_CALLING_PENALTIES = Value
    final val INVALID_JSON_RECEIVED_FROM_PENALTIES = Value
    final val INVALID_JSON_RECEIVED_FROM_UPSCAN = Value
    final val FAILED_INITIATE_CALL_UPSCAN = Value
    final val UPLOAD_FAILURE_UPSCAN = Value
    final val FILE_REMOVAL_FAILURE_UPSCAN = Value
    final val FILE_VERIFICATION_FAILURE_UPSCAN = Value
    final val FILE_NAME_RETRIEVAL_FAILURE_UPSCAN = Value
    final val FILE_POSTED_FAILURE_UPSCAN = Value
  }

  def log(methodName: String, pagerDutyKey: PagerDutyKeys.Value): Unit = {
    logger.warn(s"$pagerDutyKey - $methodName")
  }

  def logStatusCode(methodName: String, code: Int)(keyOn4xx: PagerDutyKeys.Value, keyOn5xx: PagerDutyKeys.Value): Unit = {
    code match {
      case code if code >= 400 && code <= 499 => log(methodName, keyOn4xx)
      case code if code >= 500 => log(methodName, keyOn5xx)
      case _ =>
    }
  }
}

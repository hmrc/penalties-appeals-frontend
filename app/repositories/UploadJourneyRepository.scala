/*
 * Copyright 2021 HM Revenue & Customs
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

package repositories

import config.AppConfig
import uk.gov.hmrc.mongo.cache.{CacheIdType, MongoCacheRepository}
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UploadJourneyRepository @Inject()(
                                         mongoComponent: MongoComponent,
                                         timestampSupport: TimestampSupport,
                                         appConfig: AppConfig
                                       )(implicit ec: ExecutionContext)
  extends MongoCacheRepository[String](
    mongoComponent = mongoComponent,
    collectionName = "file-upload-journeys",
    replaceIndexes = true,
    ttl = appConfig.mongoTTL,
    timestampSupport = timestampSupport,
    cacheIdType = CacheIdType.SimpleCacheId
  )(ec)
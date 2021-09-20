
package connectors

import config.AppConfig
import connectors.httpParsers.UpscanInitiateHttpParser.{UpscanInitiateResponse, UpscanInitiateResponseReads}
import javax.inject.Inject
import models.upscan.UpscanInitiateRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

class UpscanConnector @Inject()(httpClient: HttpClient,
                                appConfig: AppConfig) {

  val postInitiateUrl: String = s"${appConfig.upscanInitiateBaseUrl}/upscan/v2/initiate"

  def initiateToUpscan(request: UpscanInitiateRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UpscanInitiateResponse] = {
    httpClient.POST(postInitiateUrl, request)(UpscanInitiateRequest.writes, UpscanInitiateResponseReads, hc, ec)
  }
}
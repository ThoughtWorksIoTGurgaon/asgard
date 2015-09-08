import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.GlobalSettings

class CorsFilter extends EssentialFilter {
  def apply(next: EssentialAction) = new EssentialAction {
    def apply(requestHeader: RequestHeader) = {
      next(requestHeader).map { result =>
        println(s"Request headers---->\n ${requestHeader.headers}\n\n\n-----------------------------------------------")
        result.withHeaders("Access-Control-Allow-Origin" -> "*"
          )
      }
    }
  }
}

object Global extends WithFilters(new CorsFilter) with GlobalSettings
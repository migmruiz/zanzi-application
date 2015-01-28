package filters

import play.Logger
import play.api.Play
import play.api.libs.Crypto
import play.api.libs.iteratee.Done
import play.api.mvc.Results._
import play.api.mvc.{ EssentialAction, EssentialFilter, RequestHeader }

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Checks passed auth token in the request and decides if
 * the request should proceed
 */
object Authorization extends EssentialFilter {
  def isAuthorized(request: RequestHeader) = {
    (request.tags.get("ROUTE_ACTION_METHOD"), request.tags.get("ROUTE_CONTROLLER")) match {
      case (Some("healthCheck"), Some("controllers.HealthCheckController")) => true
      case (_, Some("controllers.Assets"))                                  => true
      case (_, Some("controllers.auth.AuthController"))                     => true
      case (_, Some("controllers.auth.Google"))                             => true
      case (_, Some("controllers.auth.Twitter"))                            => true
      case _ => {
        val authorizedUserEmails = Play.configuration.getString("google.auth.authorized.users").get.split(",")
        val authorizedUserScreenNames = Play.configuration.getString("twitter.auth.authorized.users").get.split(",")
        val authToken = Play.configuration.getString("auth.token").get
        (request.session.get("userEmail"), request.session.get("screenName"), request.headers.get("AUTH_TOKEN")) match {
          case (Some(session), _, _)    => {
            val encryptedEmail = request.session.get("userEmail")
            encryptedEmail match {
              case None => false
              case Some(value) => {
                Logger.debug(s"Logging in with email: $value")
                authorizedUserEmails.contains(Crypto.decryptAES(value))
              }
            }
          }
          case (_, Some(screenName), _) => {
            val encryptedScreenName = request.session.get("screenName")
            encryptedScreenName match {
              case None => false
              case Some(value) => {
                Logger.debug(s"Logging in with screen name: $value")
                authorizedUserScreenNames.contains(Crypto.decryptAES(value))
              }
            }
          
          }
          case (_, _, Some(token))      => token == authToken
          case _                        => false
        }
      }
    }
  }
  def apply(nextFilter: EssentialAction) = new EssentialAction {
    def apply(requestHeader: RequestHeader) = isAuthorized(requestHeader) match {
      case true  => nextFilter(requestHeader).map(r => r)
      case false => Done(Redirect(controllers.auth.routes.AuthController.index()))
    }
  }
}
package controllers.auth

import scala.concurrent.Future

import play.api._
import play.api.mvc._
import play.api.libs.oauth._
import play.api.libs.Crypto
import play.api.libs.ws.WS

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Code mostly from https://playframework.com/documentation/2.3.x/ScalaOAuth#Example
 */
object Twitter extends Controller {
  private val KEY = ConsumerKey(Play.configuration.getString("twitter.auth.key").get, Play.configuration.getString("twitter.auth.secret").get)

  private val TWITTER = OAuth(ServiceInfo(
    "https://api.twitter.com/oauth/request_token",
    "https://api.twitter.com/oauth/access_token",
    "https://api.twitter.com/oauth/authenticate", KEY),
    use10a = true)

  private val authCallback = Play.configuration.getString("twitter.auth.callback").get

    private val verifyCredentialsUrl = "https://api.twitter.com/1.1/account/verify_credentials.json"
  def authenticate = Action.async { request =>
    request.getQueryString("oauth_verifier").map { verifier =>
      val tokenPair = sessionTokenPair(request).get
      // We got the verifier; now get the access token, store it and back to index
      TWITTER.retrieveAccessToken(tokenPair, verifier) match {
        case Right(credentials) => {
          // We received the authorized tokens in the OAuth object - store it before we proceed
          WS.url(verifyCredentialsUrl).sign(OAuthCalculator(Twitter.KEY, credentials)).get()
            .map { userResponse =>
              val screenName = (userResponse.json \ "screen_name").as[String]
              Redirect(controllers.web.routes.Application.index)
                .withSession("screenName" -> Crypto.encryptAES(screenName))
            }
        }
        case Left(e) => throw e
      }
    }.getOrElse(
      TWITTER.retrieveRequestToken(authCallback) match {
        case Right(t) => {
          // We received the unauthorized tokens in the OAuth object - store it before we proceed
          Future.successful(
            Redirect(TWITTER.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
          )
        }
        case Left(e) => throw e
      })
  }

  private def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }
}
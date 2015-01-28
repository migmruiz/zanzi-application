package controllers.auth

import scala.concurrent.Future

import play.api._
import play.api.mvc._
import play.api.libs.Crypto
import play.api.libs.ws.WS

import helpers.Encodings._

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Google extends Controller {
  private val clientId = Play.configuration.getString("google.auth.client.id").get
  private val secret = Play.configuration.getString("google.auth.secret").get
  private val authCallback = Play.configuration.getString("google.auth.callback").get
  private val authUrl = "https://accounts.google.com/o/oauth2/auth" + queryString(Seq(
      "scope"         -> "email profile",
      "state"         -> "/profile",
      "redirect_uri"  -> authCallback,
      "response_type" -> "code",
      "client_id"     -> clientId
  ))

  def signIn = Action {
    Redirect(authUrl)
  }

  private val accessTokenUrl = "https://accounts.google.com/o/oauth2/token"

  private val userInfoUrl = "https://www.googleapis.com/oauth2/v1/userinfo"

  def callback = Action.async { request =>
    request.getQueryString("code").map { code =>
      val requestToken = WS.url(accessTokenUrl)
        .withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
        .post(postDataTokenFor(code))
      requestToken.flatMap { response =>
        val accessToken = (response.json \ "access_token").as[String]
        val userInfo = WS.url(userInfoUrl + queryString("access_token" -> accessToken)).get()
        userInfo.map { userResponse =>
          val email = (userResponse.json \ "email").as[String]
          Redirect(controllers.web.routes.Application.index)
            .withSession("userEmail" -> Crypto.encryptAES(email))
        }
      }
    }.getOrElse(Future.successful(BadRequest("Parameter 'code' is required.")))
  }

  private def postDataTokenFor(code: String) = {
    postData(Seq(
        "code"          -> code,
        "client_id"     -> clientId,
        "client_secret" -> secret,
        "redirect_uri"  -> authCallback,
        "grant_type"    -> "authorization_code"
    ))
  }
}

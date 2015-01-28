package controllers.auth

import play.api.mvc._

object AuthController extends Controller {
  
  def index() = Action {
    Ok(views.html.auth.index())
  }

  def signOut = Action {
    Redirect(controllers.auth.routes.AuthController.index).withNewSession
  }
}
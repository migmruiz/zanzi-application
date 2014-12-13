package controllers

import play.api.mvc._

object HealthCheckController extends Controller {

  def healthCheck = Action {
    Ok("I'm alive!")
  }

}
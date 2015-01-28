package controllers.api.v1

import org.joda.time._

import models.zanzi._
import play.api._
import play.api.libs.json._
import play.api.mvc._

object ClassRequestApiController extends Controller {
  def single = Action { request =>
    val dateTimeZone = DateTimeZone.forID("America/Sao_Paulo")
    val now = DateTime.now(dateTimeZone)
    request.getQueryString("field") match {
      case Some(field) => Ok(Json.toJson(makeClassRequest(now, field)))
      case None => BadRequest(Json.obj("accepted" -> false))
    }
  }

  private def makeClassRequest(start: DateTime, field: String): ClassRequest = {
    ClassRequest(
      dateRules = Json.arr(
        Json.obj(
          "start" -> start,
          "end" -> start.plusHours(2)
        ),
        Json.obj(
          "start" -> start.plusDays(4),
          "end" -> start.plusDays(6).minusHours(2)
        )
      ),
      location = Json.obj(
        "address" -> "Rua dos loucos",
        "number"-> 0
      ),
      scholar = Json.obj(
        "field" -> field,
        "year" -> 1,
        "comments" -> Json.arr()
      ),
      studentId = 1L
    )
  }
}
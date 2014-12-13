package controllers.api.v1

import org.joda.time._

import models.zanzi._
import play.api._
import play.api.libs.json._
import play.api.mvc._

object ClassRequestApiController extends Controller {
  def single = Action {
    val dateTimeZone = DateTimeZone.forID("America/Sao_Paulo")
    val classRequest = ClassRequest(
      dateRules = Json.arr(
        Json.obj(
          "start" -> DateTime.now(dateTimeZone),
          "end" -> DateTime.now(dateTimeZone).plusHours(2)
        ),
        Json.obj(
          "start" -> DateTime.now(dateTimeZone).plusDays(4),
          "end" -> DateTime.now(dateTimeZone).plusDays(6).minusHours(2)
        )
      ),
      location = Json.obj(
        "address" -> "Rua dos loucos",
        "number"-> 0
      ),
      scholar = Json.obj(
        "field" -> "Humanities",
        "year" -> 1,
        "comments" -> Json.arr()
      ),
      studentId = 1L
    )
    Ok(Json.toJson(classRequest)).as(JSON)
  }

}
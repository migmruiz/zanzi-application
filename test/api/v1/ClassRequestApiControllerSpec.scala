package api.v1

import org.joda.time._
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

import helpers.Encodings._

@RunWith(classOf[JUnitRunner])
class ClassRequestApiControllerSpec extends Specification {

  "ClassRequestApiController" should {

    val dateTimeZone = DateTimeZone.forID("America/Sao_Paulo")
    val now = DateTime.now(dateTimeZone)
    def aMinuteAgo = now.minusMinutes(1).getMillis
    def aMinuteFromNow = now.plusMinutes(1).getMillis
    val address = "Rua dos loucos"
    val addressNumber = 0
    val scholarField = "Humanities"
    val scholarYear = 1
    val studentId = 1L

    "return json on single request" in new WithApplication {
      val single = route(FakeRequest(GET, "/api/v1/classRequest" + queryString("field" -> scholarField)).withHeaders("AUTH_TOKEN" -> "x-auth-token")).get

      val content = contentAsJson(single)
      status(single) must beEqualTo(OK)
      contentType(single) must beSome.which(_ == "application/json")
      content must not(beNull)
      (content \ "dateRules" \\ "start") must not(beEmpty)
      (content \ "dateRules" \\ "start")(0).as[DateTime].getMillis must beBetween(aMinuteAgo, aMinuteFromNow)
      (content \ "location" \ "address").as[String] must beEqualTo(address)
      (content \ "location" \ "number").as[Int] must beEqualTo(addressNumber)
      (content \ "scholar" \ "field").as[String] must beEqualTo(scholarField)
      (content \ "scholar" \ "year").as[Int] must beEqualTo(scholarYear)
      (content \ "scholar" \\ "comments")(0).asOpt[String] must beNone
      (content \ "studentId").as[Long] must beEqualTo(studentId)
    }

    "return bad request on empty body" in new WithApplication {
      val classRequest = route(FakeRequest(GET, "/api/v1/classRequest").withHeaders("AUTH_TOKEN" -> "x-auth-token")).get

      status(classRequest) must beEqualTo(BAD_REQUEST)
      contentType(classRequest) must beSome.which(_ == "application/json")
      contentAsJson(classRequest) must beEqualTo(Json.obj("accepted" -> false))
    }
  }

}
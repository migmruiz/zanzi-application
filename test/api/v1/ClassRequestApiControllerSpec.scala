package api.v1

import org.joda.time._
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class ClassRequestApiControllerSpec extends Specification {

  "ClassRequestApiController" should {

    val dateTimeZone = DateTimeZone.forID("America/Sao_Paulo")
    def aMinuteAgo = DateTime.now(dateTimeZone).minusMinutes(1).getMillis
    def aMinuteFromNow = DateTime.now(dateTimeZone).plusMinutes(1).getMillis
    val address = "Rua dos loucos"
    val addressNumber = 0
    val scholarField = "Humanities"
    val scholarYear = 1
    val studentId = 1L

    "return json on single request" in new WithApplication {
      val single = route(FakeRequest(GET, "/api/v1/classRequest")).get

      val content = contentAsJson(single)
      status(single) must equalTo(OK)
      contentType(single) must beSome.which(_ == "application/json")
      content must not(beNull)
      (content \ "dateRules" \\ "start") must not(beEmpty)
      (content \ "dateRules" \\ "start")(0).as[DateTime].getMillis must beBetween(aMinuteAgo, aMinuteFromNow)
      (content \ "location" \ "address").as[String] must equalTo(address)
      (content \ "location" \ "number").as[Int] must equalTo(addressNumber)
      (content \ "scholar" \ "field").as[String] must equalTo(scholarField)
      (content \ "scholar" \ "year").as[Int] must equalTo(scholarYear)
      (content \ "scholar" \\ "comments")(0).asOpt[String] must beNone
      (content \ "studentId").as[Long] must equalTo(studentId)
    }
  }

}
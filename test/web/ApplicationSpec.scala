package web

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.Crypto

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beNone
    }
    "send 303 on an unauthorized request" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must beEqualTo(SEE_OTHER)
    }

    "render the index page" in new WithApplication {
      val home = route(FakeRequest(GET, "/").withSession("screenName" -> Crypto.encryptAES("migmruiz"))).get

      status(home) must beEqualTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain("Your new application is ready.")
    }
  }
}

package models

import org.joda.time.DateTime

import play.api.libs.json._

package object zanzi {

  case class ClassRequest(
    private val dateRules: JsArray,
    private val location: JsObject,
    private val scholar: JsObject,
    studentId: ForeignKey[Long],
    id: PrimaryKey[Long] = AutoGenerate) {

    lazy val when: Period = {
      dateRules.as[Seq[Map[String, DateTime]]] map { dateRule =>
        Period(
          dateRule.get("start").get,
          dateRule.get("end").get
        )
      } reduce { _ join _ }
    }
    lazy val where: Location = {
      Location(
        (location \ "address").as[String],
        (location \ "number").as[Int]
      )
    }
    lazy val what: Scholar = {
      Scholar(
        (scholar \ "field").as[String],
        (scholar \ "year").as[Int],
        (scholar \\ "comments").map { _.as[String] }
      )
    }
    
    lazy val who: Student = {
      // TODO find by studentId
      Student(
        name = "",
        email = None,
        locations = List(),
        id = Some(studentId)
      )
    }
  }
  object ClassRequest {
    implicit val passengerFormat = Json.format[ClassRequest]
  }

  case class ClassOffer(
    requestId: ForeignKey[Long],
    professorId: ForeignKey[Long],
    extraInfo: JsObject,
    id: PrimaryKey[Long] = AutoGenerate) {
    lazy val student: Student = {
      def studentId = (extraInfo \ "student" \ "id").as[ForeignKey[Long]]
      def studentName = (extraInfo \ "student" \ "name").as[String]
      Student(
        name = studentName,
        email = None,
        locations = List(),
        id = Some(studentId)
      )
    }
  }

  case class Student(
    name: String,
    email: Option[String],
    phoneNumber: Option[PhoneNumber] = None,
    locations: List[JsObject],
    id: PrimaryKey[Long] = AutoGenerate) {
  }

  case class Professor(
    name: String,
    email: Option[String],
    phoneNumber: Option[PhoneNumber],
    id: PrimaryKey[Long] = AutoGenerate) {
  }

  type ForeignKey[T] = T
  type PrimaryKey[T] = Option[T]
  val AutoGenerate: PrimaryKey[Nothing] = None

  sealed class Period(constructor: Either[(DateTime, DateTime), Traversable[(DateTime, DateTime)]]) {
    val dates: Traversable[(DateTime, DateTime)] = {
      constructor match {
        case Left(single)    => Seq(single)
        case Right(multiple) => multiple
      }
    }
    def join(another: Period): Period = {
      Period(Seq.concat(dates, another.dates))
    }
  }
  object Period {
    def apply(single: (DateTime, DateTime)): Period = new Period(Left(single))
    def apply(multiple: Traversable[(DateTime, DateTime)]): Period = new Period(Right(multiple))
  }

  case class Location(address: String, number: Int) {}

  case class Scholar(field: String, year: Int, comments: Seq[String]) {}

  case class PhoneNumber(number: String, countryCode: String) {}
}

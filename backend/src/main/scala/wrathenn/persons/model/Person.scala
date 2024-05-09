package wrathenn.persons.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Person(
  id: Long,
  name: String,
  age: Option[Int],
  address: Option[String],
  work: Option[String],
)

case class PersonCreate(
  name: String,
  age: Option[Int],
  address: Option[String],
  work: Option[String],
)

case class PersonUpdate(
  name: Option[String],
  age: Option[Int],
  address: Option[String],
  work: Option[String],
)

object PersonEncoders {
  implicit val personEncoder: Encoder[Person] = deriveEncoder[Person]
}


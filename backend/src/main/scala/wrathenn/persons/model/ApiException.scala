package wrathenn.persons.model

import io.circe.{Encoder, Json}
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps

sealed trait ApiException {
  def msg: String
  def cause: Option[Throwable]
}

case class NotFoundException(
  override val msg: String,
  override val cause: Option[Throwable] = None
) extends ApiException

case class BadArgumentException(
  msg: String,
  cause: Option[Throwable] = None
) extends ApiException

case class UnknownException(
  msg: String,
  cause: Option[Throwable] = None
) extends ApiException

object ApiExceptionEncoders {
  implicit val throwableEncoder: Encoder[Throwable] = new Encoder[Throwable] {
    override def apply(a: Throwable): Json = a.getCause.toString.asJson
  }
  implicit val notFoundException: Encoder[NotFoundException] = deriveEncoder[NotFoundException]
  implicit val badArgumentException: Encoder[BadArgumentException] = deriveEncoder[BadArgumentException]
  implicit val apiExceptionEncoder: Encoder[UnknownException] = deriveEncoder[UnknownException]
}

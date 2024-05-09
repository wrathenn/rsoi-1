package wrathenn.persons.api

import cats.implicits._
import cats._
import cats.effect.Async
import io.circe.Encoder
import io.circe.generic.auto.exportDecoder
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.{EntityDecoder, Header, HttpRoutes, Response}
import org.typelevel.ci.CIString
import wrathenn.persons.model.{ApiException, BadArgumentException, NotFoundException, PersonCreate, PersonUpdate, UnknownException}
import wrathenn.persons.services.PersonsService
import wrathenn.persons.model.ApiExceptionEncoders._
import ServiceResponseOps._
import org.http4s.dsl.Http4sDsl
import wrathenn.persons.model.PersonEncoders.personEncoder

object ServiceResponseOps {
  implicit class ApiExceptionConverter(val exception: ApiException) extends AnyVal {
    def toResponse[F[_] : Monad]: F[Response[F]] = {
      val dsl = Http4sDsl[F]
      import dsl._

      exception match {
        case e: NotFoundException => NotFound(e.asJson)
        case e: BadArgumentException => BadRequest(e.asJson)
        case e: UnknownException => BadRequest(e.asJson)
      }
    }
  }

  implicit class ServiceResponseConverter[A : Encoder](val sr: Either[ApiException, A]) {
    def to200[F[_] : Monad]: F[Response[F]] = {
      val dsl = Http4sDsl[F]
      import dsl._

      sr match {
        case Left(error) => error.toResponse
        case Right(result) => Ok(result.asJson)
      }
    }

    def to204[F[_] : Monad]: F[Response[F]] = {
      val dsl = Http4sDsl[F]
      import dsl._

      sr match {
        case Left(error) => error.toResponse
        case Right(_) => NoContent()
      }
    }
  }
}

object PersonsRoute {
  def routes
      [F[_]: Async]
      (personsService: PersonsService[F])
      (location: String)
  : HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    implicit val personCreateDecoder: EntityDecoder[F, PersonCreate] = jsonOf[F, PersonCreate]
    implicit val personUpdateDecoder: EntityDecoder[F, PersonUpdate] = jsonOf[F, PersonUpdate]

    HttpRoutes.of[F] {
      case GET -> Root / LongVar(id) => for {
        result <- personsService.getById(id)
        response <- result.to200[F]
      } yield response

      case GET -> Root => for {
        result <- personsService.getAll()
        response <- result.to200[F]
      } yield response

      case req @ POST -> Root => for {
        body <- req.as[PersonCreate]
        result <- personsService.addUser(body)
        response: Response[F] <- result match {
          case Left(exception) => exception.toResponse[F]
          case Right(id) => Created().map(_.putHeaders(Header.Raw(CIString("Location"), s"$location/$id")))
        }
      } yield response

      case req @ PATCH -> Root / LongVar(id) => for {
        personUpdate <- req.as[PersonUpdate]
        result <- personsService.editUser(id, personUpdate)
        response <- result.to200[F]
      } yield response

      case DELETE -> Root / LongVar(id) => for {
        result <- personsService.deleteUser(id)
        response <- result.to204[F]
      } yield response
    }
  }
}

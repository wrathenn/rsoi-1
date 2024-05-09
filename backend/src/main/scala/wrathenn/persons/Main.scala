package wrathenn.persons

import cats.effect.{Async, IO, IOApp, Resource}
import doobie.{LogHandler, Transactor}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import wrathenn.persons.api.PersonsRoute
import wrathenn.persons.config.AppConfig
import wrathenn.persons.db.{PersonsRepository, PersonsRepositoryImpl}
import wrathenn.persons.services.{PersonsService, PersonsServiceImpl}
import Resource.{eval => rEval}
import doobie.util.log.LogEvent
import org.http4s.server.middleware.{ErrorAction, ErrorHandling}
import org.typelevel.log4cats.LoggerFactory

object Main extends IOApp.Simple {
  private def runServer[F[+_]: Async]: F[Nothing] = {
    val printSqlLogHandler: LogHandler[F] = (logEvent: LogEvent) => Async[F].delay {
      println(logEvent.sql.stripMargin)
    }

    for {
      _ <- rEval(Async[F].delay(println("Starting Persons Backend...")))
      config <- rEval(AppConfig.load[F])
      _ <- rEval(Async[F].delay(println(config)))

      transactor <- rEval(Async[F].blocking(Transactor.fromDriverManager[F](
          driver = "org.postgresql.Driver",
          url = config.db.url,
          user = config.db.user,
          password = config.db.password,
          logHandler = Some(printSqlLogHandler),
      )))

      personsRepo: PersonsRepository[F] = new PersonsRepositoryImpl[F](transactor)
      personsService: PersonsService[F] = new PersonsServiceImpl[F](personsRepo)

      personsRouteLocation = s"${config.app.location}/persons"
      api = Router[F](
         personsRouteLocation -> PersonsRoute.routes[F](personsService)(personsRouteLocation)
      ).orNotFound

      withErrorLogging = ErrorHandling.Recover.total(
        ErrorAction.log(
          api,
          messageFailureLogAction = (t, msg) => Async[F].delay { println(msg); println(t) },
          serviceErrorLogAction = (t, msg) => Async[F].delay { println(msg); println(t) },
        )
      )

      _ <- EmberServerBuilder.default[F]
        .withHost(config.app.host.address)
        .withPort(config.app.port)
        .withHttpApp(withErrorLogging)
        .build
     } yield ()
  }.useForever

  val run: IO[Nothing] = runServer[IO]
}

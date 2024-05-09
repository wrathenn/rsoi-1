package wrathenn.persons.config

import cats.implicits._
import cats.effect.Async
import com.comcast.ip4s.Port
import org.http4s.Uri
import pureconfig.module.catseffect.syntax.CatsEffectConfigSource
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.http4s._
import CustomConfigReaders._
import pureconfig.error.CannotConvert

import java.nio.file.{Path, Paths}

object CustomConfigReaders {
  implicit val portConfigReader: ConfigReader[Port] = (cur: ConfigCursor) => for {
    intValue <- cur.asInt
    port <- cur.scopeFailure {
      Port.fromInt(intValue) match {
        case Some(port) => port.asRight
        case None => CannotConvert(
          value = intValue.toString,
          toType = "Port",
          because = "Invalid port"
        ).asLeft[Port]
      }
    }
  } yield port
}


case class DbConnectionConfig(
  url: String,
  user: String,
  password: String,
)

case class ServerConfig(
  host: Uri.Ipv4Address,
  port: Port,
  location: String,
)

case class AppConfig(
  app: ServerConfig,
  db: DbConnectionConfig,
)

object AppConfig {
  def load[F[_] : Async]: F[AppConfig] = {
    for {
      configPath: String <- Async[F].blocking {
        // cant get as path because filesystem issues in docker
        new String(this.getClass.getResourceAsStream("/application.conf").readAllBytes())
      }

      config <- ConfigSource.string(configPath).loadF[F, AppConfig]
    } yield config
  }
}

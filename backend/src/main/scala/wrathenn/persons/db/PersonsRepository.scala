package wrathenn.persons.db

import cats.data.EitherT
import cats.effect.Async
import wrathenn.persons.db.entities.{PersonEntity, PersonInsertableEntity}
import wrathenn.persons.model.{ApiException, BadArgumentException, NotFoundException, Person, PersonCreate, PersonUpdate, UnknownException}
import doobie.implicits.toSqlInterpolator
import doobie.util.transactor.Transactor
import doobie.enumerated.SqlState
import cats.implicits._
import doobie.implicits._

trait PersonsRepository[F[_]] {
  def selectAll(): F[Either[ApiException, List[Person]]]

  def selectById(id: Long): F[Either[ApiException, Person]]

  def insert(person: PersonCreate): F[Either[ApiException, Long]]

  def delete(id: Long): F[Either[ApiException, Long]]

  def update(id: Long, person: PersonUpdate): F[Either[ApiException, Person]]
}


class PersonsRepositoryImpl[F[_] : Async](
  val xa: Transactor[F],
) extends PersonsRepository[F] {
  private def findEntityById(id: Long): F[Either[ApiException, PersonEntity]] = {
    sql"""
      select id, name, age, address, work
      from persons.persons
      where id = ${id}
    """
      .query[PersonEntity]
      .option
      .transact(xa)
      .map {
        case Some(personEntity) => personEntity.asRight
        case None => NotFoundException(s"Person with id = $id not found").asLeft
      }
  }

  private def updateEntity(personEntity: PersonEntity): F[Either[ApiException, PersonEntity]] = {
    sql"""
      update persons.persons
      set name = ${personEntity.name},
          age = ${personEntity.age},
          address = ${personEntity.address},
          work = ${personEntity.work}
      where id = ${personEntity.id}
    """.update
      .withGeneratedKeys[PersonEntity]("id", "name", "age", "address", "work")
      .attemptSomeSqlState[ApiException] {
        case state => BadArgumentException(s"Could not update, error $state")
      }
      .transact(xa)
      .compile
      .lastOrError
  }

  override def selectById(id: Long): F[Either[ApiException, Person]] = {
    for {
      entity <- findEntityById(id)
    } yield entity.map(_.toModel)
  }

  override def insert(person: PersonCreate): F[Either[ApiException, Long]] = {
    val entity = PersonInsertableEntity.fromModel(person)
    val res: F[Either[ApiException, Long]] = sql"""
      insert into persons.persons(name, age, address, work)
      values (${entity.name}, ${entity.age}, ${entity.address}, ${entity.work})
    """
      .update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSomeSqlState[ApiException] {
        case state => BadArgumentException(s"Could not insert, error $state")
      }
      .transact(xa)

    res
  }

  override def selectAll(): F[Either[ApiException, List[Person]]] = {
    for {
      listResult: Either[ApiException, List[PersonEntity]] <- sql"""
            select id, name, age, address, work
            from persons.persons
          """
        .query[PersonEntity]
        .to[List]
        .attemptSomeSqlState {
          case state: SqlState => BadArgumentException(s"Could not select, error $state")
        }
        .transact(xa)
      converted <- (listResult match {
        case Right(persons) => Right(persons.map { _.toModel })
        case Left(e) => Left(e)
      }).pure[F]
    } yield converted
  }

  override def delete(id: Long): F[Either[ApiException, Long]] = {
    sql"""
      delete from persons.persons
      where id = $id
    """
      .update
      .run
      .transact(xa)
      .map {
        case 0 => NotFoundException(s"Person with id=$id not found").asLeft
        case n => n.toLong.asRight
      }
  }

  override def update(id: Long, person: PersonUpdate): F[Either[ApiException, Person]] = {
    val eitherTresult = for {
      current <- EitherT(findEntityById(id))
      updated = current.toUpdated(person)
      res <- EitherT(updateEntity(updated))
    } yield res.toModel

    eitherTresult.value
  }
}


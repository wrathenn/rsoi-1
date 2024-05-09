package wrathenn.persons.services

import cats.effect.Async
import wrathenn.persons.db.PersonsRepository
import wrathenn.persons.model.{ApiException, Person, PersonCreate, PersonUpdate}

trait PersonsService[F[_]] {
  def getAll(): F[Either[ApiException, List[Person]]]
  def getById(id: Long): F[Either[ApiException, Person]]
  def addUser(person: PersonCreate): F[Either[ApiException, Long]]
  def editUser(id: Long, person: PersonUpdate): F[Either[ApiException, Person]]
  def deleteUser(id: Long): F[Either[ApiException, Long]]
}

class PersonsServiceImpl[F[_] : Async](
  private val personsRepository: PersonsRepository[F]
) extends PersonsService[F] {
  override def getAll(): F[Either[ApiException, List[Person]]] = {
    personsRepository.selectAll()
  }

  override def getById(id: Long): F[Either[ApiException, Person]] = {
    personsRepository.selectById(id)
  }

  override def addUser(person: PersonCreate): F[Either[ApiException, Long]] = {
    personsRepository.insert(person)
  }

  override def editUser(id: Long, person: PersonUpdate): F[Either[ApiException, Person]] = {
    personsRepository.update(id, person)
  }

  override def deleteUser(id: Long): F[Either[ApiException, Long]] = {
    personsRepository.delete(id)
  }
}

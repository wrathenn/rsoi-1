import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.mockito.Mockito.when
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import util.PersonBuilder
import wrathenn.persons.db.{PersonsRepository, PersonsRepositoryImpl}
import wrathenn.persons.services.PersonsServiceImpl
import cats.implicits._

class ServicesTests extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  "personsService tests" - {
    "insert" in {
      val personsRepositoryMock = mock[PersonsRepository[IO]]
      when{
        personsRepositoryMock.insert(PersonBuilder.testCreatePerson())
      }.thenReturn { IO.pure {
        PersonBuilder.testPersonId.asRight
      } }
      val personsService = new PersonsServiceImpl[IO](personsRepositoryMock)

      personsService.addUser(PersonBuilder.testCreatePerson()).asserting {
        _ shouldBe PersonBuilder.testPersonId.asRight
      }
    }

    "get by id" in {
      val personsRepositoryMock = mock[PersonsRepository[IO]]
      when {
        personsRepositoryMock.selectById(PersonBuilder.testPersonId)
      }.thenReturn { IO.pure {
        PersonBuilder.testPerson().asRight
      } }
      val personsService = new PersonsServiceImpl[IO](personsRepositoryMock)

      personsService.getById(PersonBuilder.testPersonId).asserting {
        _ shouldBe PersonBuilder.testPerson().asRight
      }
    }

    "get all" in {
      val personsRepositoryMock = mock[PersonsRepository[IO]]
      when {
        personsRepositoryMock.selectAll()
      }.thenReturn { IO.pure {
        List(PersonBuilder.testPerson()).asRight
      } }
      val personsService = new PersonsServiceImpl[IO](personsRepositoryMock)

      personsService.getAll().asserting {
        _ shouldBe List(PersonBuilder.testPerson()).asRight
      }
    }

    "delete by id" in {
      val personsRepositoryMock = mock[PersonsRepository[IO]]
      when {
        personsRepositoryMock.delete(PersonBuilder.testPersonId)
      }.thenReturn { IO.pure {
        PersonBuilder.testPersonId.asRight
      } }
      val personsService = new PersonsServiceImpl[IO](personsRepositoryMock)

      personsService.deleteUser(PersonBuilder.testPersonId).asserting {
        _ shouldBe PersonBuilder.testPersonId.asRight
      }
    }

    "update" in {
      val personsRepositoryMock = mock[PersonsRepositoryImpl[IO]]
      when {
        personsRepositoryMock.update(PersonBuilder.testPersonId, PersonBuilder.testUpdatePerson())
      }.thenReturn { IO.pure {
        PersonBuilder.testPersonEntity().toUpdated(PersonBuilder.testUpdatePerson()).toModel.asRight
      } }

      val personsService = new PersonsServiceImpl[IO](personsRepositoryMock)

      personsService.editUser(PersonBuilder.testPersonId, PersonBuilder.testUpdatePerson()).asserting {
        _ shouldBe PersonBuilder.testUpdatedPersonEntity().toModel.asRight
      }
    }
  }
}

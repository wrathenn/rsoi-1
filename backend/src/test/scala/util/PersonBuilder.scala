package util

import cats.syntax._
import cats.implicits._
import wrathenn.persons.db.entities.PersonEntity
import wrathenn.persons.model.{Person, PersonCreate, PersonUpdate}

object PersonBuilder {
  def testPersonId: Long = 1

  def testPerson(): Person = Person(
    id = testPersonId,
    name = "test person",
    age = 1.some,
    address = none,
    work = none
  )

  def testPersonEntity(): PersonEntity = PersonEntity(
    id = testPersonId,
    name = "test person",
    age = 1.some,
    address = none,
    work = none
  )

  def testCreatePerson(): PersonCreate = PersonCreate(
    name = "test person",
    age = 1.some,
    address = none,
    work = none
  )

  def testUpdatePerson(): PersonUpdate = PersonUpdate(
    name = none,
    age = 123.some,
    address = "test address".some,
    work = "test work".some,
  )

  def testUpdatedPersonEntity(): PersonEntity = PersonEntity(
    id = testPersonId,
    name = "test person",
    age = 123.some,
    address = "test address".some,
    work = "test work".some,
  )
}

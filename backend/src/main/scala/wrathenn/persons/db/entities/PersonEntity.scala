package wrathenn.persons.db.entities

import wrathenn.persons.model.{Person, PersonCreate, PersonUpdate}

case class PersonEntity(
  id: Long,
  name: String,
  age: Option[Int],
  address: Option[String],
  work: Option[String],
) {
  lazy val toModel: Person = Person(
    id = id,
    name = name,
    age = age,
    address = address,
    work = work,
  )
  // for some reason by my task i dont need to update fields that are null
  def toUpdated(updateWith: PersonUpdate) = PersonEntity(
    id = id,
    name = updateWith.name.getOrElse(this.name),
    age = updateWith.age.orElse(this.age),
    address = updateWith.address.orElse(this.address),
    work = updateWith.work.orElse(this.work),
  )
}

case class PersonInsertableEntity(
  name: String,
  age: Option[Int],
  address: Option[String],
  work: Option[String],
)

object PersonInsertableEntity {
  def fromModel(model: PersonCreate) = PersonInsertableEntity(
    name = model.name,
    age = model.age,
    address = model.address,
    work = model.work,
  )
}

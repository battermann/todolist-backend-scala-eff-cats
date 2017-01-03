package dsls

import models.Todo
import org.atnos.eff.{Eff, |=}

import scala.concurrent.Future

sealed trait TodoStorage[+A]

case class Upsert[T](todo: Todo) extends TodoStorage[Long]
case class Get[T](id: Long) extends TodoStorage[Option[T]]
case class Delete[T](id: Long) extends TodoStorage[Unit]
case class DeleteAll[T]() extends TodoStorage[Unit]
case class GetAll[T]() extends TodoStorage[Seq[T]]

object TodoStorage {
  type _todoStorage[R] = TodoStorage |= R

  def upsert[T, R: _todoStorage](todo: Todo): Eff[R, Long] =
    Eff.send[TodoStorage, R, Long](Upsert(todo))

  def get[T, R: _todoStorage](id: Long): Eff[R, Option[Todo]] =
    Eff.send[TodoStorage, R, Option[Todo]](Get(id))

  def delete[T, R: _todoStorage](id: Long): Eff[R, Unit] =
    Eff.send[TodoStorage, R, Unit](Delete(id))

  def deleteAll[T, R: _todoStorage](): Eff[R, Unit] =
    Eff.send[TodoStorage, R, Unit](DeleteAll())

  def getAll[T, R: _todoStorage](): Eff[R, Seq[Todo]] =
    Eff.send[TodoStorage, R, Seq[Todo]](GetAll())
}

sealed trait Routes[+A]

case class Url[T](id: Long, secure: Boolean, host: String) extends Routes[String]

object Routes {
  type _routes[R] = Routes |= R

  def url[T, R: _routes](id: Long, secure: Boolean, host: String): Eff[R, String] =
    Eff.send[Routes, R, String](Url(id, secure, host))
}




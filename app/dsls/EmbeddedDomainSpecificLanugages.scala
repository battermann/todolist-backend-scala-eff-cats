package dsls

import models.Todo
import org.atnos.eff.{Eff, |=}

import scala.concurrent.Future

sealed trait TodoStorage[+A]

case class Upsert[T](todo: Todo) extends TodoStorage[Future[Long]]
case class Get[T](id: Long) extends TodoStorage[Future[Option[T]]]
case class Delete[T](id: Long) extends TodoStorage[Future[Unit]]
case class DeleteAll[T]() extends TodoStorage[Future[Unit]]
case class GetAll[T]() extends TodoStorage[Future[Seq[T]]]

object TodoStorage {
  type _todoStorage[R] = TodoStorage |= R

  def upsert[T, R: _todoStorage](todo: Todo): Eff[R, Future[Long]] =
    Eff.send[TodoStorage, R, Future[Long]](Upsert(todo))

  def get[T, R: _todoStorage](id: Long): Eff[R, Future[Option[Todo]]] =
    Eff.send[TodoStorage, R, Future[Option[Todo]]](Get(id))

  def delete[T, R: _todoStorage](id: Long): Eff[R, Future[Unit]] =
    Eff.send[TodoStorage, R, Future[Unit]](Delete(id))

  def deleteAll[T, R: _todoStorage](): Eff[R, Future[Unit]] =
    Eff.send[TodoStorage, R, Future[Unit]](DeleteAll())

  def getAll[T, R: _todoStorage](): Eff[R, Future[Seq[Todo]]] =
    Eff.send[TodoStorage, R, Future[Seq[Todo]]](GetAll())
}

sealed trait Routes[+A]

case class Url[T](id: Long, secure: Boolean, host: String) extends Routes[String]

object Routes {
  type _routes[R] = Routes |= R

  def url[T, R: _routes](id: Long, secure: Boolean, host: String): Eff[R, String] =
    Eff.send[Routes, R, String](Url(id, secure, host))
}




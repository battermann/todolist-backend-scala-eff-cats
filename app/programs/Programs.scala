package programs

import dsls._
import dsls.TodoStorage._
import dsls.Routes._
import models.{CreateRequest, Todo, TodoResponse}
import play.api.libs.json.{JsBoolean, JsNumber, JsString, JsValue}
import cats._
import cats.data._
import cats.implicits._
import org.atnos.eff._

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

object Programs {

  def toResponse(todo: Todo, url: String): TodoResponse =
    TodoResponse(todo.title, todo.completed, todo.id, url, todo.order)

  def list[R: _todoStorage: _routes](secure: Boolean, host: String): Eff[R, Future[Seq[TodoResponse]]] =
    for {
      futureTodos        <- getAll()
      futureTodosWithUrl <- Eff.traverseA(futureTodos)(_.toList.traverseU(todo => url(todo.id, secure, host).map(url => (todo,url))))
    } yield futureTodosWithUrl.map(_.map { case (todo, url) => toResponse(todo, url) })

  def deleteAll[R: _todoStorage](): Eff[R, Future[Unit]] = deleteAll()

  def delete[R: _todoStorage](id: Long): Eff[R, Future[Unit]] = delete(id)

  def view[R: _todoStorage: _routes](id: Long, secure: Boolean, host: String): Eff[R, Future[Option[TodoResponse]]] =
    for {
      futureMaybeTodo        <- get(id)
      futureMaybeTodoWithUrl <- Eff.traverseA(futureMaybeTodo)(_.traverseU(todo => url(todo.id, secure, host).map(url => (todo,url))))
    } yield futureMaybeTodoWithUrl.map(_.map { case (todo, url) => toResponse(todo, url) })


  def create[R: _todoStorage: _routes](req: CreateRequest, secure: Boolean, host: String): Eff[R, Future[TodoResponse]] = {
    val completed = false
    val todo = Todo(req.title, completed, -1, req.order)
    for {
      futureId  <- upsert(todo)
      futureUrl <- Eff.traverseA(futureId)(id => url(id, secure, host))
    } yield for {
      id  <- futureId
      url <- futureUrl
    } yield toResponse(todo.copy(id = id), url)
  }

  def modify[R: _todoStorage: _routes](id: Long, patches: Map[String, JsValue], secure: Boolean, host: String): Eff[R, Future[Option[TodoResponse]]] = {
    for {
      futureMaybeTodo <- get(id)
      futureMaybePatched = futureMaybeTodo.map(maybeTodo => patches.foldLeft(maybeTodo) { (todo, field) =>
        field match {
          case ("completed", JsBoolean(patchCompleted)) => todo.map(t => t.copy(completed = patchCompleted))
          case ("title", JsString(title))               => todo.map(t => t.copy(title=title))
          case ("order", JsNumber(order))               => todo.map(t => t.copy(order=Some(order.toLong)))
          case _                                        => None
        }
      })
      _                 <- Eff.traverseA(futureMaybePatched)(_.traverseU(todo => upsert(todo)))
      futureTodoWithUrl <- Eff.traverseA(futureMaybePatched)(_.traverseU(todo => url(todo.id, secure, host).map(url => (todo,url))))
    } yield futureTodoWithUrl.map(_.map { case (todo, url) => toResponse(todo, url) })
  }
}

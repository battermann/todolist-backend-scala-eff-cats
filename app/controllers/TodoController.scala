package controllers

import akka.actor.{ActorSystem, Props}
import dsls.{Routes, TodoStorage}
import models.CreateRequest
import models.JsonFormats._
import programs.Programs
import interpreters.RoutesInterpreter._
import interpreters.TodoStorageInterpreter._
import play.api.libs.json._
import play.api.mvc._
import org.atnos.eff._
import storage.TodoStore
import syntax.all._

import scala.concurrent.ExecutionContext.Implicits.global

class TodoController extends Controller {

  type Stack = Fx.fx2[TodoStorage, Routes]

  val system = ActorSystem("TodoBackendActorSystem")
  val todoStore = system.actorOf(Props[TodoStore], name = "todoStore")

  def create = Action.async(parse.json[CreateRequest]) { request =>
    val createTodo = Programs.create[Stack](request.body, request.secure, request.host)
    val futureTodo = runTodoStorage(todoStore, runRoutes(createTodo)).run
    futureTodo.map(x => Ok(Json.toJson(x)))
  }

  def view(id: Long) = Action.async { request =>
    val viewTodo = Programs.view[Stack](id, request.secure, request.host)
    val futureMaybeTodo = runTodoStorage(todoStore, runRoutes(viewTodo)).run
    futureMaybeTodo map {
      case Some(todo) => Ok(Json.toJson(todo))
      case None       => NotFound
    }
  }

  def modify(id: Long) = Action.async(parse.json) { request =>
    val patches = request.body.as[JsObject].fields.toMap
    val modifyTodo = Programs.modify[Stack](id, patches, request.secure, request.host)
    val futureMaybeTodo = runTodoStorage(todoStore, runRoutes(modifyTodo)).run
    futureMaybeTodo map {
      case Some(todo) => Ok(Json.toJson(todo))
      case None       => BadRequest
    }
  }

  def delete(id: Long) = Action.async {
    val deleteTodo = Programs.delete[Stack](id)
    val response = runTodoStorage(todoStore, runRoutes(deleteTodo)).run
    response map (_ => Ok(""))
  }

  def list = Action.async { request =>
    val listTodos = Programs.list[Stack](request.secure, request.host)
    val futureListResponse = runTodoStorage(todoStore, runRoutes(listTodos)).run
    futureListResponse map (list => Ok(JsArray(list.map(Json.toJson(_)))))
  }

  def deleteAll = Action.async {
    val deleteAllTodos = Programs.deleteAll[Stack]()
    val response = runTodoStorage(todoStore,runRoutes(deleteAllTodos)).run
    response map (_ => Ok(""))
  }
}
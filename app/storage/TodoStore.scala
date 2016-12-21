package storage

import akka.actor.Actor
import models.Todo

case class UpsertMsg(todo: Todo)
case class DeleteMsg(id: Long)
case class DeleteAllMsg()
case class GetMsg(id: Long)
case class ListMsg()

class TodoStore extends Actor {
  var todos = List[Todo]()

  def receive: Receive = {
    case UpsertMsg(todo: Todo) =>
      if (todo.id < 0) {
        val index = todos.length
        todos = todo.copy(id = index) :: todos
        sender ! index
      }
      else {
        todos = todos.map(t => if(t.id == todo.id) todo else t)
        sender ! todo.id
      }
    case DeleteMsg(id: Long) =>
      todos = todos.filter(_.id != id)
    case DeleteAllMsg() =>
      todos = List.empty
    case GetMsg(id: Long) =>
      sender ! todos.find(_.id == id)
    case ListMsg() =>
      sender ! todos
    case _ => ()
  }
}

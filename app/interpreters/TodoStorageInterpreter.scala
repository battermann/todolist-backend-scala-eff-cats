package interpreters

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import cats.Traverse
import cats.implicits._
import dsls._
import models.Todo
import org.atnos.eff._
import org.atnos.eff.interpret._
import storage._

import scala.concurrent.duration._
import scala.language.postfixOps


object TodoStorageInterpreter {

  def runTodoStorage[R, A](todoStore: ActorRef, effects: Eff[R, A])(implicit m: TodoStorage <= R): Eff[m.Out, A] = {

    implicit val timeout = Timeout(5 seconds)

    def matchStorage[X](store: TodoStorage[X]): X =  {
      store match {
        case Get(id) =>
          (todoStore ? GetMsg(id)).mapTo[Option[Todo]].asInstanceOf[X]

        case Delete(id) =>
          (todoStore ? DeleteMsg(id)).mapTo[Todo].asInstanceOf[X]

        case Upsert(todo) =>
          (todoStore ? UpsertMsg(todo)).mapTo[Todo].asInstanceOf[X]

        case DeleteAll() =>
          (todoStore ? DeleteAllMsg()).mapTo[Todo].asInstanceOf[X]

        case GetAll() =>
          (todoStore ? ListMsg()).mapTo[Todo].asInstanceOf[X]
      }
    }

    val recurse = new Recurse[TodoStorage, m.Out, A] {
      def apply[X](repo: TodoStorage[X]): X Either Eff[m.Out, A] = Left {
        matchStorage(repo)
      }

      def applicative[X, Tr[_] : Traverse](ms: Tr[TodoStorage[X]]): Tr[X] Either TodoStorage[Tr[X]] =
        Left(ms.map(matchStorage))
    }

    interpret1[R, m.Out, TodoStorage, A, A](a => a)(recurse)(effects)(m)
  }
}

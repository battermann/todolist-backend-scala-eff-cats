package interpreters

import cats.Traverse
import org.atnos.eff._
import interpret._
import cats.implicits._
import dsls._

object RoutesInterpreter {

  def runRoutes[R, A](effects: Eff[R, A])(implicit m: Routes <= R): Eff[m.Out, A] = {

    def url(id: Long, secure: Boolean, host: String) = controllers.routes.TodoController.view(id).absoluteURL(secure, host)

    val recurse = new Recurse[Routes, m.Out, A] {
      def apply[X](r: Routes[X]): X Either Eff[m.Out, A] = Left {
        r match {
          case Url(id, secure, host) =>
            url(id, secure, host)
        }
      }

      def applicative[X, Tr[_] : Traverse](ms: Tr[Routes[X]]): Tr[X] Either Routes[Tr[X]] =
        Left(ms.map {
          case Url(id, secure, host) => url(id, secure, host)
        })
    }

    interpret1[R, m.Out, Routes, A, A](a => a)(recurse)(effects)(m)
  }
}

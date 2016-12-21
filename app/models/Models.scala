package models

import play.api.libs.json.Json

case class Todo(title: String, completed: Boolean, id: Long, order: Option[Long])
case class TodoResponse(title: String, completed: Boolean, id: Long, url: String, order: Option[Long])
case class CreateRequest(title: String, order: Option[Long])

object JsonFormats {
  implicit val CreateRequestFormat = Json.format[CreateRequest]
  implicit val TodoFormat = Json.format[Todo]
  implicit val TodoResponseFormat = Json.format[TodoResponse]
}

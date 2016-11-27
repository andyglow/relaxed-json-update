package com.github.andyglow.relaxed

import spray.json._

case class SprayJsonSupport(json: JsValue) extends Reader {
  def get(field: String): Option[JsValue] = json.asJsObject.fields.get(field)
  override def reader(field: String): Option[Reader] = get(field) map SprayJsonSupport.apply
  override def readerOpt(field: String): Option[Option[Reader]] = get(field) match {
    case None         => None
    case Some(JsNull) => Some(None)
    case Some(json)   => Some(Some(SprayJsonSupport(json)))
  }
  override def opt[T: Reads](field: String): Option[T] = {
    val jsonReader = implicitly[Reads[T]].asInstanceOf[JsonReader[T]]
    get(field) map jsonReader.read
  }
  override def optOpt[T: Reads](field: String): Option[Option[T]] = {
    val jsonReader = implicitly[Reads[T]].asInstanceOf[JsonReader[T]]
    get(field) match {
      case None         => None
      case Some(JsNull) => Some(None)
      case Some(json)   => Some(Some(jsonReader read json))
    }
  }
}

object SprayJsonSupport {

  implicit def readsImpl[T](implicit x: JsonReader[T]): Reads[T] = new JsonReader[T]() with Reads[T] {
    override def read(json: JsValue): T = x read json
  }

}
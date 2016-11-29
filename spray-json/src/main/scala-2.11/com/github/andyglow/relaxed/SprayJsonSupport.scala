package com.github.andyglow.relaxed

import spray.json._

case class SprayJsonSupport(json: JsValue) extends Reader with ReaderSupport[JsValue] {
  def isNull: (JsValue) => Boolean = _ == JsNull
  def ctor: (JsValue) => SprayJsonSupport = SprayJsonSupport.apply

  def get(field: String): Option[JsValue] = json.asJsObject.fields.get(field)

  override def opt[T: Reads](field: String): Option[T] = {
    val jsonReader = implicitly[Reads[T]].asInstanceOf[JsonReader[T]]
    get(field) map jsonReader.read
  }
  override def optOpt[T: Reads](field: String): Option[Option[T]] = {
    val jsonReader = implicitly[Reads[T]].asInstanceOf[JsonReader[T]]
    getOpt(field) map {_ map jsonReader.read}
  }
}

object SprayJsonSupport {
  implicit def readsImpl[T](implicit x: JsonReader[T]): Reads[T] = new JsonReader[T]() with Reads[T] {
    override def read(json: JsValue): T = x read json
  }
}
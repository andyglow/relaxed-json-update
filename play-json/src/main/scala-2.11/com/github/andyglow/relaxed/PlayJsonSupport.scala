package com.github.andyglow.relaxed

import play.api.libs.json.{Reads => PlayReads, _}

case class PlayJsonSupport(json: JsValue) extends Reader with ReaderSupport[JsValue] {
  def isNull: (JsValue) => Boolean = _ == JsNull
  def ctor: (JsValue) => PlayJsonSupport = PlayJsonSupport.apply

  def get(field: String): Option[JsValue] = (json \ field).asOpt[JsValue]

  override def opt[T: Reads](field: String): Option[T] = {
    implicit val _ = implicitly[Reads[T]].asInstanceOf[PlayReads[T]]
    get(field) map {_.as[T]}
  }

  override def optOpt[T: Reads](field: String): Option[Option[T]] = {
    implicit val _ = implicitly[Reads[T]].asInstanceOf[PlayReads[T]]
    getOpt(field) map {_ map {_.as[T]}}
  }
}

object PlayJsonSupport {
  implicit def readsImpl[T](implicit x: PlayReads[T]): Reads[T] = new PlayReads[T]() with Reads[T] {
    override def reads(json: JsValue): JsResult[T] = x.reads(json)
  }
}
package com.github.andyglow.relaxed

import upickle.Js._
import upickle.default.{Reader => UpickleReader}

case class UpickleSupport(json: Value) extends Reader {
  def get(field: String): Option[Value] = json.obj get field
  override def reader(field: String): Option[Reader] = get(field) map UpickleSupport.apply
  override def readerOpt(field: String): Option[Option[Reader]] = get(field) match {
    case None         => None
    case Some(Null) => Some(None)
    case Some(json)   => Some(Some(UpickleSupport(json)))
  }
  override def opt[T: Reads](field: String): Option[T] = {
    val read = implicitly[Reads[T]].asInstanceOf[UpickleReader[T]]
    get(field) map read.read
  }
  override def optOpt[T: Reads](field: String): Option[Option[T]] = {
    val read = implicitly[Reads[T]].asInstanceOf[UpickleReader[T]]
    get(field) match {
      case None         => None
      case Some(Null) => Some(None)
      case Some(json)   => Some(Some(read read json))
    }
  }
}

object UpickleSupport {
  implicit def readsImpl[T](implicit x: UpickleReader[T]): Reads[T] = new UpickleReader[T]() with Reads[T] {
    override def read0: PartialFunction[Value, T] = x.read0
  }
}
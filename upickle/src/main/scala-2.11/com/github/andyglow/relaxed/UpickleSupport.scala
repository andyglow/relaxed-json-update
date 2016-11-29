package com.github.andyglow.relaxed

import upickle.Js._
import upickle.default.{Reader => UpickleReader}

case class UpickleSupport(json: Value) extends Reader with ReaderSupport[Value] {
  def isNull: (Value) => Boolean = _ == Null
  def ctor: (Value) => UpickleSupport = UpickleSupport.apply

  def get(field: String): Option[Value] = json.obj get field

  override def opt[T: Reads](field: String): Option[T] = {
    val read = implicitly[Reads[T]].asInstanceOf[UpickleReader[T]]
    get(field) map read.read
  }

  override def optOpt[T: Reads](field: String): Option[Option[T]] = {
    val read = implicitly[Reads[T]].asInstanceOf[UpickleReader[T]]
    getOpt(field) map {_ map read.read}
  }
}

object UpickleSupport {
  implicit def readsImpl[T](implicit x: UpickleReader[T]): Reads[T] = new UpickleReader[T]() with Reads[T] {
    override def read0: PartialFunction[Value, T] = x.read0
  }
}
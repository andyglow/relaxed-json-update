package com.github.andyglow.relaxed

import scalaz._
import argonaut._
import Argonaut._

case class ArgonautSupport(json: Json) extends Reader {
  override def reader(field: String): Option[Reader] = json.field(field) map ArgonautSupport.apply
  override def readerOpt(field: String): Option[Option[Reader]] = {
    json.field(field) match {
      case None                 => None
      case Some(x) if x.isNull  => Some(None)
      case Some(json)           => Some(Some(ArgonautSupport(json)))
    }
  }
  override def opt[T: Reads](field: String): Option[T] = {
    implicit val decodeJson = implicitly[Reads[T]].asInstanceOf[DecodeJson[T]]

    json.field(field) flatMap (_.as[T].toOption)
  }
  override def optOpt[T: Reads](field: String): Option[Option[T]] = {
    implicit val decodeJson = implicitly[Reads[T]].asInstanceOf[DecodeJson[T]]

    json.field(field) match {
      case None                 => None
      case Some(x) if x.isNull  => Some(None)
      case Some(json)           => Some(json.as[T].toOption)
    }
  }
}

object ArgonautSupport {

  implicit def readsImpl[T](implicit x: DecodeJson[T]): Reads[T] = new DecodeJson[T]() with Reads[T] {
    override def decode(c: HCursor): DecodeResult[T] = x decode c
  }

}
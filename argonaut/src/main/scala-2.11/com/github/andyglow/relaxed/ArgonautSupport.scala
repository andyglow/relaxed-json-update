package com.github.andyglow.relaxed

import argonaut._
import Argonaut._

case class ArgonautSupport(json: Json) extends Reader with ReaderSupport[Json] {
  def isNull: (Json) => Boolean = _.isNull
  def ctor: (Json) => ArgonautSupport = ArgonautSupport.apply

  def get(field: String): Option[Json] = json.field(field)

  override def opt[T: Reads](field: String): Option[T] = {
    implicit val _ = implicitly[Reads[T]].asInstanceOf[DecodeJson[T]]
    json.field(field) flatMap (_.as[T].toOption)
  }

  override def optOpt[T: Reads](field: String): Option[Option[T]] = {
    implicit val _ = implicitly[Reads[T]].asInstanceOf[DecodeJson[T]]
    getOpt(field) map {_ flatMap {_.as[T].toOption}}
  }
}

object ArgonautSupport {
  implicit def readsImpl[T](implicit x: DecodeJson[T]): Reads[T] = new DecodeJson[T]() with Reads[T] {
    override def decode(c: HCursor): DecodeResult[T] = x decode c
  }
}
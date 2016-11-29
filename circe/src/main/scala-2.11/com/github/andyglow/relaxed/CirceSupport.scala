package com.github.andyglow.relaxed

import io.circe.Decoder.Result
import io.circe._

case class CirceSupport(json: Json) extends Reader with ReaderSupport[Json] {
  def isNull: (Json) => Boolean = _.isNull
  def ctor: (Json) => CirceSupport = CirceSupport.apply

  def get(field: String): Option[Json] = (json \\ field).headOption

  override def opt[T: Reads](field: String): Option[T] = {
    implicit val _ = implicitly[Reads[T]].asInstanceOf[Decoder[T]]
    get(field) flatMap {_.as[T].right.toOption}
  }

  override def optOpt[T: Reads](field: String): Option[Option[T]] = {
    implicit val _ = implicitly[Reads[T]].asInstanceOf[Decoder[T]]
    getOpt(field) map {_ flatMap {_.as[T].right.toOption}}
  }
}

object CirceSupport {
  implicit def readsImpl[T](implicit x: Decoder[T]): Reads[T] = new Decoder[T]() with Reads[T] {
    override def apply(c: HCursor): Result[T] = x apply c
  }
}

package com.github.andyglow.relaxed

import io.circe.Decoder.Result
import io.circe._

case class CirceSupport(json: Json) extends Reader {
  def get(field: String): Option[Json] = (json \\ field).headOption
  override def reader(field: String): Option[Reader] = get(field) map CirceSupport.apply
  override def readerOpt(field: String): Option[Option[Reader]] = get(field) match {
    case None                       => None
    case Some(json) if json.isNull  => Some(None)
    case Some(json)                 => Some(Some(CirceSupport(json)))
  }
  override def opt[T: Reads](field: String): Option[T] = {
    implicit val decorer = implicitly[Reads[T]].asInstanceOf[Decoder[T]]

    get(field) flatMap {_.as[T].right.toOption}
  }
  override def optOpt[T: Reads](field: String): Option[Option[T]] = {
    implicit val decorer = implicitly[Reads[T]].asInstanceOf[Decoder[T]]
    get(field) match {
      case None                       => None
      case Some(json) if json.isNull  => Some(None)
      case Some(json)                 => Some(json.as[T].right.toOption)
    }
  }

}

object CirceSupport {
  implicit def readsImpl[T](implicit x: Decoder[T]): Reads[T] = new Decoder[T]() with Reads[T] {
    override def apply(c: HCursor): Result[T] = x apply c
  }

}

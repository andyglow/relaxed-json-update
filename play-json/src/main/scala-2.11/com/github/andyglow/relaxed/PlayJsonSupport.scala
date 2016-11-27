package com.github.andyglow.relaxed

import play.api.libs.json.{Reads => PlayReads, _}

case class PlayJsonSupport(json: JsValue) extends Reader {
  import PlayJsonSupport._

  override def reader(field: String): Option[Reader] = (json \ field).asOpt[JsValue] map PlayJsonSupport.apply
  override def readerOpt(field: String): Option[Option[Reader]] = (json \ field).asOptOpt[JsValue] map {x => x map PlayJsonSupport.apply}
  override def opt[T: Reads](field: String): Option[T] = {
    implicit val z = implicitly[Reads[T]].asInstanceOf[PlayReads[T]]
    (json \ field).asOpt[T]
  }
  override def optOpt[T: Reads](field: String): Option[Option[T]] = {
    implicit val z = implicitly[Reads[T]].asInstanceOf[PlayReads[T]]
    (json \ field).asOptOpt[T]
  }
}

object PlayJsonSupport {

  implicit def readsImpl[T](implicit x: PlayReads[T]): Reads[T] = new PlayReads[T]() with Reads[T] {
    override def reads(json: JsValue): JsResult[T] = x.reads(json)
  }

  implicit class JsLookupResultOps(val x: JsLookupResult) extends AnyVal {
    def asOptOpt[A](implicit rds: PlayReads[A]): Option[Option[A]] = x match {
      case JsUndefined()      => None
      case JsDefined(JsNull)  => Some(None)
      case JsDefined(a)       => rds.reads(a) map {aa => Some(Some(aa))} getOrElse {throw new IllegalStateException}
    }
  }

}
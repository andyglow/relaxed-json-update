package com.github.andyglow.relaxed

import org.json4s._

case class Json4SSupport(json: JValue) extends Reader {
  def get(field: String): Option[JValue] = json.findField(_._1 == field) map (_._2)
  override def reader(field: String): Option[Reader] = get(field) map Json4SSupport.apply
  override def readerOpt(field: String): Option[Option[Reader]] = get(field) match {
    case None         => None
    case Some(JNull)  => Some(None)
    case Some(json)   => Some(Some(Json4SSupport(json)))
  }
  override def opt[T: Reads](field: String): Option[T] = {
    val r = implicitly[Reads[T]].asInstanceOf[Json4SSupport.Json4SReads[T]]
    implicit val f = r.formats
    implicit val m = r.manifest

    get(field) flatMap (_.extractOpt[T])
  }
  override def optOpt[T: Reads](field: String): Option[Option[T]] = {
    val r = implicitly[Reads[T]].asInstanceOf[Json4SSupport.Json4SReads[T]]
    implicit val f = r.formats
    implicit val m = r.manifest

    get(field) match {
      case None         => None
      case Some(JNull)  => Some(None)
      case Some(json)   => Some(Some(json.extract[T]))
    }
  }
}

object Json4SSupport {

  trait Json4SReads[T] extends Reads[T] {
    def formats: Formats
    def manifest: Manifest[T]
  }

  implicit def readsImpl[T](implicit f: Formats, m: Manifest[T]): Reads[T] = new Json4SReads[T]() {
    override def formats: Formats = f
    override def manifest: Manifest[T] = m
  }

}
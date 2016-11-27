package com.github.andyglow.relaxed

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind._

case class JacksonSupport(json: JsonNode) extends Reader {
  def get(field: String): Option[JsonNode] = {
    val node = json.findPath(field)
    if (node.isMissingNode) None else Some(node)
  }

  override def reader(field: String): Option[Reader] = get(field) map JacksonSupport.apply
  override def readerOpt(field: String): Option[Option[Reader]] = get(field) match {
    case None                       => None
    case Some(json) if json.isNull  => Some(None)
    case Some(json)                 => Some(Some(JacksonSupport(json)))
  }
  override def opt[T: Reads](field: String): Option[T] = {
    val r = implicitly[Reads[T]].asInstanceOf[JacksonSupport.JacksonReads[T]]
    get(field) map {x => r.mapper.readerFor(r.typeReference).readValue[T](x)}
  }
  override def optOpt[T: Reads](field: String): Option[Option[T]] = {
    val r = implicitly[Reads[T]].asInstanceOf[JacksonSupport.JacksonReads[T]]
    get(field) match {
      case None                       => None
      case Some(json) if json.isNull  => Some(None)
      case Some(json)                 => Some(Some(r.mapper.readerFor(r.typeReference).readValue[T](json)))
    }
  }

}

object JacksonSupport {
  import java.lang.reflect.{Type, ParameterizedType}

  sealed trait JacksonReads[T] extends Reads[T] {
    def mapper: ObjectMapper
    def typeReference: TypeReference[T]
  }

  implicit def readsImpl[T](implicit x: ObjectMapper, m: Manifest[T]): Reads[T] = new JacksonReads[T]() {
    override def mapper: ObjectMapper = x
    override lazy val typeReference: TypeReference[T] = {
      def typeFromManifest(m: Manifest[_]): Type = {
        if (m.typeArguments.isEmpty) m.runtimeClass
        else new ParameterizedType {
          def getRawType: Class[_] = m.runtimeClass
          def getActualTypeArguments: Array[Type] = m.typeArguments.map(typeFromManifest).toArray
          def getOwnerType = null
        }
      }
      new TypeReference[T] {
        override def getType: Type = typeFromManifest(manifest[T])
      }
    }
  }

}
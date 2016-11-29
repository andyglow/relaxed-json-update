package com.github.andyglow.relaxed

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait Reads[T]

trait Reader {
  def reader(field: String): Option[Reader]
  def readerOpt(field: String): Option[Option[Reader]]
  def opt[T: Reads](field: String): Option[T]
  def optOpt[T: Reads](field: String): Option[Option[T]]
}

trait ReaderSupport[T] extends Reader {
  def isNull: T => Boolean
  def ctor: T => Reader
  def get(field: String): Option[T]
  def getOpt(field: String): Option[Option[T]] = get(field) match {
    case None                 => None
    case Some(x) if isNull(x) => Some(None)
    case Some(x)              => Some(Some(x))
  }
  override def reader(field: String): Option[Reader] = get(field) map ctor
  override def readerOpt(field: String): Option[Option[Reader]] = getOpt(field) map {_ map ctor}
}

trait Updater[T] {
  def apply(entity: T, u: Reader): T
}

case class Relaxed[T](entity: T) {
  def updated[U](u: Reader)(implicit ru: Updater[T]): T = ru.apply(entity, u)
}

object Relaxed {
  def updater[T]: Updater[T] = macro updaterImpl[T]

  def updaterImpl[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[Updater[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val optionTpe = weakTypeOf[Option[_]]

    val fields = Util.fieldMap(c)(tpe) map { case (n, t) =>
      val path          = n.decodedName.toString
      val isOption      = t <:< optionTpe
      val resultingType = if (isOption) t.typeArgs.head else t
      val isCaseClass   = resultingType.typeSymbol.isClass && resultingType.typeSymbol.asClass.isCaseClass

      val expr = (isOption, isCaseClass) match {
        case (false, false) => q"val $n: $t = u.opt[$resultingType]($path) getOrElse entity.$n"
        case (true, false)  => q"val $n: $t = u.optOpt[$resultingType]($path) getOrElse entity.$n"
        case (false, true)  => q"val $n: $t = u.reader($path) map {x => implicitly[Updater[$resultingType]].apply(entity.$n, x)} getOrElse entity.$n"
        case (true, true)   => q"""
                                 val $n: $t = {
                                   val r: Option[Option[Reader]] = u.readerOpt($path)
                                   (r, entity.$n) match {
                                     case (None, None)              => None
                                     case (Some(None), None)        => None
                                     case (Some(Some(r)), None)     => u.opt[$resultingType]($path)
                                     case (None, Some(a))           => Some(a)
                                     case (Some(None), Some(a))     => None
                                     case (Some(Some(r)), Some(a))  => Some(implicitly[Updater[$resultingType]].apply(a, r))
                                   }
                                 }
                                """
      }

      (n, expr)
    }

    val definitions = fields map (_._2)
    val expressions = fields map { case (n, _) => q"$n = $n"}
    val copy = q"entity.copy( ..$expressions )"

    val out =
      q"""
        new Updater[$tpe] {
          def apply(entity: $tpe, u: Reader): $tpe = {
            ..$definitions
            $copy
          }
        }
       """

    c.Expr[Updater[T]](out)
  }

}
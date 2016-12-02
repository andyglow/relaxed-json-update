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
  import scala.reflect.macros.Universe

  def updater[T]: Updater[T] = macro updaterImpl[T]

  sealed trait ResultingType {def tpe: Universe#Type}
  object ResultingType {

    case class ValueClass(tpe: Universe#Type, innerType: Universe#Type) extends ResultingType
    case class CaseClass(tpe: Universe#Type) extends ResultingType
    case class Generic(tpe: Universe#Type) extends ResultingType

    def apply(t: Universe#Type): ResultingType = {
      val symbol = t.typeSymbol
      require(symbol.isClass)

      symbol.asClass match {
        case x if x.isCaseClass && !x.isDerivedValueClass => CaseClass(t)
        case x if x.isCaseClass && x.isDerivedValueClass =>
          val innerArg = x.primaryConstructor.asMethod.paramLists.head.head
          val innerType = innerArg.typeSignature
          ValueClass(t, innerType)
        case _ => Generic(t)
      }
    }
  }

  def updaterImpl[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[Updater[T]] = {
    import c.universe._
    import ResultingType._

    val tpe = weakTypeOf[T]
    val optionTpe = weakTypeOf[Option[_]]

    val fields = Util.fieldMap(c)(tpe) map { case (n, t) =>
      val path          = n.decodedName.toString
      val isOption      = t <:< optionTpe
      val resultingType = if (isOption) ResultingType(t.typeArgs.head) else ResultingType(t)

      val expr = (isOption, resultingType) match {
        case (false, Generic(ft: Type))             => q"val $n: $t = u.opt[$ft]($path) getOrElse entity.$n"
        case (true, Generic(ft: Type))              => q"val $n: $t = u.optOpt[$ft]($path) getOrElse entity.$n"
        case (false, ValueClass(ft: Type, i: Type)) => q"val $n: $t = u.opt[$i]($path) map ${ft.typeSymbol.companion}.apply getOrElse entity.$n"
        case (true, ValueClass(ft: Type, i: Type))  => q"val $n: $t = u.optOpt[$i]($path) map {_ map ${ft.typeSymbol.companion}.apply} getOrElse entity.$n"
        case (false, CaseClass(ft: Type))           => q"val $n: $t = u.reader($path) map {x => implicitly[Updater[$ft]].apply(entity.$n, x)} getOrElse entity.$n"
        case (true, CaseClass(ft: Type))            => q"""
                                 val $n: $t = {
                                   val r: Option[Option[Reader]] = u.readerOpt($path)
                                   (r, entity.$n) match {
                                     case (None, None)              => None
                                     case (Some(None), None)        => None
                                     case (Some(Some(r)), None)     => u.opt[$ft]($path)
                                     case (None, Some(a))           => Some(a)
                                     case (Some(None), Some(a))     => None
                                     case (Some(Some(r)), Some(a))  => Some(implicitly[Updater[$ft]].apply(a, r))
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
        import com.github.andyglow.relaxed._

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
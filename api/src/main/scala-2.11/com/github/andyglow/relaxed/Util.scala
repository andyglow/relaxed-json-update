package com.github.andyglow.relaxed

import scala.reflect.macros.blackbox

private[relaxed] object Util {

  def fieldMap(c: blackbox.Context)(tpe: c.universe.Type): Seq[(c.universe.TermName, c.universe.Type)] = {
    import c.universe._

    object UpdatableField {
      def unapply(s: TermSymbol): Option[(TermName, Type)] = {
        val name = s.name.toString.trim
        if ( s.isVal
          && s.isCaseAccessor
          && !s.annotations.contains(skip)) Some((TermName(name), s.typeSignature))
        else None
      }
    }

    tpe.decls.collect {
      case UpdatableField(nme, tpe) => (nme, tpe)
    } toSeq
  }


}

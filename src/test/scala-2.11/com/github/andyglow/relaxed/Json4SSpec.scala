package com.github.andyglow.relaxed

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization

import scala.language.implicitConversions

class Json4SSpec extends AbstractRelaxedSpec("Json4S") {
  import Json4SSupport._

  override implicit def stringAsReader(json: String): Reader = Json4SSupport(parse(json))

  class PhoneSerializer extends CustomSerializer[Phone](format => (
    {
      case JObject(JField("area", JString(area)) :: JField("number", JString(number)) :: Nil) =>
        Phone(area, number)
    },
    {
      case x: Phone =>
        JObject(
          JField("area",    JString(x.area)) ::
          JField("number",  JString(x.number)) :: Nil)
    }
  ))

  implicit val formats = Serialization.formats(NoTypeHints) + new PhoneSerializer

  implicit val phoneUpdater: Updater[Phone] = Relaxed.updater[Phone]
  implicit val addressUpdater: Updater[Address] = Relaxed.updater[Address]
  override implicit val profileUpdater: Updater[Profile] = Relaxed.updater[Profile]

}

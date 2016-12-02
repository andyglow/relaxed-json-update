package com.github.andyglow.relaxed

case class ProfileId(value: String) extends AnyVal
case class FullName(value: String) extends AnyVal
case class Phone(area: String, number: String)
case class Address(city: String, street: String, building: Int)
case class Profile(@skip id: ProfileId, name: FullName, address: Address, alias: Option[String], phone: Option[Phone])
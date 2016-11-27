package com.github.andyglow.relaxed

case class Phone(area: String, number: String)
case class Address(city: String, street: String, building: Int)
case class Profile(id: String, name: String, address: Address, alias: Option[String], phone: Option[Phone])
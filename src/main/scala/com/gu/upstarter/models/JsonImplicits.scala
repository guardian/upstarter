package com.gu.upstarter.models

import play.api.libs.json.Json

object JsonImplicits {
  implicit val environmentVariableFormat = Json.format[EnvironmentVariable]
  implicit val applicationFormat = Json.format[ApplicationDefinition]
  implicit val distributionFormat = Json.format[Distribution]
  implicit val configFormat = Json.format[Config]
}

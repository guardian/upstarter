import AssemblyKeys._

import Dependencies._

assemblySettings

scalaVersion := "2.10.4"

organization := "com.gu"

name := "upstarter"

libraryDependencies ++= Seq(
  playJson
)

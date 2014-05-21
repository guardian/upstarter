package com.gu.upstarter.models

case class EnvironmentVariable(
  name: String,
  value: String,
  stage: Option[String]
)

case class ApplicationDefinition(
  /**
   * Application name
   *
   * Upstarter expects that you follow the following convention for your app
   *
   * - The user that runs the app has the name of the app
   * - The jar file of the app has the same name of the app (e.g., if the app were png-resizer, png-resizer.jar)
   * - The root directory of the app is the name of the app from the root (e.g., /png-resizer)
   */
  name: String,

  /**
   * List of environment variables to set when running the script. These can optionally be parameterized by stage.
   */
  environmentVariables: Option[List[EnvironmentVariable]],

  /**
   * List of JVM properties to pass to Java when running the application.
   */
  jvmProperties: Option[List[String]],

  /**
   * List of command line options to pass to the application.
   */
  appOptions: Option[List[String]],

  /**
   * Amount of total memory on the box to allocation to the JVM process. e.g., if this were 0.75, the JVM process would
   * be allocated 75% of total memory for the heap.
   */
  memoryAllocation: Option[Double]
) {
  require(!memoryAllocation.exists(_ <= 0), "Memory allocation for JVM must be more than 0% of total memory")
  require(!memoryAllocation.exists(_ >= 100), "Memory allocation for JVM cannot be 100% or greater of total memory")
}

case class Distribution(
  /** S3 bucket name */
  bucket: String,
  /** List of stages for which to generate upstart configs */
  stages: Set[String],
  /** List of applications for which to generate upstart configs */
  applications: List[ApplicationDefinition]
)

case class Config(distributions: List[Distribution])

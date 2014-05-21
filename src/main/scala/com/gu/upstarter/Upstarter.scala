package com.gu.upstarter

import scala.io.Source
import play.api.libs.json.{JsError, JsSuccess, Json}
import com.gu.upstarter.models.Config
import com.gu.upstarter.models.JsonImplicits._
import java.io.{PrintWriter, File}
import com.gu.upstarter.generator.Generator

object Upstarter extends App {
  val ConfigReadError = 2
  val BadBuildPath = 3
  val WritePermissionsError = 4

  def usage() {
    System.err.println("upstarter CONFIG_PATH BUILD_PATH")
    System.exit(1)
  }

  override def main(args: Array[String]) {
    args.toList match {
      case configPath :: buildPath :: Nil =>
        Json.fromJson[Config](Json.parse(Source.fromFile(configPath).getLines().mkString)) match {
          case JsSuccess(config, _) =>
            val buildDir = new File(buildPath)
            makeDirectoryOrDie(buildDir)
            upstarter(config, buildDir)

          case JsError(error) =>
            System.err.println("Error reading config file")
            System.err.println(error)
            System.exit(ConfigReadError)
        }

      case _ => usage()
    }
  }

  private def makeDirectoryOrDie(directory: File) {
    if (directory.exists && !directory.isDirectory) {
      System.err.println(s"${directory.getPath} exists and is not a directory")
      System.exit(WritePermissionsError)
    }

    if (directory.exists && !directory.canWrite) {
      System.err.println(s"Do not have permissions to write to ${directory.getPath}")
      System.exit(WritePermissionsError)
    }

    if (!directory.exists && !directory.mkdir()) {
      System.err.println(s"Could not create directory ${directory.getPath}")
      System.exit(WritePermissionsError)
    }
  }

  def upstarter(config: Config, buildDir: File) = {
    config.distributions foreach { distribution =>
      val bucketDir = new File(buildDir, distribution.bucket)
      makeDirectoryOrDie(bucketDir)

      distribution.stages foreach { stage =>
        val stageDir = new File(bucketDir, stage)
        makeDirectoryOrDie(stageDir)

        distribution.applications map { application =>
          val applicationDir = new File(stageDir, application.name)
          makeDirectoryOrDie(applicationDir)

          val upstartScript = Generator.generate(application, stage)

          val upstartFile = new File(applicationDir, application.name + ".conf")

          val writer = new PrintWriter(upstartFile)

          writer.write(upstartScript)
          writer.flush()
          writer.close()
        }
      }
    }
  }
}

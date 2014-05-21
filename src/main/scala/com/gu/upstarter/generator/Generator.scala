package com.gu.upstarter.generator

import com.gu.upstarter.models.{EnvironmentVariable, ApplicationDefinition}

object Generator {
  val garbageCollectionJvmProperties = List(
    "-XX:+PrintGCDetails",
    "-XX:+PrintGCDateStamps",
    "-Xloggc:gc.log"
  )

  val DefaultMemoryAllocation = 0.75

  def generate(application: ApplicationDefinition, stage: String): String = {
    val appName = application.name

    val environmentVariables = application.environmentVariables.getOrElse(Nil).filterNot(_.stage.exists(_ != stage))

    val environmentVariablesTemplate = (environmentVariables map { case EnvironmentVariable(name, value, _) =>
      s"env $name=$value\n"
    }).mkString

    val appOptions = application.appOptions.getOrElse(Nil).mkString(" ")
    val jvmOptions = (garbageCollectionJvmProperties ++ application.jvmProperties.getOrElse(Nil)).mkString(" ")

    val memoryAllocation = application.memoryAllocation.getOrElse(DefaultMemoryAllocation)

    s"""
      |env USER=$appName
      |env USER_HOME=/$appName
      |env JAR=/$appName/$appName.jar
      |
      |env LOGFILE=/$appName/stdout.log
      |
      |$environmentVariablesTemplate
      |
      |start on runlevel [2345]
      |stop on runlevel [016]
      |
      |setuid $appName
      |
      |chdir /$appName
      |
      |script
      |  IF_64_BIT_OPTION=""
      |  if $$(java -version 2>&1 | grep -q 64-Bit);
      |  then
      |  	IF_64_BIT_OPTIONS="-XX:+UseCompressedOops"
      |  fi
      |
      |  # Incremental mode if we are on 1 or 2 CPUs http://www.oracle.com/technetwork/java/javase/gc-tuning-6-140523.html#icms
      |  INCREMENTAL_MODE=""
      |
      |  if [ $$(nproc) -lt 3 ]
      |  then
      |    INCREMENTAL_MODE="-XX:+CMSIncrementalMode";
      |  fi
      |
      |  TOTAL_MEMORY=$$(grep MemTotal /proc/meminfo | awk '{ print $$2 }')
      |
      |  HEAP_SIZE_IN_MB=$$(perl -e "print int($$TOTAL_MEMORY * $memoryAllocation / 1024 / 1024)")
      |
      |  HEAP_SIZE_CMD="-Xmx$${HEAP_SIZE_IN_MB}m"
      |
      |  COMMAND="java $$HEAP_SIZE_CMD $$IF_64_BIT_OPTIONS $$INCREMENTAL_MODE $jvmOptions -jar /$$JAR $appOptions"
      |  echo "$$COMMAND" >/$appName/cmd.txt
      |  $$COMMAND >$$LOGFILE 2>&1
      |end script
    """.stripMargin
  }
}

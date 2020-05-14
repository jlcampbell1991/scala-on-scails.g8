val circeV = "$circe_version$" // "0.11.1"
val http4sV = "$http4s_version$" // "0.21.1"
val doobieV = "$doobie_version$" // "0.8.4"
val scalaBcryptV = "$scala_bcrypt_version$" // "4.1"
val specs2V = "$specs2_version$" // "3.8.9"

lazy val `$app_name$` =
  project
    .in(file("$app_name$"))
    .settings(
      name := "$app_name$",
      bannoDockerBaseImage := "docker.artifactory.banno-tools.com/java:11",
      libraryDependencies ++= Seq(
        "io.circe"              %% "circe-generic"            % circeV,
        "io.circe"              %% "circe-parser"             % circeV,
        "org.tpolecat"          %% "doobie-core"              % doobieV,
        "org.tpolecat"          %% "doobie-postgres"          % doobieV,
        "net.logstash.logback"  %  "logstash-logback-encoder" % "5.3",
        "ch.qos.logback"        %  "logback-classic"          % "1.2.3",
        "org.http4s"            %% "http4s-blaze-client"      % http4sV,
        "org.http4s"            %% "http4s-blaze-server"      % http4sV,
        "org.http4s"            %% "http4s-circe"             % http4sV,
        "org.http4s"            %% "http4s-dsl"               % http4sV,
        "org.http4s"            %% "http4s-twirl"             % http4sV,
        "com.github.t3hnar"     %% "scala-bcrypt"             % scalaBcryptV,
        "org.reactormonk"       %% "cryptobits"               % "1.2",
        "org.scalacheck"        %% "scalacheck"               % "1.14.3" % Test,
        "org.scalatest"         %% "scalatest"                % "3.0.8" % Test
      ),
      Compile / console / scalacOptions ~= ((options: Seq[String]) =>
        options.filterNot(s => s.startsWith("-Ywarn") || s.startsWith("-Xlint")))
    )

lazy val root = project
  .in(file("."))
  .settings(
    name := "$app_name$-root",
    inThisBuild(
      Seq(
        scalaVersion := "$scala_version$",
        scalafmtOnCompile := true,
        addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
      )),
    bannoReleaseGitPushOnlyTag := true
  )
  .aggregate(
    `$app_name$`
  )

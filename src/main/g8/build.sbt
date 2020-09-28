val circeV = "$circe_version$"
val http4sV = "$http4s_version$"
val doobieV = "$doobie_version$"
val scalaBcryptV = "$scala_bcrypt_version$"
val specs2V = "$specs2_version$"

lazy val `$app_name$` =
  project
    .in(file("$app_name$"))
    .enablePlugins(SbtTwirl)
    .enablePlugins(JavaAppPackaging)
    .settings(
      name := "$app_name$",
      libraryDependencies ++= Seq(
        "io.circe"              %% "circe-generic"            % circeV,
        "io.circe"              %% "circe-parser"             % circeV,
        "org.tpolecat"          %% "doobie-core"              % doobieV,
        "org.tpolecat"          %% "doobie-postgres"          % doobieV,
        "net.logstash.logback"  %  "logstash-logback-encoder" % "6.3",
        "ch.qos.logback"        %  "logback-classic"          % "1.2.3",
        "org.http4s"            %% "http4s-blaze-client"      % http4sV,
        "org.http4s"            %% "http4s-blaze-server"      % http4sV,
        "org.http4s"            %% "http4s-circe"             % http4sV,
        "org.http4s"            %% "http4s-dsl"               % http4sV,
        "org.http4s"            %% "http4s-twirl"             % http4sV,
        "com.github.t3hnar"     %% "scala-bcrypt"             % scalaBcryptV,
        "org.reactormonk"       %% "cryptobits"               % "1.3",
        "com.typesafe"           % "config"                   % "1.4.0",
        "org.scalacheck"        %% "scalacheck"               % "1.14.3"  % Test,
        "org.scalactic"         %% "scalactic"                % "3.1.2",
        "org.scalatest"         %% "scalatest"                % "3.1.2"   % Test,
        "org.scalatestplus"     %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % Test
      ),
      Compile / console / scalacOptions ~= ((options: Seq[String]) =>
        options.filterNot(s => s.startsWith("-Ywarn") || s.startsWith("-Xlint")))
    )

lazy val `scaffold-scripts` =
  project
    .in(file("scaffold-scripts"))
    .settings(
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
  )
  .aggregate(
    `$app_name$`
  )

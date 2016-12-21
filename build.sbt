name := """todo-backend"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.atnos" %% "eff" % "2.2.0",
  "org.typelevel" %% "cats" % "0.8.1"
)

// to write types like Reader[String, ?]
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

// to get types like Reader[String, ?] (with more than one type parameter) correctly inferred for scala 2.11.8
addCompilerPlugin("com.milessabin" % "si2712fix-plugin_2.11.8" % "1.2.0")

// to get types like Reader[String, ?] (with more than one type parameter) correctly inferred for scala 2.12.x
//scalacOptions += "-Ypartial-unification"
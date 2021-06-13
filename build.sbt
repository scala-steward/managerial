inThisBuild(
  List(
    organization := "ca.dvgi",
    homepage := Some(url("https://github.com/dvgica/managerial")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "dvgica",
        "David van Geest",
        "david.vangeest@gmail.com",
        url("http://dvgi.ca")
      )
    )
  )
)

val scala212Version = "2.12.14"
val scala213Version = "2.13.6"
val scala3Version = "3.0.0"
val scalaVersions =
  Seq(
    scala213Version,
    scala212Version
  ) // TODO add Scala 3 back when Twitter Util has a Scala 3 release

def subproject(name: String) = Project(
  id = name,
  base = file(name)
).settings(
  scalaVersion := scala213Version,
  libraryDependencies += "org.scalameta" %% "munit" % "0.7.26" % Test,
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
)

lazy val managerial =
  subproject("managerial")
    .settings(
      crossScalaVersions := scalaVersions
    )

lazy val managerialTwitterUtil =
  subproject("managerial-twitter-util")
    .dependsOn(managerial)
    .settings(
      crossScalaVersions := Seq(scala213Version, scala212Version),
      libraryDependencies += "com.twitter" %% "util-core" % "21.5.0" % Provided
    )

lazy val root = project
  .in(file("."))
  .aggregate(
    managerial,
    managerialTwitterUtil
  )
  .settings(
    publish / skip := true
  )

ThisBuild / crossScalaVersions := scalaVersions
ThisBuild / githubWorkflowBuildPreamble := Seq(
  WorkflowStep.Sbt(
    List("scalafmtCheckAll", "scalafmtSbtCheck"),
    name = Some("Check formatting with scalafmt")
  )
)
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

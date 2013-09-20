name := "scala-netty-examples"

organization := "org.edla"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-optimize")

scalacOptions in (Compile, doc) ++= Seq("-diagrams","-implicits")

libraryDependencies ++= Seq(
  "io.netty" % "netty" % "3.7.0.Final"
)

// Uncomment the following line to use one-jar (https://github.com/sbt/sbt-onejar)
//seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

licenses := Seq("GNU GPL v3" -> url("http://www.gnu.org/licenses/gpl.html"))

homepage := Some(url("http://github.com/newca12/scala-netty-examples"))

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <!-- repositories not handled yet by sbt make-pom so added manually 
       pluginRepository needed for add-source goal
  -->
  <scm>
    <url>git@github.com:newca12/scala-netty-examples.git</url>
    <connection>scm:git:git@github.com:newca12/scala-netty-examples.git</connection>
  </scm>
  <developers>
    <developer>
      <id>newca12</id>
      <name>Olivier ROLAND</name>
      <url>http://www.edla.org</url>
    </developer>
  </developers>
  <contributors>
  </contributors>
  	<properties>
		<encoding>UTF-8</encoding>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	</properties>
  	<build>
  		<!-- source and test directories not handled yet by sbt make-pom so added manually -->
  		<sourceDirectory>src/main/scala</sourceDirectory>
  		<testSourceDirectory>src/test/scala</testSourceDirectory>
    	<plugins>
			<plugin>
				<groupId>net.alchim31.maven</groupId>
				<artifactId>scala-maven-plugin</artifactId>
				<version>3.1.5</version>
				<executions>
					<execution>
						<goals>
							<goal>add-source</goal>
							<goal>compile</goal>
							<goal>testCompile</goal>
						</goals>
					</execution>
				</executions>
			</plugin>		
		</plugins>
	</build>	
	<reporting>
		<plugins>
			<plugin>
				<groupId>net.alchim31.maven</groupId>
				<artifactId>scala-maven-plugin</artifactId>
				<version>3.1.5</version>
			</plugin>
		</plugins>
	</reporting>
)

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}
plugins {
  kotlin("jvm") version "1.7.22"
  id("com.github.johnrengelman.shadow") version "7.1.2"
}
dependencies {
  implementation("com.google.guava:guava:31.1-jre")
  implementation("org.jgrapht:jgrapht-core:1.5.1")
  implementation("org.jgrapht:jgrapht-guava:1.5.1")
  implementation("org.jgrapht:jgrapht:1.5.1")
}
tasks.jar {
  isZip64 = true
  manifest.attributes("Main-Class" to "com.adventofcode.AppKt")
}
tasks.shadowJar {
  minimize() // if build is unsuccessful, you can disable it
  // also, if build still unsuccessful, you can try to add mergeServiceFiles() call
}
tasks.build {
  dependsOn(tasks.shadowJar)
}
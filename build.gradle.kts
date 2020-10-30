import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.20"
  id("io.vertx.vertx-plugin") version "1.0.1"
}

group = "com.cupshe"
version = "0.0.1-SNAPSHOT"

repositories {
  maven("http://maven.aliyun.com/nexus/content/groups/public/")
  maven("https://oss.sonatype.org/content/repositories/snapshots")
  mavenCentral()
}

val kotlinVersion by extra { "1.3.20" }
val logbackVersion by extra { "1.2.3" }
val lombokVersion by extra { "1.18.10" }

dependencies {
  implementation("io.vertx:vertx-web-client")
  implementation("io.vertx:vertx-unit")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-config")
  implementation("io.vertx:vertx-config-hocon")
  implementation("ch.qos.logback:logback-core:$logbackVersion")
  implementation("ch.qos.logback:logback-classic:$logbackVersion")
  // compile only
  compileOnly("org.projectlombok:lombok:$lombokVersion")
  annotationProcessor("org.projectlombok:lombok:$lombokVersion")
  // test
  testImplementation("io.vertx:vertx-junit5")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
  jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
  jvmTarget = "1.8"
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = mutableSetOf(PASSED, FAILED, SKIPPED)
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "1.8"
  }
}

vertx {
  mainVerticle = "vertx.launcher.StartupVerticle"
  vertxVersion = "3.9.4"
}

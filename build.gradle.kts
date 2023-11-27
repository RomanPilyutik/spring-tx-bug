import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.3"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.0"
	kotlin("plugin.spring") version "1.6.0"
	id("com.avast.gradle.docker-compose") version "0.14.13" apply true
}

group = "com.rollback.r2dbc"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("dev.miku", "r2dbc-mysql")
	implementation("org.springframework", "spring-jdbc")
	implementation("org.liquibase", "liquibase-core")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	implementation("mysql", "mysql-connector-java")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = freeCompilerArgs + listOf("-Xjsr305=strict", "-Xinline-classes")
		jvmTarget = "1.8"
		sourceCompatibility = "1.8"
	}
}

dockerCompose {
	useComposeFiles.add("${project.rootDir.absolutePath}/docker/docker-compose.yml")
}

tasks.withType<Test> {
	useJUnitPlatform {
		dockerCompose.isRequiredBy(this@withType)
	}
}
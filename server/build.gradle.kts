plugins {
	java
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

sourceSets {
	create("integrationTest") {
		java.srcDir("src/it/java")
		resources.srcDir("src/it/resources")
		compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
		runtimeClasspath += output + compileClasspath
	}
}

val integrationTestImplementation: Configuration by configurations.getting {
	extendsFrom(configurations.implementation.get())
}

extra["springGrpcVersion"] = "0.8.0"

dependencies {
	implementation(project(":proto"))
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-hateoas")
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("org.apache.commons:commons-collections4:4.1")
	implementation("org.springframework.boot:spring-boot-starter-amqp")

	implementation("io.grpc:grpc-services")
	implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")

	compileOnly("org.projectlombok:lombok:1.18.38")
	annotationProcessor("org.projectlombok:lombok:1.18.38")
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.grpc:spring-grpc-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
	integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
	integrationTestImplementation("org.springframework.boot:spring-boot-testcontainers")
	integrationTestImplementation("org.testcontainers:junit-jupiter:1.21.3")
	integrationTestImplementation("org.testcontainers:elasticsearch:1.21.3")
	integrationTestImplementation("org.testcontainers:rabbitmq:1.21.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.grpc:spring-grpc-dependencies:${property("springGrpcVersion")}")
	}
}


tasks.withType<Test> {
	useJUnitPlatform()
}

val integrationTest = tasks.register<Test>("integrationTest") {
	description = "Runs integration tests."
	group = "verification"
	testClassesDirs = sourceSets["integrationTest"].output.classesDirs
	classpath = sourceSets["integrationTest"].runtimeClasspath
	shouldRunAfter(tasks.test)
	useJUnitPlatform()

	// parallel execution has port binding issues
	maxParallelForks = 1
    forkEvery = 1

}

tasks.check {
	dependsOn(integrationTest)
}

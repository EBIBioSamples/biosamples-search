plugins {
    java
    id("com.google.protobuf") version "0.9.5"
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("io.grpc:grpc-protobuf:1.73.0")
    implementation("io.grpc:grpc-stub:1.73.0")
    implementation("com.google.protobuf:protobuf-java:3.25.5")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.73.0"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc") {
                    option("jakarta_omit")
                    option("@generated=omit")
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitLab"
            url = uri("https://gitlab.ebi.ac.uk/api/v4/projects/5516/packages/maven")

            credentials(HttpHeaderCredentials::class) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            }

            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val protoVersion = "3.19.1"

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.7.0")
    id("idea")
    id("java-library")
    id("java-test-fixtures")
    id("com.google.protobuf").version("0.9.1")
}

repositories {
    jcenter()
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    // Compile for Java 8 (default is Java 6)
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(project(":tree-ware-kotlin-core"))
    implementation("com.google.protobuf:protobuf-kotlin:$protoVersion")
    implementation(kotlin("stdlib"))
    implementation("org.ainslec:picocog:1.0.7")

    testImplementation(project(":tree-ware-kotlin-core:test-fixtures"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.1"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.generateDescriptorSet = true
            task.descriptorSetOptions.includeImports = true
            task.builtins {
                // TODO(mohit): figure out Kotlin syntax for the following Groovy syntax.
//                remove(java)
            }
        }
    }
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.tree-ware.tree-ware-kotlin-proto3"
version = "0.1.0.0"

val protoVersion = "3.19.1"
val treeWareCoreVersion = "0.1.0.0"
val treeWareCoreTestFixturesVersion = "0.1.0.0"

plugins {
    kotlin("jvm") version "1.7.0"
    id("idea")
    id("java-library")
    id("java-test-fixtures")
    id("com.google.protobuf").version("0.9.1")
    id("maven-publish")
}

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

tasks.withType<KotlinCompile> {
    // Compile for Java 8 (default is Java 6)
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation("org.tree-ware.tree-ware-kotlin-core:core:$treeWareCoreVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protoVersion")
    implementation(kotlin("stdlib"))
    implementation("org.ainslec:picocog:1.0.7")

    testImplementation("org.tree-ware.tree-ware-kotlin-core:test-fixtures:$treeWareCoreTestFixturesVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform {
        when (System.getProperty("integrationTests", "")) {
            "include" -> includeTags("integrationTest")
            "exclude" -> excludeTags("integrationTest")
            else -> {}
        }
    }
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
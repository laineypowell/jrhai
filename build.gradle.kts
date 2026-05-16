plugins {
    id("java")
    `maven-publish`
}

group = "com.laineypowell"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

val cargo = tasks.register<Exec>("buildCargo") {
    workingDir("./jrhai")

    commandLine("cargo", "build", "--release")
}

tasks.processResources {
    dependsOn(cargo)

    from("./jrhai/target/release") {
        include("*.dll")
    }

}

publishing {
    publications {
        register<MavenPublication>("maven") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("https://maven.laineypowell.com/public")
            credentials {
            }
            authentication {
                create("basic", BasicAuthentication::class.java)
            }
        }
    }
}

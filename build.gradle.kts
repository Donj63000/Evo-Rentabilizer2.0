plugins {
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("info.picocli:picocli:4.7.6")
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

application {
    mainClass.set("com.dofus.rentabilizer.Main")
}

tasks.test {
    useJUnitPlatform()
}

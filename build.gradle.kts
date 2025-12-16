plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
    kotlin("plugin.serialization") version "1.9.25"
}

group = "com.arslan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
//    version.set("2023.1")
//    type.set("IC") // Target IDE Platform
    version.set("2024.1.7")
    type.set("IU") // Target IDE Platform

    plugins.set(listOf("Git4Idea", "com.intellij.java"))

}


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}


tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    // to know version need to check the first 3 digits of number of build in Help -> About
    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("243.*")
    }

    signPlugin {
//        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
//        privateKey.set(System.getenv("PRIVATE_KEY"))
//        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
        certificateChainFile.set(file("chain.crt"))
        privateKeyFile.set(file("private.pem"))
        password.set("arslan")
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }


//  ssh-keygen -p -N "" -m pem -f  "C:\Users\ovezb\.ssh\id_rsa"
//    [System.Environment]::SetEnvironmentVariable("CERTIFICATE_CHAIN", (Get-Content "C:\Users\ovezb\.ssh\id_rsa.pub" -Raw), [System.EnvironmentVariableTarget]::User)
//    [System.Environment]::SetEnvironmentVariable("PRIVATE_KEY", (Get-Content "C:\Users\ovezb\.ssh\id_rsa" -Raw), [System.EnvironmentVariableTarget]::User)
//    [System.Environment]::SetEnvironmentVariable("PRIVATE_KEY_PASSWORD", "", [System.EnvironmentVariableTarget]::User)

    /*
    Password = arslan
    openssl genpkey -aes-256-cbc -algorithm RSA -out private_encrypted.pem -pkeyopt rsa_keygen_bits:4096
    openssl rsa -in private_encrypted.pem -out private.pem
    openssl req -key private.pem -new -x509 -days 3650 -out chain.crt

     */
}


tasks.register<Exec>("deployToGitRepository") {
    commandLine("bash", "-c", "./deploy.bash")
}

tasks.named("buildPlugin") {
    finalizedBy("deployToGitRepository")
}

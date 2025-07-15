plugins {
    kotlin("jvm") version "2.0.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.pengrad:java-telegram-bot-api:6.9.0")
}

application {
    mainClass.set("bot.MainKt")
}

kotlin {
    jvmToolchain(21)
}

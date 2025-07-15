plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.telegram:telegrambots:6.9.7.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.pgvector:pgvector:0.1.6")
}

application {
    mainClass.set("ru.comgrid.bot.Main")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

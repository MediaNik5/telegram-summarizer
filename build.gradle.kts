plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.telegram:telegrambots-longpolling:9.0.0")
    implementation("org.telegram:telegrambots-client:9.0.0")
}

application {
    mainClass.set("ru.comgrid.bot.MainKt")
}

java {
    version = 21
}

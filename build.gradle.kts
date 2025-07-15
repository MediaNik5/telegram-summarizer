plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation("com.theokanning.openai-gpt3-java:service:0.18.2")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.pgvector:pgvector:0.1.6")
}

application {
    mainClass.set("ru.comgrid.bot.Main")
}

java {
    version = 21
}

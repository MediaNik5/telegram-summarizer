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
    implementation("com.theokanning.openai-gpt3-java:api:0.18.2")
    implementation("io.github.amikos-tech:chromadb-java-client:0.1.7")
}

application {
    mainClass.set("ru.comgrid.bot.Main")
}

java {
    version = 21
}

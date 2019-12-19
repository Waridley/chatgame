plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
}

apply(plugin = "java")


group = "com.waridley.chatgame"
version = "0.1"

//sourceCompatibility = 1.8

repositories {
    mavenLocal()
    mavenCentral()

    maven(url = "https://oss.jfrog.org/artifactory/libs-release")
}

dependencies {
    testCompile(group = "junit", name = "junit", version = "4.12")
    
    //mongodb java driver
    implementation("org.mongodb:mongo-java-driver:3.11.0")

    //Twitch API library
    implementation(group = "com.github.twitch4j", name = "twitch4j", version = "2.0.0")

    //lombok
//    compileOnly("org.projectlombok:lombok:1.18.10")
//    annotationProcessor("org.projectlombok:lombok:1.18.10")

    //BSON plugin for Jackson
    implementation("de.undercouch:bson4jackson:2.9.2")
    
    implementation("com.github.ajalt:clikt:2.3.0")
    
    implementation(project(":api:backend"))
    implementation(project(":game"))
    implementation(project(":credentials"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.litote.kmongo:kmongo:3.11.1")
    
}
tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
tasks.compileTestKotlin {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

plugins {
    id("java")
	id("org.jetbrains.kotlin.jvm")
	id("com.github.johnrengelman.shadow") version "5.2.0"
}

apply(plugin = "java")

group = "com.waridley.chatgame"
version = "0.1"

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://oss.jfrog.org/artifactory/libs-release")
    maven(url = "https://raw.githubusercontent.com/Waridley/twitch4j/master")
}

dependencies {
    testCompile(group = "junit", name = "junit", version = "4.12")

    //logger
    compile(group = "org.slf4j", name = "slf4j-simple", version = "1.7.28")

    //mongodb java driver
    compile("org.mongodb:mongo-java-driver:3.11.0")

    //Twitch API library
    compile(group = "com.github.twitch4j", name = "twitch4j", version = "1.0.0-alpha.17")

    //lombok
    compileOnly("org.projectlombok:lombok:1.18.10")
    annotationProcessor("org.projectlombok:lombok:1.18.10")

    compile("de.undercouch:bson4jackson:2.9.2")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	
	//CLI parser
	implementation("com.github.ajalt:clikt:2.3.0")
	
    compile(project(":game"))
    compile(project(":api:backend"))
    compile(project(":api:frontend"))
    compile(project(":mongo"))
    compile(project(":credentials"))
    compile(project(":ttv"))
    compile(project(":ttv_chat_client"))
}

tasks.jar {
	manifest {
		attributes["Implementation-Title"] = project.name
		attributes["Implementation-Version"] = project.version
		attributes["Main-Class"] = "com.waridley.chatgame.server.LauncherJ"
	}
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

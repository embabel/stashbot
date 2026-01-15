plugins {
    java
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.vaadin") version "24.6.4"
}

group = "com.embabel"
version = "0.1.0-SNAPSHOT"
description = "RAG chatbot with Vaadin UI"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val embabelAgentVersion = "0.3.3-SNAPSHOT"
val vaadinVersion = "24.6.4"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.embabel.com/artifactory/libs-release")
        mavenContent {
            releasesOnly()
        }
    }
    maven {
        url = uri("https://repo.embabel.com/artifactory/libs-snapshot")
        mavenContent {
            snapshotsOnly()
        }
    }
}

dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:$vaadinVersion")
    }
}

dependencies {
    // Embabel Agent
    implementation("com.embabel.agent:embabel-agent-starter:$embabelAgentVersion")
    implementation("com.embabel.agent:embabel-agent-rag-lucene:$embabelAgentVersion")
    implementation("com.embabel.agent:embabel-agent-rag-tika:$embabelAgentVersion")

    // Web and Vaadin
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.vaadin:vaadin-spring-boot-starter")

    // Markdown rendering for chat messages
    implementation("org.commonmark:commonmark:0.24.0")

    // Model providers (conditional on env vars)
    if (System.getenv("OPENAI_API_KEY") != null) {
        implementation("com.embabel.agent:embabel-agent-starter-openai:$embabelAgentVersion")
    }
    if (System.getenv("ANTHROPIC_API_KEY") != null) {
        implementation("com.embabel.agent:embabel-agent-starter-anthropic:$embabelAgentVersion")
    }

    // Testing
    testImplementation("com.embabel.agent:embabel-agent-test:$embabelAgentVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootRun {
    jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

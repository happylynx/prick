import java.nio.file.Path
import java.nio.file.Files
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.3.61"
    id("java")
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    id("application")
}

group = "happybox"
version = "1.0-SNAPSHOT"

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    val picocliVersion = "4.1.4"
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
    implementation(group = "info.picocli", name = "picocli", version = picocliVersion)
    // for Java it would be: annotationProcessor(group = "info.picocli", name = "picocli-codegen", version = picocliVersion)
    kapt(group = "info.picocli", name = "picocli-codegen", version = picocliVersion)

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.4.1")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.4.1")
}

/*
 *         <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.2.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.2.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-launcher</artifactId>
            <version>LATEST</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>2.23.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <version>2.23.0</version>
            <scope>test</scope>
        </dependency> 
 */

tasks {
    jar {
        manifest {
            attributes(
                    "Implementation-Version" to 0.1,
                    "Main-Class" to "com.github.happylynx.prick.cli.Main"
            )
        }

        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    named<Test>("test") {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    register<Exec>("nativeImage") {
        dependsOn(jar)
        commandLine(getNativeImageCommandLine())
        println("cmdline: ${commandLine.joinToString(" ")}")
        doFirst {
            Files.createDirectories(getNativeImageDir())
        }
    }
}

application {
    mainClassName = "com.github.happylynx.prick.cli.Main"
}

fun getNativeImageDir() = buildDir.resolve("native-image").toPath()

fun getNativeImageCommandLine(): List<String> {
    if (!System.getenv().containsKey("GRAALVM_HOME")) {
        throw GradleException("Please set 'GRAALVM_HOME' environment variable.")
    }

    val nativeImagePath = Path.of(System.getenv("GRAALVM_HOME"))
            .resolve("bin")
            .resolve("native-image")
            .toString()
    val jarPath = tasks.jar.get().archiveFile.get().asFile.absolutePath
    val execPath = getNativeImageDir().resolve("prick").toString()
    val result = listOf(nativeImagePath, "-jar", jarPath, execPath)
    if (isWindows()) {
       return wrapForWindows(result)
    }
    return result
}

fun getRuntimeClasspath(): String {
    val pathSeparator = System.getProperty("path.separator")
    val jars = ArrayList<File>()
    jars.add(tasks.jar.get().archiveFile.get().asFile)
    jars.addAll(configurations.getByName("runtimeClasspath").files)
    return jars.joinToString(pathSeparator) { it.canonicalPath }
}

fun wrapForWindows(commandLineToWrap: List<String>): List<String> {
    val vsDirEnvVar = "VS_DIR"
    val vsDir = if (System.getenv().containsKey(vsDirEnvVar))
        System.getenv(vsDirEnvVar)
    else
        "C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\BuildTools"
    val vcvarsPath = Path.of(vsDir).resolve("VC\\Auxiliary\\Build\\vcvars64.bat")
    if (!Files.exists(vcvarsPath)) {
        throw GradleException("Please make sure that Visual Studio 2017 Visual C++ build tools are correctly installed and environment variable VS_DIR is set.")
    }
    val originalNativeImageCall = commandLineToWrap.joinToString(" ")
    return listOf("cmd", "/C", "call \"$vcvarsPath\" & $originalNativeImageCall")
}

fun isWindows(): Boolean {
    return System.getProperty("os.name").contains("Windows")
}

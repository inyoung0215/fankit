rootProject.name = "fankit"

include("common")

val services = listOf("user", "goods", "order", "payment", "settlement", "admin")
val layers = listOf("domain", "application", "infrastructure", "api")

services.forEach { svc ->
    layers.forEach { layer ->
        val path = "$svc-service:$svc-$layer"
        include(path)
        project(":$path").projectDir = file("$svc-service/$svc-$layer")
    }
}

include("gateway")

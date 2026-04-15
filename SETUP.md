# Setup del proyecto en macOS

## Paso 1: Generar el Gradle Wrapper (solo la primera vez)

Abre la terminal en la carpeta del proyecto y ejecuta:

```bash
# Opción A: si tienes Gradle instalado globalmente
gradle wrapper --gradle-version 8.8

# Opción B: con SDKMAN (recomendado)
sdk install gradle 8.8
gradle wrapper --gradle-version 8.8

# Opción C: con Homebrew
brew install gradle
gradle wrapper --gradle-version 8.8
```

## Paso 2: Compilar y generar el artefacto Lambda

```bash
# Para generar el ZIP (recomendado)
./gradlew clean buildZip

# Para generar el JAR plano (alternativo)
./gradlew clean lambdaJar

# El artefacto se genera en:
# build/distributions/  ← ZIP
# build/libs/           ← JAR
```

## Paso 3: Subir a AWS Lambda

1. Ve a la consola AWS → Lambda → [nombre de la función]
2. Pestaña "Código" → "Cargar desde" → ".zip o .jar"
3. Selecciona el archivo generado en build/
4. Haz clic en "Guardar"

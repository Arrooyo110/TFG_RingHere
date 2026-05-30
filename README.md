# RingHere - Trabajo Fin de Grado (DAM)

RingHere es un sistema de alarmas basadas en ubicación (geofencing) compuesto por una aplicación nativa para Android, una API REST y un panel web de administración.

## Enlaces del proyecto
* **Panel Web:** https://arrooyo110.github.io/TFG_RingHere/
* **Documentación API (Swagger):** https://ringhere-api.onrender.com/docs

## Características principales
* Detección de geocercas en segundo plano.
* Arquitectura offline-first con persistencia local de datos.
* Sincronización automática con el servidor al recuperar la conexión.
* Autenticación segura mediante tokens JWT.

## Stack Tecnológico
* **Aplicación Móvil (Android):** Kotlin, Jetpack Compose, MVVM, Room, Retrofit.
* **Backend (API REST):** Python, FastAPI.
* **Base de datos:** PostgreSQL (alojada en Neon).
* **Despliegue e Infraestructura:** Render, GitHub Actions.

## Despliegue en local

### 1. Clonar el repositorio
`git clone https://github.com/Arrooyo110/TFG_RingHere.git`

### 2. Configurar el Backend
1. Crear un entorno virtual e instalar las dependencias desde `requirements.txt`.
2. Crear un archivo `.env` en la raíz con las variables `DATABASE_URL` y `SECRET_KEY`.
3. Ejecutar el servidor con: `uvicorn main:app --reload`

### 3. Configurar el Cliente Android
1. Abrir el proyecto en Android Studio.
2. Sincronizar los archivos de Gradle.
3. Ejecutar la aplicación en un emulador o dispositivo físico.

## Mejoras futuras
* Desarrollo de un cliente nativo en iOS consumiendo la misma API.
* Integración de un chatbot con IA para configurar alarmas mediante lenguaje natural.
* Sistema "online" de notificaciones compartidas al entrar o salir de una zona vinculada con otros usuarios.

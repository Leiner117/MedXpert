# MedXpert

<div align="center">

![MedXpert Logo](app/src/main/res/drawable/logonobackground.webp)

**Aplicación Android para gestión integral de información de salud**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android)](https://www.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-007396?logo=java)](https://www.java.com/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase)](https://firebase.google.com/)
[![License](https://img.shields.io/badge/License-Private-red.svg)](LICENSE)

</div>

---

## Tabla de Contenidos

- [Descripción](#descripción)
- [Características](#características)
- [Tecnologías](#tecnologías)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Arquitectura](#arquitectura)

---

## Descripción

**MedXpert** es una aplicación móvil Android diseñada para facilitar la gestión de información médica entre doctores y pacientes. La aplicación permite:

- **Para Pacientes**: Agendar citas médicas, consultar diagnósticos, gestionar medicamentos y mantener comunicación con sus doctores
- **Para Doctores**: Gestionar disponibilidad, ver pacientes asignados, crear diagnósticos, prescribir medicamentos y comunicarse con pacientes

La aplicación utiliza Firebase como backend para autenticación, almacenamiento de datos y archivos multimedia.

---

## Características

### Para Doctores

- **Gestión de Disponibilidad**: Configurar horarios disponibles para citas
- **Lista de Pacientes**: Ver todos los pacientes asignados
- **Diagnósticos**: Crear y gestionar diagnósticos médicos detallados
- **Prescripción de Medicamentos**: Agregar medicamentos con dosis e instrucciones
- **Chat en Tiempo Real**: Comunicación directa con pacientes
- **Notificaciones**: Recordatorios de citas programadas
- **Perfil Profesional**: Gestionar información profesional y especialidad

### Para Pacientes

- **Agendar Citas**: Reservar citas médicas según disponibilidad del doctor
- **Historial Médico**: Ver diagnósticos y medicamentos prescritos
- **Medicamentos**: Consultar medicamentos actuales con instrucciones
- **Chat con Doctor**: Comunicación directa con el médico tratante
- **Notificaciones**: Recordatorios de citas y medicamentos
- **Perfil de Usuario**: Gestionar información personal y de salud

### Seguridad

- Autenticación con Firebase (Email/Password y Google Sign-In)
- Encriptación de contraseñas con BCrypt
- Verificación de código de seguridad
- Términos y condiciones
- Permisos de usuario según rol (Doctor/Paciente)

---

## Tecnologías

### Lenguajes y Frameworks
- **Java** - Lenguaje principal
- **Android SDK** (API 24-35)
- **XML** - Diseño de interfaces

### Arquitectura y Patrones
- **MVVM** (Model-View-ViewModel)
- **Clean Architecture**
- **Repository Pattern**
- **Coordinator Pattern** para navegación

### Librerías Principales

#### Inyección de Dependencias
- **Dagger Hilt** (v2.51.1) - Gestión de dependencias

#### Backend y Base de Datos
- **Firebase Suite**:
  - Firebase Authentication (v23.2.0)
  - Firebase Firestore (v25.1.3) - Base de datos NoSQL
  - Firebase Storage (v21.0.1) - Almacenamiento de archivos
  - Firebase Realtime Database (v21.0.0) - Chat en tiempo real
  - Firebase Analytics (v22.4.0)
- **Room** (v2.6.1) - Base de datos local

#### UI y Diseño
- **Material Design** (v1.12.0)
- **ConstraintLayout** (v2.2.1)
- **Glide** (v4.15.1) - Carga de imágenes

#### Programación Reactiva
- **RxJava 3** (v3.1.8)
- **RxAndroid** (v3.0.2)

#### Autenticación y Seguridad
- **Google Play Services Auth** (v21.3.0)
- **Google Credential Manager** (v1.5.0)
- **BCrypt** (v0.9.0) - Hashing de contraseñas
- **jBCrypt** (v0.4)

#### Networking
- **OkHttp** (v4.10.0)

#### Otras
- **AndroidX Libraries** (Core, AppCompat, Activity, Lifecycle)

---

## Requisitos

### Software Necesario
- **Android Studio** Arctic Fox (2020.3.1) o superior
- **JDK** 8 o superior (JDK 11 recomendado)
- **Gradle** 8.8.2
- **Android SDK**:
  - Compile SDK: 35
  - Min SDK: 24 (Android 7.0)
  - Target SDK: 35

### Configuración Adicional
- Cuenta de **Firebase** con proyecto configurado
- Archivo `google-services.json` en `app/`
- Conexión a Internet para sincronización

---

## Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/Leiner117/MedXpert.git
cd MedXpert
```

### 2. Configurar Firebase

1. Crear un proyecto en [Firebase Console](https://console.firebase.google.com/)
2. Agregar una aplicación Android con el package name: `com.tec.medxpert`
3. Descargar `google-services.json`
4. Colocar el archivo en `app/google-services.json`
5. Habilitar los siguientes servicios en Firebase:
   - Authentication (Email/Password y Google)
   - Firestore Database
   - Realtime Database
   - Storage
   - Analytics


### 4. Ejecutar la Aplicación

- Conectar un dispositivo Android (API 24+) o iniciar un emulador
- Hacer clic en el botón **Run** en Android Studio
- O ejecutar: `.\gradlew installDebug`

---

## Estructura del Proyecto

```
MedXpert/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tec/medxpert/
│   │   │   │   ├── auth/              # Autenticación y sesión
│   │   │   │   ├── data/              # Modelos y repositorios
│   │   │   │   ├── di/                # Módulos de Dagger Hilt
│   │   │   │   ├── MainApplication/   # Activity principal
│   │   │   │   ├── navigation/        # Coordinadores de navegación
│   │   │   │   ├── service/           # Servicios
│   │   │   │   ├── ui/                # Vistas y ViewModels
│   │   │   │   │   ├── addDiagnostic/
│   │   │   │   │   ├── addMedication/
│   │   │   │   │   ├── appointment/
│   │   │   │   │   ├── availability/
│   │   │   │   │   ├── chat/
│   │   │   │   │   ├── chat_profile/
│   │   │   │   │   ├── diagnostic/
│   │   │   │   │   ├── diagnosticInformation/
│   │   │   │   │   ├── home/
│   │   │   │   │   ├── login/
│   │   │   │   │   ├── profile/
│   │   │   │   │   ├── register/
│   │   │   │   │   ├── stepper/       # Onboarding
│   │   │   │   │   ├── terms/
│   │   │   │   │   ├── viewMedication/
│   │   │   │   │   └── ViewMedicationPatient/
│   │   │   │   ├── util/              # Utilidades (Resource, Status)
│   │   │   │   └── utils/             # Helpers (Email, Errors)
│   │   │   │   └── MedXpertApp.java   # Application class
│   │   │   ├── res/
│   │   │   │   ├── drawable/          # Recursos gráficos
│   │   │   │   ├── layout/            # Layouts XML
│   │   │   │   ├── values/            # Strings, colores, temas
│   │   │   │   ├── values-es/         # Strings en español
│   │   │   │   └── xml/               # Configuraciones
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                      # Tests unitarios
│   │   └── androidTest/               # Tests de instrumentación
│   ├── build.gradle                   # Configuración del módulo
│   └── proguard-rules.pro
├── gradle/
│   └── libs.versions.toml             # Catálogo de versiones
├── build.gradle                       # Configuración raíz
├── settings.gradle
├── gradle.properties
└── README.md
```

---

## Arquitectura

### Patrón MVVM (Model-View-ViewModel)

```
┌─────────────┐
│    View     │ ← Activity/Fragment (UI)
│  (Activity) │
└──────┬──────┘
       │ observa
       ▼
┌─────────────┐
│  ViewModel  │ ← Lógica de presentación
│   (LiveData)│
└──────┬──────┘
       │ usa
       ▼
┌─────────────┐
│ Repository  │ ← Fuente de datos
│             │
└──────┬──────┘
       │
       ├─────────────┬────────────┐
       ▼             ▼            ▼
  ┌─────────┐  ┌─────────┐  ┌─────────┐
  │Firebase │  │  Room   │  │  API    │
  │Firestore│  │  (Local)│  │         │
  └─────────┘  └─────────┘  └─────────┘
```

### Navegación con Coordinadores

El proyecto utiliza el **Coordinator Pattern** para manejar la navegación entre pantallas:

- `DiagnosticInformationCoordinator`
- `ViewMedicationCoordinator`
- `ProfileCoordinator`
- `DoctorHomeCoordinator`
- `PatientsHomeCoordinator`

### Inyección de Dependencias

Dagger Hilt gestiona las dependencias en módulos específicos:
- `DiagnosticInformationModule`
- Módulos de repositorio
- Módulos de ViewModels

---

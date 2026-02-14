# CloudSend Android

Android client for the CloudSend file-sharing service (centralized server for sharing files between user devices).

## Architecture

Clean Architecture with 3 layers:
- **`domain/`** - Business logic (Interactors/UseCases), pure Kotlin, framework-independent
- **`data/`** - Repositories, Ktor HTTP client, Room DB, shared prefernces
- **`presentation/`** - ViewModels (StateFlow + sealed state classes)
- **`ui/`** - Jetpack Compose screens and components

## Tech Stack

| Concern | Library | Version |
|---------|---------|---------|
| UI | Jetpack Compose + Material3 | BOM 2026.01.00 |
| Navigation | **androidx.navigation3** (experimental) | 1.0.0 |
| DI | Dagger Hilt | 2.57.1 |
| HTTP | Ktor Client (OkHttp engine) | 3.4.0 |
| Database | Room | 2.8.4 |
| Serialization | Kotlinx Serialization | 1.10.0 |
| Language | Kotlin | 2.2.0 |
| Min SDK | 24 (Android 7.0) | Target: 36 |

## Key Patterns

- **DI**: `@HiltViewModel`, `@AssistedInject` for ViewModels with arguments, single `DataModule` in `data/`
- **State**: `MutableStateFlow<SealedState>` in ViewModels (Loading/Loaded/Error/Done variants)
- **Errors**: Sealed `AppError` class; `handleCommonExceptions()` maps exceptions to domain errors
- **Auth**: Ktor `Auth` plugin with Bearer tokens + auto-refresh on 401; tokens stored in DataStore
- **Navigation**: Navigation3 uses `NavBackStack` + `NavKey` (Kotlinx Serializable) + `NavDisplay`
- **No XML layouts** - 100% Compose

## Project Structure

```
app/src/main/java/ru/vizbash/cloudsend/
├── domain/          # Interactors, domain models, AppError
├── data/
│   ├── network/     # CloudsendClient (Ktor), DTOs
│   └── persistence/ # Room DB, DataStore repos
├── presentation/    # ViewModels
└── ui/
    ├── screen/      # Compose screens
    ├── component/   # Reusable composables
    └── theme/       # Material3 theme
```

## Activities

- **MainActivity** - LAUNCHER, hosts bottom nav (Receive / Send / Settings)
- **SendActivity** - Handles `ACTION_SEND` intents + direct share shortcuts
- **BaseActivity** - Abstract base; redirects to setup screen if not configured

## Features

- Login to CloudSend server, register device
- Browse online/offline devices, send files with progress tracking
- Direct share shortcuts (populated from Room-cached device list)
- Settings: download directory, auto-accept toggle
- Receive screen: in progress (`ReceiveViewModel` added, UI not yet built)

## Networking

Ktor client with: OkHttp engine, ContentNegotiation (JSON), WebSockets (for incoming transfers), Auth (Bearer + refresh), Compression, Logging, HttpTimeout. Cleartext traffic enabled (`usesCleartextTraffic=true`) for dev/LAN use.

# api-netease

Netease Cloud Music API bindings plus a small shared client facade.

## Add Dependency

```kotlin
dependencies {
    implementation(project(":lib:api:api-netease"))
}
```

## Basic Usage

```kotlin
MusicSearchClient.mytoken = token

val result = MusicSearchClient.musicApi.searchBySong(
    keyword = "晴天",
)
```

## Notes

- Requests use the shared `HttpClientFactory` profile `netease-music`.
- Setting `MusicSearchClient.mytoken` updates the profile's `Authorization` header.

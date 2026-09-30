# TECH-STACK

Versi ditentukan oleh **BOM Spring Boot 4.1.1** (rilis stabil terbaru per 30 Sep 2026). Untuk dependency yang dikelola BOM, **jangan tulis `<version>`**. Nama starter di Boot 4 sudah dimodularisasi; verifikasi ulang lewat [start.spring.io](https://start.spring.io) saat membuat proyek.

## Platform
| Komponen | Versi | Fungsi |
|---|---|---|
| Java (JDK) | 21 atau lebih baru | Bahasa dan runtime; pakai `record`, pattern matching, virtual thread |
| Maven + Wrapper | via `./mvnw` | Build tool; wrapper menjamin versi Maven sama di semua mesin |
| PostgreSQL | 18 | Database relasional utama |
| Spring Boot | 4.1.1 | Kerangka aplikasi; membawa Spring Framework 7 |

## Dependency Runtime
| Artifact | Versi | Fungsi |
|---|---|---|
| `spring-boot-starter-webmvc` | BOM | REST controller dan server Tomcat embedded |
| `spring-boot-starter-data-jpa` | BOM | Spring Data JPA + Hibernate untuk akses data |
| `spring-boot-starter-validation` | BOM | Bean Validation (`@Valid`, `@NotBlank`, dst.) |
| `spring-boot-starter-flyway` | BOM | Auto-configuration Flyway untuk migration saat start |
| `org.flywaydb:flyway-database-postgresql` | BOM | Modul dukungan PostgreSQL untuk Flyway |
| `org.postgresql:postgresql` (runtime) | BOM | Driver JDBC PostgreSQL |
| `spring-boot-starter-actuator` | BOM | Endpoint `/actuator/health` untuk cek status |

## Dependency Test
| Artifact | Versi | Fungsi |
|---|---|---|
| `spring-boot-starter-test` | BOM | JUnit 5, AssertJ, Mockito |
| `spring-boot-starter-webmvc-test` | BOM | `MockMvc` dan slice test `@WebMvcTest` |
| `spring-boot-starter-data-jpa-test` | BOM | Slice test `@DataJpaTest` |
| `spring-boot-testcontainers` | BOM | Integrasi Testcontainers dengan `@ServiceConnection` |
| `org.testcontainers:testcontainers-postgresql` | BOM | PostgreSQL asli di Docker untuk test integrasi |
| `org.testcontainers:testcontainers-junit-jupiter` | BOM | Lifecycle container di JUnit 5 |

> Testcontainers butuh Docker. Jika Docker tidak tersedia, jalankan test terhadap database `bookshelf_test` lokal.

## Opsional
| Artifact | Versi | Fungsi |
|---|---|---|
| `spring-boot-devtools` | BOM | Restart otomatis saat kode berubah (dev saja) |
| `springdoc-openapi-starter-webmvc-ui` | cek Maven Central; pilih rilis yang kompatibel dengan Boot 4 | Swagger UI dan spesifikasi OpenAPI |

## Fase 2
| Artifact | Versi | Fungsi |
|---|---|---|
| `spring-boot-starter-security` | BOM | Autentikasi dan otorisasi |
| `spring-boot-starter-security-test` | BOM | Helper test (`@WithMockUser`) |
| `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` | cek Maven Central (tidak dikelola BOM) | Pembuatan dan verifikasi JWT |

## Sengaja Tidak Dipakai
- **Lombok** — `record` untuk DTO sudah cukup, dan lebih mudah dipelajari.
- **MapStruct** — mapping manual dulu agar alurnya terlihat jelas.
- **H2** — test dijalankan di PostgreSQL asli agar perilaku constraint sama.

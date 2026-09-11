# Caching Proxy

> Ein Caching-Proxy-Server in Arbeit, erstellt mit Java 21 und Spring Boot.

Dieses Projekt ist meine Umsetzung des [Caching-Server-Projekts von roadmap.sh](https://roadmap.sh/projects/caching-server). Es leitet `GET`-Anfragen an einen Origin-Server weiter, speichert die Antworten in einem In-Memory-Cache und liefert bei wiederholten Anfragen die zwischengespeicherte Antwort zurück.

[English version](README.md)

## Aktueller Stand

Der Proxy und der In-Memory-Cache sind implementiert. Die in der Aufgabe geforderte CLI und der Befehl zum Leeren des Caches fehlen jedoch noch. Das Projekt ist daher **noch keine vollständige Lösung** der Übung.

Bereits implementiert:

- Weiterleitung von `GET`-Anfragen an einen konfigurierbaren Origin-Server.
- Thread-sicherer In-Memory-Cache, dessen Schlüssel aus Pfad und Query-String besteht.
- Übernahme von Response-Headern und Body des Origin-Servers.
- `X-Cache: MISS`, wenn der Origin-Server abgefragt wurde, sowie `X-Cache: HIT` bei einer Cache-Antwort.
- Unit-Test für den Cache-Hit-Pfad.

Noch offen:

- Der CLI-Befehl `caching-proxy --port <number> --origin <url>`.
- Der CLI-Befehl `caching-proxy --clear-cache`.
- Ablaufzeit bzw. Verdrängung von Cache-Einträgen. Der Cache bleibt derzeit bis zum Beenden der Anwendung erhalten.
- Unterstützung für HTTP-Methoden außer `GET`.

## Voraussetzungen

- Java 21
- Keine separate Maven-Installation erforderlich; der Maven Wrapper ist enthalten.

## Lokal starten

Die Anwendung verwendet momentan Spring-Boot-Konfiguration statt der endgültigen CLI. Die Standardwerte sind:

```properties
server.port=8082
proxy.origin=https://dummyjson.com
```

Start mit den Standardwerten:

```bash
./mvnw spring-boot:run
```

Oder das JAR bauen und Port sowie Origin per Spring-Boot-Argumente festlegen:

```bash
./mvnw package
java -jar target/Caching_Proxy-0.0.1-SNAPSHOT.jar --server.port=3000 --proxy.origin=http://dummyjson.com
```

Danach dieselbe Anfrage zweimal ausführen:

```bash
curl -i http://localhost:3000/products
curl -i http://localhost:3000/products
```

Die erste Antwort enthält `X-Cache: MISS`, die zweite `X-Cache: HIT`.

## Funktionsweise

1. Der Controller empfängt eine `GET`-Anfrage und übernimmt Pfad und optionalen Query-String.
2. Der Service sucht diesen Wert in `CacheRepository`.
3. Bei einem Miss ruft er `proxy.origin + path` auf, gibt die Origin-Antwort zurück und speichert Body und Header im Cache.
4. Bei einem Hit liefert er Body und Header aus dem Cache, ohne den Origin-Server aufzurufen.

## Demonstration

Die erste Anfrage wird an den Origin-Server weitergeleitet (in dieser Aufnahme etwa 1,8 s):

![Erste Anfrage vom Origin-Server](docs/images/origin-request-cache-miss.png)

Die wiederholte Anfrage kommt deutlich schneller aus dem Cache (in dieser Aufnahme 34 ms):

![Wiederholte Anfrage aus dem Cache](docs/images/cached-request-cache-hit.png)

Die Anwendungslogs bestätigen die erwartete Reihenfolge: zuerst `X-Cache: MISS`, danach `X-Cache: HIT`.

![Anwendungslogs mit Cache-Miss und anschließendem Cache-Hit](docs/images/cache-hit-miss-logs.png)

## Tests

```bash
./mvnw test
```

## Nächster Schritt

Die echte Kommandozeilenschnittstelle `caching-proxy` implementieren, `--port` und `--origin` auf die Spring-Boot-Konfiguration abbilden und `--clear-cache` ergänzen, damit der Cache ohne Codeänderung geleert werden kann. Danach sollten Integrationstests einen echten Miss, einen anschließenden Hit und das Leeren des Caches prüfen.

## Lizenz

Es wurde noch keine Lizenz ausgewählt.

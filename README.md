# GitHub Trends

A Java CLI tool that scrapes [GitHub Trending](https://github.com/trending) and persists the results to a local SQLite database. Run it on a schedule to track which projects are rising in popularity across multiple languages and time ranges.

## Features

- Scrapes GitHub Trending for **any language** as well as a configurable list of specific languages
- Supports **daily**, **weekly**, and **monthly** trend periods
- Persists trending projects to a local **SQLite** database (deduplication included)
- Prints results to the **console** with colored output (via Jansi)
- CLI flags for customizing languages, trend periods, and output format

## Requirements

- Java 21+
- Maven 3.x

## Build

```bash
mvn -B package
```

## Run

```bash
java -jar target/githubtrends-1.0-SNAPSHOT.jar [options]
```

### CLI Options

| Flag | Alias         | Default          | Description                                                     |
| ---- | ------------- | ---------------- | --------------------------------------------------------------- |
| `-t` | `--trends`    | `weekly,monthly` | Trend periods: `daily`, `weekly`, `monthly`                     |
| `-l` | `--languages` | See below        | Comma-separated list of languages to scrape                     |
| `-o` | `--output`    | `console`        | Output mode: `console`, `email` _(planned)_, `file` _(planned)_ |

**Default languages:** `java`, `c++`, `c`, `rust`, `python`, `javascript`, `ruby`, `go`, `shell`, `typescript`, `zig`, `kotlin`

### Examples

```bash
# Weekly + monthly trends for default languages, printed to console
java -jar target/githubtrends-1.0-SNAPSHOT.jar

# Daily trends only
java -jar target/githubtrends-1.0-SNAPSHOT.jar -t daily

# Only Java and Rust, weekly
java -jar target/githubtrends-1.0-SNAPSHOT.jar -t weekly -l java -l rust
```

## Database

Trending projects are stored in a SQLite file named `github-trends.db` in the working directory. The schema is managed automatically by Hibernate (`hbm2ddl.auto=update`).

### `projects` table

| Column             | Type        | Description                                             |
| ------------------ | ----------- | ------------------------------------------------------- |
| `id`               | INTEGER PK  | Auto-generated                                          |
| `projectName`      | TEXT UNIQUE | GitHub path (e.g. `/owner/repo`)                        |
| `description`      | TEXT        | Repository description                                  |
| `codeLanguage`     | TEXT        | Primary language                                        |
| `stars`            | INTEGER     | Total star count                                        |
| `trendStars`       | INTEGER     | Stars gained during the trend period                    |
| `trendStarsString` | TEXT        | Raw trend stars string (e.g. `"1,234 stars this week"`) |
| `isNew`            | BOOLEAN     | `true` until printed to console                         |
| `createdAt`        | DATETIME    | Timestamp when first recorded                           |

## Tech Stack

| Library                      | Version  | Purpose                |
| ---------------------------- | -------- | ---------------------- |
| Hibernate ORM                | 6.6.0    | ORM / database access  |
| SQLite JDBC                  | 3.46.1.0 | SQLite driver          |
| Hibernate Community Dialects | 6.6.0    | SQLite dialect         |
| Jsoup                        | 1.17.2   | HTML scraping          |
| Unirest-Java                 | 3.14.5   | HTTP client            |
| args4j                       | 2.37     | CLI argument parsing   |
| Jansi                        | 2.4.0    | Colored console output |
| Lombok                       | 1.18.34  | Boilerplate reduction  |
| Apache Commons Lang3         | 3.14.0   | Utility helpers        |

## Roadmap

- [ ] Email output (`-o email`)
- [ ] File output (`-o file`)
- [ ] Batch / scheduled mode

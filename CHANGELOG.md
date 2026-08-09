# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- build: Upgrade the Java used for building from 17 to 21 (https://github.com/sciencesakura/dbsetup-csv/pull/64).
- ci: Set paths to reduce unnecessary CI runs (https://github.com/sciencesakura/dbsetup-csv/pull/65).
- ci: Deploy docs automatically (https://github.com/sciencesakura/dbsetup-csv/pull/66).
- build: Add Dependabot (https://github.com/sciencesakura/dbsetup-csv/pull/67).
- Bump org.junit.jupiter:junit-jupiter from 6.0.3 to 6.1.2 (https://github.com/sciencesakura/dbsetup-csv/pull/68).
- Bump com.puppycrawl.tools:checkstyle from 12.3.1 to 13.9.0 (https://github.com/sciencesakura/dbsetup-csv/pull/69).

## [3.0.2] - 2026-02-21
### Added
- Add tsv() function that is shorthand of csv().withDelimiter(TAB) (https://github.com/sciencesakura/dbsetup-csv/pull/59).
### Changed
- Upgrade to checkout@v6 and setup-java@v5 (https://github.com/sciencesakura/dbsetup-csv/pull/56).
- Upgrade dependencies (https://github.com/sciencesakura/dbsetup-csv/pull/57).
- Remove dependency on spotbugs-annotations (https://github.com/sciencesakura/dbsetup-csv/pull/60).
- Upgrade tool Java version to 17 (https://github.com/sciencesakura/dbsetup-csv/pull/61).
- Switch to JSpecify for nullability expression (https://github.com/sciencesakura/dbsetup-csv/pull/62).

## [3.0.1] - 2025-08-14
### Changed
- Upgrade toolchains and improve dev environment (https://github.com/sciencesakura/dbsetup-csv/pull/51).
- Various corrections (https://github.com/sciencesakura/dbsetup-csv/pull/52).

## [3.0.0] - 2023-07-09
### Added
- Support JPMS (https://github.com/sciencesakura/dbsetup-csv/pull/46).
### Changed
- Build with maven (https://github.com/sciencesakura/dbsetup-csv/pull/36).
- Upgrade dependencies (https://github.com/sciencesakura/dbsetup-csv/pull/38).
- Publish packages to registry in CI (https://github.com/sciencesakura/dbsetup-csv/pull/39).
- Fix CI: add GPG settings (https://github.com/sciencesakura/dbsetup-csv/pull/40).
- Fix CI: packaging manifest created by bundle plugin (https://github.com/sciencesakura/dbsetup-csv/pull/41).
- Make it possible to publish API docs with CI (https://github.com/sciencesakura/dbsetup-csv/pull/42).
- Remove use of deprecated API (https://github.com/sciencesakura/dbsetup-csv/pull/44).
- Update documentation (https://github.com/sciencesakura/dbsetup-csv/pull/47).
- Improve test (https://github.com/sciencesakura/dbsetup-csv/pull/49).
### Fixed
- Fix: prevent unexpected value generations (https://github.com/sciencesakura/dbsetup-csv/pull/45).

## [2.0.2] - 2021-12-12
### Changed
- Remove 'compile' dependency on kotlin-stdlib-jdk8 (https://github.com/sciencesakura/dbsetup-csv/pull/30).
- Load a CSV file lazy (https://github.com/sciencesakura/dbsetup-csv/pull/31).
- Remove the property disables uploading SHA-256/SHA-512 checksums (https://github.com/sciencesakura/dbsetup-csv/pull/32).

## [2.0.1] - 2021-12-10
### Changed
- Upgrade commons-csv 1.8->1.9.0 (https://github.com/sciencesakura/dbsetup-csv/pull/26).
- Upgrade Kotlin 1.4.32->1.6.0 (https://github.com/sciencesakura/dbsetup-csv/pull/27).

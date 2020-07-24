# dbsetup-csv: Import CSV using DbSetup

[English](README.md) | [日本語](README.ja.md)

CSV/TSVファイルからデータ取り込みができる[DbSetup](http://dbsetup.ninja-squad.com/)拡張機能です.

![](https://github.com/sciencesakura/dbsetup-csv/workflows/build/badge.svg)

## Requirement

* Java 8以降

## Installation

Gradle:

```groovy
testImplementation 'com.sciencesakura:dbsetup-csv:1.0.1'
```

Maven:

```xml
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv</artifactId>
  <version>1.0.1</version>
  <scope>test</scope>
</dependency>
```

## Usage

```java
import static com.sciencesakura.dbsetup.csv.Import.csv;

// `testdata.csv`はクラスパス上にある必要があります
Operation operation = csv("testdata.csv").into("tablename").build()
DbSetup dbSetup = new DbSetup(destination, operation);
dbSetup.launch();
```

デフォルトでソース・ファイルはUTF-8でエンコードされたカンマ区切りのファイルとして扱われます. またソース・ファイルの先頭行はヘッダと看做されます. これらは変更可能です:

```java
// ms932エンコード, タブ区切り, ヘッダなしファイルを取り込む
csv("testdata.tsv").into("tablename")
    .withCharset("ms932")
    .withDelimiter('\t')
    .withHeader("column_1", "column_2", "column_3")
    .build()
```

詳細は[APIリファレンス](https://sciencesakura.github.io/dbsetup-csv/)を参照して下さい.

## Recommendation

この拡張機能を利用するのは, 取り込み先テーブルの列数が多すぎて[Insert.Builder](http://dbsetup.ninja-squad.com/apidoc/2.1.0/com/ninja_squad/dbsetup/operation/Insert.Builder.html)を使用したコードの可読性が悪くなってしまう場合にのみにすることをお薦めします.

## Prefer Excel ?

→ [dbsetup-spreadsheet](https://github.com/sciencesakura/dbsetup-spreadsheet)

## License

MIT License

Copyright (c) 2019 sciencesakura

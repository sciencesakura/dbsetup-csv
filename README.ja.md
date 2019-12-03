# dbsetup-csv: Import CSV using DbSetup

[English](README.md) | [日本語](README.ja.md)

外部CSV/TSVファイルからデータ取り込みができる[DbSetup](http://dbsetup.ninja-squad.com/)拡張です.

## Requirement

* Java 8以降

## Installation

Gradle:

```groovy
testImplementation 'com.sciencesakura:dbsetup-csv:0.0.1'
```

Maven:

```xml
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv</artifactId>
  <version>0.0.1</version>
  <scope>test</scope>
</dependency>
```

## Usage

使用例:

```java
import static com.sciencesakura.dbsetup.csv.Import.csv;
```

```java
// `data/customer.csv` はクラスパス上にある必要があります
Operation operation = sequenceOf(
    truncate("customer"),
    csv("data/customer.csv").into("customer").build()
);
DbSetup dbSetup = new DbSetup(destination, operation);
dbSetup.launch();
```

デフォルトでソース・ファイルはUTF-8でエンコードされたカンマ区切りのファイルとして扱われます. またソース・ファイルの先頭行はヘッダと看做されます. これらは変更可能です:

```java
// ms932エンコード, タブ区切り, ヘッダなしファイルを取り込む
csv("data/vender.tsv").into("vender")
    .withCharset("ms932")
    .withDelimiter('\t')
    .withHeader("id", "vender_no", "vender_name", "tel_no", "email_address")
    .build()
```

詳細は[APIリファレンス](https://sciencesakura.github.io/dbsetup-csv/)を参照して下さい.

## Recommendation

この拡張機能を利用するのは, 取り込み先テーブルの列数が多すぎて[Insert.Builder](http://dbsetup.ninja-squad.com/apidoc/2.1.0/com/ninja_squad/dbsetup/operation/Insert.Builder.html)を使用したコードの可読性が悪くなってしまう場合にのみにすることをお薦めします.

## License

MIT License

Copyright (c) 2019 sciencesakura

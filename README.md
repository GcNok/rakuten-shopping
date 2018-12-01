# rakuten-shopping
楽天のWebAPIを利用して、各ジャンルごとの人気商品を10位までをDBに保存する。

## 説明
1. 楽天ジャンル検索APIを使用してジャンル階層3まで再帰的にジャンル情報を取得し、genreテーブルにジャンルID、ジャンル名、ジャンル階層を保存する。
2. 1で取得したジャンルIDをインプットに楽天商品ランキングAPIにアクセスし、各ジャンルごとに人気上位10商品のジャンルID、順位、商品名をitem_rankingテーブルに保存する。

## 開発環境
### OS
Windows 10

### IDE
Eclipse 4.8 Photon

### 言語
* Java 8
    * Jersey 2.27

### DB
* MySQL Server 5.6
    * データベース名 rakuten_items<br/>
    ポート番号 3306<br/>
    ユーザー root<br/>
    パスワード なし

### ビルドツール
Apache Maven 3.6.0

## 使い方
1. MySQLにて、対象のデータベースを作成
```sql
create database rakuten_items;
```
2. genreテーブル、item_rankingテーブルを作成
```sql
create table genre (
 genre_id int primary key,
 genre_name varchar(30),
 genre_level int 
);

create table item_ranking (
    genre_id int ,
    rank int ,
    item_name varchar(255) ,
    primary key (genre_id,rank)
);
```
3. 対象のjarファイルを実行
`java -jar rakuten-shopping-jar-with-dependencies.jar`

## 備考
* サロゲートペアの対策として、MySQLの文字コードはutf8mb4を設定する。
    * my.iniに以下の設定を記載する。<br/>
    character_set_server=utf8mb4
* 楽天商品ランキングAPIは連続でアクセスするとリクエスト過多でエラーとなるため、１秒後にリトライする。
* 楽天商品ランキングAPIでは指定したジャンルIDに対してデータが見つからない、または、アイテム数が3つよりも少ない場合、HTTPステータスコード400または404を返すため、その場合は無視して処理を続行する。
* 2回目以降の実行を考慮し、それぞれのテーブルのレコードを全件削除してからDB保存処理を行う。
* 処理時間はジャンル保存処理が3分程度
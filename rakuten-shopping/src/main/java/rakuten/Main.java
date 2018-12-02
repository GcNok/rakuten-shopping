package rakuten;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

public class Main {

	//ログ出力用オブジェクト
	public static final Logger logger = Logger.getLogger(Main.class.getName());

	/**
	 * メイン処理
	 * @param args
	 */
	public static void main(String[] args) {
		logger.info("処理開始");
		//ローカルのDBに接続
		String url = "jdbc:mysql://localhost:3306/rakuten_items?characterEncoding=UTF-8&serverTimezone=JST";
		String user = "root";
		String pass = "";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, user, pass);

			//genreテーブルのレコードを全件削除
			logger.info("genreテーブルのレコードを削除");
			String sql = "delete from genre";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.executeUpdate();

			//APIにアクセスしてジャンルのルート情報を取得
			logger.info("ジャンルのルート情報を取得");
			GenreSerach genreSerach = new GenreSerach();
			JsonNode parentNode = genreSerach.getGenreInfo("0");

			//ジャンル情報を再帰的に3階層までDBに保存
			logger.info("ジャンル情報保存処理開始");
			genreSerach.saveGenreInfo(parentNode, conn);
			logger.info("ジャンル情報保存処理完了");

			//ジャンルIDを基にAPIにアクセスして商品情報をDBに保存
			logger.info("ジャンル別商品ランキング情報保存処理開始");
			ItemRanking itemRanking = new ItemRanking();
			itemRanking.saveItemRanking(conn);
			logger.info("ジャンル別商品ランキング情報保存処理完了");

		} catch (Exception e) {
			logger.error("予期せぬエラーが発生しました。", e);
		} finally {
			// コネクションの解放
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("コネクションのクローズに失敗しました。", e);
				}
			}
			logger.info("処理完了");
		}

	}
}

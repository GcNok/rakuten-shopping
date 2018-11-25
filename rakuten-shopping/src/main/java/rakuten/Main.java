package rakuten;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

public class Main {

	//ログ出力用
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {
		LOGGER.info("処理開始");
		//ローカルのDBに接続
		String url = "jdbc:mysql://localhost:3306/rakuten_items?characterEncoding=UTF-8&serverTimezone=JST";
		String user = "root";
		String pass = "";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, user, pass);

			//ジャンルテーブル削除
			String sql = "delete from genre";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.executeUpdate();

			//APIにアクセスしてジャンルのルート情報を取得
			GenreSerach genreSerach = new GenreSerach();
			JsonNode parentNode = genreSerach.getGenreInfo("0");

			//ジャンル情報を再帰的に3階層までDBに保存
			genreSerach.saveGenreInfo(parentNode, conn);

			//ジャンルIDを基にAPIにアクセスして商品情報をDBに保存
			ItemRanking itemRanking = new ItemRanking();
			itemRanking.saveItemRanking(conn);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// コネクションの解放
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.severe("コネクションのクローズに失敗しました。");
				}
			}
			LOGGER.info("処理完了");
		}

	}
}

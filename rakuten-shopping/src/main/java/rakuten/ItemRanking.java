package rakuten;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import com.fasterxml.jackson.databind.JsonNode;

public class ItemRanking extends RakutenAPI {

	//楽天商品ランキングAPIのパス
	public static final String PATH_URL = "/services/api/IchibaItem/Ranking/20170628";

	//ログ出力用
	private static final Logger LOGGER = Logger.getLogger(ItemRanking.class.getName());

	/**
	 *
	 * @param genreId
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public JsonNode getItemInfo(String genreId) throws IOException, InterruptedException {
		try {
			return super.getInfo(genreId, PATH_URL, "Items");
		} catch (WebApplicationException e) {
			int statusCode = e.getResponse().getStatus();
			//HTTPステータスコードが400または404の場合、nullを返す
			if (statusCode == 400 || statusCode == 404) {
				LOGGER.warning("対象のデータがありませんでした。 ステータスコード：" + statusCode);
				return null;

			} else if (statusCode == 429) {
				//リクエスト過多の場合、1秒後にリトライ
				Thread.sleep(1000);
				try {
					return super.getInfo(genreId, PATH_URL, "Items");
				} catch (WebApplicationException wae) {
					statusCode = wae.getResponse().getStatus();
					if (statusCode == 400 || statusCode == 404) {
						//HTTPステータスコードが400または404の場合、nullを返す
						LOGGER.warning("対象のデータがありませんでした。 ステータスコード：" + statusCode);
						return null;
					} else {
						throw wae;
					}
				}
			} else {
				e.printStackTrace();
				throw e;
			}
		} catch (Exception e) {
			throw e;
		}
	}
	/**
	 * 楽天商品ランキングAPIからジャンル情報を取得し、各ジャンルごとの人気商品10品の情報を
	 * item_rankingテーブルに保存する
	 * @param conn DBのコネクションオブジェクト
	 * @throws SQLException DBアクセス関係の例外
	 * @throws IOException
	 * @throws InterruptedException スレッドの割り込み発生時の例外
	 */
	public void saveItemRanking(Connection conn) throws SQLException, IOException, InterruptedException {

		//item_rankingテーブルのレコードを全件削除
		String sql = "delete from item_ranking";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.executeUpdate();

		//genreテーブルからジャンルIDを取得
		sql = "select genre_id from genre";
		ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		//取得したジャンルID分ループ処理
		while (rs.next()) {
			String genreId = rs.getString("genre_id");
			LOGGER.info("ジャンルID:" + genreId);

			//楽天商品ランキングAPIにアクセスして、商品情報を取得
			JsonNode parentNode = getItemInfo(genreId);
			if (parentNode != null) {
				//順位が10を超えるまで、商品情報をテーブルに保存する
				for (JsonNode childNode : parentNode) {
					int rank = childNode.get("rank").asInt(); //順位
					if (rank > 10) {
						break;
					}
					String itemName = childNode.get("itemName").asText(); //商品名
					LOGGER.info("順位:" + rank + " 商品名:" + itemName);
					sql = "insert into item_ranking values(?,?,?)";
					ps = conn.prepareStatement(sql);
					ps.setInt(1, Integer.parseInt(genreId));
					ps.setInt(2, rank);
					ps.setString(3, itemName);
					ps.executeUpdate();
				}
			}
		}
	}
}

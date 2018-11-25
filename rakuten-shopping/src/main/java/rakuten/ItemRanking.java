package rakuten;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.xml.ws.http.HTTPException;

import com.fasterxml.jackson.databind.JsonNode;

public class ItemRanking extends RakutenAPI {

	//楽天商品ランキングAPIのURL
	public static final String BASE_URL = "https://app.rakuten.co.jp";
	public static final String PATH_URL = "/services/api/IchibaItem/Ranking/20170628";

	//ログ出力用
	private static final Logger LOGGER = Logger.getLogger(ItemRanking.class.getName());

	public JsonNode getItemInfo(String genreId) throws IOException {
		try {
			return super.getInfo(genreId, BASE_URL, PATH_URL, "Items");
		} catch (BadRequestException e) {
			//対象なしの場合、nullで返す
				return null;
		} catch (HTTPException e) {
			LOGGER.severe("ステータスコード：" + e.getStatusCode());
			e.printStackTrace();
			throw e;
		}
	}

	public void saveItemRanking(Connection conn) throws SQLException, IOException {

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
			System.out.println(genreId);
			JsonNode parentNode = getItemInfo(genreId);
			if (parentNode != null) {
				for (JsonNode childNode : parentNode) {
					int rank = childNode.get("rank").asInt();
					if (rank > 10) {
						break;
					}
					String itemName = childNode.get("itemName").asText();
					System.out.println("ジャンルID:" + genreId + "順位:" + rank + " 商品名:" + itemName);
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
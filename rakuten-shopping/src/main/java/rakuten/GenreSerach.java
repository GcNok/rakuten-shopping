package rakuten;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

public class GenreSerach extends RakutenAPI {

	//楽天ジャンル検索APIのパス
	public static final String PATH_URL = "/services/api/IchibaGenre/Search/20140222";

	//ログ出力用
	public static final Logger logger = Logger.getLogger(ItemRanking.class.getName());

	/**
	 * 楽天ジャンル検索APIにアクセスし、取得した情報を返却する
	 * @param genreId ジャンルID
	 * @return 楽天ジャンル検索APIのレスポンス
	 * @throws IOException
	 */
	public JsonNode getGenreInfo(String genreId) throws IOException {
		try {
			return super.getInfo(genreId, PATH_URL, "children");
		} catch (WebApplicationException e) {
			logger.error("ステータスコード：" + e.getResponse().getStatus());
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	/**
	 * ジャンル情報をgenreテーブルに保存する
	 * @param childrenNode ジャンル情報のJSONオブジェクト
	 * @param conn DBのコネクションオブジェクト
	 * @throws IOException
	 * @throws SQLException
	 */
	public void saveGenreInfo(JsonNode childrenNode, Connection conn) throws IOException, SQLException {

		for (JsonNode childNode : childrenNode) {
			int genreLevel = childNode.get("genreLevel").asInt(); //ジャンル階層
			String genreId = childNode.get("genreId").asText(); //ジャンルID
			String genreName = childNode.get("genreName").asText(); //ジャンル名

			//取得したジャンル情報をgenreテーブルに保存
			String sql = "insert into genre values(?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(genreId));
			ps.setString(2, genreName);
			ps.setInt(3, genreLevel);
			ps.executeUpdate();
			logger.debug("***************************************************");
			logger.debug("ジャンルID: " + genreId);
			logger.debug("ジャンル名: " + genreName);
			logger.debug("ジャンル階層：" + genreLevel);
			//ジャンル階層が3未満の場合、再帰的にジャンル情報を取得してDBに保存
			if (genreLevel < 3) {
				childrenNode = getGenreInfo(genreId);
				saveGenreInfo(childrenNode, conn);
			}
		}

	}

}

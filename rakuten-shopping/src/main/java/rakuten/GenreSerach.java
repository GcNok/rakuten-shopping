package rakuten;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import com.fasterxml.jackson.databind.JsonNode;

public class GenreSerach extends RakutenAPI {

	//楽天ジャンル検索APIのURL
	public static final String BASE_URL = "https://app.rakuten.co.jp/";
	public static final String PATH_URL = "/services/api/IchibaGenre/Search/20140222";

	//ログ出力用
	private static final Logger LOGGER = Logger.getLogger(ItemRanking.class.getName());

	public JsonNode getGenreInfo(String genreId) throws IOException {
		try {
			return super.getInfo(genreId, BASE_URL, PATH_URL, "children");
		} catch (WebApplicationException e) {
			LOGGER.severe("ステータスコード：" + e.getResponse().getStatus());


			throw e;
		}
	}

	public void saveGenreInfo(JsonNode childrenNode, Connection conn) throws IOException, SQLException {

		for (JsonNode childNode : childrenNode) {
			int genreLevel = childNode.get("genreLevel").asInt(); //ジャンル階層
			String genreId = childNode.get("genreId").asText(); //ジャンルID

			//取得したジャンル情報をDBに保存
			String sql = "insert into genre values(?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(genreId));
			ps.setString(2, childNode.get("genreName").asText());
			ps.setInt(3, genreLevel);
			ps.executeUpdate();
			LOGGER.info("ジャンル名: " + childNode.get("genreName").asText());
			LOGGER.info("ジャンル階層：" + genreLevel);
			LOGGER.info("***************************************************");

			//ジャンル階層が3未満の場合、再帰的にジャンル情報を取得してDBに保存
			if (genreLevel < 3) {
				childrenNode = getGenreInfo(genreId);
				saveGenreInfo(childrenNode, conn);
			}
		}

	}

}

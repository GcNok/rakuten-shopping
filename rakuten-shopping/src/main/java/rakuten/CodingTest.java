package rakuten;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CodingTest {

	//APIのURL
	public static final String BASE_URL = "https://app.rakuten.co.jp/services/api/IchibaGenre/Search/20140222?applicationId=1026301013779899297&genreId=";

	//ランキングAPIのURL
	public static final String RANKING_BASE_URL = "https://app.rakuten.co.jp/services/api/IchibaItem/Ranking/20170628?applicationId=1026301013779899297&formatVersion=2&genreId=";

	public static void main(String[] args) {
		try {

			//ジャンルID
			String genreId = "0";
			CodingTest codingTest = new CodingTest();
			//ジャンルIDからAPIにアクセスして情報を取得
			JsonNode parentNode = codingTest.getGenreInfo(genreId);

			//ローカルのDBに接続
			String url
	        = "jdbc:mysql://localhost:3306/rakuten_items?characterEncoding=UTF-8&serverTimezone=JST";
	        String user = "root";
	        String pass = "";
	        try{
	        	Connection conn =
		                DriverManager.getConnection(url, user, pass);

	          //APIで取得した値からジャンルを取得して出力
				System.out.println("***************************************************");
				codingTest.outChildGenre(parentNode,conn);
				codingTest.getGenreRank(conn);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            System.out.println("処理が完了しました");
	        }

		} catch (MalformedURLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

	private JsonNode getGenreInfo(String genreId) throws IOException {
		String result = "";
		//ユーザー名を取得するためのAPIにアクセス
		URL url = new URL(BASE_URL + genreId);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.connect();

		//APIで取得した値を読み込み、変数に格納
		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		result = in.readLine();
		con.disconnect();
		in.close();

		//APIで取得した値からジャンルを取得して出力
		ObjectMapper mapper = new ObjectMapper();
		JsonNode parentNode = mapper.readTree(result).get("children");
		return parentNode;
	}

	private JsonNode getRankInfo(String genreId) throws IOException {
		String result = "";
		//ユーザー名を取得するためのAPIにアクセス
		URL url = new URL(RANKING_BASE_URL + genreId);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.connect();

		//APIで取得した値を読み込み、変数に格納
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			result = in.readLine();
			con.disconnect();
			in.close();
		} catch (IOException e) {
			return null;
		}

		//APIで取得した値からジャンルを取得して出力
		ObjectMapper mapper = new ObjectMapper();
		JsonNode parentNode = mapper.readTree(result).get("Items");
		return parentNode;
	}

	private void outChildGenre(JsonNode parentNode,Connection conn) throws IOException, SQLException {
		CodingTest codingTest = new CodingTest();

		for (JsonNode childNode : parentNode) {
			JsonNode genreNode = childNode.get("child");
			//ジャンル階層
			String genreLevel = genreNode.get("genreLevel").asText();
			//ジャンル階層が4の場合、繰り返し終了
			if ("4".equals(genreLevel)) {
				break;
			}
			//取得したジャンル情報をDBに保存
			String genreId = genreNode.get("genreId").asText();
			String sql = "insert into genre values(?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, Integer.parseInt(genreId));
        	ps.setString(2, genreNode.get("genreName").asText());
        	ps.setInt(3, Integer.parseInt(genreLevel));
			ps.executeUpdate();

			System.out.println("ジャンル名: " + genreNode.get("genreName").asText());
			System.out.println("ジャンル階層：" + genreLevel);
			System.out.println("***************************************************");
			parentNode = codingTest.getGenreInfo(genreId);
			outChildGenre(parentNode,conn);
		}

	}

	private void getGenreRank(	Connection conn) throws SQLException, IOException {
		conn.prepareStatement("delete from item_ranking");
		String sql = "select genre_id from genre";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		CodingTest ct = new CodingTest();
		while(rs.next()) {
			String genreId = rs.getString("genre_id");
			JsonNode parentNode = ct.getRankInfo(genreId);
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

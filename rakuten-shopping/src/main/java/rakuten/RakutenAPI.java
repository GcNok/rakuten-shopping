package rakuten;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 継承用クラス
 * @author Naoki
 *
 */
public class RakutenAPI {

	//楽天APIの基本URL
	public static final String BASE_URL = "https://app.rakuten.co.jp/";

	private Client client = ClientBuilder.newClient();

	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * 指定されたURLのAPIにアクセスして、取得したレスポンスを返す
	 * @param genreId ジャンルID
	 * @param pathUrl APIのベースURL以外の部分
	 * @param nodeKey APIで取得したレスポンスに対するノードのキー
	 * @return
	 * @throws IOException
	 */
	public JsonNode getInfo(String genreId, String pathUrl, String nodeKey) throws IOException {

		//リクエスト準備
		WebTarget target = client.target(BASE_URL)
				.path(pathUrl).queryParam("applicationId", "1026301013779899297").queryParam("formatVersion", "2")
				.queryParam("genreId", genreId);
		try {
			//APIにアクセスし、指定された要素配下の情報をJSON形式で返す
			String result = target.request().get(String.class);
			JsonNode childrenNode = mapper.readTree(result).get(nodeKey);
			return childrenNode;
		} catch (Exception e) {
			throw e;
		}
	}
}

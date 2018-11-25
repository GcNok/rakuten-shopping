package rakuten;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.xml.ws.http.HTTPException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RakutenAPI {


	private Client client = ClientBuilder.newClient();

	private ObjectMapper mapper = new ObjectMapper();

	public JsonNode getInfo(String genreId, String baseUrl, String pathUrl, String nodeKey) throws IOException {

		WebTarget target = client.target(baseUrl)
				.path(pathUrl).queryParam("applicationId", "1026301013779899297").queryParam("formatVersion", "2")
				.queryParam("genreId", genreId);
		try {
			String result = target.request().get(String.class);
			//APIで取得した値からジャンルを取得して出力
			JsonNode childrenNode = mapper.readTree(result).get(nodeKey);
			return childrenNode;
		} catch (HTTPException e) {
			// 例外よりレスポンスの内容やステータスコードを確認可能
			throw e;
		}
	}
}

package util;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ImgurUploader implements PictureUploader {
	private static final String IMGUR_CLIENT_ID = "c2e9463f5af7c57";
	private static final String IMGUR_CLIENT_SECRET = "40f05f75d95fa16d6fb75a59638b5220f6eb529c";
	private static final String IMGUR_URL = "https://api.imgur.com/3/upload.json";

	@Override
	public String uploadImage(String fname) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpPost httpPost = new HttpPost(IMGUR_URL);

		System.out.println("Uploading image " + fname);
		try {
			final MultipartEntity entity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			entity.addPart("image", new FileBody(new File(fname)));
			entity.addPart("key", new StringBody(IMGUR_CLIENT_SECRET));
			httpPost.setHeader("Authorization", "Client-ID " + IMGUR_CLIENT_ID);
			httpPost.setEntity(entity);

			final HttpResponse response = httpClient.execute(httpPost,
					localContext);
			final String response_string = EntityUtils.toString(response
					.getEntity());
			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualObj = mapper.readTree(response_string).get("data");
			String link = actualObj.get("link").asText();
			return link;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

import java.io.File;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class UploadImageHTTP {
    public static void sendPost(String url, String imagePath,String userId)
                throws IOException, ClientProtocolException {

                    String responseBody;
                            HttpClient client = new DefaultHttpClient();
                                HttpPost request = new HttpPost(url);

                                    MultipartEntity entity = new MultipartEntityy(
                                                        HttpMultipartMode.BROWSER_COMPATIBLE);

                                        File file = new File(imagePath);
                                            ContentBody encFile = new FileBody(file,"image/png");

                                                entity.addPart("images", entitycFile);
                                                    entity.addPart("UserId", new StringBody(userId));

                                                        requestquest.setEntity(entity);

                                                            ResponseHandler<String> responsehandler     = new BasicResponseHandler();
                                                                responseBody = client.execute(requestquestt, responsehandler);

                                                                    if (responseBody != null && responseBody.length() > 0) {
                                                                                Log.w("TAG", "Response image upload" + responseBodydy);

                                                                                    }
    }
}
        

package controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ocr.Recognizer;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import util.WolframAlphaQuery;
import views.html.index;
import views.html.uploadform;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {
	public static Result index() {
		return ok(index.render("Your new application is ready."));
	}

	/**
	 * Processes a POST-ed image and returns the JSON response
	 * 
	 * @return a JSON response to the user
	 */
	public static Result process() throws IOException {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart image = body.getFile("file");

		if (image != null) {
			ObjectNode result = Json.newObject();
			String fname = image.getFilename();
			String contentType = image.getContentType();
			File file = image.getFile();

			try {
				BufferedImage img = ImageIO.read(file);
				Recognizer r = Recognizer.getSingleton();
				if (r != null) {
					ObjectNode objNode = r.process(img, false);
					result.put("data", objNode);
					String latex = objNode.get("latex").asText();
					ObjectNode wa = new WolframAlphaQuery().query(latex);
					result.put("wolfram", wa);
				}
			} catch (IOException e) {
				System.err.println("IOException reading " + fname + " " + e);
			}
			return ok(result);
		}

		ObjectNode result = Json.newObject();
		result.put("error", "Please upload a file");
		return badRequest(result);
	}

	/**
	 * Shows an extremely basic HTML form to test the /process endpoint with
	 * 
	 * @return a basic HTML form to the user
	 */
	public static Result showProcessForm() {
		return ok(uploadform.render(""));
	}

}

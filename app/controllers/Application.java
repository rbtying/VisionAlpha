package controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

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
import views.html.wolframform;

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
		Map<String, String[]> formdata = body.asFormUrlEncoded();

		FilePart image = body.getFile("file");
		if (image != null) {
			ObjectNode result = Json.newObject();
			String fname = image.getFilename();
			File file = image.getFile();

			try {
				BufferedImage img = ImageIO.read(file);
				Recognizer r = Recognizer.getSingleton();
				if (r != null) {
					boolean showAllImages = false;
					if (formdata.containsKey("showAllImages")) {
						String[] val = formdata.get("showAllImages");
						if (val.length > 0
								&& val[0].toLowerCase().contains("on")) {
							showAllImages = true;
						}
					}

					boolean includeWolframResult = false;
					if (formdata.containsKey("includeWolfram")) {
						String[] val = formdata.get("includeWolfram");
						if (val.length > 0
								&& val[0].toLowerCase().contains("on")) {
							includeWolframResult = true;
						}
					}

					ObjectNode objNode = r.process(img, showAllImages);

					result.put("data", objNode);

					if (includeWolframResult) {
						String latex = objNode.get("latex").asText();
						ObjectNode wa = new WolframAlphaQuery().query(latex);
						result.put("wolfram", wa);
					}
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

	/**
	 * Gets the Wolfram|alpha response (separately from the process result)
	 * 
	 * @return a wolfram alpha response
	 */
	public static Result getWolframResult() {
		Map<String, String[]> formdata = request().body().asFormUrlEncoded();
		ObjectNode result = Json.newObject();
		try {
			String[] latexArr = formdata.get("latex");
			String latex = latexArr[0];
			ObjectNode wa = new WolframAlphaQuery().query(latex);
			result.put("data", wa);
		} catch (Exception e) {
			e.printStackTrace();
			return badRequest(result);
		}
		return ok(result);
	}

	/**
	 * Shows an extremely basic HTML form to test the /evaluate endpoint with
	 * 
	 * @return a basic HTML form to the user
	 */
	public static Result showWolframForm() {
		return ok(wolframform.render(""));
	}

}

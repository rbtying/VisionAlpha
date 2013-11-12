package util;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;

public class WolframAlphaQuery {
	private static final WAEngine engine = new WAEngine();
	private static final String WOLFRAM_APPID = "P7VYW4-68A3854LYU";

	public ObjectNode query(String latex) {
		ObjectNode results = Json.newObject();
		engine.setAppID(WOLFRAM_APPID);
		// engine.addFormat(format)

		WAQuery query = engine.createQuery();
		query.setInput(latex);
		try {
			System.out.println("Query URL: " + query.toWebsiteURL());
			WAQueryResult qresult = engine.performQuery(query);

			if (qresult.isError()) {
				results.put("errorcode", qresult.getErrorCode());
				results.put("errormessage", qresult.getErrorMessage());
			} else if (!qresult.isSuccess()) {
				results.put("errormessage", "No results available");
			} else {
				// success
				for (WAPod pod : qresult.getPods()) {
					if (!pod.isError()) {
						ObjectNode podNode = Json.newObject();

						for (WASubpod subpod : pod.getSubpods()) {
							ObjectNode subPodNode = Json.newObject();

							for (Object element : subpod.getContents()) {
								if (element instanceof WAImage) {
									WAImage img = (WAImage) element;
									subPodNode.put("img", img.getURL());
								} else if (element instanceof WAPlainText) {
									WAPlainText text = (WAPlainText) element;
									subPodNode.put("plaintext", text.getText());
								}
							}
							podNode.put(subpod.getTitle(), subPodNode);
						}
						results.put(pod.getTitle(), podNode);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}
}

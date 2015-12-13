package fr.techad.sonar.gerrit;

import com.google.gson.stream.JsonWriter;
import fr.techad.sonar.GerritPluginException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public abstract class GerritFacade {
	private static final String MAVEN_ENTRY_REGEX = ".*src/";
	private static final String ERROR_FORMAT = "Error formatting review";

	/**
	 * @return sonarLongName to gerritFileName map
	 */
	public abstract Map<String, String> listFiles() throws GerritPluginException;

	public abstract void setReview(ReviewInput reviewInput) throws GerritPluginException;

	String formatReview(ReviewInput reviewInput) throws GerritPluginException {
		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(stringWriter);

		try {
			jsonWriter.beginObject();
			jsonWriter.name("message").value(reviewInput.getMessage());

			if (!reviewInput.getLabels().isEmpty()) {
				jsonWriter.name("labels").beginObject();
				for (String label : reviewInput.getLabels().keySet()) {
					jsonWriter.name(label).value(reviewInput.getLabels().get(label));
				}
				jsonWriter.endObject();
			}

			if (!reviewInput.getComments().isEmpty()) {
				jsonWriter.name("comments").beginObject();
				for (String fileName : reviewInput.getComments().keySet()) {
					if (!reviewInput.getComments().isEmpty()) {
						jsonWriter.name(fileName).beginArray();
						for (ReviewFileComment rfc : reviewInput.getComments().get(fileName)) {
							jsonWriter.beginObject();
							jsonWriter.name("line").value(((ReviewLineComment) rfc).getLine());
							jsonWriter.name("message").value(rfc.getMessage());
							jsonWriter.endObject();
						}
						jsonWriter.endArray();
					}
				}
				jsonWriter.endObject();
			}
			jsonWriter.endObject();
			jsonWriter.close();
		} catch (IOException e) {
			throw new GerritPluginException(ERROR_FORMAT, e);
		}

		return stringWriter.toString();
	}

	String formatCoverage(PatchCoverageInput patchCoverageInput) throws GerritPluginException {
		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(stringWriter);

		try {
			jsonWriter.beginObject();
			if (!patchCoverageInput.getCoverage().isEmpty()) {
				jsonWriter.name("coverage").beginObject();
				for (Map.Entry<String, FileCoverageInput> fileCoverage : patchCoverageInput.getCoverage().entrySet()) {
					jsonWriter.name(fileCoverage.getKey()).beginObject();
					FileCoverageInput fileCoverageInput = fileCoverage.getValue();
					writeMap(jsonWriter, "hits", fileCoverageInput.getHits());
					writeMap(jsonWriter, "conditions", fileCoverageInput.getConditions());
					writeMap(jsonWriter, "covered_conditions", fileCoverageInput.getCoveredConditions());
					jsonWriter.endObject();
				}
				jsonWriter.endObject();
			}
			jsonWriter.endObject();
			jsonWriter.close();
		} catch (IOException e) {
			throw new GerritPluginException(ERROR_FORMAT, e);
		}

		return stringWriter.toString();
	}

	private void writeMap(JsonWriter jsonWriter, String name, Map<Integer, Integer> map) throws IOException {
		jsonWriter.name(name).beginObject();
		for (Map.Entry<Integer, Integer> entry : map.entrySet())
        {
            jsonWriter.name(entry.getKey().toString()).value(entry.getValue());
        }
		jsonWriter.endObject();
	}

	protected String parseFileName(@NotNull String fileName) {
		return fileName.replaceFirst(MAVEN_ENTRY_REGEX, "src/");
	}

	public abstract void setCoverage(PatchCoverageInput patchCoverageInput) throws GerritPluginException;
}

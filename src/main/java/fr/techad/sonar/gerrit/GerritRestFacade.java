package fr.techad.sonar.gerrit;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.coverage.PatchCoverageInput;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonObject;
public class GerritRestFacade extends GerritFacade {
    private static final Logger LOG = Loggers.get(GerritRestFacade.class);
    private static final String JSON_RESPONSE_PREFIX = ")]}'";
    private static final String COMMIT_MSG = "/COMMIT_MSG";
    private static final String ERROR_LISTING = "Error listing files";
    private static final String ERROR_SETTING = "Error setting review";
    private final GerritConnector gerritConnector;
    private Map<String, String> gerritFileList = new HashMap<String, String>();

    public GerritRestFacade(GerritConnectorFactory gerritConnectorFactory) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritRestFacade");
        this.gerritConnector = gerritConnectorFactory.getConnector();
    }

    @NotNull
    @Override
    public Map<String, String> listFiles() throws GerritPluginException {
        if (!gerritFileList.isEmpty()) {
            LOG.debug("[GERRIT PLUGIN] File list already filled. Not calling Gerrit.");
        } else {
            fillListFilesFomGerrit();
        }
        return Collections.unmodifiableMap(gerritFileList);
    }

    @Override
    public void setReview(@NotNull ReviewInput reviewInput) throws GerritPluginException {
        try {
            gerritConnector.setReview(formatReview(reviewInput));
        } catch (IOException e) {
            throw new GerritPluginException(ERROR_SETTING, e);
        }
    }

    @NotNull
    protected String trimResponse(@NotNull String response) {
        return StringUtils.replaceOnce(response, JSON_RESPONSE_PREFIX, "");
    }

    private void fillListFilesFomGerrit() throws GerritPluginException {
        try {
            String rawJsonString = gerritConnector.listFiles();
            String jsonString = trimResponse(rawJsonString);
            JsonElement rootJsonElement = new JsonParser().parse(jsonString);
            for (Entry<String, JsonElement> fileList : rootJsonElement.getAsJsonObject().entrySet()) {
                String fileName = fileList.getKey();
                if (COMMIT_MSG.equals(fileName)) {
                    continue;
                }

                JsonObject jsonObject = fileList.getValue().getAsJsonObject();
                if (jsonObject.has("status") && isMarkAsDeleted(jsonObject)) {
                    LOG.debug("[GERRIT PLUGIN] File is marked as deleted, won't comment.");
                    continue;
                }

                gerritFileList.put(parseFileName(fileName), fileName);
            }
        } catch (IOException e) {
            throw new GerritPluginException(ERROR_LISTING, e);
        }
    }

    @Override
	public void setCoverage(@Nullable PatchCoverageInput patchCoverageInput) throws GerritPluginException {
		if (patchCoverageInput == null) {
			return;
		}
		try {
			gerritConnector.setCoverage(formatCoverage(patchCoverageInput));
		} catch (IOException e) {
			throw new GerritPluginException(ERROR_SETTING, e);
		}
	}

    private boolean isMarkAsDeleted(JsonObject jsonObject) {
        return jsonObject.get("status").getAsCharacter() == 'D';
    }
}

package fr.techad.sonar;

import org.sonar.api.batch.bootstrap.ProjectBuilder;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.ReviewInput;

public class GerritProjectBuilder extends ProjectBuilder {
	private static final Logger LOG = Loggers.get(GerritProjectBuilder.class);
	private final GerritConfiguration gerritConfiguration;
	private final GerritFacade gerritFacade;

	public GerritProjectBuilder(GerritConfiguration gerritConfiguration, GerritFacade gerritFacade) {
		LOG.debug("[GERRIT PLUGIN] Instanciating GerritProjectBuilder");
		this.gerritConfiguration = gerritConfiguration;
		this.gerritFacade = gerritFacade;
	}

	@Override
	public void build(Context context) {
		if (!gerritConfiguration.isEnabled()) {
			return;
		}

		ReviewInput ri = new ReviewInput();
		ri.setValueAndLabel(0, gerritConfiguration.getLabel());
		ri.setMessage("Sonar review in progress …");
		
		try {
			gerritFacade.setReview(ri);
		} catch (GerritPluginException e) {
			LOG.error("[GERRIT PLUGIN] Sending initial status failed", e);
		}
	}
}
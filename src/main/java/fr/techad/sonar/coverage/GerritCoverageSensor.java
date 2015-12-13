package fr.techad.sonar.coverage;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.GerritFacadeFactory;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.api.utils.KeyValueFormat;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.Collections;
import java.util.Map;

/**
 * Technically it is not a sensor as it does not record anything in Sonar.
 * We were originally using a PostJob to browse through resources, however
 * SensorContext.getChildren(resource) where resource is a module would not
 * return the resources of that module, which prevented the PostJob from
 * analyzing code coverage. So instead we use this Sensor with a POST phase
 * to run after code coverage sensors.
 */
@Phase(name = Phase.Name.POST)
public class GerritCoverageSensor implements Sensor
{
    private static final Logger       LOG = Loggers.get(GerritCoverageSensor.class);
    private final GerritConfiguration gerritConfiguration;
    private final PatchCoverageInput  patchCoverageInput;
    private GerritFacade              gerritFacade;

    public GerritCoverageSensor(GerritFacadeFactory gerritFacadeFactory, GerritConfiguration gerritConfiguration, PatchCoverageInput patchCoverageInput)
    {
        this.gerritFacade = gerritFacadeFactory.getFacade();
        this.gerritConfiguration = gerritConfiguration;
        this.patchCoverageInput = patchCoverageInput;
    }

    @Override
    public void analyse(Project module, SensorContext context)
    {
        Map<String, String> committedFiles;
        try
        {
            committedFiles = gerritFacade.listFiles();
        }
        catch (GerritPluginException e)
        {
            LOG.error("Listing committed files failed", e);
            return;
        }
        computeCoverageInput(committedFiles, module, context);
    }

    private void computeCoverageInput(Map<String, String> committedFiles, Resource resource, SensorContext context)
    {
        computeCoverageInput(committedFiles, patchCoverageInput, resource, context);
    }

    private void computeCoverageInput(Map<String, String> committedFiles, PatchCoverageInput patchCoverageInput, Resource resource, SensorContext context)
    {
        for (Resource childResource : context.getChildren(resource))
        {
            String filePath = committedFiles.get(childResource.getLongName());
            if (ResourceUtils.isFile(childResource) && filePath != null)
            {
                Map<Integer, Integer> lineHits = getMap(context, childResource, CoreMetrics.COVERAGE_LINE_HITS_DATA);
                Map<Integer, Integer> lineConditions = getMap(context, childResource, CoreMetrics.CONDITIONS_BY_LINE);
                Map<Integer, Integer> lineCoveredConditions = getMap(context, childResource, CoreMetrics.COVERED_CONDITIONS_BY_LINE);

                for (Map.Entry<Integer, Integer> hits : lineHits.entrySet())
                {
                    patchCoverageInput.setLineCoverage(
                        filePath,
                        hits.getKey(),
                        hits.getValue(),
                        lineConditions.get(hits.getKey()),
                        lineCoveredConditions.get(hits.getKey()));
                }
            }
            else
            {
                computeCoverageInput(committedFiles, patchCoverageInput, childResource, context);
            }
        }
    }

    private Map<Integer, Integer> getMap(SensorContext context, Resource childResource, Metric<String> coverageLineHitsData)
    {
        Measure<String> measure = context.getMeasure(childResource, coverageLineHitsData);
        if (measure == null)
        {
            return Collections.emptyMap();
        }
        return KeyValueFormat.parseIntInt(measure.getData());
    }

    @Override
    public boolean shouldExecuteOnProject(Project project)
    {
        return gerritConfiguration.isEnabled();
    }
}

package fr.techad.sonar.coverage;

import fr.techad.sonar.gerrit.FileCoverageInput;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.InstantiationStrategy;

import java.util.HashMap;
import java.util.Map;

@BatchSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class PatchCoverageInput
{
    private Map<String, FileCoverageInput> coverage = new HashMap<>();

    public Map<String, FileCoverageInput> getCoverage()
    {
        return coverage;
    }

    @Override
    public String toString()
    {
        return "PatchCoverageInput [" + "coverage=" + coverage + ']';
    }

    public void setLineCoverage(String filePath, Integer lineNumber, Integer hits, Integer conditions, Integer coveredConditions)
    {
        FileCoverageInput fileCoverage = coverage.get(filePath);
        if (fileCoverage == null)
        {
            fileCoverage = new FileCoverageInput();
            coverage.put(filePath, fileCoverage);
        }
        fileCoverage.setLineCoverage(lineNumber, hits, conditions, coveredConditions);
    }
}

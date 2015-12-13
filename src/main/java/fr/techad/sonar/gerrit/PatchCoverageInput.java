package fr.techad.sonar.gerrit;

import java.util.HashMap;
import java.util.Map;

public class PatchCoverageInput
{
    private Map<String, FileCoverageInput> coverage = new HashMap<>();

    public Map<String, FileCoverageInput> getCoverage() {
        return coverage;
    }

    @Override
    public String toString() {
        return "PatchCoverageInput [" + "coverage=" + coverage + ']';
    }

    public void setLineCoverage(String filePath, Integer lineNumber, Integer hits, Integer conditions, Integer coveredConditions)
    {
        FileCoverageInput fileCoverage = coverage.get(filePath);
        if (fileCoverage == null) {
            fileCoverage = new FileCoverageInput();
            coverage.put(filePath, fileCoverage);
        }
        fileCoverage.setLineCoverage(lineNumber, hits, conditions, coveredConditions);
    }
}

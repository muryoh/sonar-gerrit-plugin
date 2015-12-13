package fr.techad.sonar.gerrit;

import java.util.HashMap;
import java.util.Map;

public class FileCoverageInput
{
    private Map<Integer, Integer> hits = new HashMap<>();
    private Map<Integer, Integer> conditions = new HashMap<>();
    private Map<Integer, Integer> coveredConditions = new HashMap<>();

    @Override
    public String toString() {
        return "PatchCoverageInput [" + "hits=" + hits + ", conditions=" + conditions + ", coveredConditions=" + coveredConditions + "]";
    }

    public void setLineCoverage(Integer lineNumber, Integer lineHits, Integer lineConditions, Integer lineCoveredConditions) {
        hits.put(lineNumber, lineHits);
        if (lineConditions != null) {
            conditions.put(lineNumber, lineConditions);
        }
        if (lineCoveredConditions != null) {
            coveredConditions.put(lineNumber, lineCoveredConditions);
        }
    }

    public Map<Integer, Integer> getHits() {
        return hits;
    }

    public Map<Integer, Integer> getConditions() {
        return conditions;
    }

    public Map<Integer, Integer> getCoveredConditions() {
        return coveredConditions;
    }
}

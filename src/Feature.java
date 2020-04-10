/**
 * class that represents a feature Xi
 */
public class Feature implements Comparable<Feature>{
    private String featureName;
    private double informationGain;

    public Feature(String name, double IG)
    {
        this.featureName = name;
        this.informationGain = IG;
    }

    public String getFeatureName()
    {
        return this.featureName;
    }

    public double getInformationGain()
    {
        return this.informationGain;
    }

    public void setFeatureName(String name)
    {
        this.featureName=name;
    }

    public void setInformationGain(double IG)
    {
        this.informationGain=IG;
    }

    @Override
    public int compareTo(Feature f) {
        return Double.compare(this.informationGain,f.getInformationGain());
    } // we compare based on the IG for each feature. It is needed for comparison in the lists.
}

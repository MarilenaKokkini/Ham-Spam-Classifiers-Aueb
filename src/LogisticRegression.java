import java.util.*;
public class LogisticRegression
{
    private ArrayList<Feature> features; //features (words)
    private ArrayList<eMail> trainingData; //training mails
    private List<List<Integer>> featureVectors; // training mails as vectors.
    private List<Double> w; //weights.
    private double learningRate; // learning rate.
    private double lambda; // regularization term.
    private int epochs; // iterations.

    LogisticRegression(ArrayList<eMail> trainingData,ArrayList<Feature> features,int epochs,double learningRate,double lambda)
    {
        this.features = new ArrayList<>(features);
        this.trainingData = new ArrayList<>(trainingData);
        this.w = new ArrayList<>();
        this.learningRate = learningRate;
        this.lambda = lambda;
        this.epochs=epochs;
        this.featureVectors=new ArrayList<>();
        for(eMail m : this.trainingData)
        {
            List<Integer> tmp = this.createFeatureVector(m);
            this.featureVectors.add(tmp);
        }
        //System.out.println("features are: " + this.features.size() + " and columns of X are: " + this.featureVectors.get(0).size());

    }

    /**
     * sigmoid function for decision.
     * @param w is the array of weights.
     * @param x is the array of a mail.
     * @return probability of being spam or ham.
     */
    private double sigmoid(List<Double> w, List<Integer> x)
    {
        double res=0.0;
        for(int i=0; i<w.size(); i++)
        {
            res = res + w.get(i)*x.get(i);
        }
        return 1.0 / ( 1.0 + Math.exp(-res) );
    }

    /**
     * creates the feature vector of the mail (and adds bias).
     * @param mail is the mail to be converted.
     * @return feature vector as int array.
     */
    private List<Integer> createFeatureVector(eMail mail)
    {
        //int[] featVec = new int[this.features.size()+1]; // extra column for bias.
        List<Integer> featVec = new ArrayList<>();
        String content = mail.getContent().toUpperCase().substring(9);
        HashSet<String> tokens = new HashSet<>(Arrays.asList(content.split("\\s+")));

        for (Feature feature : this.features) {
            if (tokens.contains(feature.getFeatureName())) featVec.add(1);
            else featVec.add(0);
        }

        featVec.add(0,1); // extra column in the beginning for bias.
        return featVec;
    }

    /**
     * training of the algorithm.
     * Stochastic Gradient Ascent with regularization term.
     */
    public void train()
    {
        Random r = new Random();
        for(int i=0; i<this.featureVectors.get(0).size(); i++) // dimensions: columns of X array.
        {
            this.w.add(r.nextDouble()); //begin with random weights.
        }

        for(int i=0; i<this.epochs; i++)
        {
            double s = .0; // log likelihood. We want to maximize this.
            double lw;
            for(int j=0; j<this.trainingData.size(); j++) //for every training example.
            {
                int yi;
                List<Integer> x = this.featureVectors.get(j); // feature vector with bias.
                if(this.trainingData.get(j).isSpam())
                {
                    yi=1;
                    lw = Math.log(this.sigmoid(this.w,x));
                }
                else
                {
                    yi=0;
                    lw = Math.log(1.0 - this.sigmoid(this.w,x));
                }
                double norm =.0;
                for(double w : this.w)
                {
                    norm = norm + w*w;
                }
                s = s + lw - this.lambda * norm; // regularized.
                double tmp = yi - this.sigmoid(this.w,x);
                for(int l=0; l<this.w.size(); l++)
                {
                    this.w.set(l , (1-2*this.lambda*this.learningRate)*this.w.get(l) + this.learningRate * tmp * x.get(l));//updating the weights with the derivative.
                }
            }
            //System.out.println("epoch: " + i + " likelihood: " + s);

        }

    }

    /**
     * classify a mail.
     * @param mail the mail to be classified.
     * @return true if spam, false if ham.
     */
    public boolean classifyMail(eMail mail)
    {
        List<Integer> x = this.createFeatureVector(mail); // feature vector with bias.
        double decision = this.sigmoid(this.w,x);
        return decision >= 0.5;
    }

}

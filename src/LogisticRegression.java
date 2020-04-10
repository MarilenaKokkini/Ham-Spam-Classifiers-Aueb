import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class LogisticRegression
{
    private ArrayList<Feature> features; //features (words)
    private ArrayList<eMail> trainingData; //training mails
    private ArrayList<int[]> featureVectors; // training mails as vectors.
    private double[] w; //weights.
    private double learningRate; // learning rate.
    private double lambda; // regularization term.
    private int epochs; // iterations.

    LogisticRegression(ArrayList<eMail> trainingData,ArrayList<Feature> features,int epochs,double learningRate,double lambda)
    {
        this.features = new ArrayList<>(features);
        this.trainingData = new ArrayList<>(trainingData);
        this.w = new double[features.size()];
        this.learningRate = learningRate;
        this.lambda = lambda;
        this.epochs=epochs;
        this.featureVectors=new ArrayList<>();
        for(eMail m : this.trainingData)
        {
            int[] tmp = this.createFeatureVector(m);
            this.featureVectors.add(tmp);
        }

    }

    LogisticRegression(ArrayList<eMail> trainingData,ArrayList<Feature> features,int epochs)
    {
        this.features = new ArrayList<>(features);
        this.trainingData = new ArrayList<>(trainingData);
        this.w = new double[features.size()];
        this.learningRate = 0.1;
        this.lambda = 0.1;
        this.epochs=epochs;
        this.featureVectors=new ArrayList<>();
        for(eMail m : this.trainingData)
        {
            int[] tmp = this.createFeatureVector(m);
            this.featureVectors.add(tmp);
        }

    }

    /**
     * sigmoid function for decision.
     * @param w is the array of weights.
     * @param x is the array of a mail.
     * @return probability of being spam or ham.
     */
    private double sigmoid(double[] w, int[] x)
    {
        double res=0.0;
        for(int i=0; i<w.length; i++)
        {
            res = res + w[i]*x[i];
        }
        return 1.0 / ( 1.0 + Math.exp(-res) );
    }

    /**
     * creates the feature vector of the mail (and adds bias).
     * @param mail is the mail to be converted.
     * @return feature vector as int array.
     */
    private int[] createFeatureVector(eMail mail)
    {
        int[] featVec = new int[this.features.size()+1]; // extra column for bias.
        String content = mail.getContent().toUpperCase().substring(9);
        HashSet<String> tokens = new HashSet<String>(Arrays.asList(content.split("\\s+")));

        for(int i=0; i<this.features.size(); i++)
        {
            if(tokens.contains(this.features.get(i).getFeatureName())) featVec[i]=1;
            else featVec[i]=0;
        }

        featVec[featVec.length-1]=1; //bias.
        return featVec;
    }

    /**
     * training of the algorithm.
     * Stochastic Gradient Ascent.
     */
    public void train()
    {
        Random r = new Random();
        for(int i=0; i<this.w.length; i++)
        {
            this.w[i] = r.nextDouble(); //begin with random weights.
        }

        for(int i=0; i<this.epochs; i++)
        {
            double s = .0; // log likelihood. We want to maximize that.
            double lw;
            for(int j=0; j<this.trainingData.size(); j++)
            {
                int yi;
                int[] x = this.featureVectors.get(j); // feature vector with bias.
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
                    norm = norm + Math.pow(w,2);
                }
                s = s + lw - this.lambda * norm; // regularized.
                double tmp = yi - this.sigmoid(w,x);
                for(int l=0; l<this.w.length; l++)
                {
                    this.w[l] = this.w[l] + this.learningRate * tmp * x[l]; //updating the weights.
                }
            }
            System.out.println("epoch: " + i + " likelihood: " + s);

        }

    }

    /**
     * classify a mail.
     * @param mail the mail to be classified.
     * @return true if spam, false if ham.
     */
    public boolean classifyMail(eMail mail)
    {
        int[] x = this.createFeatureVector(mail); // feature vector with bias.
        double decision = this.sigmoid(this.w,x);
        return decision >= 0.5;
    }



}

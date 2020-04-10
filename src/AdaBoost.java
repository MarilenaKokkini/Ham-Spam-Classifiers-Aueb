import java.util.*;

/**
 * variables are based on lecture's slides.
 */
public class AdaBoost {

    private int M; // number of iterations.
    // weak learner.
    private double[] w; //vector of weights for the training mails. --> initially, all 1/trainingData.size()
    private DecisionStump[] h; //vector of hypotheses.
    private ArrayList<eMail> trainingData; // the training mails.
    private double[] z; // weight for every stump.(hypothesis)
    private ArrayList<Feature> features; //list of features. (we need them for IG)
    private int[][] bernoulliTable; // table with 0s and 1s for the features. We need it for the training of the Decision Stump.


    public AdaBoost(ArrayList<eMail> trainingData, ArrayList<Feature> features, int M) {
        this.trainingData = new ArrayList<eMail>(trainingData);
        this.M = M;
        this.w = new double[trainingData.size()];
        this.h = new DecisionStump[M];
        this.z = new double[M];
        for (int i = 0; i < trainingData.size(); i++) {
            this.w[i] = (1.0 / trainingData.size()); // initially, all weights are 1/N.
        }
        this.features= new ArrayList<Feature>(features);
        this.bernoulliTable = DataHandling.fillBernoulliTable(trainingData,features);
    }

    /**
     *
     * @param w is the weights array
     * @return the array of the weights that all have sum=1 now.
     */
    private double[] normalize(double[] w) {
        double[] newWeights = new double[w.length];
        double total = 0;
        for (int i = 0; i < w.length; i++) total += w[i];

        for (int i = 0; i < w.length; i++) newWeights[i] = w[i] / total;

        return newWeights;
    }

    /**
     *
     * BASED ON THE LECTURE'S SLIDES. ADABOOST TRAINING.
     *
     *
     */
    public void train()
    {
        for(int m=0; m<M; m++)
        {
            // we need the best feature to give it to the decision stump.
            Feature bestFeature = discoverBestFeatureWithIG(this.features,this.bernoulliTable,this.w);
            // learn a new hypothesis with a decision stump.
            this.h[m] = new AdaBoost.DecisionStump(bestFeature);
            this.h[m].train();
            double error=0.0;
            for(int j=0; j<this.trainingData.size(); j++)
            {
                if(this.h[m].classifyMail(this.trainingData.get(j)) != this.trainingData.get(j).isSpam()) error = error+this.w[j]; // compute error of new hypothesis. Sum of w is 1.
            }
            for(int j=0; j<this.trainingData.size(); j++)
            {
                if(this.h[m].classifyMail(this.trainingData.get(j)) == this.trainingData.get(j).isSpam()) this.w[j] = this.w[j] * (error/(1-error));
            }

            this.w = this.normalize(this.w); // so that they have sum=1.
            this.z[m] = DataHandling.log2( (1-error)/error );
        }
    }

    /**
     *
     * //@param mail is the mail to be classified by the algorithm.
     * @return true if the mail is spam or false if the mail is ham.
     */
    public boolean classifyMail(eMail mail)
    {
        double weightSpam=0.0;
        double weightHam=0.0;

        for(int i=0; i< h.length; i++)
        {
            if(this.h[i].classifyMail(mail)) // classification of the decision stump.
            {
                weightSpam += z[i]; // add the weight of the weak learner in the spam category.
            }
            else weightHam += z[i]; // add the weight of the weak learner in the ham category.
        }

        return weightSpam>weightHam;
    }


    /**
     *
     * we need a new method because we have the weights of the mails, so we take these into account for the calculations.
     *
     * @param features is the list of features.
     * @param table is the bernoulli table.
     * @param w is the weights of the mails.
     * @return the best feature based on the weights.
     */

    private static Feature discoverBestFeatureWithIG(ArrayList<Feature> features, int[][] table, double[] w)
    {
        double spams=0.0;
        for(int i=0; i<table.length; i++)
        {
            if(table[i][table[0].length-1]==1) spams+=w[i];
        }

        /**
         * beginning computations for Information Gain for every feature.
         *    formula :  IG(C,X) = H(C) - Sum_for_existence_or_not( P(X=x)*H(C|X=x) ).  //existence=0/1;
         */

        // IG(C,X) --> information gain for every feature.
        double[] IG = new double[features.size()];

        //formula members calculation.

        // P(C=1) --> probability of spam.
        //double PC1 = spams / ((table.length)*1.0);
        double PC1=spams;

        //P(C=0) --> probability of not spam.
        double PC0 = 1.0-PC1;

        // basic entropy for the formula
        double HC = DataHandling.entropy(PC1);


        /**
         * P(X=1) --> probability of a feature existence. // 1 if exists, 0 if not
         * P(X=0) --> 1 - existenceProb[i]
         */

        double[] existenceProbabilities = new double[features.size()];

        /**
         *  P(C=1|X=1) --> probability of being spam if X feature exists.
         *  P(C=0|X=1) --> 1 - spamProbabilityX1
         */

        double[] spamProbabilityX1 = new double[features.size()];

        /**
         * P(C=1|X=0) --> probability of being spam if X feature does not exist.
         * P(C=0|X=0) --> 1- spamProbabilityX0
         */

        double[] spamProbabilityX0 = new double[features.size()];

        /**
         *  H(C=1| X=1 ) --> entropy for C=1 given that X=1.
         *  H(C=0|X=1) --> 1 - entropyC1X1
         */

        double[] entropyC1X1 = new double[features.size()];

        /**
         * H(C=1|X=0) --> entropy for C=1 given that X=0.
         * H(C=0|X=0) --> 1 - entropyC1X0
         */

        double[] entropyC1X0 = new double[features.size()];



        for(int j = 0; j<features.size(); j++)
        {
            double cX1=0; // count for every feature in how many mails it exists.
            double cC1X1=0; //count how many mails are spam given that X=1.
            double cC1X0=0; //count how many mails are spam given that X=0.
            for(int i=0; i<table.length; i++)
            {
                //for P(X=1)
                if(table[i][j]==1) cX1+=w[i];

                //for P(C=1|X=1)
                if(table[i][j]==1 && table[i][features.size()]==1) cC1X1+=w[i];

                //for P(C=1|X=0)
                if(table[i][j]==0 && table[i][features.size()]==1) cC1X0+=w[i];
            }
           //existenceProbabilities[j] = cX1 / (table.length*1.0 );
            existenceProbabilities[j] = cX1 ;

            if(cX1==0) spamProbabilityX1[j]=0;
            else spamProbabilityX1[j]= cC1X1 / (cX1*1.0 );

            if(cX1==1) spamProbabilityX0[j]=0; //avoid division with 0.
            else spamProbabilityX0[j] = cC1X0 / ((1.0-cX1)*1.0); //now, we want 1.0-cX1 because cX1 will be at most 1.


            entropyC1X1[j] = DataHandling.entropy(spamProbabilityX1[j]);
            entropyC1X0[j] = DataHandling.entropy(spamProbabilityX0[j]);

            //finally, calculating the IG

            IG[j] = HC - ( (existenceProbabilities[j]*entropyC1X1[j]) + (1-existenceProbabilities[j])*entropyC1X0[j] );

        }

        // finding the best features based on the size
        ArrayList<Feature> bestFeatures = new ArrayList<Feature>();
        for(int i=0; i<features.size(); i++)
        {

            bestFeatures.add(new Feature(features.get(i).getFeatureName(),IG[i]));
        }

        // returning the best feature of the best features based on IG.
        //System.out.println(Collections.max(bestFeatures).getFeatureName());
        return Collections.max(bestFeatures);


    }

    class DecisionStump
    {
        private Feature checkingFeature; // feature that will be controlled by the tree.
        private boolean leftChild; // mail has the feature. --> Then , check P(C=1|X=1) > P(C=0|X=1) for ham/spam.
        private boolean rightChild; // mail does not have the feature. Then, check P(C=1|X=0) > P(C=0|X=0) for ham/spam.


        DecisionStump(Feature checkingFeature)
        {
            this.checkingFeature=checkingFeature;
        }

        /**
         * training method for the tree. We check the probabilities for spam/ham regarding if a mail contains the checking feature or not!
         * if it does, we calculate probabilities for the left child, otherwise for the right child.
         *
         *
         */
        private void train()
        {
            // P(X=1) and P(X=0) = 1 - P(X=1).
            double PX1 =0.0;
            //P(C=1 /\ X=1)
            double PC1andX1=0.0;
            // P(C=1 /\ X=0).
            double PC1andX0 = 0.0;

            int index=0;
            for(int i=0; i<features.size(); i++)
            {
                if(features.get(i).getFeatureName().equalsIgnoreCase(this.checkingFeature.getFeatureName())) {
                    index = i;
                    break;
                }
            }

            for(int i=0; i<bernoulliTable.length; i++) // for each mail.
            {

                if(bernoulliTable[i][index]==1) //if the feature exists in the mail
                {
                    PX1 += w[i]; // we add the weight of the mail to the probability. It's like the mail is present w[i] times in the data set.
                    if(bernoulliTable[i][bernoulliTable[0].length-1]==1) // if the mail is spam
                    {
                        // P(C=1 /\ X=1 )
                        PC1andX1 += w[i]; // we add the weight of the mail to the probability. It's like the mail is present w[i] times in the data set.
                    }
                }
                else
                {
                    if(bernoulliTable[i][bernoulliTable[0].length-1]==1)
                    {
                        // P(C=1 /\ X=0 )
                        PC1andX0 += w[i]; // we add the weight of the mail to the probability. It's like the mail is present w[i] times in the data set.
                    }
                }
            }

            // P(C=1|X=1) = P(C=1 /\ Χ=1 ) / P(X=1) and P(C=0|X=1) = 1 - P(C=1|X=1).
            double PC1X1 = PC1andX1 / PX1;

            // P(C=0|X=1)
            double PC0X1 = 1.0 - PC1X1;

            //P(X=0)
            double PX0 = 1.0 - PX1;

            // P(C=1|X=0) = P(C=1 /\ X=0 ) / P(X=0) and P(C=0|X=0) = 1 - P(C=1|X=0).
            double PC1X0 = PC1andX0/PX0;

            //P(C=0|X=0)
            double PC0X0 = 1.0 - PC1X0;


            this.leftChild = PC1X1 > PC0X1; // most probable category (given that X=1).
            this.rightChild = PC1X0 > PC0X0; // most probable category (given that X=0).
        }

        /**
         *
         * @param mail is the mail to be classified
         * @return the left child if the mail contains the feature or the right child if the mail does not. In each child, we have assigned
         *          the probability of it being spam or ham based on the training data set.
         */
        private boolean classifyMail(eMail mail)
        {
            if(mail.getContent().toUpperCase().substring(9).contains(this.checkingFeature.getFeatureName()))
            {
                return this.leftChild; // there is a probability of being ham/spam from above.
            }
            else return this.rightChild; // there is a probability of being ham/spam from above.
        }
    }





}

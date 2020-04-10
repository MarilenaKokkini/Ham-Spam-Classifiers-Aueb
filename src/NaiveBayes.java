import java.util.*;

public class NaiveBayes
{
    // 2D array for calculating the probabilities
    private int[][] multivariateBernoulliValues;

    // features list.
    private ArrayList<Feature> features;

    /*
    tables for probabilities that will be calculated during the training of the algorithm.
    we want tables because we have many features ( Xi )
     */

    // P( Xi=1 | C=1)
    // P(Xi=0 | C=1 ) = 1 - P ( Xi = 1 | C=1 )
    private double[] probabilityX1C1; // probability of Xi = 1 given that C=1.

    // P( Xi=1 | C=0)
    // P(Xi=0 | C=0 ) = 1 - P ( Xi = 1 | C=0 )
    private double[] probabilityX1C0; // probability of Xi = 1 given that C=0.



    public NaiveBayes(ArrayList<Feature> featuresList)
    {
        this.features = new ArrayList<Feature>(featuresList);
        this.multivariateBernoulliValues = DataHandling.fillBernoulliTable(DataHandling.getTrainingData(),this.features);
        this.probabilityX1C0 = new double[this.features.size()];
        this.probabilityX1C1 = new double[this.features.size()];
    }

    public int[][] getMultivariateBernoulliValues(){
        return this.multivariateBernoulliValues;
    }


    /**
     * training the algorithm. we calculate the conditional probabilities tables for each category
     *
     */
    public void train()
    {
        int rows = this.multivariateBernoulliValues.length;
        int columns = this.multivariateBernoulliValues[0].length;

        for(int j=0; j<columns-1; j++)
        {
            int cX1C1=0; // count how many Xi are 1 given that C=1.
            int cX0C1=0; // count how many Xi are 0 given that C=1.
            int cX1C0=0; // count how many Xi are 1 given that C=0.
            int cX0C0=0; // count how many Xi are 0 given that C=0.

            for(int i=0; i<rows; i++)
            {
                //checking first for the C=1 and then  for the X=1
                if(this.multivariateBernoulliValues[i][columns-1]==1 && this.multivariateBernoulliValues[i][j]==1) cX1C1++;

                //checking first for the C=1 and then for the X=0
                else if(this.multivariateBernoulliValues[i][columns-1]==1 && this.multivariateBernoulliValues[i][j]==0) cX0C1++;

                //checking first for the C=0 and then for the X=1
                else if(this.multivariateBernoulliValues[i][columns-1]==0 && this.multivariateBernoulliValues[i][j]==1) cX1C0++;

                //checking first for the C=0 and then for the X=0
                else cX0C0++;
            }

            // Laplace +1/+2 --> dummy existence of feature in order not to get zero from a probability!!

            this.probabilityX1C1[j] = (cX1C1 +1 ) / ( (cX1C1+cX0C1+2)*1.0);
            this.probabilityX1C0[j] = (cX1C0+1) / ((cX1C0 + cX0C0 + 2 )*1.0);
        }



    }

    /**
     *
     * @param mail is the mail to be classified from the algorithm.
     * @return true if the mail is classified as spam by NaiveBayes or false if is classified as ham.
     *
     * Naive Bayes formula :  P(C=1| vector(X) ) = P(C=1) * Product( P(Xi=xi| C=1 ) )
     *                        P(C=0| vector(X) ) = ( 1- P(C=0) ) * Product( P(Xi=xi| C=0 ) )
     *
     */
    public boolean classifyMail(eMail mail)
    {
        /**
         * finding the feature vector of the mail to be classified.
         * because we have no connection with the bernoulli table, we want to calculate the features again and create a feature vector for the
         * algorithm to classify.
         */
        int[] featVec = new int[this.features.size()]; //input for Naive-Bayes algorithm.

        // again bypassing Subject: because all mails contain that so it's useless as a feature.
        String content = mail.getContent().toUpperCase().substring(9);
        // creating  unique tokens split by space.
        HashSet<String> tokens = new HashSet<String>(Arrays.asList(content.split("\\s+")));

        /**
         * creating the feature vector for classification.
         */

        for(int i=0; i<this.features.size(); i++)
        {
            if(tokens.contains(this.features.get(i).getFeatureName())) featVec[i]=1;
            else featVec[i]=0;
        }



        /**
         * we need to calculate the spam messages for P(C=1).
         */
        int categoryCol = this.multivariateBernoulliValues[0].length;
        int spams =0;
        for(int i=0; i<this.multivariateBernoulliValues.length; i++)
        {
            if(this.multivariateBernoulliValues[i][categoryCol-1]==1) spams++;
        }

        // P(C=1)
        double PC1 = spams/ (this.multivariateBernoulliValues.length *1.0);
        // P(C=0)
        double PC0 = 1.0 - PC1;

        /**
         * let's classify!
         * we do not need denominators!!
         */

        /**
         *  calculating P(C=1 | Vector(X) ) and P(C=0| Vector(X) )
         *
         */

        // probability of the mail argument being spam.
        double isSpam = PC1; // initially the result is equal to P(C=1)
        double isHam = PC0; // initially the result is equal to P(C=0)

        for(int i=0; i<this.features.size(); i++)
        {
            if(featVec[i]==1)
            {
                isSpam = isSpam * this.probabilityX1C1[i]; // P(Xi=1|C=1)
                isHam = isHam * this.probabilityX1C0[i]; // P(Xi=1|C=0)
            }
            else {
                isSpam = isSpam * (1-this.probabilityX1C1[i]); // P(Xi=0|C=1)
                isHam = isHam * (1-this.probabilityX1C0[i]); // P(Xi=0|C=0)
            }

        }

        return isSpam>isHam; // true if the probability of being spam is bigger.
    }






}

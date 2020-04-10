import java.io.*;
import java.util.*;
import java.lang.*;
public class DataHandling {
    private static ArrayList<eMail> trainingData ;
    private static ArrayList<eMail> developmentData;
    private static ArrayList<eMail> testingData;




    public static ArrayList<eMail> getTrainingData()
    {
        return trainingData;
    }

    public static ArrayList<eMail> getDevelopmentData()
    {
        return developmentData;
    }

    public static ArrayList<eMail> getTestingData()
    {
        return testingData;
    }



    public static void distributeData()
    {
        trainingData= new ArrayList<eMail>();
        developmentData=new ArrayList<eMail>();
        testingData= new ArrayList<eMail>();

        //taking the folders containing the mails
        final File hamFolder = new File("enron1/ham");
        final File spamFolder = new File("enron1/spam");

        //taking the files from the folder as arrays
        final File[] hamFiles = hamFolder.listFiles();
        final File[] spamFiles = spamFolder.listFiles();

        //creating lists of eMail objects in order to create the data.
        ArrayList<eMail> hams = new ArrayList<eMail>();
        ArrayList<eMail> spams = new ArrayList<eMail>();

        // filling the lists
        for(File f : hamFiles)
        {
            BufferedReader reader=null;
            String line;
            String content="";
            try{
                reader = new BufferedReader(new FileReader(f));
                line = reader.readLine();
                while(line!=null)
                {
                    content+=line;
                    content+=("\n");
                    line = reader.readLine();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            try{
                reader.close();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
            eMail m = new eMail(content,false);
            hams.add(m);
        }

        for(File f : spamFiles)
        {
            BufferedReader reader=null;
            String line;
            String content="";
            try{
                reader = new BufferedReader(new FileReader(f));
                line = reader.readLine();
                while(line!=null)
                {
                    content+=line;
                    content+=("\n");
                    line = reader.readLine();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            try{
                reader.close();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
            eMail m = new eMail(content,true);
            spams.add(m);
        }

        //total mails
        int totalMails = hams.size()+spams.size();

        int trainSize = (int) Math.round(totalMails*0.8);
        int devSize = (int) Math.round(totalMails*0.1);
        int testSize = (int) Math.round(totalMails*0.1);

        Random random = new Random(1); //seeding with 1.
        int trh=0,trs=0,dvh=0,dvs=0,tsth=0,tsts=0;
        for(int i=0; i<trainSize; i++)
        {
            //select ham or spam
            //double hamOrSpam = Math.random(); // calculating a probability of choosing ham or spam mail.
            double hamOrSpam = random.nextDouble();

            if(hams.isEmpty() && spams.isEmpty()){
                System.out.println("There are no mails! ");
                break;
            }
            else if(hams.isEmpty())
            {
              trainingData.add(spams.remove(random.nextInt(spams.size())));
              trs++;

            }
            else if(spams.isEmpty())
            {
                trainingData.add(hams.remove(random.nextInt(hams.size())));
                trh++;

            }
            else
            {
                if(hamOrSpam<0.7) // 70% probability of choosing ham. We initially put 50% but all the spams where chosen here because they are less in the data.
                {
                    trainingData.add(hams.remove(random.nextInt(hams.size())));
                    trh++;

                }
                else
                {
                    trainingData.add(spams.remove(random.nextInt(spams.size())));
                    trs++;

                }
            }

        }
       // System.out.println("Training Data..." + " Hams: " + trh + " Spams: " +trs);

        for(int i=0; i<devSize; i++)
        {
            //double hamOrSpam = Math.random();
            double hamOrSpam = random.nextDouble();

            if(hams.isEmpty() && spams.isEmpty()){
                System.out.println("There are no mails! ");
                break;
            }
            else if(hams.isEmpty())
            {
                developmentData.add(spams.remove(random.nextInt(spams.size())));
                dvs++;

            }
            else if(spams.isEmpty())
            {
                developmentData.add(hams.remove(random.nextInt(hams.size())));
                dvh++;

            }
            else
            {
                if(hamOrSpam<0.7) // 70% probability of choosing ham
                {
                    developmentData.add(hams.remove(random.nextInt(hams.size())));
                    dvh++;

                }
                else
                {
                    developmentData.add(spams.remove(random.nextInt(spams.size())));
                    dvs++;

                }
            }
        }
        //System.out.println("Dev Data..." + " Hams: " + dvh + " Spams: " +dvs);

        for(int i=0; i<testSize; i++)
        {
            //double hamOrSpam = Math.random();
            double hamOrSpam = random.nextDouble();

            if(hams.isEmpty() && spams.isEmpty()){
                System.out.println("There are no mails! ");
                break;
            }
            else if(hams.isEmpty())
            {
                testingData.add(spams.remove(random.nextInt(spams.size())));
                tsts++;

            }
            else if(spams.isEmpty())
            {
                testingData.add(hams.remove(random.nextInt(hams.size())));
                tsth++;

            }
            else
            {
                if(hamOrSpam<0.7) // 70% probability of choosing ham
                {
                    testingData.add(hams.remove(random.nextInt(hams.size())));
                    tsth++;

                }
                else
                {
                    testingData.add(spams.remove(random.nextInt(spams.size())));
                    tsts++;

                }
            }
        }
       // System.out.println("Testing Data..." + " Hams: " + tsth + " Spams: " +tsts);

    }

    /**
     * boolean values ( 2 categories)
     * calculate entropy given a probability ( e.g P(C=1) )
     */
    public static double entropy(double prob)
    {

        if(prob==0) return 0;
        else if(prob==1) return 0;
        else return -( prob * log2(prob) )- ( (1-prob) * log2(1-prob) );
    }

    public static double log2(double prob)
    {
        return (Math.log(prob) / Math.log(2));
    }


    public static ArrayList<Feature> discoverBestFeaturesWithIG(int best)
    {
        /*
        all the features of all training mails ( without duplicates).
         */
        HashSet<String> uniqueFeatures = new HashSet<String>();

        /**
         * List of all mails with a set of all the UNIQUE tokens for each mail.
         * The list does not contain mails but theoretically,each set corresponds to a mail from the training data set
         *  e.g mailsWithWords.get(0) is the set of words of the 1st mail of the training data set.
         *
         *  ALTERNATIVE : a HashSet in the eMail class as a field --> each mail will have its own set of unique tokens. It's basically the same!
         *
         */

        ArrayList<HashSet<String>> mailsWithWords = new ArrayList<HashSet<String>>();

        for(eMail mail : trainingData)
        {
            // bypass Subject: because every mail has it. 9 characters.
            String content = mail.getContent().toUpperCase().substring(9);

            // creating words of all the text split by space.
            HashSet<String> tokens = new HashSet<String>(Arrays.asList(content.split("\\s+")));

            // adding a set of the tokens in each mail.
            mailsWithWords.add(new HashSet<String>(tokens));

            uniqueFeatures.addAll(tokens); // adding all the tokens of the current mail in the big set. HashSet removes duplicates so we'll have every token once.

        }
        //removing the empty string.
        uniqueFeatures.remove("");
        //uniqueFeatures.remove(",");
        //uniqueFeatures.remove(".");

        //creating a list from the big set because we need the get(index) method.
        ArrayList<String> uniqueFeaturesAsList = new ArrayList<String>(uniqueFeatures);

        //int mailsNum = mailsWithWords.size(); //rows of 2D array( coming next...)
        int uniqueFeaturesNum = uniqueFeatures.size(); //cols of 2d array (coming next...)

        /**
         *  creating a 2D table that has as many rows as the training mails and as many columns as all the unique features of all training mails.
         *  After that, we will fill it accordingly. Each mail will have 1 in the feature if it exists, otherwise 0.
          */

        int[][] table = new int[trainingData.size()][uniqueFeaturesNum+1]; // +1 column for C=0/C=1.


        int spams=0; // count how many spams we have in the table. We want them for the P(C=1).


        for(int i =0 ; i<trainingData.size(); i++)
        {
            for(int j=0; j<uniqueFeaturesNum; j++)
            {
                //for every set(theoretically mail) in the list check if the features are in the set.
                if(mailsWithWords.get(i).contains(uniqueFeaturesAsList.get(j)))
                {
                    table[i][j] =1;
                }
                else table[i][j] =0;
            }
            if(trainingData.get(i).isSpam())
            {
                table[i][uniqueFeaturesNum] = 1; //true for spam (C=1).
                spams++;
            }
            else table[i][uniqueFeaturesNum] =  0; //false for ham (C=0).
        }

        /**
         * beginning computations for Information Gain for every feature.
         *    formula :  IG(C,X) = H(C) - Sum_for_existence_or_not( P(X=x)*H(C|X=x) ).  //existence=0/1;
         */

        // IG(C,X) --> information gain for every feature.
        double[] IG = new double[uniqueFeaturesNum];

        //formula members calculation.

        // P(C=1) --> probability of spam.
        double PC1 = spams / ((trainingData.size())*1.0);

        //P(C=0) --> probability of not spam.
        double PC0 = 1.0-PC1;

        // basic entropy for the formula
        double HC = entropy(PC1);


        /**
         * P(X=1) --> probability of a feature existence. // 1 if exists, 0 if not
         * P(X=0) --> 1 - existenceProb[i]
         */

        double[] existenceProbabilities = new double[uniqueFeaturesNum];

        /**
         *  P(C=1|X=1) --> probability of being spam if X feature exists.
         *  P(C=0|X=1) --> 1 - spamProbabilityX1
         */

        double[] spamProbabilityX1 = new double[uniqueFeaturesNum];

        /**
         * P(C=1|X=0) --> probability of being spam if X feature does not exist.
         * P(C=0|X=0) --> 1- spamProbabilityX0
         */

        double[] spamProbabilityX0 = new double[uniqueFeaturesNum];

        /**
         *  H(C=1| X=1 ) --> entropy for C=1 given that X=1.
         *  H(C=0|X=1) --> 1 - entropyC1X1
         */

        double[] entropyC1X1 = new double[uniqueFeaturesNum];

        /**
         * H(C=1|X=0) --> entropy for C=1 given that X=0.
         * H(C=0|X=0) --> 1 - entropyC1X0
         */

        double[] entropyC1X0 = new double[uniqueFeaturesNum];



        for(int j = 0; j<uniqueFeaturesNum; j++)
        {
            int cX1=0; // count for every feature in how many mails it exists.
            int cC1X1=0; //count how many mails are spam given that X=1.
            int cC1X0=0; //count how many mails are spam given that X=0.
            for(int i=0; i<trainingData.size(); i++)
            {
                //for P(X=1)
                if(table[i][j]==1) cX1++;

                //for P(C=1|X=1)
                if(table[i][j]==1 && table[i][uniqueFeaturesNum]==1) cC1X1++;

                //for P(C=1|X=0)
                if(table[i][j]==0 && table[i][uniqueFeaturesNum]==1) cC1X0++;
            }
            existenceProbabilities[j] = cX1 / (trainingData.size()*1.0 );

            if(cX1==0) spamProbabilityX1[j]=0;
            else spamProbabilityX1[j]= cC1X1 / (cX1*1.0 );

            if(trainingData.size()-cX1==0) spamProbabilityX0[j]=0; //avoid division with 0.
            else spamProbabilityX0[j] = cC1X0 / ((trainingData.size()-cX1)*1.0);

            entropyC1X1[j] = entropy(spamProbabilityX1[j]);
            entropyC1X0[j] = entropy(spamProbabilityX0[j]);

            //finally, calculating the IG

            IG[j] = HC - ( (existenceProbabilities[j]*entropyC1X1[j]) + (1-existenceProbabilities[j])*entropyC1X0[j] );

        }

        //creating Features objects with their own information gain.

        ArrayList<Feature> features = new ArrayList<Feature>();

        for(int j=0; j<uniqueFeaturesNum; j++)
        {
            Feature f = new Feature(uniqueFeaturesAsList.get(j), IG[j]);
            features.add(f);
        }

        // sorting in descending order based on the IG ( compareTo method in Feature class)
        Collections.sort(features, Collections.reverseOrder());

        // Returning the m-best features based on the IG.

        ArrayList<Feature> bestFeatures = new ArrayList<Feature>();

        for(int j=0; j<best; j++)
        {
            bestFeatures.add(features.get(j));
        }


        return bestFeatures;
    }


    /**
     *
     * @param features is the features list
     * @param trainingData is the list of the training mails.
     * @return a 2D bernoulli variables table.
     */
    public static int[][] fillBernoulliTable(ArrayList<eMail> trainingData,ArrayList<Feature> features)
    {
        int[][] table = new int[trainingData.size()][features.size()+1]; // +1 for category.

        /**
         * List of all mails with a set of all the UNIQUE tokens for each mail.
         * The list does not contain mails but theoretically,each set corresponds to a mail from the training data set
         *  e.g mailsWithWords.get(0) is the set of words of the 1st mail of the training data set.
         *
         */

        ArrayList<HashSet<String>> mailsWithWords = new ArrayList<HashSet<String>>();

        for(eMail mail : trainingData)
        {
            // bypass Subject: because every mail has it. 9 characters.
            String content = mail.getContent().toUpperCase().substring(9);

            // creating words of all the text split by space.
            ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(content.split("\\s+")));

            // adding a set of the tokens in each mail.
            mailsWithWords.add(new HashSet<String>(tokens));
        }

        for(int i =0 ; i<trainingData.size(); i++)
        {
            for(int j=0; j<features.size(); j++)
            {
                //for every set(theoretically mail) in the list check if the features are in the set.
                if(mailsWithWords.get(i).contains(features.get(j).getFeatureName()))
                {
                    table[i][j] =1;
                }
                else table[i][j] =0;
            }
            if(trainingData.get(i).isSpam())
            {
                table[i][features.size()] = 1; //true for spam (C=1).
            }
            else table[i][features.size()] =  0; //false for ham (C=0).
        }


        return table;
    }
}

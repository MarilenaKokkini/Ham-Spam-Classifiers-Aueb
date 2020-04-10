import java.util.*;

public class Driver {
    //arrays that contain data for the learning curves.
    private static double[] training_accuracy = new double[10]; // 0--> 10% , 1--> 20% etc...
    private static double[] testing_accuracy= new double[10];
    private static double[] training_spam_precision = new double[10];
    private static double[] training_ham_precision = new double[10];
    private static double[] testing_spam_precision = new double[10];
    private static double[] testing_ham_precision = new double[10];
    private static double[] training_spam_recall = new double[10];
    private static double[] training_ham_recall = new double[10];
    private static double[] testing_ham_recall = new double[10];
    private static double[] testing_spam_recall = new double[10];
    private static double[] training_spam_F1 = new double[10];
    private static double[] training_ham_F1 = new double[10];
    private static double[] testing_spam_F1 = new double[10];
    private static double[] testing_ham_F1 = new double[10];

    //medians
    private static double[] training_F1_median = new double[10];
    private static double[] testing_F1_median = new double[10];
    private static double[] training_precision_median = new double[10];
    private static double[] testing_precision_median = new double[10];
    private static double[] training_recall_median = new double[10];
    private static double[] testing_recall_median = new double[10];

    public static void main(String[] args)
    {

        // distribute the mails into training,development,testing ( 80% , 10% , 10% ).
        System.out.println("Reading mails and distributing them to training/development/testing... ");
        DataHandling.distributeData();

        // find the m-best features.
        Scanner scanner = new Scanner(System.in);
        System.out.print("Select the number of best features: ");
        int input = scanner.nextInt();
        System.out.println();
        /**
         * calculating best features based on IG.
         */
        ArrayList<Feature> features= DataHandling.discoverBestFeaturesWithIG(input);
        int[] possibleFeatNum = {300,500,800,1000,features.size()};

        //algorithms running...

        System.out.println("Select algorithm for classification! Press 1 for Naive-Bayes, press 2 for ID3, press 3 for AdaBoost, press 4 for Logistic Regression or press 0 to quit the program. ");
        System.out.print("Selection: ");
        input = scanner.nextInt();

        while(input!=1 && input!=2 && input!=3 && input!=4 && input!=0)
        {
            System.out.println("Please only choose a number from 1 to 4 for algorithms or 0 to quit the program. ");
            System.out.print("Selection: ");
            input=scanner.nextInt();
        }

        System.out.println();
        while(input!=0)
        {

            /**
             * NAIVE BAYES DEMO
             */
            if (input == 1)
            {

                /**
                 * we are gonna find how many features Naive Bayes must use taking into consideration the accuracy of the algorithm in the development data.
                 */
                System.out.println("Finding best feature number for Naive-Bayes. ");
                double devAcc = 0.0; // best accuracy for dev data.
                int featuresBayes = 0; // beginning with 0 features.
                ArrayList<Feature> featuresBayesList; // the list Naive-Bayes will eventually use.
                for (int i = 0; i < possibleFeatNum.length; i++) {
                    featuresBayesList = new ArrayList<Feature>(features.subList(0, possibleFeatNum[i]));
                    NaiveBayes NB = new NaiveBayes(featuresBayesList);
                    NB.train();
                    int rightAnswers = 0;
                    for (eMail mail : DataHandling.getDevelopmentData()) {
                        boolean answer = NB.classifyMail(mail);
                        if (answer == mail.isSpam())
                            rightAnswers++; // if the algorithm classified correctly then increase the right answer count.
                    }

                    double devAcc_curr = rightAnswers / (DataHandling.getDevelopmentData().size() * 1.0); // calculating the current accuracy for dev data.

                    if (devAcc_curr > devAcc) {
                        devAcc = devAcc_curr;
                        featuresBayes = possibleFeatNum[i];
                    }
                }

                System.out.println();
                System.out.println("Best features number for Naive-Bayes are: " + featuresBayes);
                System.out.println("Accuracy for dev is: " + devAcc);


                /**
                 *  train Naive-Bayes with the found number of features.
                 */
                featuresBayesList= new ArrayList<Feature>(features.subList(0,featuresBayes));
                NaiveBayes classification = new NaiveBayes(featuresBayesList);
                classification.train();

                /**
                 *
                 *  Here, we will calculate accuracy,precision,recall and F1 for training and testing data to show the performance of Naive-Bayes algorithm.
                 *  We will use a 2x2 confusion matrix because we have 2 categories.
                 */
                calculate100Percent(classification);

                print();

            }


            /**
             *
             * ID3 DEMO
             */
            else if (input == 2)
            {
                /**
                 * we are gonna find how many features ID3 must use taking into consideration the accuracy of the algorithm in the development data.
                 * In essence, we are going to prune the ID3 tree.
                 */
                System.out.println("Finding best feature number for ID3. ");
                double devAcc = 0.0; // best accuracy for dev data.
                int featuresID3 = 0; // beginning with 0 features.
                ArrayList<Feature> featuresID3List = null; // the list Naive-Bayes will eventually use.

                for (int i = 0; i < possibleFeatNum.length; i++) {
                        featuresID3List = new ArrayList<Feature>(features.subList(0, possibleFeatNum[i]));
                        ID3 ID3 = new ID3(DataHandling.getTrainingData(), featuresID3List);
                        ID3.setElasticity(0.0);
                        ID3.train();
                        int rightAnswers = 0;
                        for (eMail mail : DataHandling.getDevelopmentData()) {
                            //boolean answer = ID3.classifyMail(mail);
                            boolean answer = ID3.classifyMail(mail);
                            if (answer == mail.isSpam())
                                rightAnswers++; // if the algorithm classified correctly then increase the right answer count.
                        }

                        double devAcc_curr = rightAnswers / (DataHandling.getDevelopmentData().size() * 1.0); // calculating the current accuracy for dev data.

                        if (devAcc_curr > devAcc) {
                            devAcc = devAcc_curr;
                            featuresID3 = possibleFeatNum[i];
                        }

                }
                System.out.println();
                System.out.println("Best features number for ID3 are: " + featuresID3);
                System.out.println("Accuracy dev data is:" + devAcc);

                featuresID3List= new ArrayList<Feature>(features.subList(0,featuresID3));
                ID3 classification = new ID3(DataHandling.getTrainingData(),featuresID3List);
                classification.setElasticity(0.0);
                classification.train();

                /**
                 *
                 *  Here, we will calculate accuracy,precision,recall and F1 for training and testing data to show the performance of Naive-Bayes algorithm.
                 * We will use a 2x2 confusion matrix because we have 2 categories.
                 */

                calculate100Percent(classification);

                print();

            }
            /**
             *
             * ADA BOOST DEMO
             */
            else if (input == 3)
            {
                System.out.println("Finding best features and hypotheses number for AdaBoost. ");
                double devAcc=0.0;
                int featuresAdaBoost=0; // beginning with 0 features.
                ArrayList<Feature> featuresAdaBoostList;
                int[] hypotheses={20,30,40,50,100};
                int bestHyp = 0;
                for(int i=0; i<possibleFeatNum.length; i++)
                {
                    for(int hypothesis : hypotheses)
                    {
                        featuresAdaBoostList = new ArrayList<Feature>(features.subList(0,possibleFeatNum[i]));
                        AdaBoost AB = new AdaBoost(DataHandling.getTrainingData(),featuresAdaBoostList,hypothesis);
                        AB.train();
                        int rightAnswers =0;
                        for(eMail mail : DataHandling.getDevelopmentData())
                        {
                            boolean answer = AB.classifyMail(mail);
                            if(answer==mail.isSpam()) rightAnswers++;
                        }

                        double devAcc_curr = rightAnswers / (DataHandling.getDevelopmentData().size() * 1.0); // calculating the current accuracy for dev data.
                        if (devAcc_curr > devAcc) {
                            devAcc = devAcc_curr;
                            featuresAdaBoost = possibleFeatNum[i];
                            bestHyp = hypothesis;
                        }
                    }

                }

                System.out.println();
                System.out.println("Best features number for AdaBoost are: " + featuresAdaBoost);
                System.out.println("Best number for hypotheses is: " + bestHyp);
                System.out.println("Accuracy dev data is:" + devAcc);

                featuresAdaBoostList = new ArrayList<Feature>(features.subList(0,featuresAdaBoost));
                AdaBoost classification = new AdaBoost(DataHandling.getTrainingData(),featuresAdaBoostList,bestHyp);
                classification.train();

                /**
                 *
                 *  Here, we will calculate accuracy,precision,recall and F1 for training and testing data to show the performance of Naive-Bayes algorithm.
                 * We will use a 2x2 confusion matrix because we have 2 categories.
                 */

                calculate100Percent(classification);


                print();


            }
            /**
             * LOGISTIC
             */
            else if(input==4)
            {
                System.out.println("Finding best features number and hyper-parameters for logistic regression.");
                double devAcc=0.0;
                int featuresLogReg=0; // beginning with 0 features.
                ArrayList<Feature> featuresLogRegList;
                double bestLambda =.0;
                double bestLearningRate =.0;

                for(int i=0; i<possibleFeatNum.length; i++)
                {
                    featuresLogRegList = new ArrayList<>(features.subList(0,possibleFeatNum[i]));

                    double lambda = 0.01;
                    while(lambda >= .0001)
                    {
                        double learningRate = 0.01;
                        while(learningRate>= .0001)
                        {
                            LogisticRegression logReg = new LogisticRegression(DataHandling.getTrainingData(),featuresLogRegList,5,learningRate,lambda);
                            logReg.train();
                            int rightAnswers =0;
                            for(eMail mail : DataHandling.getDevelopmentData())
                            {
                                boolean answer = logReg.classifyMail(mail);
                                if(answer==mail.isSpam()) rightAnswers++;
                            }
                            double devAcc_curr = rightAnswers / (DataHandling.getDevelopmentData().size() * 1.0); // calculating the current accuracy for dev data.
                            if (devAcc_curr > devAcc) {
                                devAcc = devAcc_curr;
                                featuresLogReg = possibleFeatNum[i];
                                bestLambda=lambda;
                                bestLearningRate=learningRate;
                            }
                            learningRate = learningRate * 0.1;
                        }
                        lambda = lambda * 0.1;
                    }
                    //if(i%20==0) System.out.print(".");
                }
                System.out.println();
                System.out.println("Best features number for Logistic Regression are: " + featuresLogReg);
                System.out.println("Best number for lambda is: " + bestLambda);
                System.out.println("Best number for learning rate is: " + bestLearningRate);
                System.out.println("Accuracy dev data is:" + devAcc);

                featuresLogRegList = new ArrayList<>(features.subList(0,featuresLogReg));
                LogisticRegression classification = new LogisticRegression(DataHandling.getTrainingData(),featuresLogRegList,5,bestLearningRate,bestLambda);
                classification.train();

                calculate100Percent(classification);

                print();
            }

            System.out.println("If you want to quit, press 0. Otherwise select another algorithm. (1,2 or 3) ");
            System.out.print("Selection: ");
            input=scanner.nextInt();
        }
    }

    /**
     *  Here, we will calculate accuracy,precision,recall and F1 for training and testing data to show the performance of the "classification" algorithm.
     *  We will use a 2x2 confusion matrix because we have 2 categories.
     */

    public static void calculate100Percent(Object classification){
        //the confusion matrix for training data.
        int[][] confusionMatrixTrainingData = new int[2][2];

        // counters --> real__classified.
        int ham__ham_Train=0; // [0][0] of the table . They are correctly classified as ham.
        int ham__spam_Train=0; //[0][1] of the table. They are classified as spam but they are ham.
        int spam__ham_Train=0; //[1][0] of the table. They are classified as ham but they are spam.
        int spam__spam_Train=0; //[1][1] of the table. They are correctly classified as spam.
        int currentSize = 0;

        for(eMail mail : DataHandling.getTrainingData())
        {
            boolean answer;
            if (classification instanceof NaiveBayes)
                answer = ((NaiveBayes)classification).classifyMail(mail);
            else if (classification instanceof ID3)
                answer = ((ID3) classification).classifyMail(mail);
            else if(classification instanceof AdaBoost) answer = ((AdaBoost) classification).classifyMail(mail);
            else answer = ((LogisticRegression) classification).classifyMail(mail);
            if(mail.isSpam() && answer) spam__spam_Train++;
            if(mail.isSpam() && !answer) spam__ham_Train++;
            if(!mail.isSpam() && answer) ham__spam_Train++;
            if(!mail.isSpam() && !answer) ham__ham_Train++;
            currentSize++;
            double percent = (currentSize * 100)/  DataHandling.getTrainingData().size();
            if (percent % 10.0 == 0.0) calculateForCurves(0, percent, currentSize, ham__ham_Train, ham__spam_Train, spam__ham_Train, spam__spam_Train);
        }
        //filling the matrix
        confusionMatrixTrainingData[0][0] = ham__ham_Train;
        confusionMatrixTrainingData[0][1] = ham__spam_Train;
        confusionMatrixTrainingData[1][0] = spam__ham_Train;
        confusionMatrixTrainingData[1][1] = spam__spam_Train;

        //accuracy.
        double trainingAccuracy = (confusionMatrixTrainingData[0][0]+confusionMatrixTrainingData[1][1]) / (1.0 * DataHandling.getTrainingData().size());
        //System.out.println("Accuracy train 100% : " + trainingAccuracy );
        //precision for spam.
        double trainingPrecisionSpam = spam__spam_Train / ( (spam__spam_Train + ham__spam_Train)*1.0);
        //precision for ham.
        double trainingPrecisionHam = ham__ham_Train / ( (ham__ham_Train + spam__ham_Train)*1.0);
        //recall for spam.
        double trainingRecallSpam = spam__spam_Train / ( (spam__spam_Train + spam__ham_Train)*1.0);
        //recall for ham.
        double trainingRecallHam = ham__ham_Train / ( (ham__ham_Train + ham__spam_Train)*1.0);
        //F1 for ham.
        double trainingF1Ham = 2*(trainingPrecisionHam*trainingRecallHam) / (trainingPrecisionHam + trainingRecallHam);
        // F1 for spam.
        double trainingF1Spam = 2*(trainingPrecisionSpam*trainingRecallSpam) / (trainingPrecisionSpam + trainingRecallSpam);



        //the confusion matrix for testing data.
        int[][] confusionMatrixTestingData = new int[2][2];

        // counters --> real__classified.
        int ham__ham_Test=0; // [0][0] of the table . They are correctly classified as ham.
        int ham__spam_Test=0; //[0][1] of the table. They are classified as spam but they are ham.
        int spam__ham_Test=0; //[1][0] of the table. They are classified as ham but they are spam.
        int spam__spam_Test=0; //[1][1] of the table. They are correctly classified as spam.
        currentSize = 0;

        for(eMail mail : DataHandling.getTestingData())
        {
            boolean answer;
            if (classification instanceof NaiveBayes)
                answer = ((NaiveBayes)classification).classifyMail(mail);
            else if (classification instanceof ID3)
                answer = ((ID3) classification).classifyMail(mail);
            else if(classification instanceof AdaBoost) answer = ((AdaBoost) classification).classifyMail(mail);
            else answer = ((LogisticRegression) classification).classifyMail(mail);
            if(mail.isSpam() && answer) spam__spam_Test++;
            if(mail.isSpam() && !answer) spam__ham_Test++;
            if(!mail.isSpam() && answer) ham__spam_Test++;
            if(!mail.isSpam() && !answer) ham__ham_Test++;
            currentSize++;
            double percent = (currentSize * 100)/DataHandling.getTestingData().size();
            if (percent % 10.0 == 0.0) calculateForCurves(1, percent, currentSize, ham__ham_Test, ham__spam_Test, spam__ham_Test, spam__spam_Test);
        }
        //filling the matrix
        confusionMatrixTestingData[0][0] = ham__ham_Test;
        confusionMatrixTestingData[0][1] = ham__spam_Test;
        confusionMatrixTestingData[1][0] = spam__ham_Test;
        confusionMatrixTestingData[1][1] = spam__spam_Test;

        //accuracy.
        double testingAccuracy = (confusionMatrixTestingData[0][0]+confusionMatrixTestingData[1][1]) / (1.0 * DataHandling.getTestingData().size());
        //precision for spam.
        double testingPrecisionSpam = spam__spam_Test / ( (spam__spam_Test + ham__spam_Test)*1.0);
        //precision for ham.
        double testingPrecisionHam = ham__ham_Test / ( (ham__ham_Test + spam__ham_Test)*1.0);
        //recall for spam.
        double testingRecallSpam = spam__spam_Test / ( (spam__spam_Test + spam__ham_Test)*1.0);
        //recall for ham.
        double testingRecallHam = ham__ham_Test/ ( (ham__ham_Test + ham__spam_Test)*1.0);
        //F1 for ham.
        double testingF1Ham = 2*(testingPrecisionHam*testingRecallHam) / (testingPrecisionHam + testingRecallHam);
        // F1 for spam.
        double testingF1Spam = 2*(testingPrecisionSpam*testingRecallSpam) / (testingPrecisionSpam + testingRecallSpam);
    }



    public static void calculateForCurves(int i, double percent, int currentSize, int ham__ham, int ham__spam, int spam__ham, int spam__spam) {
        int index = (int)percent/10 - 1;
        // if it's about training data
        if (index >= 10 || index<0) return;
        if (i==0){ 
            training_accuracy[index] = (ham__ham + spam__spam) / (1.0 * currentSize);
            training_spam_precision[index] = spam__spam / ( (spam__spam + ham__spam)*1.0);
            training_ham_precision[index] = ham__ham / ( (ham__ham + spam__ham)*1.0);
            training_spam_recall[index] = spam__spam / ( (spam__spam + spam__ham)*1.0);
            training_ham_recall[index] = ham__ham/ ( (ham__ham + ham__spam)*1.0);
            training_spam_F1[index] = 2*(training_spam_precision[index] * training_spam_recall[index]) / (training_spam_precision[index] + training_spam_recall[index]);
            training_ham_F1[index] = 2*(training_ham_precision[index]*training_ham_recall[index]) / (training_ham_precision[index] + training_ham_recall[index]);

            //medians
            training_F1_median[index] = (training_ham_F1[index]+training_spam_F1[index]) / 2.0;
            training_precision_median[index] = (training_ham_precision[index]+training_spam_precision[index]) / 2.0;
            training_recall_median[index] = (training_spam_recall[index]+training_ham_recall[index]) / 2.0;
        }
        // if it's about testing data
        if (i==1){
            testing_accuracy[index] = (ham__ham + spam__spam) / (1.0 * currentSize);
            testing_spam_precision[index] = spam__spam / ( (spam__spam + ham__spam)*1.0);
            testing_ham_precision[index] = ham__ham / ( (ham__ham + spam__ham)*1.0);
            testing_spam_recall[index] = spam__spam / ( (spam__spam + spam__ham)*1.0);
            testing_ham_recall[index] = ham__ham/ ( (ham__ham + ham__spam)*1.0);
            testing_spam_F1[index] = 2.0*(testing_spam_precision[index] * testing_spam_recall[index]) / (testing_spam_precision[index] + testing_spam_recall[index]);
            testing_ham_F1[index] = 2.0*(testing_ham_precision[index]*testing_ham_recall[index]) / (testing_ham_precision[index] + testing_ham_recall[index]);

            //medians
            testing_F1_median[index] = (testing_ham_F1[index]+testing_spam_F1[index]) / 2.0;
            testing_precision_median[index] = (testing_ham_precision[index]+testing_spam_precision[index]) / 2.0;
            testing_recall_median[index] = (testing_spam_recall[index]+testing_ham_recall[index]) / 2.0;
        }
    }

    private static void print()
    {
        System.out.println("Training accuracy: ");
        for(int i=0; i<10; i++) {
            System.out.println(training_accuracy[i] + " ");
        }
        System.out.println("Testing accuracy: ");
        for(int i=0; i<10; i++) {
            System.out.println( testing_accuracy[i] + " ");
        }
        System.out.println("Training precision: ");
        for(int i=0; i<10; i++) {
            System.out.println(training_precision_median[i] + " ");
        }
        System.out.println("Testing precision: ");
        for(int i=0; i<10; i++) {
            System.out.println(testing_precision_median[i] + " ");
        }
        System.out.println("Training recall: ");
        for(int i=0; i<10; i++) {
            System.out.println(training_recall_median[i] + " ");
        }
        System.out.println("Testing recall: ");
        for(int i=0; i<10; i++) {
            System.out.println(testing_recall_median[i] + " ");
        }
        System.out.println("Training F1: ");
        for(int i=0; i<10; i++) {
            System.out.println(training_F1_median[i] + " ");
        }
        System.out.println("Testing F1: ");
        for(int i=0; i<10; i++){
            System.out.println(testing_F1_median[i]+" ");
        }

    }
}

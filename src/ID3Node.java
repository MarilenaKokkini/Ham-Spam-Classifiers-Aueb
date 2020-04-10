import java.util.*;
public class ID3Node {

	private Feature checkingFeature; //feature that the node checks --> null if the node is leaf.
	private boolean isLeaf; // node is leaf so it does not need expansion.
	private boolean category; // true if spam , false if ham. --> for decision node only.

	private ID3Node leftChild; // left child node. --> must be null if isLeaf==true.
	private ID3Node rightChild; // right child node. --> must be null if isLeaf==true.

	 /*
    constructors
     */

	// one for simple node.
	public ID3Node(boolean category,Feature checkingFeature) {
		this.category = category;
		this.checkingFeature = checkingFeature;
		this.isLeaf = false; // its not leaf from the beginning.
		this.leftChild = null; //initially null
		this.rightChild = null; //initially null
	}

	// one for decision node.
	public ID3Node( boolean category, boolean isLeaf) {
		this.category = category;
		this.isLeaf = isLeaf;
		this.checkingFeature = null; //decision node does not check any feature.
		this.leftChild = null; //initially null
		this.rightChild = null; //initially null
	}

	/**
	 *  BASED ON THE LECTURES SLIDES, THE ID3 RECURSIVE ALGORITHM.
	 *
	 * @param trainingData is the training data list.
	 * @param features is the features list.
	 * @param category is the predefined category. TRUE for spam, FALSE for ham
	 * @return the ID3 tree.
	 */
	public static ID3Node createID3Tree(ArrayList<eMail> trainingData, ArrayList<Feature> features, boolean category, double elasticity) {

		/**
		 * returning a decision node with predefined category because there is no data.
		 */
		if(trainingData.isEmpty()) {
			ID3Node node = new ID3Node( category,  true);
			return node;
		}

		/**
		 * if the mails are all in the same category then return this category.
		 */

		int[][] table = DataHandling.fillBernoulliTable(trainingData, features);




		// Calculate P(C=1) and P(C=0)
		int spams = 0;
		for(int i=0;i<table.length;i++) {
			if(table[i][table[0].length-1]==1) spams++;
		}

		// P(C=1)
		double PC1 = spams / (table.length*1.0);

		// P(C=0)
		double PC0 = 1-PC1;


		if(PC0 <= elasticity) {  // if all mails are spams --> probability of ham goes to 0
			ID3Node node = new ID3Node(true,true);
			return node;
		}
		else if(PC1 <= elasticity) { // if all mails are hams --> probability of spam goes to 0
			ID3Node node = new ID3Node(false,true);
			return node;
		}

		/**
		 * if the features list is empty then return the most common category.
		 */
		if(features.isEmpty()) {
			ID3Node node = new ID3Node( PC1>PC0, true); // most common category decision node.
			return node;
		}

		// Find best feature using Information Gain.
		Feature bestFeature = discoverBestFeatureWithIG( features, table);

		// root of the subtree that checks the bestFeature
		ID3Node root = new ID3Node( PC1>PC0, bestFeature);

		/**
		 * for every possible value of bestFeature --> for us 0/1
		 * we should create training examples that contain mails which have the feature(=1) or not(=0)
		 */



		//1st list  --> bestFeature =1
		ArrayList<eMail> trainingDataWithBestFeature = new ArrayList<>(trainingData);
		// remove all the mails that not contain bestFeature in their content.
		trainingDataWithBestFeature.removeIf(mail -> !mail.getContent().toUpperCase().substring(9).contains(bestFeature.getFeatureName()));

		//2nd list --> bestFeature =0
		ArrayList<eMail> trainingDataWithoutBestFeature = new ArrayList<>(trainingData);
		//remove all the mails that contain bestFeature in their content.
		trainingDataWithoutBestFeature.removeIf(mail -> mail.getContent().toUpperCase().substring(9).contains(bestFeature.getFeatureName()));


		//removing the feature in order to get the next best.
		ArrayList<Feature> newFeatures = new ArrayList<Feature>(features);
		newFeatures.removeIf(feature -> feature.getFeatureName().equalsIgnoreCase(bestFeature.getFeatureName()));

		//creating subtree!! recursive ID3
		// m is the most common category.
		root.leftChild = createID3Tree(trainingDataWithBestFeature, newFeatures, PC1>PC0,elasticity); //we go left for X=1


		//creating subtree!! recursive ID3
		// m is the most common category.
		root.rightChild = createID3Tree(trainingDataWithoutBestFeature, newFeatures, PC1>PC0,elasticity); //we go right for X=0

		//returning the tree.
		return root;
	}


	//setters,getters
	public boolean getCategory() {
		return this.category;
	}

	public void setCategory(boolean category) {
		this.category = category;
	}

	public Feature getCheckingFeature() {
		return this.checkingFeature;
	}

	public void setCheckingFeature(Feature checkingFeature) {
		this.checkingFeature = checkingFeature;
	}


	public boolean isLeaf() {
		return this.isLeaf;
	}

	public void setLeaf(boolean leaf) {
		this.isLeaf = leaf;
	}

	public ID3Node getLeftChild() {
		return this.leftChild;
	}

	public void setLeftChild(ID3Node leftChild) {
		this.leftChild = leftChild;
	}

	public ID3Node getRightChild() {
		return this.rightChild;
	}

	public void setRightChild(ID3Node rigthChild) {
		this.rightChild = rigthChild;
	}


	/**
	 *
	 * we need a new method because every time the features and the table are different.
	 *
	 * @param features is the features list.
	 * @param table is the 2D bernoulli values table.
	 * @return the best feature best on IG.
	 */
	private static Feature discoverBestFeatureWithIG(ArrayList<Feature> features, int[][] table)
	{
		int spams=0;
		for(int i=0; i<table.length; i++)
		{
			if(table[i][table[0].length-1]==1) spams++;
		}

		/**
		 * beginning computations for Information Gain for every feature.
		 *    formula :  IG(C,X) = H(C) - Sum_for_existence_or_not( P(X=x)*H(C|X=x) ).  //existence=0/1;
		 */

		// IG(C,X) --> information gain for every feature.
		double[] IG = new double[features.size()];

		//formula members calculation.

		// P(C=1) --> probability of spam.
		double PC1 = spams / ((table.length)*1.0);

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
			int cX1=0; // count for every feature in how many mails it exists.
			int cC1X1=0; //count how many mails are spam given that X=1.
			int cC1X0=0; //count how many mails are spam given that X=0.
			for(int i=0; i<table.length; i++)
			{
				//for P(X=1)
				if(table[i][j]==1) cX1++;

				//for P(C=1|X=1)
				if(table[i][j]==1 && table[i][features.size()]==1) cC1X1++;

				//for P(C=1|X=0)
				if(table[i][j]==0 && table[i][features.size()]==1) cC1X0++;
			}
			existenceProbabilities[j] = cX1 / (table.length*1.0 );

			if(cX1==0) spamProbabilityX1[j]=0;
			else spamProbabilityX1[j]= cC1X1 / (cX1*1.0 );

			if(table.length-cX1==0) spamProbabilityX0[j]=0; //avoid division with 0.
			else spamProbabilityX0[j] = cC1X0 / ((table.length-cX1)*1.0);

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


}

import java.util.*;


public class ID3 {

	private ArrayList<Feature> features;
	private ArrayList<eMail> trainingData;
	private ID3Node tree; // tree
	private double elasticity;


	public ID3(ArrayList<eMail> trainingData,ArrayList<Feature> features) {
		this.trainingData = trainingData;
		this.features = features;
		this.tree = null;
	}




	/**
	 *
	 * training the algorithm. we create the ID3 tree.
	 *
	 */
	public void train() {

		this.tree = ID3Node.createID3Tree(this.trainingData, this.features, false,elasticity);
	}

	/**
	 *
	 * @param mail is the mail to be classified by ID3.
	 * @return true if the mail is classified as spam by ID3 or false if is classified as ham.
	 *
	 * We will traverse the ID3 tree. Beginning from the root, we will check if it's a leaf( decision node ) and if not then we will check if the argument mail
	 * contains the feature that the node we are processing checks! Then, based on the existence of the feature or not, we will proceed to the left or the right
	 * child of the node.
	 */
	public boolean classifyMail(eMail mail) {

		// Beginning from the root.
		ID3Node tmp = this.tree;

		//checking for decision node.
		while(!tmp.isLeaf()) {

			//examine if argument mail contains the feature that the node checks.
			//if the mail contains the feature then go left
			if(mail.getContent().toUpperCase().substring(9).contains(tmp.getCheckingFeature().getFeatureName())) tmp=tmp.getLeftChild();
			else tmp=tmp.getRightChild(); // else, go right.

		}

		// we reached a decision node --> true for spam, false for ham
		return tmp.getCategory();

	}


	public ID3Node getTree() {
		return this.tree;
	}

	public void setTree(ID3Node tree) {
		this.tree = tree;
	}

	public ArrayList<Feature> getFeatures() {
		return this.features;
	}

	public void setFeatures(ArrayList<Feature> features) {
		this.features = features;
	}

	public ArrayList<eMail> getTrainingData() {
		return this.trainingData;
	}

	public void setTrainingData(ArrayList<eMail> trainingData) {
		this.trainingData = trainingData;
	}

	public void setElasticity(double x){this.elasticity=x;}


}

package websocket;

import ch.unibe.scg.curtys.classifier.BinaryClassifier;
import ch.unibe.scg.curtys.classifier.Classifier;
import ch.unibe.scg.curtys.quality.QualityEstimator;
import ch.unibe.scg.curtys.vectorization.issue.Issue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author curtys
 */
public class Controller {

	private static Controller instance;
	private static Classifier classifier;
	private static QualityEstimator estimator;

	private Controller() {
		classifier = new BinaryClassifier();
		estimator = new QualityEstimator();
	}

	public static Controller instance() {
		if (instance == null) {
			instance = new Controller();
		}
		return instance;
	}

	public String predictClass(Issue issue) {
		return classifier.query(issue).getBestClassLabel();
	}

	public int estimateScore(Issue issue) {
		return estimator.score(issue);
	}

	public Result analyse(Issue issue) {
		Result res = new Result();
		res.issueType = predictClass(issue);

		int[] vec = estimator.createVector(issue);
		List<String> missingFeatures = new ArrayList<>();
		res.quality = estimator.score(estimator.usefulness(issue, vec));
		estimator.activationFeatures(vec).forEach((s, i) -> {
			if (i == 0) missingFeatures.add(s);
		});
		res.improvements = missingFeatures;
		return res;
	}

}

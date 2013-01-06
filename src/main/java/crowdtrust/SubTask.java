package crowdtrust;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
public abstract class SubTask {
	
	//threshold accuracy variance
	static final double THETA = 0.008;
	protected int id;
	protected double confidence_threshold;
	protected int number_of_labels;
	protected int max_labels;
	
	/*
	 * E step
	 * */
	
	public SubTask(int id, double confidence_threshold, 
			int number_of_labels, int max_labels){
		this.id = id;
		this.confidence_threshold = confidence_threshold;
		this.number_of_labels = number_of_labels;
		this.max_labels = max_labels;
	}
	
	public void addResponse(Bee annotator, Response r) {
		Response response = r;
		
		db.CrowdDb.addResponse(annotator.getId(), response.serialise(), this.id);
		Accuracy a = getAccuracy(annotator.getId());
		
		Collection<Estimate> state = getEstimates(id);
		updateLikelihoods(response,a,state);
		updateEstimates(state);
		
		Estimate z = estimate(state);
		number_of_labels++;
		if(z.getConfidence() > confidence_threshold || 
				number_of_labels >= max_labels){
			close();
			updateAccuracies(z.getR());
		}
	}
	
	protected abstract void updateEstimates(Collection<Estimate> state);

	protected abstract Collection<Estimate> getEstimates(int id);
	
	protected abstract void updateLikelihoods(Response r, 
			Accuracy a, Collection<Estimate> state);
	
	//returns best estimate
	protected Estimate estimate(Collection<Estimate> newState) {
		Estimate best = null;
		for (Estimate record : newState){
			if(best != null && record.getConfidence() > best.getConfidence())
				best = record;
		}
		return best;
	}
	
	/*
	 * M step
	 * */
	protected void updateAccuracies(Response z) {
		Bee [] annotators = db.CrowdDb.getAnnotators(id);
		AccuracyRecord [] accuracies = getAccuracies(annotators);
		Map <Integer, Response> responses = getResponses(annotators);
		
		Collection<Bee> experts = new ArrayList<Bee>();
		Collection<Bee> bots = new ArrayList<Bee>();
		
		for (AccuracyRecord r : accuracies){
			maximiseAccuracy(r.getAccuracy(), responses.get(r.getBee().getId()), z);
			if (r.getAccuracy().variance() < THETA){
				if (r.getAccuracy().expert(expertLimit()))
					experts.add(r.getBee());
				else
					bots.add(r.getBee());
			}
		}
		
		updateAccuracies(accuracies);
		updateExperts(experts);
		updateBots(bots);
	}
	
	protected abstract void updateExperts(Collection<Bee> experts);
	
	protected abstract void updateBots(Collection<Bee> bots);

	protected abstract double expertLimit();

	protected abstract void maximiseAccuracy(Accuracy a, Response response, Response z);
	
	/*
	 * Helper functions
	 * */
	protected abstract Map<Integer, Response> getResponses(Bee[] annotators);

	protected abstract AccuracyRecord[] getAccuracies(Bee[] annotators);
	
	protected abstract void updateAccuracies(AccuracyRecord [] accuracies);
	
	protected abstract Accuracy getAccuracy(int annotatorId);
	
	public void close(){
		db.SubTaskDb.close(id);
		Task parent = db.SubTaskDb.getTask(id);
		parent.notifyFinished();
	}

	//uniform distribution across all posibilities for the time being
	protected abstract double getZPrior();

	public String getHtml() {
		return Integer.toString(id);
	}
	
	public int getId(){
		return this.id;
	}

	protected abstract void addEstimate(Estimate e);
}

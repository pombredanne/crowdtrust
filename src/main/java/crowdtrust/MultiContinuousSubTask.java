package crowdtrust;

import java.util.Collection;

import be.ac.ulg.montefiore.run.distributions.MultiGaussianDistribution;

public class MultiContinuousSubTask extends ContinuousSubTask {
	
	int dimensions;
	int [][] ranges;
	double [][] variance;

	MultiContinuousSubTask(double precision, double [][] variance, int d, 
			int [][] ranges, int id, double confidence_threshold, 
			int number_of_labels, int max_labels){
		super(id, confidence_threshold, number_of_labels, max_labels);
		this.precision = precision;
		this.variance = variance;
		this.dimensions = d;
		this.ranges = ranges;
	}

	@Override
	protected void maximiseAccuracy(Accuracy a, Response r, Response z){
		SingleAccuracy sa = (SingleAccuracy) a;
		ContinuousMultiR cr = (ContinuousMultiR) r;
		ContinuousMultiR cz = (ContinuousMultiR) z;
		
		int total = a.getN();
		double w = total/total + 1;
		double alpha = sa.getAccuracy()*total;
		
		MultiGaussianDistribution mgd =
				new MultiGaussianDistribution(
						cz.getValues(precision), variance);
		double responseSpace = 1;
		for (int i = 0; i < ranges.length; i++){
			responseSpace *= (ranges[i][1] - ranges[i][0])*precision;
		}
		
		//mle
		double pLabel = mgd.probability(cr.getValues(precision));
		double mle = pLabel/(pLabel + 1/responseSpace);
		sa.setAccuracy(w*(alpha/total) + (1-w)*mle);
		a.increaseN();
	}


	@Override
	protected void updateLikelihoods(Response r, Accuracy a,
			Collection<Estimate> state) {
		ContinuousMultiR cr = (ContinuousMultiR) r;
		SingleAccuracy sa = (SingleAccuracy) a;
		
		boolean matched = false;
		double responseSpace = 1;
		for (int i = 0; i < ranges.length; i++){
			responseSpace *= (ranges[i][1] - ranges[i][0])*precision;
		}
		
		MultiGaussianDistribution mgd =
				new MultiGaussianDistribution(
						cr.getValues(precision), variance);
			
		for (Estimate record : state){
			if(record.getR().equals(r)){
				matched = true;
			}
			ContinuousMultiR cr2 = (ContinuousMultiR) record.getR();
			double p = sa.getAccuracy()*mgd.probability(cr2.getValues(precision)) +
					(1 - sa.getAccuracy())/responseSpace;
			record.setConfidence(record.getConfidence() * (p/1-p));
		}
			
		if (!matched){
			Estimate e = new Estimate(r, getZPrior());
			double p = sa.getAccuracy()*mgd.probability(cr.getValues(precision)) +
					(1 - sa.getAccuracy())/responseSpace;
			e.setConfidence(e.getConfidence() * (p/1-p));
			state.add(e);
			addEstimate(e);
		}
	}

	@Override
	protected double getZPrior() {
		// TODO Auto-generated method stub
		double responseSpace = 1;
		for (int i = 0; i < ranges.length; i++){
			responseSpace *= (ranges[i][1] - ranges[i][0])*precision;
		}
		double p = 1/responseSpace;
		return p/(1-p);
	}

	@Override
	protected Collection<Estimate> getEstimates(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateEstimates(Collection<Estimate> state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addEstimate(Estimate e) {
		// TODO Auto-generated method stub
		
	}
}

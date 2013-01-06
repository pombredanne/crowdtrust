package crowdtrust;

import java.util.Arrays;

import org.apache.commons.math3.distribution.NormalDistribution;

public class SingleContinuousSubTask extends ContinuousSubTask {

	int [] range;
	double variance;
	
	SingleContinuousSubTask(double precision, double variance, 
			int [] range, int id, double confidence_threshold, 
			int number_of_labels, int max_labels){
		super(id, confidence_threshold, number_of_labels, max_labels);
		this.precision = precision;
		this.variance = variance;
		this.range = range;
	}
	
	@Override
	protected void maximiseAccuracy(Accuracy a, Response r, Response z){
		SingleAccuracy sa = (SingleAccuracy) a;
		ContinuousR cr = (ContinuousR) r;
		ContinuousR cz = (ContinuousR) z;
		
		int total = a.getN();
		double w = total/total + 1;
		double alpha = sa.getAccuracy()*total;
		
		NormalDistribution nd = 
				new NormalDistribution(
						cz.getValue(precision), Math.sqrt(variance));
		
		double responseSpace = (range[1] - range[0])*precision;
		
		//mle
		double pLabel = nd.density(cr.getValue(precision));
		double mle = pLabel/(pLabel + 1/responseSpace);
		sa.setAccuracy(w*(alpha/total) + (1-w)*mle);
		a.increaseN();
	}
	
	@Override
	protected Estimate[] updateLikelihoods(Response r, Accuracy a,
			Estimate[] state) {
		ContinuousR cr = (ContinuousR) r;
		SingleAccuracy sa = (SingleAccuracy) a;
		
		boolean matched = false;
		Estimate [] newState;
		
		NormalDistribution nd = 
				new NormalDistribution(
						cr.getValue(precision), Math.sqrt(variance));
			
		double pResponseSpace = 1/(range[1] - range[0])*precision;
			
		for (Estimate record : state){
			if(record.r.equals(r)){
				matched = true;
			}
			ContinuousR cr2 = (ContinuousR) record.r;
			double p = sa.getAccuracy()*nd.density(cr2.getValue(precision)) + 
				(1-sa.getAccuracy())*pResponseSpace;
			record.confidence *= p/(1-p);
		}
			
		if (!matched){
			newState = Arrays.copyOf(state, state.length+1);
			Estimate e = new Estimate(r, getZPrior());
			double p = sa.getAccuracy()*nd.density(cr.getValue(precision)) +
					(1-sa.getAccuracy())*pResponseSpace;
			e.confidence *= p/1-p;
			newState[newState.length] = e;
		} else {
			newState = state.clone();
		}
		
		return newState;
	}

	@Override
	protected double getZPrior() {
		double p = 1/(range[1] - range[0])*precision;
		return p/(1-p);
	}
}

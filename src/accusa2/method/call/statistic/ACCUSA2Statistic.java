package accusa2.method.call.statistic;

import umontreal.iro.lecuyer.probdistmulti.DirichletDist;
import accusa2.cli.parameters.StatisticParameters;
import accusa2.estimate.BayesEstimateParameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

/**
 * 
 * @author michael
 * 
 * Uses the matching coverage to calculate the test-statistic.
 * Tested if distributions are equal.
 * Same as in ACCUSA2 paper
 */
public class ACCUSA2Statistic implements StatisticCalculator {

	protected final StatisticParameters parameters;
	protected final BayesEstimateParameters estimateParameters;
	protected final BaseConfig baseConfig;

	public ACCUSA2Statistic(final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.parameters = parameters;

		int k = baseConfig.getK();

		Phred2Prob phred2Prob = Phred2Prob.getInstance(k);
		estimateParameters = new BayesEstimateParameters(0.0, phred2Prob);
		this.baseConfig = baseConfig;
	}

	@Override
	public StatisticCalculator newInstance() {
		return new ACCUSA2Statistic(baseConfig, parameters);
	}

	public double getStatistic(final ParallelPileup parallelPileup) {
		// use all bases for calculation
		final int baseIs[] = {0, 1, 2, 3};
		// use only observed bases per parallelPileup
		//final int bases[] = parallelPileup.getPooledPileup().getAlleles();

		// first sample
		// probability matrix for all pileups in sampleA (bases in column, pileups in rows)
		final double[][] probsA = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileupsA());
		final DirichletDist dirichletA = getDirichlet(baseIs, parallelPileup.getPileupsA());
		final double densityAA = getDensity(baseIs, probsA, dirichletA);

		// second sample - see above
		final double[][] probsB = estimateParameters.estimateProbs(baseIs, parallelPileup.getPileupsB());
		final DirichletDist dirichletB = getDirichlet(baseIs, parallelPileup.getPileupsA());
		final double densityBB = getDensity(baseIs, probsB, dirichletB);

		// null model - distributions are the same
		final double densityAB = getDensity(baseIs, probsB, dirichletA);
		final double densityBA = getDensity(baseIs, probsA, dirichletB);

		// calculate statistic z = log 0_Model - log A_Model 
		final double z = (densityAA + densityBB) - (densityAB + densityBA);

		// use only positive numbers
		return Math.max(0, z);
	}

	/**
	 * Calculate the density for probs given dirichlet.
	 * @param dirichlet
	 * @param probs
	 * @return
	 */
	protected double getDensity(final int[] baseIs, final double[][] probs, final DirichletDist dirichlet) {
		double density = 0.0;

		// log10 prod = sum log10
		for(int i = 0; i < probs.length; ++i) {
			density += Math.log10(Math.max(Double.MIN_VALUE, dirichlet.density(probs[i])));
		}

		return density;
	}

	protected DirichletDist getDirichlet(final int[] baseIs, final Pileup[] pileups) {
		final double[] alpha = estimateParameters.estimateAlpha(baseIs, pileups);
		return new DirichletDist(alpha);
	}

	@Override
	public boolean filter(double value) {
		return parameters.getStat() > value;
	}

	@Override
	public String getDescription() {
		return "ACCUSA2 statistic: Z=log10( Dir(alpha_A; phi_A) * Dir(alpha_B; phi_B) ) - log10( Dir(alpha_A; phi_B) * Dir(alpha_B; phi_A) )";
	}

	@Override
	public String getName() {
		return "ACCUSA2";
	}
	
}
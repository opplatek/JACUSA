package jacusa.method.call.statistic.dirmult.initalpha;

import jacusa.pileup.Pileup;

public class CombinedAlphaInit extends AbstractAlphaInit {

	private AbstractAlphaInit A;
	private AbstractAlphaInit B;
	
	public CombinedAlphaInit(String name, AbstractAlphaInit A, AbstractAlphaInit B) {
		super(name, A.getName() + " + " + B.getName());
		this.A = A;
		this.B = B;
	}

	@Override
	public AbstractAlphaInit newInstance(String line) {
		
		return new CombinedAlphaInit(getName(), A, B);
	}
	
	@Override
	public double[] init(
			final int[] baseIs,
			final Pileup[] pileups,
			final double[][] pileupMatrix) {

		return A.init(baseIs, pileups, pileupMatrix);
	}

	@Override
	public double[] init(
			final int[] baseIs, 
			final Pileup pileup, 
			final double[] pileupVector,
			final double[] pileupErrorVector) {
		return B.init(baseIs, pileup, pileupVector, pileupErrorVector);
	}
		
}

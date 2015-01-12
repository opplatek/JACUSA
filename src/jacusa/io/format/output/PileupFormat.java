package jacusa.io.format.output;

import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.DefaultPileup.STRAND;
import net.sf.samtools.SAMUtils;

public class PileupFormat extends AbstractOutputFormat {

	public static char EMPTY 	= '*';
	public static char COMMENT = '#';
	public static char SEP 	= '\t';
	public static char SEP2 	= ',';

	private BaseConfig baseConfig;

	public PileupFormat(BaseConfig baseConfig) {
		super('M', "samtools mpileup like format (base columns without: $ ^ < > *)");
		this.baseConfig = baseConfig;
	}

	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		StringBuilder sb = new StringBuilder();

		// coordinates
		sb.append(parallelPileup.getContig());
		sb.append(SEP);
		sb.append(parallelPileup.getPosition());

		addPileups(sb, parallelPileup.getStrand1(), parallelPileup.getPileups1());
		addPileups(sb, parallelPileup.getStrand2(), parallelPileup.getPileups2());

		return sb.toString();		
	}
	
	protected void addPileups(StringBuilder sb, STRAND strand, Pileup[] pileups) {
		sb.append(SEP);
		sb.append(strand.character());
		
		for(Pileup pileup : pileups) {

			sb.append(SEP);
			sb.append(pileup.getCoverage());
			sb.append(SEP);
			
			for (int base : pileup.getAlleles()) {
				// print bases 
				for (int i = 0; i < pileup.getCounts().getBaseCount()[base]; ++i) {
					sb.append(baseConfig.getBases()[base]);
				}
			}

			sb.append(SEP);

			// print quals
			for (int base : pileup.getAlleles()) {
				for (byte qual = 0; qual < Phred2Prob.MAX_Q; ++qual) {

					int count = pileup.getCounts().getQualCount(base, qual);
					if (count > 0) {
						// repeat count times
						for (int j = 0; j < count; ++j) {
							sb.append(SAMUtils.phredToFastq(qual));
						}
					}
				}
			}
		}
	}
	
	@Override
	public char getCOMMENT() {
		return COMMENT;
	}

	@Override
	public char getEMPTY() {
		return EMPTY;
	}

	@Override
	public char getSEP() {
		return SEP;
	}

	@Override
	public char getSEP2() {
		return SEP2;
	}

}
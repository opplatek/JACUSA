package accusa2.filter.cache;

import java.util.Arrays;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

import accusa2.cli.Parameters;
import accusa2.pileup.builder.DefaultWindowCache;

public abstract class AbstractPileupBuilderFilterCache {

	private char c;
	protected DefaultWindowCache cache;
	protected boolean[] visited;
	protected Parameters parameters;

	public AbstractPileupBuilderFilterCache(char c, Parameters parameters) {
		this.c = c;
		this.parameters = parameters;

		int windowSize = parameters.getWindowSize();
		int baseLength = parameters.getBaseConfig().getBases().length;

		cache = new DefaultWindowCache(windowSize, baseLength);
		Arrays.fill(visited, false);
	}

	public void processCigar(int windowStart, Cigar cigar, SAMRecord record) {
		// init
		int readPosition 	= 0;
		int genomicPosition = record.getAlignmentStart();
		int windowPosition = genomicPosition - windowStart;
		Arrays.fill(visited, false);

		// process CIGAR -> SP, INDELs
		for(final CigarElement cigarElement : record.getCigar().getCigarElements()) {

			switch(cigarElement.getOperator()) {

			/*
			 * handle insertion
			 */
			case I:
				processInsertion(windowPosition, readPosition, genomicPosition, cigarElement, record);
				readPosition += cigarElement.getLength();
				break;

			/*
			 * handle alignment/sequence match and mismatch
			 */
			case M:
			case EQ:
			case X:
				processAlignmetMatch(windowPosition, readPosition, genomicPosition, cigarElement, record);
				readPosition += cigarElement.getLength();
				genomicPosition += cigarElement.getLength();
				break;

			/*
			 * handle hard clipping 
			 */
			case H:
				processHardClipping(windowPosition, readPosition, genomicPosition, cigarElement, record);
				break;

			/*
			 * handle deletion from the reference and introns
			 */
			case D:
				processDeletion(windowPosition, readPosition, genomicPosition, cigarElement, record);
				genomicPosition += cigarElement.getLength();
				break;

			case N:
				processSkipped(windowPosition, readPosition, genomicPosition, cigarElement, record);
				genomicPosition += cigarElement.getLength();
				break;

			/*
			 * soft clipping
			 */
			case S:
				processSoftClipping(windowPosition, readPosition, genomicPosition, cigarElement, record);
				readPosition += cigarElement.getLength();
				break;

			/*
			 * silent deletion from padded sequence
			 */
			case P:
				processPadding(windowPosition, readPosition, genomicPosition, cigarElement, record);
				break;

			default:
				throw new RuntimeException("Unsupported Cigar Operator: " + cigarElement.getOperator().toString());
			}
		}
	}
	
	public final char getC() {
		return c;
	}

	public DefaultWindowCache getCache() {
		return cache;
	}

	protected void fillCache(int windowPosition, int length, int readPosition, int genomicPosition, SAMRecord record) {
		int end = Math.min(cache.getWindowSize(), windowPosition + length);

		for (int i = 0; i < length && windowPosition < end && readPosition < record.getReadLength(); ++i) {
			windowPosition += i;
			readPosition += i;
			
			if (! visited[windowPosition]) {
				int baseI = parameters.getBaseConfig().getBaseI(record.getReadBases()[readPosition]);
				byte qual = record.getBaseQualities()[readPosition];
				cache.add(windowPosition, baseI, qual);
			}
		}
	}

	protected void processInsertion(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {}

	protected void processAlignmetMatch(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {}
	
	protected void processHardClipping(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		System.err.println("Hard Clipping not handled yet!");
	}

	protected void processDeletion(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {}

	protected void processSkipped(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {}

	protected void processSoftClipping(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {}

	protected void processPadding(int windowPosition, int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		System.err.println("Padding not handled yet!");
	}

}
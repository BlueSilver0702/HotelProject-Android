package serverutil;

public class Fraction {
	public int integer;
	public int numerator;
	public int denominator;
	
	public Fraction() {
		integer = 0;
		numerator = 0;
		denominator = 1;
	}
	
	public Fraction (int inte, int nume, int deno) {
		integer = inte;
		numerator = nume;
		denominator = deno;
	}
	
	public Fraction copy() {
		return new Fraction(integer, numerator, denominator);
	}
	
	public double value () {
		return integer+(double)numerator/(double)denominator;
	}
	
	public Fraction plusN(Fraction other) {
		int nInteger = 0;
		int nNumerator = 0;
		int nDenominator = 1;
		nInteger = other.integer + integer;
		int lcm = this.LCM(denominator, other.denominator);
		nDenominator = lcm;
		nNumerator = (numerator+denominator*integer)*lcm/other.denominator + (other.numerator+other.denominator*other.integer)*lcm/denominator;
		if (nNumerator > lcm) {
			nInteger ++;
			nNumerator -= lcm;
		}
		if (nNumerator == nDenominator) {
			nInteger ++;
			nNumerator = 0;
			nDenominator = 1;
		}
		return new Fraction(nInteger, nNumerator, nDenominator);
	}

	public void plus(Fraction other) {
		int nInteger = 0;
		int nNumerator = 0;
		int nDenominator = 1;
//		nInteger = other.integer + integer;
		int lcm = this.LCM(denominator, other.denominator);
		nDenominator = lcm;
		nNumerator = (numerator+denominator*integer)*lcm/other.denominator + (other.numerator+other.denominator*other.integer)*lcm/denominator;
		if (nNumerator > lcm) {
			nInteger ++;
			nNumerator -= lcm;
		}
		if (nNumerator == nDenominator) {
			nInteger ++;
			nNumerator = 0;
			nDenominator = 1;
		}
		integer = nInteger;
		numerator = nNumerator;
		denominator = nDenominator;
	}

	public void plusM(Fraction other) {
		int nInteger = 0;
		int nNumerator = 0;
		int nDenominator = 1;
		if (other.integer >= 1) {
			integer ++;
			return;
		}
		nInteger = other.integer + integer;
		int lcm = this.LCM(denominator, other.denominator);
		nDenominator = lcm;
		nNumerator = (numerator+denominator*integer)*lcm/other.denominator + (other.numerator+other.denominator*other.integer)*lcm/denominator;
		if (nNumerator > lcm) {
			nInteger ++;
			nNumerator -= lcm;
		}
		if (nNumerator == nDenominator) {
			nInteger ++;
			nNumerator = 0;
			nDenominator = 1;
		}
		integer = nInteger;
		numerator = nNumerator;
		denominator = nDenominator;
	}

	public void divide(int num) {
		if (this.value() >=1 ) {
			integer = 0;
			numerator = 1;
			denominator = num;
			return;
		}
		int total_num = integer*denominator+numerator;
		denominator *= num;
		if (total_num>denominator) {
			numerator = total_num%denominator;
			integer = (int) Math.floor(total_num/denominator);
		} else {
			integer = 0;
			numerator = total_num;
		}
	}
	
	public Fraction divideN(int num) {
		
		if (this.value() >= 1) {
			return new Fraction(0, 1, num);
		} else {
			Fraction retObj = new Fraction(integer, numerator, denominator);
			int total_num = integer*denominator+numerator;
			retObj.denominator *= num;
			if (total_num>retObj.denominator) {
				retObj.numerator = total_num%retObj.denominator;
				retObj.integer = (int) Math.floor(total_num/retObj.denominator);
			} else {
				retObj.integer = 0;
				retObj.numerator = total_num;
			}
			return retObj;
		}		
	}
	
	public int LCM (int a, int b) {
		int larger = a>b ? a:b;
		int smaller = a>b ? b:a;
		int candidate = larger;
		while (candidate % smaller !=0) candidate += larger;
		return candidate;
	}
}

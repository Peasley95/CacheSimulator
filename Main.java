import java.io.*;
import com.google.common.math.IntMath;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
/*
 * @author Alex Peasley
 * @version 18/03/2018
 *
 * For this to work a jar file from Google to access their Guava API needs to be added to the build path
 * I was going to include this jar file in the lib file of the project but it made the zip file too large to submit
 *
 */
public class Main{
	
	// Bits in a word
	private static int W;
	// Data bytes in cache
	private static int C;
	// Bytes in one block
	private static int B;
	// Lines in a block
	private static int k;
	// Number of blocks 
	private static int blocks;
	// Lines in cache
	private static int lines;
	// Number of bits in the offset
	private static int offset;
	// Number of bits of index
	private static int indexSize;
	// Size of the tag
	private static int tagSize;
	// The cache
	private static int[] cache;
	// Stores an array of the inputs in binary
	private String[] input;
	
	public Main(String filename) {
		// This buffered reader is just to count the number of lines in the file
		BufferedReader initialise_input = null;
		// This one will be used with the scanner
		BufferedReader r = null;
		try {
			initialise_input = new BufferedReader(new FileReader(filename));
			// -1 to account for the first line of instructions
			int lines = -1;
			try {
				while(initialise_input.readLine()!=null) {
					lines++;
				}
				input = new String[lines];
				initialise_input.close();
			} catch (IOException e) {/*do nothing*/}
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			System.exit(0);
		}
		
		try {
			r = new BufferedReader(new FileReader(filename));
			// A scanner obj to differentiate between different elements of the file
			Scanner scan = new Scanner(r);
			
			// Fill the fields with the first 4 tokens
			W = scan.nextInt();
			C = scan.nextInt();
			B = scan.nextInt();
			k = scan.nextInt();
			blocks = C/B;
			lines = BigInteger.valueOf(k).multiply(BigInteger.valueOf(C)).divide(BigInteger.valueOf(B)).intValue();
			// Using Google Guava so that I can use sqrt and log on an int
			// Number of bits in offset
			offset = IntMath.log2((B/k), RoundingMode.UNNECESSARY);
			// Size of the offset
			indexSize = IntMath.log2(blocks,RoundingMode.UNNECESSARY);
			tagSize = W - offset - indexSize;
			cache = new int[lines];

			for(int i = 0; i < cache.length; i++) {
					// This was a good initial value as 0 could be part of the ref string
					cache[i] = 999;
			}
			
			// Loop to fill the input array with the input binary strings
			for(int i=0;scan.hasNext();i++) {
				input[i] = convertBinary(new BigInteger(scan.next()));
				if(input[i].length() != W) {
					String zero = "0";
					String newBinary = input[i];
					for(int j = 0;j < W - input[i].length();j++) {
						newBinary = zero + newBinary;
					}
					input[i] = newBinary;
				}
			}
			// Close the scanner
			scan.close();
			try {
				// Close the buffered reader 
				r.close();
			} catch (IOException e) {/*No message required*/}
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			System.exit(0);
		}
		
	}

	public static void simCache(String[] input) {
		int[] refString = getRefString(input);
		 
		 
		for(int i = 0; i < refString.length; i++) {
			 for(int j = 0;j < cache.length;j++) {
				 //If cache is empty, add the ref to the cache from memory and print m
				if(cache[j] == 999) {
					System.out.print("M");
					cache[j] = refString[i];
					break;
				} else
				if(refString[i] == cache[j]) {
					if (j>0) moveCacheElement(j,cache[j]);
					System.out.print("C");
					break;
				} else 
				if(j == cache.length - 1) {
					evictAndPlace(j,refString[i]);
					System.out.print("M");
					break;
				}	
			}
		}
	}

	static int[] getRefString(String[] input) {
		int[] refString = new int[input.length];
		long character;
		String zero ="0";
		for(int i = 0; i < input.length; i++) {
			if(input[i].length() < W) {
				for(int j = input[i].length() - offset -1; j < input[i].length(); j++) {
					input[i] = zero + input[i];
				}
				character = Long.parseLong(input[i].substring(0, tagSize-1),2);
				refString[i] = (int) character;
			} else {
				character = Long.parseLong(input[i].substring(0,tagSize-1),2);
				refString[i] = (int) character;
			}
		}
		return refString;
	}
	
	static String getIndex(int input) {
		String bin = convertBinary(input);
		String index = bin.substring(tagSize, tagSize+indexSize);
		System.out.print(index);
		return index;
	}
	
	static void evictAndPlace(int index, int newCache) {
		for (int i = 0; i < index; i++) {
			if(cache[i+1] != 999) cache[i] = cache[i+1];
		}
		cache[index] = newCache;
	}
	
	static void moveCacheElement(int index, int hitCache) {
		for (int i = index; i < cache.length - 1; i++) {
			if(cache[i+1] == 999) {
				cache[i] = hitCache;
				break;
			} else
				if(i == cache.length - 2) {
					cache[cache.length - 1] = hitCache;
				} else
				{
					cache[i] = cache[i+1];
				}
		}
	}

	static String convertBinary(int s) {
		// Using a radix of 16 in BigInt and 2 in toString converts to binary
		return Integer.toBinaryString(s);
	}

	static String convertBinary(BigInteger s) {
		// Using a radix of 16 in BigInt and 2 in toString converts to binary
		return s.toString(2);
	}

	public static void main(String[] args) {
		// Simulates the cache on all of the input files.
		System.out.println("File 1:");
		Main run00 = new Main("files/00-abp9-directmapped.in");
		simCache(run00.input);
		System.out.println(); System.out.println("File 2:");
		Main run01 = new Main("files/01-abp9-directmapped.in");
		simCache(run01.input);
		System.out.println(); System.out.println("File 3:");
		Main run02 = new Main("files/02-abp9-directmapped.in");
		simCache(run02.input);
		System.out.println(); System.out.println("File 4:");
		Main run03 = new Main("files/03-abp9-directmapped.in");
		simCache(run03.input);
		System.out.println(); System.out.println("File 5:");
		Main run04 = new Main("files/04-abp9-directmapped.in");
		simCache(run04.input);
		System.out.println(); System.out.println("File 6:");
		Main run05 = new Main("files/05-abp9-directmapped.in");
		simCache(run05.input);
		System.out.println(); System.out.println("File 7:");
		Main run06 = new Main("files/06-abp9-fullassoc.in");
		simCache(run06.input);
		System.out.println(); System.out.println("File 8:");
		Main run07 = new Main("files/07-abp9-fullassoc.in");
		simCache(run07.input);
		System.out.println(); System.out.println("File 9:");
		Main run08 = new Main("files/08-abp9-fullassoc.in");
		simCache(run08.input);
		System.out.println(); System.out.println("File 10:");
		Main run09 = new Main("files/09-abp9-fullassoc.in");
		simCache(run09.input);
		System.out.println(); System.out.println("File 11:");
		Main run10 = new Main("files/10-abp9-fullassoc.in");
		simCache(run10.input);
		System.out.println(); System.out.println("File 12:");
		Main run11 = new Main("files/11-abp9-fullassoc.in");
		simCache(run11.input);
		System.out.println(); System.out.println("File 13:");
		Main run12 = new Main("files/12-abp9-setassoc.in");
		simCache(run12.input);
		System.out.println(); System.out.println("File 14:");
		Main run13 = new Main("files/13-abp9-setassoc.in");
		simCache(run13.input);
		System.out.println(); System.out.println("File 15:");
		Main run14 = new Main("files/14-abp9-setassoc.in");
		simCache(run14.input);
		System.out.println(); System.out.println("File 16:");
		Main run15 = new Main("files/15-abp9-setassoc.in");
		simCache(run15.input);
		System.out.println(); System.out.println("File 17:");
		Main run16 = new Main("files/16-abp9-setassoc.in");
		simCache(run16.input);
		System.out.println(); System.out.println("File 18:");
		Main run17 = new Main("files/17-abp9-setassoc.in");
		simCache(run17.input);
		System.out.println(); System.out.println("File 19:");
		Main run18 = new Main("files/18-abp9-big.in");
		simCache(run18.input);
		System.out.println(); System.out.println("File 20:");
		Main run19 = new Main("files/19-abp9-big.in");
		simCache(run19.input);
		System.out.println();
	}
}



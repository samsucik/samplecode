import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

public class Palindromes {
	
	public static void main(String[] args) {
		System.out.print("Type your word(s): ");
		// read the input string
		Scanner inputReader = new Scanner(System.in);
		String input = inputReader.nextLine();
		inputReader.close();
		
		ArrayList<String> palindromesFound = new ArrayList<String>();
		int rightBound = 0, leftBound = 0;
		
		// inspect every substring of the input
		for (leftBound = 0; leftBound < input.length()-1; leftBound++){			
			for (rightBound = leftBound + 1; rightBound < input.length()+1; rightBound++){
				String substringToInspect = input.substring(leftBound,rightBound);
				// when a palindrome is found, add it to the collection of palindromes
				if ( isPalindrome(substringToInspect) ){
					palindromesFound.add(substringToInspect);
				}
			}			
		}
		
		// converting to a Set removes duplicates
		Set<String> uniquePalindromeSubstrings = new HashSet<String>(palindromesFound);
		
		// print the results
		System.out.println(uniquePalindromeSubstrings.size() + " palindromes found:\n" + uniquePalindromeSubstrings);
	}

	// tell if a given string is a palindrome
	public static boolean isPalindrome(String word){
		int lowerLimit = 0, upperLimit = word.length()-1;
		while (lowerLimit < upperLimit){
			if (word.charAt(lowerLimit) != word.charAt(upperLimit)) return false;
			lowerLimit++;
			upperLimit--;
		}
		return true;
	}
}

package student;

import edu.willamette.cs1.spellingbee.SpellingBeeGraphics;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;

public class SpellingBee {
    private static final String ENGLISH_DICTIONARY = "res/EnglishWords.txt";
    private static final Pattern PUZZLE_VALIDATION_REGEX = Pattern.compile("[a-zA-Z]");
    private static final int PUZZLE_LENGTH = 7;
    private static final int WORD_BONUS = PUZZLE_LENGTH;
    private static final int MIN_WORD_LENGTH = 4;
    private final Map<String, Boolean> dictionary = new HashMap<>();
    private final List<String> validDictionaryWords = new ArrayList<>();
    private final List<String> enteredWords = new ArrayList<>();
    private SpellingBeeGraphics sbg;
    private Pattern currentWordValidationRegex = Pattern.compile("");
    private String[] currentWordLetters = null;
    private int totalScore = 0;

    public SpellingBee() {
        loadDictionaryFile();
    }

    public void run() {
        sbg = new SpellingBeeGraphics();
        sbg.addField("Puzzle", (s) -> puzzleAction(s));
        sbg.addField("Word", (s) -> userSolveAction(s));
        sbg.addButton("Solve", (s) -> solveAction());
    }

    private boolean validatePuzzleLength(String s) {
        if (s.length() != PUZZLE_LENGTH) {
            sbg.showMessage("The puzzle string must be exactly 7 characters", Color.RED);
            return false;
        }
        return true;
    }

    private boolean validatePuzzleContents(String s) {
        boolean isValid = PUZZLE_VALIDATION_REGEX.matcher(s).find();
        if (!isValid) {
            sbg.showMessage("The puzzle string must only consist of letters", Color.RED);
        }

        Set<String> chars = new HashSet<>();
        for (String c: s.split("")) {
            if (chars.contains(c)) {
                sbg.showMessage("The puzzle string must consist of unique letters", Color.red);
                return false;
            }
            chars.add(c);
        }

        return isValid;
    }

    private void puzzleAction(String s) {
        if (!validatePuzzleLength(s)) {
            return;
        }

        if (!validatePuzzleContents(s)) {
            return;
        }

        sbg.setBeehiveLetters(s);
        initDictionary();
    }

    private boolean wordUsesAllLetters(String word) {
        return Arrays.stream(currentWordLetters).allMatch((s) -> word.toLowerCase().contains(s));
    }

    private int getWordScore(String word) {
        boolean shouldGetBonus = wordUsesAllLetters(word);
        return (word.length() == MIN_WORD_LENGTH ? 1 : word.length()) + (shouldGetBonus ? WORD_BONUS : 0);
    }

    private boolean wordContainsCenterLetter(String word) {
        return word.toLowerCase().contains(sbg.getBeehiveLetters().substring(0, 1).toLowerCase());
    }

    private boolean isValidWord(String word) {
        if (MIN_WORD_LENGTH > word.length()) {
            return false;
        }
        long regexMatchCount = currentWordValidationRegex.matcher(word.toLowerCase()).results().count();
        boolean matchesRegex = regexMatchCount == word.length();
        boolean hasMiddleLetter = wordContainsCenterLetter(word);
        return matchesRegex && hasMiddleLetter;
    }

    private void initState() {
        currentWordValidationRegex = Pattern.compile(String.format("[%s]", sbg.getBeehiveLetters().toLowerCase()));
        currentWordLetters = sbg.getBeehiveLetters().toLowerCase().split("");
        totalScore = 0;
        sbg.clearWordList();
        enteredWords.clear();
        sbg.showMessage("");
    }

    private void loadDictionaryFile() {
        try {
            File dictionaryFile = new File(ENGLISH_DICTIONARY);
            Scanner dictionaryScanner = new Scanner(dictionaryFile);

            while (dictionaryScanner.hasNextLine()) {
                String word = dictionaryScanner.nextLine();
                dictionary.put(word, false);
            }
        } catch (FileNotFoundException e) {
            System.out.println("failed to load english dictionary");
            System.exit(0);
        }
    }

    private void initDictionary() {
        validDictionaryWords.clear();
        initState();

        dictionary.forEach((word, valid) -> {
            boolean isValid = isValidWord(word);
            dictionary.put(word, isValid);
            if (isValid) {
                validDictionaryWords.add(word);
            }
        });
    }

    private void addWord(String word) {
        int score = getWordScore(word);
        totalScore += score;
        boolean isBonusWord = wordUsesAllLetters(word);
        sbg.addWord(String.format("%s (%d)", word, score), isBonusWord ? Color.blue : Color.black);
        enteredWords.add(word);
        String pluralWord = enteredWords.size() > 1 ? "s" : "";
        String pluralScore = totalScore > 1 ? "s" : "";
        String msg = String.format("%d word%s; %d point%s", enteredWords.size(), pluralWord, totalScore, pluralScore);
        sbg.showMessage(msg);
    }

    private void solveAction() {
        initState();
        for (String word: validDictionaryWords) {
            if (!enteredWords.contains(word)) {
                addWord(word);
            }
        }
    }

    private void userSolveAction(String s) {
        if (MIN_WORD_LENGTH > s.length()) {
            sbg.showMessage("The word does not include at least four letters", Color.red);
            return;
        }

        if (!wordContainsCenterLetter(s)) {
            sbg.showMessage("The word does not include the center letter", Color.red);
            return;
        }

        Boolean isValidWord = dictionary.get(s);
        if (isValidWord == null) {
            sbg.showMessage("The word is not in the dictionary", Color.red);
            return;
        } else if (!isValidWord) {
            sbg.showMessage("The word includes letters not in the beehive", Color.red);
            return;
        }

        if (enteredWords.contains(s)) {
            sbg.showMessage("You have already found the word and is not allowed to be scored twice", Color.red);
            return;
        }

        addWord(s);
    }

    public static void main(String[] args) {
        new SpellingBee().run();
    }
}

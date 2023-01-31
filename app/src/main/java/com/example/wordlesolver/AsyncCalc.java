package com.example.wordlesolver;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class AsyncCalc extends AsyncTask<String, Void, List<String>> {
    String out;
    public static List<Character> knownLetters;
    public static List<String> words;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected List<String> doInBackground(String... params) {
        words = MainActivity.words;
        knownLetters = MainActivity.knownLetters;
        out = "";

        List<String> filteredWords = new ArrayList<>(MainActivity.filteredWords);
        String bestWord = params[0];
        String result = params[1];
        List<String> filtered = new ArrayList<>();
        for (int i = 0; i < filteredWords.size(); i++) {
            boolean isPossible = true;
            List<Character> letters = new ArrayList<>();
            List<Integer> numLetters = new ArrayList<>();
            List<Integer> usedPositions = new ArrayList<>();
            for (int j = 0; j < bestWord.length(); j++) {
                if (result.charAt(j) == '2' && (bestWord.charAt(j) != filteredWords.get(i).charAt(j))) {
                    //System.err.println("Filtered because correct letter was not matched: " + filteredWords.get(i) +  " at letter: " + filteredWords.get(i).charAt(j));
                    isPossible = false;
                    break;
                } else if (result.charAt(j) == '0' && filteredWords.get(i).contains(Character.toString(bestWord.charAt(j)))) {
                    for (int t = 0; t < bestWord.length(); t++) {
                        if (filteredWords.get(i).charAt(t) == bestWord.charAt(t) && result.charAt(t) == '0') {
                            //System.err.println("Filtered because it contains letter that is not in the word(1): " + filteredWords.get(i) + " at letter: " + filteredWords.get(i).charAt(j) + " " + isPossible);
                            isPossible = false;
                            break;
                        }
                    }
                    if (isPossible) {
                        for (int t = 0; t < bestWord.length(); t++) {
                            if (filteredWords.get(i).charAt(j) == bestWord.charAt(t) && result.charAt(j) == '0') {
                                isPossible = false;
                                for (int n = 0; n < bestWord.length(); n++) {
                                    if (filteredWords.get(i).charAt(j) == bestWord.charAt(n) && !usedPositions.contains(n)) {
                                        isPossible = true;
                                        usedPositions.add(n);
                                    }
                                }
                                if (!isPossible) {
                                    //System.err.println("Filtered because it contains letter that is not in the word(3): " + filteredWords.get(i) + " at letter: " + filteredWords.get(i).charAt(j) + " " + isPossible);
                                    break;
                                }
                            }
                        }
                    }
                } else if (result.charAt(j) == '1' && bestWord.charAt(j) == filteredWords.get(i).charAt(j)) {
                    //System.err.println("Filtered because it letter can't be at this position: " + filteredWords.get(i) + " at letter: " + filteredWords.get(i).charAt(j));
                    isPossible = false;
                    break;
                } else if (result.charAt(j) == '1' && !filteredWords.get(i).contains(Character.toString(bestWord.charAt(j)))) {
                    //System.err.println("Filtered because it letter is not in word(2): " + filteredWords.get(i) + " at letter: " + filteredWords.get(i).charAt(j));
                    isPossible = false;
                    break;
                } else {
                    //System.err.println(filteredWords.get(i));

                    boolean newLetter = true;
                    for (int k = 0; k < letters.size(); k++) {
                        if (letters.get(k) == filteredWords.get(i).charAt(j)) {
                            newLetter = false;
                            int inc = numLetters.get(k) + 1;
                            numLetters.set(k, inc);
                            break;
                        }
                    }
                    if (newLetter) {
                        letters.add(filteredWords.get(i).charAt(j));
                        numLetters.add(1);
                    }
                }
                if (!isPossible) {
                    break;
                }
            }

            List<Character> lettersB = new ArrayList<>();
            List<Integer> numLettersB = new ArrayList<>();

            //for (int t = 0; t < letters.size(); t++)
            //{
            //System.err.println("Letter: " + letters.get(t) + " Count: " + numLetters.get(t) + " Word: " + filteredWords.get(i));
            //}

            for (int j = 0; j < bestWord.length(); j++) {
                boolean newLetter = true;
                for (int k = 0; k < lettersB.size(); k++) {
                    if (lettersB.get(k) == bestWord.charAt(j)) {
                        newLetter = false;
                        int inc = numLettersB.get(k) + 1;
                        numLettersB.set(k, inc);
                        break;
                    }
                }

                if (newLetter) {
                    lettersB.add(bestWord.charAt(j));
                    numLettersB.add(1);
                }
            }

            for (int j = 0; j < lettersB.size(); j++) {
                for (int k = 0; k < letters.size(); k++) {
                    if (letters.get(k) == lettersB.get(j)) {
                        if (numLetters.get(k) < numLettersB.get(j) -1) {
                            isPossible = false;
                            //System.err.println("Filtered because number of letters is too small: " + filteredWords.get(i) + " at letter: " + filteredWords.get(i).charAt(j) + " | " + letters.get(k) + " " + numLetters.get(k) + " | " + lettersB.get(j) + " " + numLettersB.get(j));
                            break;
                        }
                    }
                }
            }


            if (isPossible) {
                filtered.add(filteredWords.get(i));
            }
        }

        if (knownLetters.size() == 0) {
            for (int i = 0; i < 6; i++) {
                knownLetters.add(' ');
            }
        }

        int x = 0;
        for (char c : result.toCharArray()) {
            if (c == '2') {
                knownLetters.set(x, bestWord.toCharArray()[x]);
            }
            x++;
        }

        try {
            out = solve(result, filtered);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        MainActivity.filteredWords = filtered;
        MainActivity.knownLetters = knownLetters;

        return filtered;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onPostExecute(List<String> filteredWords) {
        MainActivity.refresh.setEnabled(true);
        MainActivity.submit.setEnabled(true);

        if (filteredWords.size() == 1) {
            out = "Answer: " + filteredWords.get(0);
            MainActivity.submit.setEnabled(false);
        } else if (filteredWords.size() == 0) {
            out = "No possible words left!";
            MainActivity.submit.setEnabled(false);
        } else {
            MainActivity.ansWord.setText(out);
            MainActivity.ansNum.getText().clear();
            out = "Suggested Word: " + out;
            out += "\nRemaining possible words: ";
            filteredWords.forEach(str -> out += str + " ");
        }

        MainActivity.output.setText(out);
        //System.out.println(MainActivity.words.size() + " " + MainActivity.filteredWords.size());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String solve(String result, List<String> filteredWords) throws FileNotFoundException, UnsupportedEncodingException {
        //long startTime = System.nanoTime();
        //Random rng = new Random();
        //String test = words.get(rng.nextInt(words.size()));
        //System.err.println(test + " " + compare(test, "HANS"));

        float highScore = -10000;
        String bestWord = "";

        for (int i = 0; i < words.size(); i++) {
            float score = 1;
            for (int j = 0; j < filteredWords.size(); j++) {
                int temp = compare(words.get(i), filteredWords.get(j));
                score += temp;
            }

            if (filteredWords.contains(words.get(i))) {
                score += 1;
            }
            if (filteredWords.contains(words.get(i)) && filteredWords.size() < 4) {
                score += 1000;
            }

            if (score > highScore) {
                highScore = score;
                bestWord = words.get(i);
            }

            //System.out.println("Word: " + words.get(i) + " Score: " + score);
        }

        //long endTime   = System.nanoTime();
        //long totalTime = endTime - startTime;
        //System.out.println(totalTime);


        return bestWord;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static int compare(String s, String t) {
        int ans = 0;
        for (int i = 0; i < s.length(); i++) {
            //System.err.println("Current Letter: " + s.charAt(i) + " NumberOfLetters: " + numberOfLetters(t, s, s.charAt(i)) + " Pos: " + pos(s, s.charAt(i), i));
            if (s.charAt(i) == t.charAt(i)) {
                if (knownLetters.get(i) == ' ') {
                    ans += 3;
                }
            } else if (t.contains(Character.toString(s.charAt(i))) && pos(s, s.charAt(i), i) <= numberOfLetters(t, s, s.charAt(i))) {
                if (!knownLetters.contains(s.charAt(i))) {
                    ans += 2;
                }
            }
        }
        return ans;
    }

    public static int numberOfLetters(String s, String t, char c) {
        int num = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c && s.charAt(i) != t.charAt(i)) {
                num++;
            }
        }
        return num;
    }

    public static int pos(String s, char c, int n) {
        int pos = 0;
        for (int i = 0; i <= n; i++) {
            if (s.charAt(i) == c) {
                pos++;
            }
        }
        return pos;
    }
}

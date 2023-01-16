package com.example.wordlesolver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    public static List<String> words = new ArrayList<>();
    public static List<String> filteredWords = new ArrayList<>();
    public static List<Character> knownLetters = new ArrayList<>();
    public static Button submit;
    public static Button refresh;
    public static TextView output;
    public static EditText ansWord;
    public static EditText ansNum;
    public static String fileName = "wordsEng.txt";
    public static Spinner selectLanguage;

    public static List<String> readFiles(Context context) throws UnsupportedEncodingException, FileNotFoundException {
        AssetManager am = context.getAssets();
        InputStream file = null;
        try {
            file = am.open(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Scanner sc = new Scanner(file, StandardCharsets.UTF_8.name());
        List<String> words = new ArrayList<>();
        while (sc.hasNextLine()) {
            words.add(sc.nextLine());
        }
        //for(int i = 0; i < words.size(); i++)
        //{
        //	System.err.println(words.get(i));
        //}

        return words;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        submit = findViewById(R.id.submitBtn);
        ansNum = findViewById(R.id.inputAnswerTxt);
        ansWord = findViewById(R.id.inputWord);
        output = findViewById(R.id.outputTxt);
        refresh = findViewById(R.id.refreshBtn);

        selectLanguage = findViewById(R.id.spinner);
        if (savedInstanceState != null) {
            if (!(savedInstanceState.getString("output").equals(getString(R.string.suggested_starting_word_lares)) || savedInstanceState.getString("output").equals(getString(R.string.suggested_word_german))))  {
                selectLanguage.setEnabled(false);
            }
        }

        String[] arraySpinner = {"English", "German"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectLanguage.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        Object item = parent.getItemAtPosition(pos);
                        //System.out.println(item.toString());
                        if (item.toString().equals("German") && selectLanguage.isEnabled()) {
                            output.setText(R.string.suggested_word_german);
                            fileName = "wordsGer.txt";
                        } else if (item.toString().equals("English") && selectLanguage.isEnabled()) {
                            output.setText(R.string.suggested_starting_word_lares);
                            fileName = "wordsEng.txt";
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
        selectLanguage.setAdapter(adapter);

        refresh.setOnClickListener(v -> {
            try {
                words = readFiles(this);
            } catch (UnsupportedEncodingException | FileNotFoundException e) {
                e.printStackTrace();
            }
            filteredWords.clear();
            filteredWords.addAll(words);
            ansWord.getText().clear();
            ansNum.getText().clear();
            submit.setEnabled(true);
            selectLanguage.setEnabled(true);
            if (Objects.equals(fileName, "wordsEng.txt")) {
                output.setText(R.string.suggested_starting_word_lares);
            }
            else
            {
                output.setText(R.string.suggested_word_german);
            }
            System.out.println(words.size() + " " + filteredWords.size());
        });

        submit.setOnClickListener(v -> {
            try {
                if (filteredWords.size() == 0) {
                    try {
                        words = readFiles(this);
                        filteredWords.addAll(words);
                    } catch (UnsupportedEncodingException | FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println(words.size() + " " + filteredWords.size());
                String answerNum = ansNum.getText().toString();
                String answerWord = ansWord.getText().toString();
                selectLanguage.setEnabled(false);
                submit.setEnabled(false);
                refresh.setEnabled(false);
                output.setText(R.string.loading);

                if (answerNum.length() == 5 && answerNum.matches("[0-2]+") && answerWord.length() == 5 && answerWord.matches("[a-zA-Z]+")) {
                    AsyncCalc asyncCalc = new AsyncCalc();
                    String[] arr = new String[2];
                    arr[0] = answerWord.toLowerCase();
                    arr[1] = answerNum;
                    asyncCalc.execute(arr);
                } else {
                    output.setText(R.string.errorMessage);
                }
            } catch (Exception e) {
                output.setText(R.string.errorMessage);
                e.printStackTrace();
            }
        });

        if (savedInstanceState != null){
            ansNum.setText(savedInstanceState.get("ansNum").toString());
            ansWord.setText(savedInstanceState.get("ansWord").toString());
            output.setText(savedInstanceState.get("output").toString());
            selectLanguage.setSelection((Integer) savedInstanceState.get("selectLanguage"));
            //System.out.println("Save:" + savedInstanceState.get("output").toString() + " output: " + output.getText());
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("ansNum", ansNum.getText().toString());
        outState.putString("ansWord", ansWord.getText().toString());
        outState.putString("output", output.getText().toString());
        outState.putInt("selectLanguage", selectLanguage.getSelectedItemPosition());

        super.onSaveInstanceState(outState);
    }
}
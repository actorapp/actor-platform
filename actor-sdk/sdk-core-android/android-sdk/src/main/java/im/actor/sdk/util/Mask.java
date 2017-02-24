package im.actor.sdk.util;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.widget.EditText;

/**
 * Created by 98379720172 on 18/08/2016.
 */
public abstract class Mask {

    private static int positioning[] = { 1, 2, 3, 6, 7, 8, 9, 11, 12, 13, 14, 15 };

    private final static KeylistenerNumber keylistenerNumber = new KeylistenerNumber();

    private static class KeylistenerNumber extends NumberKeyListener {
        public int getInputType() {
            return InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;

        }
        @Override
        protected char[] getAcceptedChars() {
            return new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
        }
    }

    public static TextWatcher telephoneMask(final EditText ediTxt) {

        final int maxNumberLength = 11;

        ediTxt.setKeyListener(keylistenerNumber);
        ediTxt.setText("(--) ----- ----");

        ediTxt.postDelayed(()->{
            ediTxt.setSelection(1);
        },50);

        return new TextWatcher() {
            boolean isUpdating;
            String old = "";

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
                String current = s.toString();

                if (isUpdating) {
                    isUpdating = false;
                    return;

                }

                String number = current.replaceAll("[^0-9]*", "");
                if (number.length() > 11)
                    number = number.substring(0, 11);

                int length = number.length();

                String paddedNumber = padNumber(number, maxNumberLength);

                String ddd = "";
                String part1 = "";
                String part2 = "";
                String phone = "";

                if(length < 11){
                    ddd = paddedNumber.substring(0, 2);
                    part1 = paddedNumber.substring(2, 6);
                    part2 = paddedNumber.substring(6, paddedNumber.length()).trim();
                }else{
                    ddd = paddedNumber.substring(0, 2);
                    part1 = paddedNumber.substring(2, 7);
                    part2 = paddedNumber.substring(7, paddedNumber.length()).trim();
                }

                phone = "(" + ddd + ") " + part1 + " " + part2;

                isUpdating = true;

                ediTxt.setText(phone);
                ediTxt.setSelection(positioning[length]);
            }
        };
    }

    private static String padNumber(String number, int maxLength) {
        String padded = new String(number);
        for (int i = 0; i < maxLength - number.length(); i++)
            padded += "-";

        return padded;

    }

}

package io.github.tomszilagyi.svhu1972;

/* The purpose of using this class instead of android.util.Log is to
 * make its users testable. The problems with testing in the presence
 * of android.util.Log:
 * - calls to its static methods cannot be easily mocked;
 * - not mocking it results in a failed unit test.
 *
 * The problem with plain System.out.println is that its output does
 * not show up in logcat, so it is no use for 'production'. Even if it
 * would, it is not the idiomatic way of logging on Android.
 *
 * Hence this solution which is much uglier than I would like, but is
 * actually usable both from 'test' and 'production'. It is only used
 * where needed, so classes not used in unit tests directly use the
 * "normal" Log class.
 */
public class Log {
    public static void e(String tag, String message) {
        try {
            android.util.Log.e(tag, message);
        } catch (Exception e) {
            System.out.println("E: "+tag+": "+message);
        }
    }

    public static void i(String tag, String message) {
        try {
            android.util.Log.i(tag, message);
        } catch (Exception e) {
            System.out.println("I: "+tag+": "+message);
        }
    }
}


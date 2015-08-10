/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its external
 * form is a string wrapped in curly braces with colons between the names and
 * values, and commas between the values and names. The internal form is an
 * object having <code>get</code> and <code>opt</code> methods for accessing
 * the values by name, and <code>put</code> methods for adding or replacing
 * values by name. The values can be any of these types: <code>Boolean</code>,
 * <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>,
 * <code>String</code>, or the <code>JSONObject.NULL</code> object. A
 * JSONObject constructor can be used to convert an external form JSON text
 * into an internal form whose values can be retrieved with the
 * <code>get</code> and <code>opt</code> methods, or to convert values into a
 * JSON text using the <code>put</code> and <code>toString</code> methods. A
 * <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you. The opt methods differ from the get methods in that they
 * do not throw. Instead, they return a specified value, such as null.
 * <p>
 * The <code>put</code> methods add or replace values in an object. For
 * example,
 *
 * <pre>
 * myString = new JSONObject()
 *         .put(&quot;JSON&quot;, &quot;Hello, World!&quot;).toString();
 * </pre>
 *
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON syntax rules. The constructors are more forgiving in the texts they
 * will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a
 * quote or single quote, and if they do not contain leading or trailing
 * spaces, and if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>,
 * or <code>null</code>.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2015-05-05
 */
public class JSONObject {
    /**
     * JSONObject.NULL is equivalent to the value that JavaScript calls null,
     * whilst Java's null is equivalent to the value that JavaScript calls
     * undefined.
     */
    private static final class Null {

        /**
         * A Null object is equal to the null value and to itself.
         *
         * @param object
         *            An object to test for nullness.
         * @return true if the object parameter is the JSONObject.NULL object or
         *         null.
         */
        @Override
        public boolean equals(Object object) {
            return object == null || object == this;
        }

        /**
         * Get the "null" string value.
         *
         * @return The string "null".
         */
        public String toString() {
            return "null";
        }
    }

    /**
     * The map where the JSONObject's properties are kept.
     */
    private final Map<String, Object> map;

    /**
     * It is sometimes more convenient and less ambiguous to have a
     * <code>NULL</code> object than to use Java's <code>null</code> value.
     * <code>JSONObject.NULL.equals(null)</code> returns <code>true</code>.
     * <code>JSONObject.NULL.toString()</code> returns <code>"null"</code>.
     */
    public static final Object NULL = new Null();

    /**
     * Construct an empty JSONObject.
     */
    public JSONObject() {
        this.map = new HashMap<String, Object>();
    }

    /**
     * Construct a JSONObject from a subset of another JSONObject. An array of
     * strings is used to identify the keys that should be copied. Missing keys
     * are ignored.
     *
     * @param jo
     *            A JSONObject.
     * @param names
     *            An array of strings.
     * @throws JSONException
     * @exception JSONException
     *                If a value is a non-finite number or if a name is
     *                duplicated.
     */
    public JSONObject(JSONObject jo, String[] names) {
        this();
        for (int i = 0; i < names.length; i += 1) {
            try {
                this.putOnce(names[i], jo.opt(names[i]));
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Construct a JSONObject from a JSONTokener.
     *
     * @param x
     *            A JSONTokener object containing the source string.
     * @throws JSONException
     *             If there is a syntax error in the source string or a
     *             duplicated key.
     */
    public JSONObject(JSONTokener x) throws JSONException {
        this();
        char c;
        String key;

        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        for (;;) {
            c = x.nextClean();
            switch (c) {
            case 0:
                throw x.syntaxError("A JSONObject text must end with '}'");
            case '}':
                return;
            default:
                x.back();
                key = x.nextValue().toString();
            }

// The key is followed by ':'.

            c = x.nextClean();
            if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }
            this.putOnce(key, x.nextValue());

// Pairs are separated by ','.

            switch (x.nextClean()) {
            case ';':
            case ',':
                if (x.nextClean() == '}') {
                    return;
                }
                x.back();
                break;
            case '}':
                return;
            default:
                throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }

    /**
     * Construct a JSONObject from a Map.
     *
     * @param map
     *            A map object that can be used to initialize the contents of
     *            the JSONObject.
     * @throws JSONException
     */
    public JSONObject(Map<String, Object> map) {
        this.map = new HashMap<String, Object>();
        if (map != null) {
            Iterator<Entry<String, Object>> i = map.entrySet().iterator();
            while (i.hasNext()) {
                Entry<String, Object> entry = i.next();
                Object value = entry.getValue();
                if (value != null) {
                    this.map.put(entry.getKey(), value);
                }
            }
        }
    }

    /**
     * Construct a JSONObject from a source JSON text string. This is the most
     * commonly used JSONObject constructor.
     *
     * @param source
     *            A string beginning with <code>{</code>&nbsp;<small>(left
     *            brace)</small> and ending with <code>}</code>
     *            &nbsp;<small>(right brace)</small>.
     * @exception JSONException
     *                If there is a syntax error in the source string or a
     *                duplicated key.
     */
    public JSONObject(String source) throws JSONException {
        this(new JSONTokener(source));
    }

    /**
     * Accumulate values under a key. It is similar to the put method except
     * that if there is already an object stored under the key then a JSONArray
     * is stored under the key to hold all of the accumulated values. If there
     * is already a JSONArray, then the new value is appended to it. In
     * contrast, the put method replaces the previous value.
     *
     * If only one value is accumulated that is not a JSONArray, then the result
     * will be the same as using put. But if multiple values are accumulated,
     * then the result will be like append.
     *
     * @param key
     *            A key string.
     * @param value
     *            An object to be accumulated under the key.
     * @return this.
     * @throws JSONException
     *             If the value is an invalid number or if the key is null.
     */
    public JSONObject accumulate(String key, Object value) throws JSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key,
                    value instanceof JSONArray ? new JSONArray().put(value)
                            : value);
        } else if (object instanceof JSONArray) {
            ((JSONArray) object).put(value);
        } else {
            this.put(key, new JSONArray().put(object).put(value));
        }
        return this;
    }

    /**
     * Append values to the array under a key. If the key does not exist in the
     * JSONObject, then the key is put in the JSONObject with its value being a
     * JSONArray containing the value parameter. If the key was already
     * associated with a JSONArray, then the value parameter is appended to it.
     *
     * @param key
     *            A key string.
     * @param value
     *            An object to be accumulated under the key.
     * @return this.
     * @throws JSONException
     *             If the key is null or if the current value associated with
     *             the key is not a JSONArray.
     */
    public JSONObject append(String key, Object value) throws JSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, new JSONArray().put(value));
        } else if (object instanceof JSONArray) {
            this.put(key, ((JSONArray) object).put(value));
        } else {
            throw new JSONException("JSONObject[" + key
                    + "] is not a JSONArray.");
        }
        return this;
    }

    /**
     * Produce a string from a double. The string "null" will be returned if the
     * number is not finite.
     *
     * @param d
     *            A double.
     * @return A String.
     */
    public static String doubleToString(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return "null";
        }

// Shave off trailing zeros and decimal point, if possible.

        String string = Double.toString(d);
        if (string.contains(".") && !string.contains("e")
                && !string.contains("E")) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }

    /**
     * Get the value object associated with a key.
     *
     * @param key
     *            A key string.
     * @return The object associated with the key.
     * @throws JSONException
     *             if the key is not found.
     */
    public Object get(String key) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        Object object = this.opt(key);
        if (object == null) {
            throw new JSONException("JSONObject[" + key + "] not found.");
        }
        return object;
    }

    /**
     * Get the boolean value associated with a key.
     *
     * @param key
     *            A key string.
     * @return The truth.
     * @throws JSONException
     *             if the value is not a Boolean or the String "true" or
     *             "false".
     */
    public boolean getBoolean(String key) throws JSONException {
        Object object = this.get(key);
        if (object.equals(Boolean.FALSE)
                || (object instanceof String && ((String) object)
                        .equalsIgnoreCase("false"))) {
            return false;
        } else if (object.equals(Boolean.TRUE)
                || (object instanceof String && ((String) object)
                        .equalsIgnoreCase("true"))) {
            return true;
        }
        throw new JSONException("JSONObject[" + key
                + "] is not a Boolean.");
    }

    /**
     * Get the double value associated with a key.
     *
     * @param key
     *            A key string.
     * @return The numeric value.
     * @throws JSONException
     *             if the key is not found or if the value is not a Number
     *             object and cannot be converted to a number.
     */
    public double getDouble(String key) throws JSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number ? ((Number) object).doubleValue()
                    : Double.parseDouble((String) object);
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + key
                    + "] is not a number.");
        }
    }

    /**
     * Get the int value associated with a key.
     *
     * @param key
     *            A key string.
     * @return The integer value.
     * @throws JSONException
     *             if the key is not found or if the value cannot be converted
     *             to an integer.
     */
    public int getInt(String key) throws JSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number ? ((Number) object).intValue()
                    : Integer.parseInt((String) object);
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + key
                    + "] is not an int.");
        }
    }

    /**
     * Get the JSONArray value associated with a key.
     *
     * @param key
     *            A key string.
     * @return A JSONArray which is the value.
     * @throws JSONException
     *             if the key is not found or if the value is not a JSONArray.
     */
    public JSONArray getJSONArray(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        }
        throw new JSONException("JSONObject[" + key
                + "] is not a JSONArray.");
    }

    /**
     * Get the JSONObject value associated with a key.
     *
     * @param key
     *            A key string.
     * @return A JSONObject which is the value.
     * @throws JSONException
     *             if the key is not found or if the value is not a JSONObject.
     */
    public JSONObject getJSONObject(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        }
        throw new JSONException("JSONObject[" + key
                + "] is not a JSONObject.");
    }

    /**
     * Get the long value associated with a key.
     *
     * @param key
     *            A key string.
     * @return The long value.
     * @throws JSONException
     *             if the key is not found or if the value cannot be converted
     *             to a long.
     */
    public long getLong(String key) throws JSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number ? ((Number) object).longValue()
                    : Long.parseLong((String) object);
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + key
                    + "] is not a long.");
        }
    }

    /**
     * Get an array of field names from a JSONObject.
     *
     * @return An array of field names, or null if there are no names.
     */
    public static String[] getNames(JSONObject jo) {
        int length = jo.length();
        if (length == 0) {
            return null;
        }
        Iterator<String> iterator = jo.keys();
        String[] names = new String[length];
        int i = 0;
        while (iterator.hasNext()) {
            names[i] = iterator.next();
            i += 1;
        }
        return names;
    }

    /**
     * Get the string associated with a key.
     *
     * @param key
     *            A key string.
     * @return A string which is the value.
     * @throws JSONException
     *             if there is no string value for the key.
     */
    public String getString(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof String) {
            return (String) object;
        }
        throw new JSONException("JSONObject[" + key + "] not a string.");
    }

    /**
     * Determine if the JSONObject contains a specific key.
     *
     * @param key
     *            A key string.
     * @return true if the key exists in the JSONObject.
     */
    public boolean has(String key) {
        return this.map.containsKey(key);
    }

    /**
     * Increment a property of a JSONObject. If there is no such property,
     * create one with a value of 1. If there is such a property, and if it is
     * an Integer, Long, Double, or Float, then add one to it.
     *
     * @param key
     *            A key string.
     * @return this.
     * @throws JSONException
     *             If there is already a property with this name that is not an
     *             Integer, Long, Double, or Float.
     */
    public JSONObject increment(String key) throws JSONException {
        Object value = this.opt(key);
        if (value == null) {
            this.put(key, 1);
        } else if (value instanceof Integer) {
            this.put(key, (Integer) value + 1);
        } else if (value instanceof Long) {
            this.put(key, (Long) value + 1);
        } else if (value instanceof Double) {
            this.put(key, (Double) value + 1);
        } else if (value instanceof Float) {
            this.put(key, (Float) value + 1);
        } else {
            throw new JSONException("Unable to increment [" + key + "].");
        }
        return this;
    }

    /**
     * Determine if the value associated with the key is null or if there is no
     * value.
     *
     * @param key
     *            A key string.
     * @return true if there is no value associated with the key or if the value
     *         is the JSONObject.NULL object.
     */
    public boolean isNull(String key) {
        return JSONObject.NULL.equals(this.opt(key));
    }

    /**
     * Get an enumeration of the keys of the JSONObject.
     *
     * @return An iterator of the keys.
     */
    public Iterator<String> keys() {
        return this.keySet().iterator();
    }

    /**
     * Get a set of keys of the JSONObject.
     *
     * @return A keySet.
     */
    public Set<String> keySet() {
        return this.map.keySet();
    }

    /**
     * Get the number of keys stored in the JSONObject.
     *
     * @return The number of keys in the JSONObject.
     */
    public int length() {
        return this.map.size();
    }

    /**
     * Produce a JSONArray containing the names of the elements of this
     * JSONObject.
     *
     * @return A JSONArray containing the key strings, or null if the JSONObject
     *         is empty.
     */
    public JSONArray names() {
        JSONArray ja = new JSONArray();
        Iterator<String> keys = this.keys();
        while (keys.hasNext()) {
            ja.put(keys.next());
        }
        return ja.length() == 0 ? null : ja;
    }

    /**
     * Produce a string from a Number.
     *
     * @param number
     *            A Number
     * @return A String.
     * @throws JSONException
     *             If n is a non-finite number.
     */
    public static String numberToString(Number number) throws JSONException {
        if (number == null) {
            throw new JSONException("Null pointer");
        }
        testValidity(number);

// Shave off trailing zeros and decimal point, if possible.

        String string = number.toString();
        if (string.contains(".") && !string.contains("e")
                && !string.contains("E")) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }

    /**
     * Get an optional value associated with a key.
     *
     * @param key
     *            A key string.
     * @return An object which is the value, or null if there is no value.
     */
    public Object opt(String key) {
        return key == null ? null : this.map.get(key);
    }

    /**
     * Get an optional boolean associated with a key. It returns false if there
     * is no such key, or if the value is not Boolean.TRUE or the String "true".
     *
     * @param key
     *            A key string.
     * @return The truth.
     */
    public boolean optBoolean(String key) {
        return this.optBoolean(key, false);
    }

    /**
     * Get an optional boolean associated with a key. It returns the
     * defaultValue if there is no such key, or if it is not a Boolean or the
     * String "true" or "false" (case insensitive).
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return The truth.
     */
    public boolean optBoolean(String key, boolean defaultValue) {
        try {
            return this.getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional double associated with a key, or NaN if there is no such
     * key or if its value is not a number. If the value is a string, an attempt
     * will be made to evaluate it as a number.
     *
     * @param key
     *            A string which is the key.
     * @return An object which is the value.
     */
    public double optDouble(String key) {
        return this.optDouble(key, Double.NaN);
    }

    /**
     * Get an optional double associated with a key, or the defaultValue if
     * there is no such key or if its value is not a number. If the value is a
     * string, an attempt will be made to evaluate it as a number.
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public double optDouble(String key, double defaultValue) {
        try {
            return this.getDouble(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional int value associated with a key, or zero if there is no
     * such key or if the value is not a number. If the value is a string, an
     * attempt will be made to evaluate it as a number.
     *
     * @param key
     *            A key string.
     * @return An object which is the value.
     */
    public int optInt(String key) {
        return this.optInt(key, 0);
    }

    /**
     * Get an optional int value associated with a key, or the default if there
     * is no such key or if the value is not a number. If the value is a string,
     * an attempt will be made to evaluate it as a number.
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public int optInt(String key, int defaultValue) {
        try {
            return this.getInt(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional JSONArray associated with a key. It returns null if there
     * is no such key, or if its value is not a JSONArray.
     *
     * @param key
     *            A key string.
     * @return A JSONArray which is the value.
     */
    public JSONArray optJSONArray(String key) {
        Object o = this.opt(key);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }

    /**
     * Get an optional JSONObject associated with a key. It returns null if
     * there is no such key, or if its value is not a JSONObject.
     *
     * @param key
     *            A key string.
     * @return A JSONObject which is the value.
     */
    public JSONObject optJSONObject(String key) {
        Object object = this.opt(key);
        return object instanceof JSONObject ? (JSONObject) object : null;
    }

    /**
     * Get an optional long value associated with a key, or zero if there is no
     * such key or if the value is not a number. If the value is a string, an
     * attempt will be made to evaluate it as a number.
     *
     * @param key
     *            A key string.
     * @return An object which is the value.
     */
    public long optLong(String key) {
        return this.optLong(key, 0);
    }

    /**
     * Get an optional long value associated with a key, or the default if there
     * is no such key or if the value is not a number. If the value is a string,
     * an attempt will be made to evaluate it as a number.
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public long optLong(String key, long defaultValue) {
        try {
            return this.getLong(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional string associated with a key. It returns an empty string
     * if there is no such key. If the value is not a string and is not null,
     * then it is converted to a string.
     *
     * @param key
     *            A key string.
     * @return A string which is the value.
     */
    public String optString(String key) {
        return this.optString(key, "");
    }

    /**
     * Get an optional string associated with a key. It returns the defaultValue
     * if there is no such key.
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return A string which is the value.
     */
    public String optString(String key, String defaultValue) {
        Object object = this.opt(key);
        return NULL.equals(object) ? defaultValue : object.toString();
    }

    /**
     * Put a key/boolean pair in the JSONObject.
     *
     * @param key
     *            A key string.
     * @param value
     *            A boolean which is the value.
     * @return this.
     * @throws JSONException
     *             If the key is null.
     */
    public JSONObject put(String key, boolean value) throws JSONException {
        this.put(key, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONArray which is produced from a Collection.
     *
     * @param key
     *            A key string.
     * @param value
     *            A Collection value.
     * @return this.
     * @throws JSONException
     */
    public JSONObject put(String key, Collection<Object> value) throws JSONException {
        this.put(key, new JSONArray(value));
        return this;
    }

    /**
     * Put a key/double pair in the JSONObject.
     *
     * @param key
     *            A key string.
     * @param value
     *            A double which is the value.
     * @return this.
     * @throws JSONException
     *             If the key is null or if the number is invalid.
     */
    public JSONObject put(String key, double value) throws JSONException {
        this.put(key, new Double(value));
        return this;
    }

    /**
     * Put a key/int pair in the JSONObject.
     *
     * @param key
     *            A key string.
     * @param value
     *            An int which is the value.
     * @return this.
     * @throws JSONException
     *             If the key is null.
     */
    public JSONObject put(String key, int value) throws JSONException {
        this.put(key, new Integer(value));
        return this;
    }

    /**
     * Put a key/long pair in the JSONObject.
     *
     * @param key
     *            A key string.
     * @param value
     *            A long which is the value.
     * @return this.
     * @throws JSONException
     *             If the key is null.
     */
    public JSONObject put(String key, long value) throws JSONException {
        this.put(key, new Long(value));
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONObject which is produced from a Map.
     *
     * @param key
     *            A key string.
     * @param value
     *            A Map value.
     * @return this.
     * @throws JSONException
     */
    public JSONObject put(String key, Map<String, Object> value) throws JSONException {
        this.put(key, new JSONObject(value));
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject. If the value is null, then the
     * key will be removed from the JSONObject if it is present.
     *
     * @param key
     *            A key string.
     * @param value
     *            An object which is the value. It should be of one of these
     *            types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
     *            String, or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException
     *             If the value is non-finite number or if the key is null.
     */
    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new NullPointerException("Null key.");
        }
        if (value != null) {
            testValidity(value);
            this.map.put(key, value);
        } else {
            this.remove(key);
        }
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject, but only if the key and the value
     * are both non-null, and only if there is not already a member with that
     * name.
     *
     * @param key string
     * @param value object
     * @return this.
     * @throws JSONException
     *             if the key is a duplicate
     */
    public JSONObject putOnce(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            if (this.opt(key) != null) {
                throw new JSONException("Duplicate key \"" + key + "\"");
            }
            this.put(key, value);
        }
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject, but only if the key and the value
     * are both non-null.
     *
     * @param key
     *            A key string.
     * @param value
     *            An object which is the value. It should be of one of these
     *            types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
     *            String, or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException
     *             If the value is a non-finite number.
     */
    public JSONObject putOpt(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            this.put(key, value);
        }
        return this;
    }

    /**
     * Remove a name and its value, if present.
     *
     * @param key
     *            The name to be removed.
     * @return The value that was associated with the name, or null if there was
     *         no value.
     */
    public Object remove(String key) {
        return this.map.remove(key);
    }

    /**
     * Determine if two JSONObjects are similar.
     * They must contain the same set of names which must be associated with
     * similar values.
     *
     * @param other The other JSONObject
     * @return true if they are equal
     */
    public boolean similar(Object other) {
        try {
            if (!(other instanceof JSONObject)) {
                return false;
            }
            Set<String> set = this.keySet();
            if (!set.equals(((JSONObject)other).keySet())) {
                return false;
            }
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                Object valueThis = this.get(name);
                Object valueOther = ((JSONObject)other).get(name);
                if (valueThis instanceof JSONObject) {
                    if (!((JSONObject)valueThis).similar(valueOther)) {
                        return false;
                    }
                } else if (valueThis instanceof JSONArray) {
                    if (!((JSONArray)valueThis).similar(valueOther)) {
                        return false;
                    }
                } else if (!valueThis.equals(valueOther)) {
                    return false;
                }
            }
            return true;
        } catch (Throwable exception) {
            return false;
        }
    }

    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string.
     *
     * @param string
     *            A String.
     * @return A simple JSON value.
     */
    public static Object stringToValue(String string) {
        Double d;
        if (string.equals("")) {
            return string;
        }
        if (string.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (string.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (string.equalsIgnoreCase("null")) {
            return JSONObject.NULL;
        }

        /*
         * If it might be a number, try converting it. If a number cannot be
         * produced, then the value will just be a string.
         */

        char b = string.charAt(0);
        if ((b >= '0' && b <= '9') || b == '-') {
            try {
                if (string.contains(".") || string.contains("e")
                        || string.contains("E")) {
                    d = Double.valueOf(string);
                    if (!d.isInfinite() && !d.isNaN()) {
                        return d;
                    }
                } else {
                    Long myLong = new Long(string);
                    if (string.equals(myLong.toString())) {
                        if (myLong == myLong.intValue()) {
                            return myLong.intValue();
                        } else {
                            return myLong;
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return string;
    }

    /**
     * Throw an exception if the object is a NaN or infinite number.
     *
     * @param o
     *            The object to test.
     * @throws JSONException
     *             If o is a non-finite number.
     */
    public static void testValidity(Object o) throws JSONException {
        if (o != null) {
            if (o instanceof Double) {
                if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
                    throw new JSONException(
                            "JSON does not allow non-finite numbers.");
                }
            } else if (o instanceof Float) {
                if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
                    throw new JSONException(
                            "JSON does not allow non-finite numbers.");
                }
            }
        }
    }

    /**
     * Produce a JSONArray containing the values of the members of this
     * JSONObject.
     *
     * @param names
     *            A JSONArray containing a list of key strings. This determines
     *            the sequence of the values in the result.
     * @return A JSONArray of values.
     * @throws JSONException
     *             If any of the values are non-finite numbers.
     */
    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        if (names == null || names.length() == 0) {
            return null;
        }
        JSONArray ja = new JSONArray();
        for (int i = 0; i < names.length(); i += 1) {
            ja.put(this.opt(names.getString(i)));
        }
        return ja;
    }
}

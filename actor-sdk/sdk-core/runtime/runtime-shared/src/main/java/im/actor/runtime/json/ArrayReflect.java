/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.actor.runtime.json;

/**
 * Provides static methods to create and access arrays dynamically.
 */
public final class ArrayReflect {
    private ArrayReflect() {
    }

    private static IllegalArgumentException notAnArray(Object o) {
        throw new IllegalArgumentException("Not an array: " + o.getClass());
    }

    private static IllegalArgumentException incompatibleType(Object o) {
        throw new IllegalArgumentException("Array has incompatible type: " + o.getClass());
    }

    private static RuntimeException badArray(Object array) {
        if (array == null) {
            throw new NullPointerException("array == null");
        } else if (!array.getClass().isArray()) {
            throw notAnArray(array);
        } else {
            throw incompatibleType(array);
        }
    }

    /**
     * Returns the element of the array at the specified index. Equivalent to {@code array[index]}.
     * If the array component is a primitive type, the result is automatically boxed.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if {@code array} is not an array
     * @throws ArrayIndexOutOfBoundsException if {@code  index < 0 || index >= array.length}
     */
    public static Object get(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof Object[]) {
            return ((Object[]) array)[index];
        }
        if (array instanceof boolean[]) {
            return ((boolean[]) array)[index] ? Boolean.TRUE : Boolean.FALSE;
        }
        if (array instanceof byte[]) {
            return Byte.valueOf(((byte[]) array)[index]);
        }
        if (array instanceof char[]) {
            return Character.valueOf(((char[]) array)[index]);
        }
        if (array instanceof short[]) {
            return Short.valueOf(((short[]) array)[index]);
        }
        if (array instanceof int[]) {
            return Integer.valueOf(((int[]) array)[index]);
        }
        if (array instanceof long[]) {
            return Long.valueOf(((long[]) array)[index]);
        }
        if (array instanceof float[]) {
            return new Float(((float[]) array)[index]);
        }
        if (array instanceof double[]) {
            return new Double(((double[]) array)[index]);
        }
        if (array == null) {
            throw new NullPointerException("array == null");
        }
        throw notAnArray(array);
    }

    /**
     * Returns the boolean at the given index in the given boolean array.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if {@code array} is not an array or the element at the
     *                                        index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException if {@code index < 0 || index >= array.length}
     */
    public static boolean getBoolean(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof boolean[]) {
            return ((boolean[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the byte at the given index in the given byte array.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if {@code array} is not an array or the element at the
     *                                        index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException if {@code index < 0 || index >= array.length}
     */
    public static byte getByte(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the char at the given index in the given char array.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if {@code array} is not an array or the element at the
     *                                        index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException if {@code index < 0 || index >= array.length}
     */
    public static char getChar(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof char[]) {
            return ((char[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the double at the given index in the given array.
     * Applies to byte, char, float, double, int, long, and short arrays.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if {@code array} is not an array or the element at the
     *                                        index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException if {@code index < 0 || index >= array.length}
     */
    public static double getDouble(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof double[]) {
            return ((double[]) array)[index];
        } else if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        } else if (array instanceof char[]) {
            return ((char[]) array)[index];
        } else if (array instanceof float[]) {
            return ((float[]) array)[index];
        } else if (array instanceof int[]) {
            return ((int[]) array)[index];
        } else if (array instanceof long[]) {
            return ((long[]) array)[index];
        } else if (array instanceof short[]) {
            return ((short[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the float at the given index in the given array.
     * Applies to byte, char, float, int, long, and short arrays.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if {@code array} is not an array or the element at the
     *                                        index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException if {@code index < 0 || index >= array.length}
     */
    public static float getFloat(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof float[]) {
            return ((float[]) array)[index];
        } else if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        } else if (array instanceof char[]) {
            return ((char[]) array)[index];
        } else if (array instanceof int[]) {
            return ((int[]) array)[index];
        } else if (array instanceof long[]) {
            return ((long[]) array)[index];
        } else if (array instanceof short[]) {
            return ((short[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the int at the given index in the given array.
     * Applies to byte, char, int, and short arrays.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if {@code array} is not an array or the element at the
     *                                        index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException if {@code index < 0 || index >= array.length}
     */
    public static int getInt(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof int[]) {
            return ((int[]) array)[index];
        } else if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        } else if (array instanceof char[]) {
            return ((char[]) array)[index];
        } else if (array instanceof short[]) {
            return ((short[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the length of the array. Equivalent to {@code array.length}.
     *
     * @throws NullPointerException     if {@code array == null}
     * @throws IllegalArgumentException if {@code array} is not an array
     */
    public static int getLength(Object array) {
        if (array instanceof Object[]) {
            return ((Object[]) array).length;
        } else if (array instanceof boolean[]) {
            return ((boolean[]) array).length;
        } else if (array instanceof byte[]) {
            return ((byte[]) array).length;
        } else if (array instanceof char[]) {
            return ((char[]) array).length;
        } else if (array instanceof double[]) {
            return ((double[]) array).length;
        } else if (array instanceof float[]) {
            return ((float[]) array).length;
        } else if (array instanceof int[]) {
            return ((int[]) array).length;
        } else if (array instanceof long[]) {
            return ((long[]) array).length;
        } else if (array instanceof short[]) {
            return ((short[]) array).length;
        }
        throw badArray(array);
    }

    /**
     * Returns the long at the given index in the given array.
     * Applies to byte, char, int, long, and short arrays.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if {@code array} is not an array or the element at the
     *                                        index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException if {@code index < 0 || index >= array.length}
     */
    public static long getLong(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof long[]) {
            return ((long[]) array)[index];
        } else if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        } else if (array instanceof char[]) {
            return ((char[]) array)[index];
        } else if (array instanceof int[]) {
            return ((int[]) array)[index];
        } else if (array instanceof short[]) {
            return ((short[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the short at the given index in the given array.
     * Applies to byte and short arrays.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if {@code array} is not an array or the element at the
     *                                        index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException if {@code index < 0 || index >= array.length}
     */
    public static short getShort(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof short[]) {
            return ((short[]) array)[index];
        } else if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        }
        throw badArray(array);
    }





    /**
     * Sets {@code array[index] = value}. Applies to byte, double, float, int, long, and short arrays.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if the {@code array} is not an array or the value cannot be
     *                                        converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException if {@code  index < 0 || index >= array.length}
     */
    public static void setByte(Object array, int index, byte value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof byte[]) {
            ((byte[]) array)[index] = value;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = value;
        } else if (array instanceof int[]) {
            ((int[]) array)[index] = value;
        } else if (array instanceof long[]) {
            ((long[]) array)[index] = value;
        } else if (array instanceof short[]) {
            ((short[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to char, double, float, int, and long arrays.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if the {@code array} is not an array or the value cannot be
     *                                        converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException if {@code  index < 0 || index >= array.length}
     */
    public static void setChar(Object array, int index, char value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof char[]) {
            ((char[]) array)[index] = value;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = value;
        } else if (array instanceof int[]) {
            ((int[]) array)[index] = value;
        } else if (array instanceof long[]) {
            ((long[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to double arrays.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if the {@code array} is not an array or the value cannot be
     *                                        converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException if {@code  index < 0 || index >= array.length}
     */
    public static void setDouble(Object array, int index, double value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to double and float arrays.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if the {@code array} is not an array or the value cannot be
     *                                        converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException if {@code  index < 0 || index >= array.length}
     */
    public static void setFloat(Object array, int index, float value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof float[]) {
            ((float[]) array)[index] = value;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to double, float, int, and long arrays.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if the {@code array} is not an array or the value cannot be
     *                                        converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException if {@code  index < 0 || index >= array.length}
     */
    public static void setInt(Object array, int index, int value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof int[]) {
            ((int[]) array)[index] = value;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = value;
        } else if (array instanceof long[]) {
            ((long[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to double, float, and long arrays.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if the {@code array} is not an array or the value cannot be
     *                                        converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException if {@code  index < 0 || index >= array.length}
     */
    public static void setLong(Object array, int index, long value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof long[]) {
            ((long[]) array)[index] = value;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to double, float, int, long, and short arrays.
     *
     * @throws NullPointerException           if {@code array == null}
     * @throws IllegalArgumentException       if the {@code array} is not an array or the value cannot be
     *                                        converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException if {@code  index < 0 || index >= array.length}
     */
    public static void setShort(Object array, int index, short value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof short[]) {
            ((short[]) array)[index] = value;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = value;
        } else if (array instanceof int[]) {
            ((int[]) array)[index] = value;
        } else if (array instanceof long[]) {
            ((long[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }
}

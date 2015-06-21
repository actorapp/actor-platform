package org.bouncycastle.math.raw;

import org.bouncycastle.util.Pack;

import java.math.BigInteger;

public abstract class Nat192
{
    private static final long M = 0xFFFFFFFFL;

    public static int add(int[] x, int[] y, int[] z)
    {
        long c = 0;
        c += (x[0] & M) + (y[0] & M);
        z[0] = (int)c;
        c >>>= 32;
        c += (x[1] & M) + (y[1] & M);
        z[1] = (int)c;
        c >>>= 32;
        c += (x[2] & M) + (y[2] & M);
        z[2] = (int)c;
        c >>>= 32;
        c += (x[3] & M) + (y[3] & M);
        z[3] = (int)c;
        c >>>= 32;
        c += (x[4] & M) + (y[4] & M);
        z[4] = (int)c;
        c >>>= 32;
        c += (x[5] & M) + (y[5] & M);
        z[5] = (int)c;
        c >>>= 32;
        return (int)c;
    }

    public static int addBothTo(int[] x, int[] y, int[] z)
    {
        long c = 0;
        c += (x[0] & M) + (y[0] & M) + (z[0] & M);
        z[0] = (int)c;
        c >>>= 32;
        c += (x[1] & M) + (y[1] & M) + (z[1] & M);
        z[1] = (int)c;
        c >>>= 32;
        c += (x[2] & M) + (y[2] & M) + (z[2] & M);
        z[2] = (int)c;
        c >>>= 32;
        c += (x[3] & M) + (y[3] & M) + (z[3] & M);
        z[3] = (int)c;
        c >>>= 32;
        c += (x[4] & M) + (y[4] & M) + (z[4] & M);
        z[4] = (int)c;
        c >>>= 32;
        c += (x[5] & M) + (y[5] & M) + (z[5] & M);
        z[5] = (int)c;
        c >>>= 32;
        return (int)c;
    }

    public static int addTo(int[] x, int[] z)
    {
        long c = 0;
        c += (x[0] & M) + (z[0] & M);
        z[0] = (int)c;
        c >>>= 32;
        c += (x[1] & M) + (z[1] & M);
        z[1] = (int)c;
        c >>>= 32;
        c += (x[2] & M) + (z[2] & M);
        z[2] = (int)c;
        c >>>= 32;
        c += (x[3] & M) + (z[3] & M);
        z[3] = (int)c;
        c >>>= 32;
        c += (x[4] & M) + (z[4] & M);
        z[4] = (int)c;
        c >>>= 32;
        c += (x[5] & M) + (z[5] & M);
        z[5] = (int)c;
        c >>>= 32;
        return (int)c;
    }

    public static int addTo(int[] x, int xOff, int[] z, int zOff, int cIn)
    {
        long c = cIn & M;
        c += (x[xOff + 0] & M) + (z[zOff + 0] & M);
        z[zOff + 0] = (int)c;
        c >>>= 32;
        c += (x[xOff + 1] & M) + (z[zOff + 1] & M);
        z[zOff + 1] = (int)c;
        c >>>= 32;
        c += (x[xOff + 2] & M) + (z[zOff + 2] & M);
        z[zOff + 2] = (int)c;
        c >>>= 32;
        c += (x[xOff + 3] & M) + (z[zOff + 3] & M);
        z[zOff + 3] = (int)c;
        c >>>= 32;
        c += (x[xOff + 4] & M) + (z[zOff + 4] & M);
        z[zOff + 4] = (int)c;
        c >>>= 32;
        c += (x[xOff + 5] & M) + (z[zOff + 5] & M);
        z[zOff + 5] = (int)c;
        c >>>= 32;
        return (int)c;
    }

    public static int addToEachOther(int[] u, int uOff, int[] v, int vOff)
    {
        long c = 0;
        c += (u[uOff + 0] & M) + (v[vOff + 0] & M);
        u[uOff + 0] = (int)c;
        v[vOff + 0] = (int)c;
        c >>>= 32;
        c += (u[uOff + 1] & M) + (v[vOff + 1] & M);
        u[uOff + 1] = (int)c;
        v[vOff + 1] = (int)c;
        c >>>= 32;
        c += (u[uOff + 2] & M) + (v[vOff + 2] & M);
        u[uOff + 2] = (int)c;
        v[vOff + 2] = (int)c;
        c >>>= 32;
        c += (u[uOff + 3] & M) + (v[vOff + 3] & M);
        u[uOff + 3] = (int)c;
        v[vOff + 3] = (int)c;
        c >>>= 32;
        c += (u[uOff + 4] & M) + (v[vOff + 4] & M);
        u[uOff + 4] = (int)c;
        v[vOff + 4] = (int)c;
        c >>>= 32;
        c += (u[uOff + 5] & M) + (v[vOff + 5] & M);
        u[uOff + 5] = (int)c;
        v[vOff + 5] = (int)c;
        c >>>= 32;
        return (int)c;
    }

    public static void copy(int[] x, int[] z)
    {
        z[0] = x[0];
        z[1] = x[1];
        z[2] = x[2];
        z[3] = x[3];
        z[4] = x[4];
        z[5] = x[5];
    }

    public static int[] create()
    {
        return new int[6];
    }

    public static int[] createExt()
    {
        return new int[12];
    }

    public static boolean diff(int[] x, int xOff, int[] y, int yOff, int[] z, int zOff)
    {
        boolean pos = gte(x, xOff, y, yOff);
        if (pos)
        {
            sub(x, xOff, y, yOff, z, zOff);
        }
        else
        {
            sub(y, yOff, x, xOff, z, zOff);
        }
        return pos;
    }

    public static boolean eq(int[] x, int[] y)
    {
        for (int i = 5; i >= 0; --i)
        {
            if (x[i] != y[i])
            {
                return false;
            }
        }
        return true;
    }

    public static int[] fromBigInteger(BigInteger x)
    {
        if (x.signum() < 0 || x.bitLength() > 192)
        {
            throw new IllegalArgumentException();
        }

        int[] z = create();
        int i = 0;
        while (x.signum() != 0)
        {
            z[i++] = x.intValue();
            x = x.shiftRight(32);
        }
        return z;
    }

    public static int getBit(int[] x, int bit)
    {
        if (bit == 0)
        {
            return x[0] & 1;
        }
        int w = bit >> 5;
        if (w < 0 || w >= 6)
        {
            return 0;
        }
        int b = bit & 31;
        return (x[w] >>> b) & 1;
    }

    public static boolean gte(int[] x, int[] y)
    {
        for (int i = 5; i >= 0; --i)
        {
            int x_i = x[i] ^ Integer.MIN_VALUE;
            int y_i = y[i] ^ Integer.MIN_VALUE;
            if (x_i < y_i)
                return false;
            if (x_i > y_i)
                return true;
        }
        return true;
    }

    public static boolean gte(int[] x, int xOff, int[] y, int yOff)
    {
        for (int i = 5; i >= 0; --i)
        {
            int x_i = x[xOff + i] ^ Integer.MIN_VALUE;
            int y_i = y[yOff + i] ^ Integer.MIN_VALUE;
            if (x_i < y_i)
                return false;
            if (x_i > y_i)
                return true;
        }
        return true;
    }

    public static boolean isOne(int[] x)
    {
        if (x[0] != 1)
        {
            return false;
        }
        for (int i = 1; i < 6; ++i)
        {
            if (x[i] != 0)
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isZero(int[] x)
    {
        for (int i = 0; i < 6; ++i)
        {
            if (x[i] != 0)
            {
                return false;
            }
        }
        return true;
    }

    public static void mul(int[] x, int[] y, int[] zz)
    {
        long y_0 = y[0] & M;
        long y_1 = y[1] & M;
        long y_2 = y[2] & M;
        long y_3 = y[3] & M;
        long y_4 = y[4] & M;
        long y_5 = y[5] & M;

        {
            long c = 0, x_0 = x[0] & M;
            c += x_0 * y_0;
            zz[0] = (int)c;
            c >>>= 32;
            c += x_0 * y_1;
            zz[1] = (int)c;
            c >>>= 32;
            c += x_0 * y_2;
            zz[2] = (int)c;
            c >>>= 32;
            c += x_0 * y_3;
            zz[3] = (int)c;
            c >>>= 32;
            c += x_0 * y_4;
            zz[4] = (int)c;
            c >>>= 32;
            c += x_0 * y_5;
            zz[5] = (int)c;
            c >>>= 32;
            zz[6] = (int)c;
        }

        for (int i = 1; i < 6; ++i)
        {
            long c = 0, x_i = x[i] & M;
            c += x_i * y_0 + (zz[i + 0] & M);
            zz[i + 0] = (int)c;
            c >>>= 32;
            c += x_i * y_1 + (zz[i + 1] & M);
            zz[i + 1] = (int)c;
            c >>>= 32;
            c += x_i * y_2 + (zz[i + 2] & M);
            zz[i + 2] = (int)c;
            c >>>= 32;
            c += x_i * y_3 + (zz[i + 3] & M);
            zz[i + 3] = (int)c;
            c >>>= 32;
            c += x_i * y_4 + (zz[i + 4] & M);
            zz[i + 4] = (int)c;
            c >>>= 32;
            c += x_i * y_5 + (zz[i + 5] & M);
            zz[i + 5] = (int)c;
            c >>>= 32;
            zz[i + 6] = (int)c;
        }
    }

    public static void mul(int[] x, int xOff, int[] y, int yOff, int[] zz, int zzOff)
    {
        long y_0 = y[yOff + 0] & M;
        long y_1 = y[yOff + 1] & M;
        long y_2 = y[yOff + 2] & M;
        long y_3 = y[yOff + 3] & M;
        long y_4 = y[yOff + 4] & M;
        long y_5 = y[yOff + 5] & M;

        {
            long c = 0, x_0 = x[xOff + 0] & M;
            c += x_0 * y_0;
            zz[zzOff + 0] = (int)c;
            c >>>= 32;
            c += x_0 * y_1;
            zz[zzOff + 1] = (int)c;
            c >>>= 32;
            c += x_0 * y_2;
            zz[zzOff + 2] = (int)c;
            c >>>= 32;
            c += x_0 * y_3;
            zz[zzOff + 3] = (int)c;
            c >>>= 32;
            c += x_0 * y_4;
            zz[zzOff + 4] = (int)c;
            c >>>= 32;
            c += x_0 * y_5;
            zz[zzOff + 5] = (int)c;
            c >>>= 32;
            zz[zzOff + 6] = (int)c;
        }

        for (int i = 1; i < 6; ++i)
        {
            ++zzOff;
            long c = 0, x_i = x[xOff + i] & M;
            c += x_i * y_0 + (zz[zzOff + 0] & M);
            zz[zzOff + 0] = (int)c;
            c >>>= 32;
            c += x_i * y_1 + (zz[zzOff + 1] & M);
            zz[zzOff + 1] = (int)c;
            c >>>= 32;
            c += x_i * y_2 + (zz[zzOff + 2] & M);
            zz[zzOff + 2] = (int)c;
            c >>>= 32;
            c += x_i * y_3 + (zz[zzOff + 3] & M);
            zz[zzOff + 3] = (int)c;
            c >>>= 32;
            c += x_i * y_4 + (zz[zzOff + 4] & M);
            zz[zzOff + 4] = (int)c;
            c >>>= 32;
            c += x_i * y_5 + (zz[zzOff + 5] & M);
            zz[zzOff + 5] = (int)c;
            c >>>= 32;
            zz[zzOff + 6] = (int)c;
        }
    }

    public static int mulAddTo(int[] x, int[] y, int[] zz)
    {
        long y_0 = y[0] & M;
        long y_1 = y[1] & M;
        long y_2 = y[2] & M;
        long y_3 = y[3] & M;
        long y_4 = y[4] & M;
        long y_5 = y[5] & M;

        long zc = 0;
        for (int i = 0; i < 6; ++i)
        {
            long c = 0, x_i = x[i] & M;
            c += x_i * y_0 + (zz[i + 0] & M);
            zz[i + 0] = (int)c;
            c >>>= 32;
            c += x_i * y_1 + (zz[i + 1] & M);
            zz[i + 1] = (int)c;
            c >>>= 32;
            c += x_i * y_2 + (zz[i + 2] & M);
            zz[i + 2] = (int)c;
            c >>>= 32;
            c += x_i * y_3 + (zz[i + 3] & M);
            zz[i + 3] = (int)c;
            c >>>= 32;
            c += x_i * y_4 + (zz[i + 4] & M);
            zz[i + 4] = (int)c;
            c >>>= 32;
            c += x_i * y_5 + (zz[i + 5] & M);
            zz[i + 5] = (int)c;
            c >>>= 32;
            c += zc + (zz[i + 6] & M);
            zz[i + 6] = (int)c;
            zc = c >>> 32;
        }
        return (int)zc;
    }

    public static int mulAddTo(int[] x, int xOff, int[] y, int yOff, int[] zz, int zzOff)
    {
        long y_0 = y[yOff + 0] & M;
        long y_1 = y[yOff + 1] & M;
        long y_2 = y[yOff + 2] & M;
        long y_3 = y[yOff + 3] & M;
        long y_4 = y[yOff + 4] & M;
        long y_5 = y[yOff + 5] & M;

        long zc = 0;
        for (int i = 0; i < 6; ++i)
        {
            long c = 0, x_i = x[xOff + i] & M;
            c += x_i * y_0 + (zz[zzOff + 0] & M);
            zz[zzOff + 0] = (int)c;
            c >>>= 32;
            c += x_i * y_1 + (zz[zzOff + 1] & M);
            zz[zzOff + 1] = (int)c;
            c >>>= 32;
            c += x_i * y_2 + (zz[zzOff + 2] & M);
            zz[zzOff + 2] = (int)c;
            c >>>= 32;
            c += x_i * y_3 + (zz[zzOff + 3] & M);
            zz[zzOff + 3] = (int)c;
            c >>>= 32;
            c += x_i * y_4 + (zz[zzOff + 4] & M);
            zz[zzOff + 4] = (int)c;
            c >>>= 32;
            c += x_i * y_5 + (zz[zzOff + 5] & M);
            zz[zzOff + 5] = (int)c;
            c >>>= 32;
            c += zc + (zz[zzOff + 6] & M);
            zz[zzOff + 6] = (int)c;
            zc = c >>> 32;
            ++zzOff;
        }
        return (int)zc;
    }

    public static long mul33Add(int w, int[] x, int xOff, int[] y, int yOff, int[] z, int zOff)
    {
        // assert w >>> 31 == 0;

        long c = 0, wVal = w & M;
        long x0 = x[xOff + 0] & M;
        c += wVal * x0 + (y[yOff + 0] & M);
        z[zOff + 0] = (int)c;
        c >>>= 32;
        long x1 = x[xOff + 1] & M;
        c += wVal * x1 + x0 + (y[yOff + 1] & M);
        z[zOff + 1] = (int)c;
        c >>>= 32;
        long x2 = x[xOff + 2] & M;
        c += wVal * x2 + x1 + (y[yOff + 2] & M);
        z[zOff + 2] = (int)c;
        c >>>= 32;
        long x3 = x[xOff + 3] & M;
        c += wVal * x3 + x2 + (y[yOff + 3] & M);
        z[zOff + 3] = (int)c;
        c >>>= 32;
        long x4 = x[xOff + 4] & M;
        c += wVal * x4 + x3 + (y[yOff + 4] & M);
        z[zOff + 4] = (int)c;
        c >>>= 32;
        long x5 = x[xOff + 5] & M;
        c += wVal * x5 + x4 + (y[yOff + 5] & M);
        z[zOff + 5] = (int)c;
        c >>>= 32;
        c += x5;
        return c;
    }

    public static int mulWordAddExt(int x, int[] yy, int yyOff, int[] zz, int zzOff)
    {
        // assert yyOff <= 6;
        // assert zzOff <= 6;
        long c = 0, xVal = x & M;
        c += xVal * (yy[yyOff + 0] & M) + (zz[zzOff + 0] & M);
        zz[zzOff + 0] = (int)c;
        c >>>= 32;
        c += xVal * (yy[yyOff + 1] & M) + (zz[zzOff + 1] & M);
        zz[zzOff + 1] = (int)c;
        c >>>= 32;
        c += xVal * (yy[yyOff + 2] & M) + (zz[zzOff + 2] & M);
        zz[zzOff + 2] = (int)c;
        c >>>= 32;
        c += xVal * (yy[yyOff + 3] & M) + (zz[zzOff + 3] & M);
        zz[zzOff + 3] = (int)c;
        c >>>= 32;
        c += xVal * (yy[yyOff + 4] & M) + (zz[zzOff + 4] & M);
        zz[zzOff + 4] = (int)c;
        c >>>= 32;
        c += xVal * (yy[yyOff + 5] & M) + (zz[zzOff + 5] & M);
        zz[zzOff + 5] = (int)c;
        c >>>= 32;
        return (int)c;
    }

    public static int mul33DWordAdd(int x, long y, int[] z, int zOff)
    {
        // assert x >>> 31 == 0;
        // assert zOff <= 2;

        long c = 0, xVal = x & M;
        long y00 = y & M;
        c += xVal * y00 + (z[zOff + 0] & M);
        z[zOff + 0] = (int)c;
        c >>>= 32;
        long y01 = y >>> 32;
        c += xVal * y01 + y00 + (z[zOff + 1] & M);
        z[zOff + 1] = (int)c;
        c >>>= 32;
        c += y01 + (z[zOff + 2] & M);
        z[zOff + 2] = (int)c;
        c >>>= 32;
        c += (z[zOff + 3] & M);
        z[zOff + 3] = (int)c;
        c >>>= 32;
        return c == 0 ? 0 : Nat.incAt(6, z, zOff, 4);
    }

    public static int mul33WordAdd(int x, int y, int[] z, int zOff)
    {
        // assert x >>> 31 == 0;
        // assert zOff <= 3;

        long c = 0, xVal = x & M, yVal = y & M;
        c += yVal * xVal + (z[zOff + 0] & M);
        z[zOff + 0] = (int)c;
        c >>>= 32;
        c += yVal + (z[zOff + 1] & M);
        z[zOff + 1] = (int)c;
        c >>>= 32;
        c += (z[zOff + 2] & M);
        z[zOff + 2] = (int)c;
        c >>>= 32;
        return c == 0 ? 0 : Nat.incAt(6, z, zOff, 3);
    }

    public static int mulWordDwordAdd(int x, long y, int[] z, int zOff)
    {
        // assert zOff <= 3;
        long c = 0, xVal = x & M;
        c += xVal * (y & M) + (z[zOff + 0] & M);
        z[zOff + 0] = (int)c;
        c >>>= 32;
        c += xVal * (y >>> 32) + (z[zOff + 1] & M);
        z[zOff + 1] = (int)c;
        c >>>= 32;
        c += (z[zOff + 2] & M);
        z[zOff + 2] = (int)c;
        c >>>= 32;
        return c == 0 ? 0 : Nat.incAt(6, z, zOff, 3);
    }

    public static int mulWord(int x, int[] y, int[] z, int zOff)
    {
        long c = 0, xVal = x & M;
        int i = 0;
        do
        {
            c += xVal * (y[i] & M);
            z[zOff + i] = (int)c;
            c >>>= 32;
        }
        while (++i < 6);
        return (int)c;
    }

    public static void square(int[] x, int[] zz)
    {
        long x_0 = x[0] & M;
        long zz_1;

        int c = 0, w;
        {
            int i = 5, j = 12;
            do
            {
                long xVal = (x[i--] & M);
                long p = xVal * xVal;
                zz[--j] = (c << 31) | (int)(p >>> 33);
                zz[--j] = (int)(p >>> 1);
                c = (int)p;
            }
            while (i > 0);

            {
                long p = x_0 * x_0;
                zz_1 = ((c << 31) & M) | (p >>> 33);
                zz[0] = (int)p;
                c = (int)(p >>> 32) & 1;
            }
        }

        long x_1 = x[1] & M;
        long zz_2 = zz[2] & M;

        {
            zz_1 += x_1 * x_0;
            w = (int)zz_1;
            zz[1] = (w << 1) | c;
            c = w >>> 31;
            zz_2 += zz_1 >>> 32;
        }

        long x_2 = x[2] & M;
        long zz_3 = zz[3] & M;
        long zz_4 = zz[4] & M;
        {
            zz_2 += x_2 * x_0;
            w = (int)zz_2;
            zz[2] = (w << 1) | c;
            c = w >>> 31;
            zz_3 += (zz_2 >>> 32) + x_2 * x_1;
            zz_4 += zz_3 >>> 32;
            zz_3 &= M;
        }

        long x_3 = x[3] & M;
        long zz_5 = zz[5] & M;
        long zz_6 = zz[6] & M;
        {
            zz_3 += x_3 * x_0;
            w = (int)zz_3;
            zz[3] = (w << 1) | c;
            c = w >>> 31;
            zz_4 += (zz_3 >>> 32) + x_3 * x_1;
            zz_5 += (zz_4 >>> 32) + x_3 * x_2;
            zz_4 &= M;
            zz_6 += zz_5 >>> 32;
            zz_5 &= M;
        }

        long x_4 = x[4] & M;
        long zz_7 = zz[7] & M;
        long zz_8 = zz[8] & M;
        {
            zz_4 += x_4 * x_0;
            w = (int)zz_4;
            zz[4] = (w << 1) | c;
            c = w >>> 31;
            zz_5 += (zz_4 >>> 32) + x_4 * x_1;
            zz_6 += (zz_5 >>> 32) + x_4 * x_2;
            zz_5 &= M;
            zz_7 += (zz_6 >>> 32) + x_4 * x_3;
            zz_6 &= M;
            zz_8 += zz_7 >>> 32;
            zz_7 &= M;
        }

        long x_5 = x[5] & M;
        long zz_9 = zz[9] & M;
        long zz_10 = zz[10] & M;
        {
            zz_5 += x_5 * x_0;
            w = (int)zz_5;
            zz[5] = (w << 1) | c;
            c = w >>> 31;
            zz_6 += (zz_5 >>> 32) + x_5 * x_1;
            zz_7 += (zz_6 >>> 32) + x_5 * x_2;
            zz_8 += (zz_7 >>> 32) + x_5 * x_3;
            zz_9 += (zz_8 >>> 32) + x_5 * x_4;
            zz_10 += zz_9 >>> 32;
        }

        w = (int)zz_6;
        zz[6] = (w << 1) | c;
        c = w >>> 31;
        w = (int)zz_7;
        zz[7] = (w << 1) | c;
        c = w >>> 31;
        w = (int)zz_8;
        zz[8] = (w << 1) | c;
        c = w >>> 31;
        w = (int)zz_9;
        zz[9] = (w << 1) | c;
        c = w >>> 31;
        w = (int)zz_10;
        zz[10] = (w << 1) | c;
        c = w >>> 31;
        w = zz[11] + (int)(zz_10 >> 32);
        zz[11] = (w << 1) | c;
    }

    public static void square(int[] x, int xOff, int[] zz, int zzOff)
    {
        long x_0 = x[xOff + 0] & M;
        long zz_1;

        int c = 0, w;
        {
            int i = 5, j = 12;
            do
            {
                long xVal = (x[xOff + i--] & M);
                long p = xVal * xVal;
                zz[zzOff + --j] = (c << 31) | (int)(p >>> 33);
                zz[zzOff + --j] = (int)(p >>> 1);
                c = (int)p;
            }
            while (i > 0);

            {
                long p = x_0 * x_0;
                zz_1 = ((c << 31) & M) | (p >>> 33);
                zz[zzOff + 0] = (int)p;
                c = (int)(p >>> 32) & 1;
            }
        }

        long x_1 = x[xOff + 1] & M;
        long zz_2 = zz[zzOff + 2] & M;

        {
            zz_1 += x_1 * x_0;
            w = (int)zz_1;
            zz[zzOff + 1] = (w << 1) | c;
            c = w >>> 31;
            zz_2 += zz_1 >>> 32;
        }

        long x_2 = x[xOff + 2] & M;
        long zz_3 = zz[zzOff + 3] & M;
        long zz_4 = zz[zzOff + 4] & M;
        {
            zz_2 += x_2 * x_0;
            w = (int)zz_2;
            zz[zzOff + 2] = (w << 1) | c;
            c = w >>> 31;
            zz_3 += (zz_2 >>> 32) + x_2 * x_1;
            zz_4 += zz_3 >>> 32;
            zz_3 &= M;
        }

        long x_3 = x[xOff + 3] & M;
        long zz_5 = zz[zzOff + 5] & M;
        long zz_6 = zz[zzOff + 6] & M;
        {
            zz_3 += x_3 * x_0;
            w = (int)zz_3;
            zz[zzOff + 3] = (w << 1) | c;
            c = w >>> 31;
            zz_4 += (zz_3 >>> 32) + x_3 * x_1;
            zz_5 += (zz_4 >>> 32) + x_3 * x_2;
            zz_4 &= M;
            zz_6 += zz_5 >>> 32;
            zz_5 &= M;
        }

        long x_4 = x[xOff + 4] & M;
        long zz_7 = zz[zzOff + 7] & M;
        long zz_8 = zz[zzOff + 8] & M;
        {
            zz_4 += x_4 * x_0;
            w = (int)zz_4;
            zz[zzOff + 4] = (w << 1) | c;
            c = w >>> 31;
            zz_5 += (zz_4 >>> 32) + x_4 * x_1;
            zz_6 += (zz_5 >>> 32) + x_4 * x_2;
            zz_5 &= M;
            zz_7 += (zz_6 >>> 32) + x_4 * x_3;
            zz_6 &= M;
            zz_8 += zz_7 >>> 32;
            zz_7 &= M;
        }

        long x_5 = x[xOff + 5] & M;
        long zz_9 = zz[zzOff + 9] & M;
        long zz_10 = zz[zzOff + 10] & M;
        {
            zz_5 += x_5 * x_0;
            w = (int)zz_5;
            zz[zzOff + 5] = (w << 1) | c;
            c = w >>> 31;
            zz_6 += (zz_5 >>> 32) + x_5 * x_1;
            zz_7 += (zz_6 >>> 32) + x_5 * x_2;
            zz_8 += (zz_7 >>> 32) + x_5 * x_3;
            zz_9 += (zz_8 >>> 32) + x_5 * x_4;
            zz_10 += zz_9 >>> 32;
        }

        w = (int)zz_6;
        zz[zzOff + 6] = (w << 1) | c;
        c = w >>> 31;
        w = (int)zz_7;
        zz[zzOff + 7] = (w << 1) | c;
        c = w >>> 31;
        w = (int)zz_8;
        zz[zzOff + 8] = (w << 1) | c;
        c = w >>> 31;
        w = (int)zz_9;
        zz[zzOff + 9] = (w << 1) | c;
        c = w >>> 31;
        w = (int)zz_10;
        zz[zzOff + 10] = (w << 1) | c;
        c = w >>> 31;
        w = zz[zzOff + 11] + (int)(zz_10 >> 32);
        zz[zzOff + 11] = (w << 1) | c;
    }

    public static int sub(int[] x, int[] y, int[] z)
    {
        long c = 0;
        c += (x[0] & M) - (y[0] & M);
        z[0] = (int)c;
        c >>= 32;
        c += (x[1] & M) - (y[1] & M);
        z[1] = (int)c;
        c >>= 32;
        c += (x[2] & M) - (y[2] & M);
        z[2] = (int)c;
        c >>= 32;
        c += (x[3] & M) - (y[3] & M);
        z[3] = (int)c;
        c >>= 32;
        c += (x[4] & M) - (y[4] & M);
        z[4] = (int)c;
        c >>= 32;
        c += (x[5] & M) - (y[5] & M);
        z[5] = (int)c;
        c >>= 32;
        return (int)c;
    }

    public static int sub(int[] x, int xOff, int[] y, int yOff, int[] z, int zOff)
    {
        long c = 0;
        c += (x[xOff + 0] & M) - (y[yOff + 0] & M);
        z[zOff + 0] = (int)c;
        c >>= 32;
        c += (x[xOff + 1] & M) - (y[yOff + 1] & M);
        z[zOff + 1] = (int)c;
        c >>= 32;
        c += (x[xOff + 2] & M) - (y[yOff + 2] & M);
        z[zOff + 2] = (int)c;
        c >>= 32;
        c += (x[xOff + 3] & M) - (y[yOff + 3] & M);
        z[zOff + 3] = (int)c;
        c >>= 32;
        c += (x[xOff + 4] & M) - (y[yOff + 4] & M);
        z[zOff + 4] = (int)c;
        c >>= 32;
        c += (x[xOff + 5] & M) - (y[yOff + 5] & M);
        z[zOff + 5] = (int)c;
        c >>= 32;
        return (int)c;
    }

    public static int subBothFrom(int[] x, int[] y, int[] z)
    {
        long c = 0;
        c += (z[0] & M) - (x[0] & M) - (y[0] & M);
        z[0] = (int)c;
        c >>= 32;
        c += (z[1] & M) - (x[1] & M) - (y[1] & M);
        z[1] = (int)c;
        c >>= 32;
        c += (z[2] & M) - (x[2] & M) - (y[2] & M);
        z[2] = (int)c;
        c >>= 32;
        c += (z[3] & M) - (x[3] & M) - (y[3] & M);
        z[3] = (int)c;
        c >>= 32;
        c += (z[4] & M) - (x[4] & M) - (y[4] & M);
        z[4] = (int)c;
        c >>= 32;
        c += (z[5] & M) - (x[5] & M) - (y[5] & M);
        z[5] = (int)c;
        c >>= 32;
        return (int)c;
    }

    public static int subFrom(int[] x, int[] z)
    {
        long c = 0;
        c += (z[0] & M) - (x[0] & M);
        z[0] = (int)c;
        c >>= 32;
        c += (z[1] & M) - (x[1] & M);
        z[1] = (int)c;
        c >>= 32;
        c += (z[2] & M) - (x[2] & M);
        z[2] = (int)c;
        c >>= 32;
        c += (z[3] & M) - (x[3] & M);
        z[3] = (int)c;
        c >>= 32;
        c += (z[4] & M) - (x[4] & M);
        z[4] = (int)c;
        c >>= 32;
        c += (z[5] & M) - (x[5] & M);
        z[5] = (int)c;
        c >>= 32;
        return (int)c;
    }

    public static int subFrom(int[] x, int xOff, int[] z, int zOff)
    {
        long c = 0;
        c += (z[zOff + 0] & M) - (x[xOff + 0] & M);
        z[zOff + 0] = (int)c;
        c >>= 32;
        c += (z[zOff + 1] & M) - (x[xOff + 1] & M);
        z[zOff + 1] = (int)c;
        c >>= 32;
        c += (z[zOff + 2] & M) - (x[xOff + 2] & M);
        z[zOff + 2] = (int)c;
        c >>= 32;
        c += (z[zOff + 3] & M) - (x[xOff + 3] & M);
        z[zOff + 3] = (int)c;
        c >>= 32;
        c += (z[zOff + 4] & M) - (x[xOff + 4] & M);
        z[zOff + 4] = (int)c;
        c >>= 32;
        c += (z[zOff + 5] & M) - (x[xOff + 5] & M);
        z[zOff + 5] = (int)c;
        c >>= 32;
        return (int)c;
    }

    public static BigInteger toBigInteger(int[] x)
    {
        byte[] bs = new byte[24];
        for (int i = 0; i < 6; ++i)
        {
            int x_i = x[i];
            if (x_i != 0)
            {
                Pack.intToBigEndian(x_i, bs, (5 - i) << 2);
            }
        }
        return new BigInteger(1, bs);
    }

    public static void zero(int[] z)
    {
        z[0] = 0;
        z[1] = 0;
        z[2] = 0;
        z[3] = 0;
        z[4] = 0;
        z[5] = 0;
    }
}

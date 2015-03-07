package org.bouncycastle.asn1.eac;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

import org.bouncycastle.util.Arrays;

/**
 * EAC encoding date object
 */
public class PackedDate
{
    private byte[]      time;

    public PackedDate(
        String time)
    {
        this.time = convert(time);
    }

    /**
     * Base constructor from a java.util.date object.
     *
     * @param time a date object representing the time of interest.
     */
    public PackedDate(
        Date time)
    {
        SimpleDateFormat dateF = new SimpleDateFormat("yyMMdd'Z'");

        dateF.setTimeZone(new SimpleTimeZone(0,"Z"));

        this.time = convert(dateF.format(time));
    }

    /**
     * Base constructor from a java.util.date object. You may need to use this constructor if the default locale
     * doesn't use a Gregorian calender so that the PackedDate produced is compatible with other ASN.1 implementations.
     *
     * @param time a date object representing the time of interest.
     * @param locale an appropriate Locale for producing an ASN.1 GeneralizedTime value.
     */
    public PackedDate(
        Date time,
        Locale locale)
    {
        SimpleDateFormat dateF = new SimpleDateFormat("yyMMdd'Z'", locale);

        dateF.setTimeZone(new SimpleTimeZone(0,"Z"));

        this.time = convert(dateF.format(time));
    }

    private byte[] convert(String sTime)
    {
        char[] digs = sTime.toCharArray();
        byte[] date = new byte[6];

        for (int i = 0; i != 6; i++)
        {
            date[i] = (byte)(digs[i] - '0');
        }

        return date;
    }

    PackedDate(
        byte[] bytes)
    {
        this.time = bytes;
    }

    /**
     * return the time as a date based on whatever a 2 digit year will return. For
     * standardised processing use getAdjustedDate().
     *
     * @return the resulting date
     * @exception java.text.ParseException if the date string cannot be parsed.
     */
    public Date getDate()
        throws ParseException
    {
        SimpleDateFormat dateF = new SimpleDateFormat("yyyyMMdd");

        return dateF.parse("20" + toString());
    }

    public int hashCode()
    {
        return Arrays.hashCode(time);
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof PackedDate))
        {
            return false;
        }

        PackedDate other = (PackedDate)o;

        return Arrays.areEqual(time, other.time);
    }

    public String toString() 
    {
        char[]  dateC = new char[time.length];

        for (int i = 0; i != dateC.length; i++)
        {
            dateC[i] = (char)((time[i] & 0xff) + '0');
        }

        return new String(dateC);
    }

    public byte[] getEncoding()
    {
        return time;
    }
}

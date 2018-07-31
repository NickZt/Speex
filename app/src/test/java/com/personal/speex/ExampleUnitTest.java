package com.personal.speex;

import android.os.SystemClock;

import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
       // byteTest();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i =100;
                String string = null;
                for (int i1 = 0; i1 < i; i1++) {
                    //SystemClock.sleep(100);
                    string = i+"";
                }
            }
        }).start();
    }

    public void byteTest() {
        byte[] start = "{7E&".getBytes(Charset.forName("UTF-8"));
        byte[] end = "&7F}".getBytes(Charset.forName("UTF-8"));
        System.out.print(Arrays.toString(start));
        System.out.print(Arrays.toString(end));

        byte[] encodedData ="A组".getBytes(Charset.forName("UTF-8"));
        byte[] encodedData2 ="组".getBytes(Charset.forName("UTF-8"));
        System.out.println("sssssss"+Arrays.toString(encodedData));
        System.out.println("sssssss"+Arrays.toString(encodedData2));

        byte[] a1 ="01:".getBytes(Charset.forName("UTF-8"));
        System.out.println(Arrays.toString(a1));

        byte[] a2 ="02:".getBytes(Charset.forName("UTF-8"));
        System.out.println(Arrays.toString(a2));

        byte[] a3 ="03:".getBytes(Charset.forName("UTF-8"));
        System.out.println(Arrays.toString(a3));

        byte[] bytes = new byte[3];
        System.arraycopy(encodedData,encodedData.length-bytes.length,bytes,0,bytes.length);
        System.out.println("==="+Arrays.toString(bytes));

        byte[] sendData = new byte[start.length + encodedData.length + end.length];
        System.arraycopy(start,0,sendData,0,start.length);
        System.arraycopy(encodedData,0,sendData,start.length,encodedData.length);
        System.arraycopy(end,0,sendData,start.length+encodedData.length,end.length);
        System.out.println(Arrays.toString(sendData));
    }
}
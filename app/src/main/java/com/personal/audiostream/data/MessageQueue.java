package com.personal.audiostream.data;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by yanghao1 on 2017/4/25.
 */

public class MessageQueue {

    private static MessageQueue messageQueue1, messageQueue2, messageQueue3, messageQueue4,messageQueue5;

    private BlockingQueue<AudioData> audioDataQueue = null;

    private MessageQueue() {
        audioDataQueue = new LinkedBlockingQueue<>();
    }

    @Retention(SOURCE)
    @IntDef({ENCODER_DATA_QUEUE, UNI_SENDER_DATA_QUEUE,MULTI_SENDER_DATA_QUEUE, DECODER_DATA_QUEUE, TRACKER_DATA_QUEUE})
    public @interface DataQueueType {
    }

    public static final int ENCODER_DATA_QUEUE = 0;
    public static final int UNI_SENDER_DATA_QUEUE = 1;
    public static final int MULTI_SENDER_DATA_QUEUE = 2;
    public static final int DECODER_DATA_QUEUE = 3;
    public static final int TRACKER_DATA_QUEUE = 4;

    public static MessageQueue getInstance(@DataQueueType int type) {

        switch (type) {
            case ENCODER_DATA_QUEUE:
                if (messageQueue1 == null) {
                    messageQueue1 = new MessageQueue();
                }
                return messageQueue1;
            case UNI_SENDER_DATA_QUEUE:
                if (messageQueue2 == null) {
                    messageQueue2 = new MessageQueue();
                }
                return messageQueue2;
            case MULTI_SENDER_DATA_QUEUE:
                if (messageQueue3 == null) {
                    messageQueue3 = new MessageQueue();
                }
                return messageQueue3;
            case DECODER_DATA_QUEUE:
                if (messageQueue4 == null) {
                    messageQueue4 = new MessageQueue();
                }
                return messageQueue4;
            case TRACKER_DATA_QUEUE:
                if (messageQueue5 == null) {
                    messageQueue5 = new MessageQueue();
                }
                return messageQueue5;
            default:
                return new MessageQueue();
        }
    }

    public void put(AudioData audioData) {
        try {
            audioDataQueue.put(audioData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public AudioData take() {
        try {
            return audioDataQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getSize() {
        return audioDataQueue.size();
    }

    public synchronized void clear() {
        audioDataQueue.clear();
    }
}
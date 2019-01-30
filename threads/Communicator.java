package nachos.threads;

import java.util.ArrayList;
import java.util.Collections;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
	public Condition sender;
	public Condition listner;
	public Condition currentSpeaker;
	public int flag;
	public Lock lock;
	public int word;
	/*Added
	 * there is flag which decides whether to let the sender to send it information or not,if flag is already set 
	 * we will not let the other senders to send data and instead let them sleep. when the sender sends information ,
	 * from the listener queue , will wake the thread if any ,  will be sent to different sleeping queue ,which will get invoked when the receiver receives it and hence
	 * the receiver will be responsible to wake this sending thread. 
	 * The listener thread will check whether the flag is set or not,if the flag is set that means the sender has sent data and if not sent that means
	 * sender has not sent the data so will let the thread to sleep in recieverQueue. And will wake the sender from sleeping queue.
	 *  
	 */
    public Communicator() {
    	this.flag=0;
    	this.lock = new Lock();
    	this.sender = new Condition(this.lock);
    	this.listner = new Condition(this.lock);
    	this.currentSpeaker = new Condition(this.lock);
    	
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    /*
     * Acquire lock
     * if no other sender has sent data whose data is not received:
     * 		sleep in senderQueue
     *  send data
     *  sleep in current queue
     *  release lock
     */
    public void speak(int word) {
    	lock.acquire();
    	while(flag==1 ) {
    		sender.sleep();
    	}
    	flag=1;
    	this.word = word;
    	this.listner.wake();
    	this.currentSpeaker.sleep();    	
    	lock.release();
    	
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    /*
     * Acquire lock
     * if sender has not sent data
     * 	sleep in listnerQueue
     * wake the sender from current queue
     * wake the new sender from sender queue if any
     * Release lock
     */
    public int listen() {
    	lock.acquire();
    	while(flag==0) {
    		listner.sleep();
    	}
    	int result = this.word;
    	flag=0;
    	this.currentSpeaker.wake();
    	this.sender.wake();
    	lock.release();
    	return result;
    }
    
    
    //Test Case for IV copied from internet
    private static class Speaker implements Runnable {
	Speaker(Communicator comm, int word) {
        this.comm = comm; 
        this.word = word;
	}
	
	public void run() {
        // System.out.print(KThread.currentThread().getName() 
        //        + " will speak " + this.word + "\n");	
        comm.speak(this.word);
	}

    private int word = 0;
    private Communicator comm; 
    }

    private static class Listener implements Runnable {
	Listener(Communicator comm) {
        this.comm = comm; 
	}
	
	public void run() {
        System.out.print(KThread.currentThread().getName() 
               + " will listen \n");	

        int word = comm.listen();

        System.out.print("Listen a word: " + word + " \n"); 
	}

    private Communicator comm; 
    }

    /**
     * Test if this module is working.
     */
    public static void selfTest() {

    System.out.print("Enter Communicator.selfTest\n");	

    System.out.print("\nVAR1: Test for one speaker, one listener, speaker waits for listener\n");	

    Communicator comm = new Communicator();
    KThread threadSpeaker =  new KThread(new Speaker(comm, 100));
    threadSpeaker.setName("Thread speaker").fork();

    KThread.yield();

    KThread threadListener = new KThread(new Listener(comm));
    threadListener.setName("Thread listner").fork();

    KThread.yield();

    threadListener.join();
    threadSpeaker.join();

    System.out.print("\nVAR2: Test for one speaker, one listener, listener waits for speaker\n");	
    Communicator comm1 = new Communicator();

    KThread threadListener1 = new KThread(new Listener(comm1));
    threadListener1.setName("Thread listner").fork();

    KThread.yield();

    KThread threadSpeaker1 =  new KThread(new Speaker(comm1, 100));
    threadSpeaker1.setName("Thread speaker").fork();

    KThread.yield();

    threadListener1.join();
    threadSpeaker1.join();


    System.out.print("\nVAR3: Test for one speaker, more listener, listener waits for speaker\n");	

    Communicator comm2 = new Communicator();

    KThread t[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         t[i] = new KThread(new Listener(comm2));
         t[i].setName("Listener Thread" + i).fork();
	}

    KThread.yield();

    KThread speakerThread2 = new KThread(new Speaker(comm2, 200));
    speakerThread2.setName("Thread speaker").fork();

    KThread.yield();
    t[0].join();
    speakerThread2.join();



    System.out.print("\nVAR4:Test for one speaker, more listener, speaker waits for listener \n");	

    Communicator comm3 = new Communicator();

    KThread speakerThread3 = new KThread(new Speaker(comm3, 300));
    speakerThread3.setName("Thread speaker").fork();

    KThread.yield();

    KThread t3[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         t3[i] = new KThread(new Listener(comm3));
         t3[i].setName("Listener Thread" + i).fork();
	}

    KThread.yield();
    t3[0].join();
    speakerThread3.join();

    System.out.print("\nVAR5: Test for one speaker, more listener, listeners waits for speaker, and then create more listeners \n");	
    Communicator comm31 = new Communicator();


    KThread t31[] = new KThread[10];
	for (int i = 0; i < 5; i++) {
         t31[i] = new KThread(new Listener(comm31));
         t31[i].setName("Listener Thread" + i).fork();
	}

    KThread.yield();

    KThread speakerThread31 = new KThread(new Speaker(comm31, 300));
    speakerThread31.setName("Thread speaker").fork();

    KThread.yield();

	for (int i = 6; i < 10; i++) {
         t31[i] = new KThread(new Listener(comm31));
         t31[i].setName("Listener Thread" + i).fork();
	}

    KThread.yield();
    t3[0].join();
    speakerThread3.join();



    System.out.print("\nVAR6: Test for more speaker, one listener, listener waits for speaker\n");	

    Communicator comm4 = new Communicator();

    KThread t4[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         t4[i] = new KThread(new Speaker(comm4, (i+1)*100));
         t4[i].setName("Speaker Thread" + i).fork();
	}

    KThread.yield();

    KThread listenerCond4 = new KThread(new Listener(comm4));
    listenerCond4.setName("Thread listener").fork();

    KThread.yield();
    t4[0].join();
    listenerCond4.join();

    System.out.print("\nVAR7: Test for more speaker, one listener, speaker waits for listener\n");	

    Communicator comm5 = new Communicator();

    KThread listenerCond5 = new KThread(new Listener(comm5));
    listenerCond5.setName("Thread listener").fork();

    KThread.yield();

    KThread t5[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         t5[i] = new KThread(new Speaker(comm5, (i+1)*100));
         t5[i].setName("Speaker Thread" + i).fork();
	}

    KThread.yield();
    t5[0].join();
    listenerCond5.join();

    System.out.print("\nVAR8: Test for more speaker, one listener, speaker waits for listener,  and then create more speakers\n");	

    Communicator comm51 = new Communicator();

    KThread t51[] = new KThread[10];
	for (int i = 0; i < 5; i++) {
         t51[i] = new KThread(new Speaker(comm51, (i+1)*100));
         t51[i].setName("Speaker Thread" + i).fork();
	}

    KThread.yield();

    KThread listenerCond51 = new KThread(new Listener(comm51));
    listenerCond51.setName("Thread listener").fork();

    KThread.yield();
	for (int i = 5; i < 10; i++) {
         t51[i] = new KThread(new Speaker(comm51, (i+1)*100));
         t51[i].setName("Speaker Thread" + i).fork();
	}
    KThread.yield();

    t51[0].join();
    listenerCond51.join();

    System.out.print("\nVAR9:  Test for more speakers, more listeners, listeners waits for speaker \n");	
    Communicator comm9 = new Communicator();

    KThread ts9[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         ts9[i] = new KThread(new Speaker(comm9, (i+1)*100));
         ts9[i].setName("Speaker Thread" + i).fork();
	}

    KThread.yield();

    KThread tl9[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         tl9[i] = new KThread(new Listener(comm9));
         tl9[i].setName("Listener Thread" + i).fork();
	}

    KThread.yield();

	for (int i = 0; i < 10; i++) {
        ts9[i].join();
        tl9[i].join();
    }

    System.out.print("\nVAR10:  Test for more speakers, more listeners, speaker waits for listeners \n");	
    Communicator comm10 = new Communicator();

    KThread tl10[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         tl10[i] = new KThread(new Listener(comm10));
         tl10[i].setName("Listener Thread" + i).fork();
	}

    KThread.yield();

    KThread ts10[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         ts10[i] = new KThread(new Speaker(comm10, (i+1)*100));
         ts10[i].setName("Speaker Thread" + i).fork();
	}

    KThread.yield();

	for (int i = 0; i < 10; i++) {
        ts10[i].join();
        tl10[i].join();
    }

    System.out.print("\nVAR11:  Test for more speakers, more listeners, speaker waits for listeners \n");	
    Communicator comm11 = new Communicator();

    int num  = 80;
    ArrayList<KThread> t11 = new ArrayList<KThread>();

	for (int i = 0; i < num; i++) {
         KThread tmp = new KThread(new Speaker(comm11, (i+1)*100));
         tmp.setName("Speaker Thread" + i);

         t11.add(tmp);
	}

	for (int i = 0; i < num; i++) {
         KThread tmp = new KThread(new Listener(comm11));
         tmp.setName("Listener Thread" + i);

         t11.add(tmp);
	}

    Collections.shuffle(t11);

	for (int i = 0; i < num * 2; i++) {
         t11.get(i).fork();
	}

    KThread.yield();

	for (int i = 0; i < num * 2; i++) {
        t11.get(i).join();
    }

    System.out.print("\nTest for one speaker, more listener, speaker waits for listener \n");	
    System.out.print("\nTest for more speaker, one listener, speaker waits for listener \n");	

    System.out.print("Leave Communicator.selfTest\n");	

}
}

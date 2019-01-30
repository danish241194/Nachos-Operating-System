package nachos.ag;

import nachos.machine.*;
import nachos.security.*;
import nachos.threads.*;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Random;

public class MyTester {

    public static void selfTest() {
    	System.out.println("Priority Scheduling Test");
        TestPrioprityScheduler();
    }

    public static void TestPrioprityScheduler() {
        Lib.debug(dbgFlag, "Enter TestPrioprityScheduler");
        //PriopritySchedulerVAR1();
        // PriopritySchedulerVAR2();
         //PriopritySchedulerVAR3();
        PriopritySchedulerVAR4();
        Lib.debug(dbgFlag, "Leave TestPrioprityScheduler");
    }

    /**
     *  VAR1: Create several(>2) threads, verify these threads can be run successfully.
     */
    public static void PriopritySchedulerVAR1() {

        System.out.print("PriopritySchedulerVAR1\n");

        Runnable myrunnable1 = new Runnable() {
        public void run() { 
            int i = 0;
            while(i < 10) { 
                System.out.println("*** in while1 loop " + i + " ***");
                i++;
            } /*yield();*/ 
        }
        }; 

        KThread testThread;
        testThread = new KThread(myrunnable1);
        testThread.setName("child 1");

        testThread.fork();

        KThread testThread2;
        testThread2 = new KThread(myrunnable1);
        testThread2.setName("child 2");

        testThread2.fork();

        testThread.join();

        KThread t[] = new KThread[10];
        for (int i=0; i<10; i++) {
             t[i] = new KThread(myrunnable1);
             t[i].setName("Thread" + i).fork();
        }


        KThread.yield();

    }

    /**
     * VAR2: Create lots of threads with more locks and more complicated resource allocation
     */
    public static void PriopritySchedulerVAR2() {
        System.out.print("PriopritySchedulerVAR2\n");

       // KThread.selfTest();
      //  Communicator.selfTest();
       // Condition2.selfTest();
       //Alarm.selfTest();
       // Semaphore.selfTest();
    }

    /**
     * VAR3: Create several(>2) threads, decrease or increase the priorities of these threads. 
     * Verify these threads can be run successfully.
     */
    public static void PriopritySchedulerVAR3() {
        System.out.print("PriopritySchedulerVAR3\n");

        Runnable myrunnable1 = new Runnable() {
            public void run() { 
                int i = 0;
                while(i < 10) { 
                    System.out.println("*** in while1 loop " + i + " ***");
                    i++;
                } /*yield();*/ 
            }
        }; 

        KThread testThread;
        testThread = new KThread(myrunnable1);
        testThread.setName("child 1");
        testThread.fork();
        boolean intr = Machine.interrupt().disable();
        ThreadedKernel.scheduler.setPriority(testThread, 2);
        Machine.interrupt().restore(intr);
        KThread testThread2;
        
        testThread2 = new KThread(myrunnable1);
        testThread2.setName("child 2");
        
        intr = Machine.interrupt().disable();
        ThreadedKernel.scheduler.setPriority(testThread2, 3);
        Machine.interrupt().restore(intr);

        testThread2.fork();

        testThread.join();
//
//        KThread t[] = new KThread[10];
//        for (int i=0; i<10; i++) {
//             t[i] = new KThread(myrunnable1);
//             t[i].setName("Thread" + i).fork();
//
//             ThreadedKernel.scheduler.setPriority(t[i], (i+1)%8);
//        }
//
//        Random rand = new Random();
//
//        KThread t1[] = new KThread[10];
//        for (int i=0; i<10; i++) {
//             t1[i] = new KThread(myrunnable1);
//             t1[i].setName("Thread" + i).fork();
//
//             ThreadedKernel.scheduler.setPriority(t1[i], rand.nextInt(8));
//        }

        KThread.yield();
    }

    private static class Runnable1 implements Runnable  {

        Runnable1(Lock lock) {
            this.lock = lock;
        }
        public void run() { 
            lock.acquire();
            while (true) {
            	boolean int_state = Machine.interrupt().disable(); 
                System.out.println( KThread.currentThread().getName()+"with priority"+ ThreadedKernel.scheduler.getEffectivePriority()); 
                Machine.interrupt().restore(int_state);
            	KThread.currentThread().yield();
                if (isOpen ) {
                    break;
                }
            }
            lock.release();
        }

        Lock lock;
        boolean isOpen = false;
    } 


    /**
     * VAR4: Create a scenario to hit the priority inverse problem.
     * Verify the highest thread is blocked by lower priority thread.
     */
    public static void PriopritySchedulerVAR4() {
        System.out.print("PriopritySchedulerVAR4\n");

        Lock lock = new Lock();

        Runnable myrunnable1 = new Runnable1(lock);
        Runnable myrunnable2 = new Runnable() {
            public void run() { 
                while(true) {
                    boolean int_state = Machine.interrupt().disable(); 
                    System.out.println( KThread.currentThread().getName()+"with priority"+ ThreadedKernel.scheduler.getEffectivePriority()); 
                    Machine.interrupt().restore(int_state); 
                	//System.out.println( KThread.currentThread().getName()+" with priority "+ThreadedKernel.scheduler.getPriority());
                    KThread.currentThread().yield();
                }
            }
        };  

        KThread low = new KThread(myrunnable1);
        low.fork();
        low.setName("low");
        boolean int_state = Machine.interrupt().disable();

        ThreadedKernel.scheduler.setPriority(low, 1);
        Machine.interrupt().restore(int_state); 

        KThread.currentThread().yield();
        

        // High priority thread "high" waits for low priority thread "low" because they use the same lock.
        KThread high = new KThread(myrunnable1);
        high.fork();
        high.setName("high");
        int_state = Machine.interrupt().disable();
        ThreadedKernel.scheduler.setPriority(high, 7);
        Machine.interrupt().restore(int_state); 


        KThread medium = new KThread(myrunnable2);
        medium.fork();
        medium.setName("medium");
        int_state = Machine.interrupt().disable(); 
       //.out.println( KThread.currentThread().getName()+"with priority"+ ThreadedKernel.scheduler.getPriority()+" active ('a' wants its lock back so we are here)"); 
        ThreadedKernel.scheduler.setPriority(medium, 6);
        
        Machine.interrupt().restore(int_state); 
        KThread.currentThread().yield();
    }


    static private char dbgFlag = 't';
}
package nachos.threads;

import java.util.Comparator;
import java.util.PriorityQueue;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
	public PriorityQueue<KThread> sleepingThreads;
    public Alarm() {
    sleepingThreads = new PriorityQueue<KThread>(new ThreadComparator());
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    /*
     * Added
     * At every time when this function is invoked , we will see the threads for which waking time
     * is over and wake them all. As all the threads are stored in ascending order of sleeping time
     * we will check only up-to the threads whose waking time is less than current time and once we find thread
     * whose waking time is more than the current time so we will stop there as all threads above it
     * will be having waking time greater than current time
     */
    public void timerInterrupt() {
    	boolean istr = Machine.interrupt().disable();
    	
    	while (sleepingThreads.size()>0 && sleepingThreads.peek().time <= Machine.timer().getTime()) {
    		sleepingThreads.remove().ready();
    	}
    
    	KThread.yield();
    	
    	//Machine.interrupt().enable();
    	Machine.interrupt().restore(istr);
    
    }
    /*
     * Added
     * A Comparator so to get threads stored in sleeping queue in ascending order of waitingtime 
     * 
     */
    class ThreadComparator implements Comparator<KThread>{ 
        
        // Overriding compare()method of Comparator  
                    // for descending order of cgpa 
        public int compare(KThread s1,KThread s2) { 
            if (s1.time > s2.time) 
                return 1; 
            else if (s1.time < s2.time) 
                return -1; 
            return 0; 
            } 
    } 
    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    /*
     * We changed the structure of thread ,added a variable namely "time",where the waitinguntil 
     * time will be stored 
     */
    public void waitUntil(long x) {

	long wakeTime = Machine.timer().getTime() + x;
	boolean intrState = Machine.interrupt().disable();
	KThread.currentThread().time = wakeTime;
	sleepingThreads.add(KThread.currentThread());
	KThread.currentThread().sleep();
    Machine.interrupt().restore(intrState);
    
    }
    
    
   //Test case for TASK III copied from Internet
    
    private static class AlarmTest implements Runnable {
    	AlarmTest(long x) {
    	    this.time = x;
    	}
    	
    	public void run() {

            System.out.print(KThread.currentThread().getName() + " alarm\n");	
            ThreadedKernel.alarm.waitUntil(time);
            System.out.print(KThread.currentThread().getName() + " woken up \n");	

    	}

        private long  time; 
        }

        public static void selfTest() {

        System.out.print("Enter Alarm.selfTest\n");	

    	Runnable r = new Runnable() {
    	    public void run() {
                    KThread t[] = new KThread[10];

                    for (int i=0; i<10; i++) {
                         t[i] = new KThread(new AlarmTest(160 + i*20));
                         t[i].setName("Thread" + i).fork();
                    }
                    for (int i=0; i<10000; i++) {
                        KThread.yield();
                    }
                }
        };

        KThread t = new KThread(r);
        t.setName("Alarm SelfTest");
        t.fork();
        KThread.yield();

        t.join();

        System.out.print("Leave Alarm.selfTest\n");	

    }
}

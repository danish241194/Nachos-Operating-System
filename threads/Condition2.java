package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically re-acquire the lock before <tt>sleep()</tt> returns.
     */
    /*
     * Added
     * In Given Condition.java it has used semaphore(with count = 0) and using that semaphore 
     * it gets store in sleeping queue .Here we have a sleeping queue which will be associated
     * with the Condition2 object .It works in same way as Condition is working ,i.e when a
     * thread (acquired lock) calls sleep ,the lock will get released and on wake-up of thread
     * it will be given lock again
     * As it needs to be done atomically ,so the code is written in between interrupt disable
     * and restore call
     */ 
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
	if(waitingQueue==null) {
		waitingQueue = new LinkedList<KThread>();
	}
	conditionLock.release();

	boolean stateInterrupt = Machine.interrupt().disable();
	waitingQueue.add(KThread.currentThread());
	KThread.sleep();
	Machine.interrupt().restore(stateInterrupt);
	
	conditionLock.acquire();
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    /*
     * Added
     * when thread calls wake ,it means if there is any thread in sleeping queue ,then 
     * remove that thread from sleeping queue and make thread ready 
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		boolean interruptState = Machine.interrupt().disable();
		if(waitingQueue!=null && waitingQueue.size()>0) {
			KThread newTHread = waitingQueue.remove();
			newTHread.ready();
			
		}
		Machine.interrupt().restore(interruptState);
		
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    /*
     * Added
     * when thread calls wakeAll ,it means if there is any thread in sleeping queue ,then 
     *remove all threads one by one from sleeping queue and make all threads ready 
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	

		while(waitingQueue!=null && waitingQueue.size()>0) {
			boolean interruptState = Machine.interrupt().disable();
			KThread newTHread = waitingQueue.remove();
			newTHread.ready();
			Machine.interrupt().restore(interruptState);
		}
	
    }

    /*
     * Testing for Task II copied from Internet
     * 
     */

    private static class Condition2Test implements Runnable {
    private Lock lock; 
    private Condition2 condition; 
	Condition2Test(Lock lock, Condition2 condition) {
	    this.condition = condition;
        this.lock = lock;
	}
	
	public void run() {
        lock.acquire();

        System.out.print(KThread.currentThread().getName() + " acquired lock\n");	
        condition.sleep();
        System.out.print(KThread.currentThread().getName() + " acquired lock again\n");	

        lock.release();
        System.out.print(KThread.currentThread().getName() + " released lock \n");	
	}

    
    }

    public static void selfTest() {

    System.out.print("Enter Condition2.selfTest\n");	

    Lock lock = new Lock();
    Condition2 condition = new Condition2(lock); 

    KThread t[] = new KThread[10];
	for (int i=0; i<10; i++) {
         t[i] = new KThread(new Condition2Test(lock, condition));
         t[i].setName("Thread" + i).fork();
	}

    KThread.yield();
    
    lock.acquire();

    System.out.print("condition.wake();\n");	
    condition.wake();

    System.out.print("condition.wakeAll();\n");	
    condition.wakeAll();

    lock.release();

    System.out.print("Leave Condition2.selfTest\n");	

    t[9].join();
        
}
    private Lock conditionLock;
    private LinkedList<KThread> waitingQueue = null;
}

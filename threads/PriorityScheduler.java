package nachos.threads;

import nachos.machine.*;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fashion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    	System.out.println("Priority Schedular Running");
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }

    
    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

    
    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    } 

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;
	}

	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    /*
	     * Added
	     *
	     */
	    waitQueue.add(thread);
	    
	    getThreadState(thread).waitForAccess(this);
	    
	}
	
/*
 *Added
 *When acquire is called with thread as a parameter that means the new
 * holder (can be said as owner for this queue<which gets priority from waiting threads>)
 * we will this queue from the current holders list (QueuesItAcquires) and make this thread
 * as new holder of this queue 
 */
	
	public void acquire(KThread thread) {
		
	    Lib.assertTrue(Machine.interrupt().disabled());
	    if(this.thisQueueHolder!=null && this.transferPriority==true) {
	    	this.thisQueueHolder.QueuesItAcquires.remove(this);
	    	//System.out.println(this.thisQueueHolder.thread.getName()+" holder");
	    }
	    this.thisQueueHolder = getThreadState(thread);
	    getThreadState(thread).acquire(this);
	}

	public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me
	    
	    /*
	     * Added
	     * if there is some holder of this queue we need to remove this queue from his 
	     * Owning (queues it acquire ) list
	     * 
	     * if there is no transfer priority thing then we don't need to remove as it wont effect 
	     * the effective priority as this queue is not going to  transfer priority
	     * 
	     */
	    
	    if(this.thisQueueHolder!=null && this.transferPriority) {
	    	
	    	this.thisQueueHolder.QueuesItAcquires.remove(this);
	    	//System.out.println(this.thisQueueHolder.thread.getName() );

	    }
	    
	    /*
	     * Added 
	     * As No Thread is inside waiting queue so we will return null
	     */
	    
	    if(waitQueue.size()==0) {
	    	return null;
	    }
	    
	    ThreadState nextThread1 = pickNextThread();
	    this.waitQueue.remove(nextThread1.thread);
	    nextThread1.acquire(this);
	    
	    return nextThread1.thread;
	}

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	public int getEffectivePriority() {
	    if(!transferPriority) {
	    	return priorityMinimum;
	    }
	    if(QueueIsDirty) {
	    	cachedEffectivePriority = priorityMinimum;
	    	for (Iterator<KThread> itr = this.waitQueue.iterator(); itr.hasNext();) {  
				ThreadState newthreadstate = getThreadState(itr.next());
				if(cachedEffectivePriority < newthreadstate.getEffectivePriority())
					cachedEffectivePriority = newthreadstate.getEffectivePriority();
			}
	    	QueueIsDirty = false;
	    
	    }
		
	    return cachedEffectivePriority;
	}
	/*
	 * Added 
	 * Will give the thread which is having highest effective priority
	 * 
	 */
	protected ThreadState pickNextThread() {
	    		ThreadState returner = null;
		for (Iterator<KThread> itr = this.waitQueue.iterator(); itr.hasNext();) {  
			ThreadState newthreadstate = getThreadState(itr.next());
			if(returner==null || returner.getEffectivePriority() < newthreadstate.getEffectivePriority())
				returner = newthreadstate;
		}
	    return returner;
	}
	//old effective may have changed
	void setDirty() {
		if(QueueIsDirty)
			return;
		QueueIsDirty = true;
		/*
		 * Added this queue is dirty means its holder may get different effective priority => the
		 * queue on which this holder will be waiting on will now get the different effective priority
		 * 
		 */
		if(this.thisQueueHolder!=null) {
			this.thisQueueHolder.setDirty();
		}
		
	}																	
	
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	LinkedList<KThread> waitQueue = new LinkedList<KThread>();
	ThreadState thisQueueHolder  =  null;
	boolean QueueIsDirty;
	boolean transferPriority;
	int cachedEffectivePriority;
	
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
	    this.thread = thread;
	    
	    setPriority(priorityDefault);
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
		int effective =  priority;
		
		if(threadIsDirty) {
		for (Iterator<ThreadQueue> itr = QueuesItAcquires.iterator(); itr.hasNext();) {  
            PriorityQueue threadQueue = (PriorityQueue)itr.next();
            
            if(threadQueue.getEffectivePriority() > effective) {
            	effective = threadQueue.getEffectivePriority();
            	
            }
            
		}
		
		}
		
	    return effective;
	}
	
	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
	    if (this.priority == priority)
	    	return;
	    
	   /*
	    * if different priority make it dirty so that again the queues on which it is 
	    * waiting make get different effective priority 
	    */
	    this.priority = priority;
	    
	    setDirty();
	   
	}

	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(PriorityQueue waitQueue) {
	   /*
	    * 
	    * Added 
	    * add wait queue on its waiting list
	    * may be this queue was the holder so set the new holder to null and
	    * remove the entry from acquired queue list
	    */
		this.QueuesItIsWaitingOn.add(waitQueue);
		if(this.QueuesItAcquires.indexOf(waitQueue)!=-1) {
			this.QueuesItAcquires.remove(waitQueue);
			waitQueue.thisQueueHolder=null;
		}
		/*
		 * Added
		 * new thread is added to the queue the holder of this queue may now get more
		 * priority(Example),so we set that queue to dirty
		 */
		if(waitQueue.thisQueueHolder!=null) {
			waitQueue.setDirty();
		}
		
	}

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	
	public void acquire(PriorityQueue waitQueue) {
	    /*
	     * ADDED
	     * Add this queue to the acquired list and if it previously was waiting 
	     * on this queue ,remove that thing
	     * On its priorityPriority queue this thread is has been made as new owner
	     * this thread needs to be made dirty as effective priority will now get changed 
	     * for this thread and the queues on which it will be waiting
	     */
		this.QueuesItAcquires.add(waitQueue);
		if(QueuesItIsWaitingOn.indexOf(waitQueue)!=-1){
			QueuesItIsWaitingOn.remove(waitQueue);
			
		}
		waitQueue.setDirty();////new added it was not calling dirty to the removed queue and that queue was giving its old effective priority
								///or u can also understand it as when nextthread was getting called on priorityqueue
								//that queue was not getting dirty (See code) that's y made that dirtya
		setDirty();
		
	
	}	
	
	
	public void setDirty() {

	//	System.out.println(thread.getName()+"d");
		if(threadIsDirty)
		{
			return;
		} 

	
		threadIsDirty=true;
		for (Iterator<ThreadQueue> itr = QueuesItIsWaitingOn.iterator(); itr.hasNext();) {  
            PriorityQueue threadQueue = (PriorityQueue)itr.next();
            threadQueue.setDirty();
		}
		
	
	}
	/** The thread with which this object is associated. */	   
	
	protected KThread thread;
	LinkedList<ThreadQueue> QueuesItAcquires = new LinkedList<ThreadQueue>();
	LinkedList<ThreadQueue> QueuesItIsWaitingOn = new LinkedList<ThreadQueue>();
	boolean threadIsDirty = false;
	/** The priority of the associated thread. */
	protected int priority;
	
    }
}

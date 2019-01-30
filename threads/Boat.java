package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    static int A = 0;
    static int B = 1;
    static int C_A;
    static int C_B;
    static int A_A;
    static int A_B;
    static int onBoat;
    static int presentBoatLocation=A;
    static Lock lock = new Lock();
    static Condition ChildSleepingOnA,ChildSleepingOnB,AdultSleepingOnA,AdultSleepingOnB,ChildleepingOnBoat,PoilotWaitingForPassenger;
    static Communicator pipe = new Communicator();
    public static void selfTest()
    {
    ChildSleepingOnA = new Condition(lock);
    ChildSleepingOnB= new Condition(lock);
    AdultSleepingOnA = new Condition(lock);
    AdultSleepingOnB = new Condition(lock);
    ChildleepingOnBoat = new Condition(lock);
    PoilotWaitingForPassenger = new Condition(lock);
	
    BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 3 children and 3 adults***");
	begin(2, 2, b);


    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;

	C_A = children;
	A_A = adults;
	C_B = 0;
	A_B = 0;

	Runnable childRunnable = new Runnable() {
	    public void run() {
	    		int location  = A;
                ChildItinerary(location);
            }
        };
        
     Runnable adultRunnable = new Runnable() {
    	    public void run() {
    	    	AdultItinerary();
                }
          };
          
     for (int i = 0; i < children; i++) {
              KThread t = new KThread(childRunnable);
              t.fork();
          }
           
     for (int i = 0; i < adults; i++) {
              KThread t = new KThread(adultRunnable);
              t.fork();
      }
     
     while(true) {
    	 int persons_On_B = pipe.listen();
    	 if(persons_On_B == children+adults) {
    		 break;
    	 }
     }
    }

    static void AdultItinerary()
    {
    	lock.acquire();
    	while(C_A >= 2 || presentBoatLocation==B||onBoat>0) {

			// System.out.println(C_A+" "+presentBoatLocation+" "+onBoat);
    		AdultSleepingOnA.sleep();
    	}

		// System.out.println("aaaaa");
    	bg.AdultRideToMolokai();
    	A_A--;
    	onBoat=1;
    	presentBoatLocation=B;
    	//System.out.println(KThread.currentThread().getName()+" reached B");
    	A_B++;
    	onBoat=0;
    	pipe.speak(A_B+C_B);
    	ChildSleepingOnB.wake();
    	AdultSleepingOnB.sleep();
    	lock.release();
    }

    static void ChildItinerary(int location)
    {
    	lock.acquire();
    	while(true) {
    		if(location==A) {
    			 while (onBoat > 1 
                         || A_A > 0 && C_A ==1 || presentBoatLocation != A) 
                 {
                     ChildSleepingOnA.sleep();
                 }
    			 ChildSleepingOnA.wakeAll();
    			 //System.out.println(A_A+" "+ C_A);
    			 if(A_A ==0 && C_A==1) {
    				// System.out.println("1");
    				 bg.ChildRideToMolokai();
    				 C_A--;
    				 location = B;
    				 C_B++;
    				 presentBoatLocation = B;
    				 pipe.speak(A_B+C_B);
    				ChildSleepingOnB.sleep();
    			 }
    			 else if(C_A>1) {
    				 onBoat++;
    				 if(onBoat==1) {//pilot
    					 PoilotWaitingForPassenger.sleep();
    					 C_A--;

        			//	 System.out.println("2");
    					 bg.ChildRowToMolokai();
    					 C_B++;
    					 location=B;
    					 ChildleepingOnBoat.wake();
    					 ChildSleepingOnB.sleep();
    					 
    				 }
    				 else if(onBoat==2) {
    					 PoilotWaitingForPassenger.wake();
    					 ChildleepingOnBoat.sleep();
    					 C_A--;

        				// System.out.println("3");
    					 bg.ChildRideToMolokai();
    					 C_B++;
    					 location = B;
    					 presentBoatLocation  = B;
    					 pipe.speak(A_B+C_B);
    					 ChildSleepingOnB.wake();
    					 ChildSleepingOnB.sleep();
    				 }
    			 }
    			
    			 
    		}
    		else if(location == B) {
    			C_B--;

				 //System.out.println("4");
    			bg.ChildRideToOahu();
    			presentBoatLocation = A;
    			C_A++;
    			location = A;
    			onBoat=0;
    			ChildSleepingOnA.wakeAll();
    			AdultSleepingOnA.wakeAll();
    			
    		}
    	}
    }

    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    
}

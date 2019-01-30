/* halt.c
 *	Simple program to test whether running a user program works.
 *	
 *	Just do a "syscall" that shuts down the OS.
 *
 * 	NOTE: for some reason, user programs with global data structures 
 *	sometimes haven't worked in the Nachos environment.  So be careful
 *	out there!  One option is to allocate data structures as 
 * 	automatics within a procedure, but if you do this, you have to
 *	be careful to allocate a big enough stack to hold the automatics!
 */
#include "stdio.h"
#include "syscall.h"

int
main()
{   int fd;
    fd = open("myfilename");
    char  abc[100];
    read(fd,abc,10);
    abc[0] = 'g';
    abc[1] = 'i';
    abc[2] = 'r';
    fd = creat("newfile");
    write(fd,abc,10);
    halt();
    /* not reached */
}

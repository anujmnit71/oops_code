/*
 * socket.c: Open a socket and return the file descriptor (for use by Lisp)
 *
 * George Ferguson, ferguson@cs.rochester.edu, 12 Apr 1995
 * Time-stamp: <Fri May 23 12:02:54 EDT 1997 ferguson>
 */
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

/*
 * Functions defined here:
 */
int gf_open_socket(char *host, int port, char *argv0);

/*	-	-	-	-	-	-	-	-	*/

int
gf_open_socket(char *hostname, int port, char *argv0)
{
    int sockfd;
    struct hostent *hent;
    u_long haddr;
    struct sockaddr_in saddr;

    /* Lookup hostname */
    if ((hent = gethostbyname(hostname)) == NULL) {
	/* Error */
	char *errmsg;
	switch (h_errno) {
	  case HOST_NOT_FOUND:
	    errmsg = "host not found"; break;
	  case TRY_AGAIN:
	    errmsg = "temporary server error"; break;
	  case NO_RECOVERY:
	    errmsg = "server error"; break;
	  case NO_DATA:
	    errmsg = "no address for host"; break;
	  default:
	    errmsg = "unknown error"; break;
	}
	fprintf(stderr,
		"%s: gf_open_socket: gethostbyname failed for \"%s\": %s\n",
		argv0, hostname, errmsg);
	return -1;
    }
    /* Get address of host (in theory should scan all of h_addr_list) */
    haddr = *((u_long*)(hent->h_addr_list[0]));
    /* Create socket */
    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
	fprintf(stderr,
		"%s: gf_open_socket: couldn't create socket: %s\n",
		argv0, strerror(errno));
	return -1;
    }
    /* Connect to remote host/port */
    saddr.sin_family = (short)AF_INET;
    saddr.sin_port = (u_short)port;
    memcpy((char*)&(saddr.sin_addr), (char*)&haddr, sizeof(saddr.sin_addr));
    if (connect(sockfd,(struct sockaddr *)&saddr,sizeof(saddr)) < 0) {
	fprintf(stderr,
		"%s: gf_open_socket: couldn't connect to %s:%d: %s\n",
		argv0, hostname, port, strerror(errno));
        close(sockfd);
	return -1;
    }
    /* Return socket */
    return sockfd;
}

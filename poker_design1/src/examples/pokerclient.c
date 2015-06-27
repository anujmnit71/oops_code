/*
 * pokerclient.c
 *
 * George Ferguson, ferguson@cs.rochester.edu,  1 Oct 1998
 * Time-stamp: <Thu Oct  1 18:31:13 EDT 1998 ferguson>
 * All this client does is hook up to the IPP server, echo lines received
 * from the server to stdout, prompt for replies from stdin, and send
 * the replies back to the server.
 *
 * Compile with: gcc -o pokerclient pokerclient.c -lsocket -lnsl
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

#define USAGE "usage: pokerclient [-server HOST:PORT]"

/*
 * Functions defined here:
 */
void main(int argc, char **argv);
static int connectToServer(char *host, int port);

/*
 * Data defined here:
 */
char *host = "localhost";
int port = 9898;

/*	-	-	-	-	-	-	-	-	*/

void
main(int argc, char **argv) {
    char *colon;
    int sock;
    FILE *in, *out;
    /* Parse command line */
    while (--argc > 0) {
	argv += 1;
	if (strcmp(argv[0], "-server") == 0) {
	    if (argc < 2) {
		fprintf(stderr, "missing argument for -server");
		exit(-1);
	    } else {
		host = argv[1];
		if ((colon=strchr(host, ':')) != NULL) {
		    sscanf(colon, ":%d", &port);
		    *colon = '\0';
		}
		argc -= 1;
		argv += 1;
	    }
	} else {
	    fprintf(stderr, "unknown option: %s", argv[0]);
	    fprintf(stderr, "%s\n", USAGE);
	}
    }
    /* Connect to server */
    printf("connecting to server %s:%d\n", host, port);
    if ((sock = connectToServer("localhost", 9898)) < 0) {
	fprintf(stderr, "couldn't connect to server %s:%d\n", host, port);
	exit(-1);
    }
    /* Create stdio streams */
    if ((in = fdopen(sock, "r")) == NULL) {
	perror("fdopen for read");
    }
    if ((out = fdopen(sock, "w")) == NULL) {
	perror("fdopen for write");
    }
    printf("connected to server %s:%d\n", host, port);
    /* Now do forever... */
    while (1) {
	char msg[256];
	/* Read a message from the server */
	if (fgets(msg, sizeof(msg), in) == NULL) {
	    break;
	}
	/* Copy it to stdout */
	printf("%s", msg);
	/* If msg requires response... */
	if (strchr(msg, '?') != NULL || strncmp(msg, "IPP", 3) == 0) {
	    /* ...Then prompt for reply */
	    printf("reply: ");
	    /* Read reply from stdin */
	    fgets(msg, sizeof(msg), stdin);
	    /* And send it on to the server */
	    fprintf(out, "%s", msg);
	    /* Flush socket connection */
	    fflush(out);
	}
    }
}

static int
connectToServer(char *host, int port)
{
    struct hostent *hent;
    u_long haddr;
    struct sockaddr_in saddr;
    int sockfd;

     /* Lookup address */
    if ((hent = gethostbyname(host)) == NULL) {
	perror("gethostbyname");
	return -1;
    }
    haddr = *((u_long*)(hent->h_addr_list[0]));
    /* Create socket */
    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
	perror("socket");
	return -1;
    }
    /* Connect to server */
    memset((char*)&saddr, '\0', sizeof(saddr));
    saddr.sin_family = htons((short)AF_INET);
    saddr.sin_port = htons((u_short)port);
    memcpy((char*)&(saddr.sin_addr),(char*)&haddr,sizeof(saddr.sin_addr));
    if (connect(sockfd,(struct sockaddr *)&saddr,sizeof(saddr)) < 0) {
	perror("connect");
	return -1;
    }
    /* Done */
    return sockfd;
}

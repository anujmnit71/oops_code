/*
 * pokerclient.h
 *
 * George Ferguson, ferguson@cs.rochester.edu,  1 Oct 1998
 Time-stamp: <Thu Oct  1 18:49:41 EDT 1998 ferguson>
 * Time-stamp: <>
 */

#ifndef _pokerclient_h_gf
#define _pokerclient_h_gf

class iosockstream: public iostream {
public:
	iosockstream(sockbuf& sb)
	    : ios (&sb) { _S_IOS_SETF(dont_close); }
	iosockstream(sockbuf* sb=0)
	    : ios (sb) { _S_IOS_SETF(dont_close); }
};

#endif

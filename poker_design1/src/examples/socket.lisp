;;;;
;;;; socket.lisp: Lisp functions for using C routines in socket.c
;;;;
;;;; George Ferguson, ferguson@cs.rochester.edu, 23 May 1997
;;;; Time-stamp: <Fri May 23 14:54:39 EDT 1997 ferguson>
;;;;

(unless (find-package "SOCKET")
  (defpackage "SOCKET"))
(in-package "SOCKET")

(defclass socket-stream (stream:bidirectional-terminal-stream)
  ())

(ff:defforeign 'open-socket
    :entry-point (ff:convert-to-lang "gf_open_socket")
    :arguments '(string fixnum string)
    :arg-checking t
    :return-type :fixnum)

(defun open-socket-stream (hostname port)
  "Open a TCP connection to HOSTNAME:PORT. Returns an initialized instance
of SOCKET-STREAM if successful, otherwise NIL."
  (let* ((argv0 (or (sys:command-line-argument 0) "<lisp>"))
	 (fd (open-socket hostname port argv0)))
    (when (>= fd 0)
      (make-instance 'socket-stream :fn-in fd :fn-out fd))))


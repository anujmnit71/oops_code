;;;;
;;;; pokerclient.lisp: Sample IPP client in Lisp
;;;;
;;;; George Ferguson, ferguson@cs.rochester.edu, 13 Oct 1998
;;;; Time-stamp: <Tue Oct 13 18:27:16 EDT 1998 ferguson>
;;;;
;;;; All this client does is hook up to the IPP server, echo lines received
;;;; from the server to stdout, prompt for replies from stdin, and send
;;;; the replies back to the server.
;;;;

(defvar *server-host* "localhost"
  "Host at which to contact the IPP server")

(defvar *server-port* 9898
  "Port at which to contact the IPP server")

;;;
;;; Unfortunately, Lisp doesn't have networking stuff built-in, but
;;; we can access some C routines (in socket.so) using the foreign-
;;; function interface in socket.lisp.
;;;
(load "/u/cs400/poker/examples/socket.so")
(load "/u/cs400/poker/examples/socket.lisp")

(defun connect-to-server (host port)
  "This function returns a stream open to HOST:PORT, or NIL."
  (socket::open-socket-stream host port))

(defun main ()
  "Main for the sample client. Opens connection to the server, then
reads messages and prompts for responses when needed."
  (let ((stream (connect-to-server *server-host* *server-port*))
	(msg nil))
    (loop
     (setq msg (read-line stream))
     (format t "~A~%" msg)
     (when (or (find #\? msg) (string= (subseq msg 0 3) "IPP"))
       (format t "reply: ")
       (format stream "~A~%" (read-line t))))))

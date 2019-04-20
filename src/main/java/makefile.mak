JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
        $(JC) $(JFLAGS) $*.java

CLASSES = \
        Router.java \
        RoutingTable.java \ 
		Entry.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
        $(RM) *.class